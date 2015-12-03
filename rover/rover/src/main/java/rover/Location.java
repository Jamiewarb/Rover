package rover;

public class Location {

    private double x;
    private double y;
    private int type;
    private String claimed;

    public Location(double x, double y) {
        this.x = x;
        this.y = y;
        this.type = -1;
        this.claimed = null;
    }

    public Location(double x, double y, int type) {
    	this.x = x;
        this.y = y;
        this.type = type;
        this.claimed = null;
    }

    public Location(double x, double y, int type, String claimed) {
    	this.x = x;
        this.y = y;
        this.type = type;
        this.claimed = claimed;
    }

    public double getX() {
    	return x;
    }

    public double getY() {
    	return y;
    }

    public int getType() {
    	return type;
    }

    public String getClaimedID() {
    	return claimed;
    }

    public void claim(String roverID) {
    	claimed = roverID;
    }

    public void unclaim(String roverID) {
    	claimed = null;
    }

    public static double distFromLoc(double from, double to, int wrap) {
        double minValue = to - from;
        double b = minValue - wrap;
        if (minValue < 0) b = minValue + wrap;

        if (Math.abs(b) < Math.abs(minValue))
            minValue = b;

        return minValue%wrap;

    }

}