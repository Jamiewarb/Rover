package rover;

import java.util.*;

public class RoverCollectFinal extends RoverSkeleton {

	public RoverCollectFinal() {
        super();

		// This is the collectbot, so we initialise stats based on the scenario
		maxSpeed = 7;
		maxScan = 1;
		maxCarry = 1;

		roverType = 0;

		scanningMode = 1;
		
		debug = true;

		try {
			//set attributes for this rover
			//speed, scan range, max load
			//has to add up to <= 9
			setAttributes(maxSpeed, maxScan, maxCarry);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	void begin() {
		super.begin();
		//called when the world is started

		findTeam();

        if (getScenario() == 0 || getScenario() == 1) {
        	System.out.println("\n\n\nERROR: This RoverCollectMulti should not be being used for scenario 0 or 1. Please use RoverCollect.");
        	isActing = true;
        	movement = new MovementMethod(0, maxScan, 0, 0, getWorldWidth(), getWorldHeight());
        } else if (getScenario() == 2 || getScenario() == 3 || getScenario() == 4 || getScenario() == 5) {
        	// At this point we should know location of all items
        	movement = new MovementMethod(0, maxScan, 0, 0, getWorldWidth(), getWorldHeight());
        } else {
        	movement = new MovementMethod(0, maxScan, 0, 0, getWorldWidth(), getWorldHeight());
        }
		
		// Wait for the commander to tell us to begin collecting. Only applicable for scenario 2+
        waitForInstruction();
		try {
			if (getScenario() == 4 || getScenario() == 5) {
				findNextResource();
			} else
				makeNextMove();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	void poll(PollResult pr) {
		// This is called when one of the actions has completed

        getLog().info("Remaining Power: " + getEnergy());

        if ((getScenario() == 4 || getScenario() == 5) && getEnergy() < 40 && getCurrentLoad() > 0) {
        	try {
				deposit();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }

		if(pr.getResultStatus() == PollResult.FAILED) {
			broadCastToTeam("outOfPower");
			try {
				deposit();
			} catch (Exception e) {
				e.printStackTrace();
			}
            getLog().info("Ran out of power...");
			return;
		}

		getAndParseMessages();
		
		switch(pr.getResultType()) {
		case PollResult.MOVE:
			//move finished
            getLog().info("Move complete.");
            // Update the rover's understood positioning
            posx = newPosx;
            posy = newPosy;

            if (debug) System.out.println("Move complete. New posx: " + posx + ". New posy: " + posy);
			
			//now scan
			try {
                getLog().info("Scanning...");
				if (getScenario() == 2 || getScenario() == 3 || getScenario() == 4 || getScenario() == 5) {
					findNextResource();
				} else {
					scan(maxScan);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			break;
		case PollResult.SCAN:
            getLog().info("Scan complete");
            boolean foundNodes = false;
						
			for (ScanItem item : pr.getScanItems()) {
				if (item.getItemType() == ScanItem.RESOURCE) {
                    getLog().info("Resource found at: " + item.getxOffset() + ", " + item.getyOffset());
                    double itemx = (posx + item.getxOffset())%getWorldWidth();
                    double itemy = (posy + item.getyOffset())%getWorldHeight();
                    if (itemx < 0) itemx += getWorldWidth();
                    if (itemy < 0) itemy += getWorldHeight();

                    if (debug) System.out.println("Resource found at: " + itemx + ", " + itemy);
                    map.addLoc(itemx, itemy, 1);
                    broadCastToTeam("resourceFound" + smallDelim + itemx + smallDelim + itemy);
                    
                    if (item.getxOffset() < 0.1 && item.getyOffset() < 0.1) {
                    	if (debug) System.out.println("Found nodes");
                    	foundNodes = true;
                    }

				} else if (item.getItemType() == ScanItem.BASE) {
                    getLog().info("Base found at: " + item.getxOffset() + ", " + item.getyOffset());
                    double itemx = (posx + item.getxOffset())%getWorldWidth();
                    double itemy = (posy + item.getyOffset())%getWorldHeight();
                    if (itemx < 0) itemx += getWorldWidth();
                    if (itemy < 0) itemy += getWorldHeight();

                    if (debug) System.out.println("Base found at: " + itemx + ", " + itemy);
                    map.addLoc(itemx, itemy, 0);
                    broadCastToTeam("baseFound" + smallDelim + itemx + smallDelim + itemy);

				} else {
                    getLog().info("Rover found at: " + item.getxOffset() + ", " + item.getyOffset());

				}
			}

			if (!foundNodes) {
				if (debug) System.out.println("Found no nodes");
				map.foundNoNodes(posx, posy, 1);
				broadCastToTeam("resourceDepleted" + smallDelim + round(posx,1) + smallDelim + round(posy,1));
			}

			findNextResource();

			break;
		case PollResult.COLLECT:
            getLog().info("Collect complete. Resources remaining: " + getWorldResources());
			if ((getScenario() == 0 || getScenario() == 1 || getScenario() == 2) && getCurrentLoad() < maxCarry && getWorldResources() > 0) {
				try {
					collect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (getScenario() == 3 || getScenario() == 4 || getScenario() == 5) {
				try {
					scan(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
            //now scan
				try {
	                getLog().info("Scanning...");
					if (getScenario() == 2 || getScenario() == 3 || getScenario() == 4 || getScenario() == 5) {
						scan(1);
					} else {
						scan(maxScan);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			break;
		case PollResult.DEPOSIT:
            getLog().info("Deposit complete. Resources remaining: " + getWorldResources());
			if (getCurrentLoad() <= 0) {
				//now scan
				try {
	                getLog().info("Scanning...");
					if (getScenario() == 2 || getScenario() == 3 || getScenario() == 4 || getScenario() == 5) {
						findNextResource();
					} else {
						scan(maxScan);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					deposit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		}
		
	}

	private void findNextResource() {
		int mode;
		Location l;
		
		if (getCurrentLoad() < maxCarry && getWorldResources() > 0) {
		// If we're not full and there's more to get
			mode = 1;
			l = map.getClosestResource(posx, posy, 1, getID());

			if (l != null) l.claim(getID());

		} else if (getCurrentLoad() <= 0 && getWorldResources() <= 0) {
		// If we're empty and there are no more resources to collect
			return;
		} else {
		// Either we're full, or there's no resources left, so deposit current ones
			// Try to head back to base
			l = map.getClosestResource(posx, posy, 0, getID());
			mode = 0;
		}
		if (l != null) {
		// If we found our next location
			if (Math.abs(l.distFromLoc(posx, l.getX(), getWorldWidth())) < 0.1 && Math.abs(l.distFromLoc(posy, l.getY(), getWorldHeight())) < 0.1) {
			// If we're close enough to collect or deposit the resource/base
				if (mode == 1) {
					try {
						collect();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (mode == 0) {
					try {
						deposit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
			// If we're not close enough to collect or deposit the resource/base
				// then we need to move towards it
					// TODO: Move towards the base by (scan*2)/5, rescan and repeat
					// TODO: For the rescan, check if we're been in this sector before
				try {
					double mx = l.distFromLoc(posx, l.getX(), getWorldWidth());
					double my = l.distFromLoc(posy, l.getY(), getWorldHeight());
	                getLog().info("Moving towards resource...");
					roverMove(mx, my, maxSpeed);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			makeNextMove();
		}
	}

	@Override
	void waitForInstruction() {
		while (!isActing) {
			getAndParseMessages();
		}
		if (debug) System.out.println("Instructions received");
	}
}