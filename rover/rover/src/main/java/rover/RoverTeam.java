package rover;

public class RoverTeam {
	private String id;
	private int type;

	public RoverTeam(String id, int type) {
		// type 0 for collect, 1 for search, 2 for Commander
		this.id = id;
		this.type = type;
	}

	public String getID() {
		return id;
	}

	public int getType() {
		return type;
	}

	public void setID(String id) {
		this.id = id;
	}

	public void setType(int type) {
		this.type = type;
	}
}