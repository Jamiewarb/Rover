package rover;

import java.util.*;

public class WorldMap {

    private static int WIDTH;
    private static int HEIGHT;
    private ArrayList<Location> locations = new ArrayList<Location>();
    private boolean[][] visited = new boolean[WIDTH][HEIGHT];

    public WorldMap() {

    }

    public WorldMap(int width, int height) {
    	WIDTH = width;
    	HEIGHT = height;
    }

    public void addLoc(double x, double y, int type) {
        x = x%WIDTH;
        y = y%HEIGHT;
    	boolean foundLoc = false;
    	for (int i = 0; i < locations.size(); i++) {
    		Location l = locations.get(i);
    		if (l.getType() == type) {
	    		double dx = Math.abs(l.getX() - round(x, 4));
	    		double dy = Math.abs(l.getY() - round(y, 4));
	    		// If there's already a node added in this area then don't add it
	    		if (dx < 0.1 && dy < 0.1) {
	    			foundLoc = true;
    			}
    		}
    	}
    	if (!foundLoc) {
    		locations.add(new Location(round(x, 4), round(y, 4), type));
        }
    }

    public int getResourcesFound() {
        int i = 0;
        for (Location loc : locations) {
            if (loc.getType() == 1) {
                i++;
            }
        }
        return i;
    }

    public void foundNoNodes(double x, double y, int type) {
    	for (int i = 0; i < locations.size(); i++) {
    		Location l = locations.get(i);
    		if (l.getType() == type) {
	    		double dx = Math.abs(l.getX() - round(x, 4));
	    		double dy = Math.abs(l.getY() - round(y, 4));
	    		// If we found a node that says it's in this area, it is obviously gone now
	    		if (dx < 0.1 && dy < 0.1) {
	    			remLoc(i);
    			}
    		}
    	}
    }

    private void remLoc(int index) {
    	locations.remove(index);
    }

    public Location getClosestResource(double x, double y, int type) {
    	return getClosestResource(x, y, type, null);
    }

    public Location getClosestResource(double x, double y, int type, String roverID) {
    	Location closestLoc = null;
    	double curDist = WIDTH + HEIGHT;
    	for (int i = 0; i < locations.size(); i++) {
    		Location curLoc = locations.get(i);
    		if (type == curLoc.getType()) {
    			double posx = getFastestDist(x, curLoc.getX(), WIDTH);
    			double posy = getFastestDist(y, curLoc.getY(), HEIGHT);
    			double dist = Math.abs(Math.sqrt((posx*posx)+(posy*posy)));
    			if (dist < curDist) {
    				// If we don't care if claimed, if it's claimed by caller, or if it's unclaimed
	    			if (roverID == null || curLoc.getClaimedID() == null || curLoc.getClaimedID().equals(roverID)) {
	    				closestLoc = curLoc;
	    				curDist = dist;
	    			}
    			}
    		}
    	}
    	return closestLoc;
    }

	// Returns the shortest value in 2D
	private double getFastestDist(double from, double to, int wrap) {
		double minValue = to - from;
        double b = minValue - wrap;
        if (minValue < 0) b = minValue + wrap;

        if (Math.abs(b) < Math.abs(minValue))
            minValue = b;

        return minValue%wrap;
	}

	private void visitNode(int x, int y) {
		visited[x][y] = true;
	}

    public void setWidth(int width) {
        WIDTH = width;
    }

    public void setHeight(int height) {
        HEIGHT = height;
    }

    public int getWidth() {
    	return WIDTH;
    }

    public int getHeight() {
    	return HEIGHT;
    }

    public double round(double value, int places) {
	    return Round.round(value, places);
	}

}