package rover;

import java.util.*;

public abstract class RoverSkeleton extends Rover {

	protected int maxSpeed, maxScan, maxCarry;
	protected int curCarrying;
	protected double posx;
	protected double posy;
	protected double newPosx;
	protected double newPosy;
	protected WorldMap map;
	protected MovementMethod movement;
	protected ArrayList<RoverTeam> team;
	protected String commander = null;
	protected int roverType;
	protected boolean isActing = false;
	protected int commandRollsReceived = 0;
	protected boolean canStillCommand = true;
	protected double myCommandRoll = 0.0;
	protected int teamSize = 0;
	protected int resourceCount;
	protected int scanningMode; // 0 means we won't collect anything - just keep swimming
	protected String bigDelim = ":";
	protected String smallDelim = "=";
	protected int totalScanners = 0;
	protected int totalCollectors = 0;
	protected int outOfPowerCount = 0;
	protected int scannersDead = 0;

	protected boolean debug = false;

	public RoverSkeleton() {
		super();

		map = new WorldMap();

		//use your username for team name
		setTeam("jw715");
	}

    abstract void poll(PollResult pr);

    abstract void waitForInstruction();

	protected void roverMove(double x, double y, double speed) {
		try {
			newPosx = (posx + x)%getWorldWidth();
			newPosy = (posy + y)%getWorldHeight();
			if (newPosx < 0) newPosx += getWorldWidth();
			if (newPosy < 0) newPosy += getWorldHeight();
			move(x, y, speed);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	void begin() {
		getLog().info("BEGIN!");
        posx = 0.0;
        posy = 0.0;

        map.setHeight(getWorldHeight());
        map.setWidth(getWorldWidth());

    	resourceCount = 0;
		if (getScenario() == 2) {
			resourceCount = getWorldResources() / 5;
		} else if (getScenario() == 3 || getScenario() == 4) {
			resourceCount = getWorldResources();
		} else if (getScenario() == 5) {
			resourceCount = getWorldResources() / 2;
		}

        team = new ArrayList<RoverTeam>();
    }

	@Override
	void end() {
		// called when the world is stopped
		// the agent is killed after this
        getLog().info("END!");
	}

	protected void iAmAlive() {
		// Tell the team we are here
		broadCastToTeam("newbot" + smallDelim + getID() + bigDelim + "roverType" + smallDelim + roverType);
		addToTeam(getID(), roverType);
	}

	public void findTeam() {
		iAmAlive();
		teamSize = 0;
		if (getScenario() == 2 || getScenario() == 3) {
			teamSize = 3;
		} else if (getScenario() == 4) {
			teamSize = 10;
		} else if (getScenario() == 5) {
			teamSize = 5;
		}
		while (team.size() < teamSize) {
			getAndParseMessages();
		}
		if (debug) System.out.println("My team is full");
		for (RoverTeam members : team) {
			if (members.getType() >= 1) {
				totalScanners++;
			}
		}
	}

	protected void getAndParseMessages() {
		retrieveMessages();
		parseMessages();
	}

	protected void parseMessages() {
		ArrayList<String> messagesArray = new ArrayList<String>(messages);
		messages.clear();
		while (messagesArray.size() > 0) {
			ArrayList<ArrayList<String>> msgComponents = new ArrayList<ArrayList<String>>();
			String msg = messagesArray.get(0);
			messagesArray.remove(0);
			// Split one message up into its components, and their individual items
			String[] tokens = msg.split("[:]+");
			for (String token : tokens) {
				String[] items = token.split("[=]+");
				ArrayList<String> msgItem = new ArrayList<String>();
				for (String item : items) {
					msgItem.add(item);
				}
				msgComponents.add(msgItem);
			} 
			readMessage(msgComponents);
		}
	}

	protected void readMessage(ArrayList<ArrayList<String>> msgComponents) {
		String from = null;
		String newBot = null;
		int rType = -1;
		for (ArrayList<String> msgItem : msgComponents) {
			String subject = msgItem.get(0);
			if (subject.equals("to")) {
				if (!(msgItem.get(1).equals(getID()))) {
					// The message isn't for us - don't bother reading it
					return;
				}
			} else if (subject.equals("from")) {
				from = msgItem.get(1);

			} else if (subject.equals("newbot")) {
				newBot = msgItem.get(1);

			} else if (subject.equals("roverType")) {
				rType = Integer.parseInt(msgItem.get(1));

			} else if (subject.equals("baseFound")) {
				double itemx = Double.parseDouble(msgItem.get(1));
				double itemy = Double.parseDouble(msgItem.get(2));
				map.addLoc(itemx, itemy, 0);

			} else if (subject.equals("resourceFound")) {
				double itemx = Double.parseDouble(msgItem.get(1));
				double itemy = Double.parseDouble(msgItem.get(2));
				map.addLoc(itemx, itemy, 1);
				if (debug) System.out.println("Resource given to me: " + map.getResourcesFound());

				if (map.getResourcesFound() >= resourceCount) {
					if (roverType == 2) {
						isActing = false;
					} else if (roverType >= 1) {
						isActing = false;
						scanningMode = 1;
					}
				}
				// If size of resources equals right size for map, 
				// isActive = true

			} else if (subject.equals("resourceDepleted")) {
				double posx = Double.parseDouble(msgItem.get(1));
				double posy = Double.parseDouble(msgItem.get(2));
				map.foundNoNodes(posx, posy, 1);

			} else if (subject.equals("resourceClaimed")) {
				double x = Double.parseDouble(msgItem.get(1));
				double y = Double.parseDouble(msgItem.get(2));
				String roverID = msgItem.get(3);

			} else if (subject.equals("resourceUnclaimed")) {
				double x = Double.parseDouble(msgItem.get(1));
				double y = Double.parseDouble(msgItem.get(2));

			} else if (subject.equals("scannerOutOfPower")) {
				if (roverType == 2) {
					scannersDead++;
					if (scannersDead == totalScanners) {
						isActing = false;
					}
				}
			} else if (subject.equals("outOfPower")) {
				if (roverType == 2) {
					outOfPowerCount++;
					isActing = true;
				}
			} else if (subject.equals("instruction")) {
				if (msgItem.get(1).equals("act")) {
					isActing = true;
				} else if (msgItem.get(1).equals("commanderFound")) {
					commander = msgItem.get(2);

				} else if (msgItem.get(1).equals("movement")) {
					int x = Integer.parseInt(msgItem.get(2));
					int y = Integer.parseInt(msgItem.get(3));
					int w = Integer.parseInt(msgItem.get(4));
					int h = Integer.parseInt(msgItem.get(5));
					movement = new MovementMethod(0, maxScan, x, y, w, h);
					isActing = true;
				}

			} else if (subject.equals("question")) {


			} else {
				System.out.println("WARNING - Received communication I can't handle: " + msgComponents + ". Subject: " + subject);
			}
		}
		if (newBot != null && roverType != -1) {
			if (newBot != getID()) {
				addToTeam(newBot, rType);
			}
		}
	}

	protected void addToTeam(String roverID) {
		team.add(new RoverTeam(roverID,-1));
	}

	protected void addToTeam(String roverID, int roverType) {
		team.add(new RoverTeam(roverID,roverType));
	}

    protected void makeNextMove() {
		Location nextMove = movement.getNextMove();
		double mx = nextMove.distFromLoc(posx, nextMove.getX(), getWorldWidth());
		double my = nextMove.distFromLoc(posy, nextMove.getY(), getWorldHeight());
		roverMove(mx, my, maxSpeed);
	}

    public double getCommandRoll() {
    	return round((new Random().nextDouble() * 10000), 5);
    }

	public double round(double value, int places) {
	    return Round.round(value, places);
	}

}