package rover;

import java.util.*;

public class MovementMethod {
	Stack moves = new Stack();

	public MovementMethod(int type, int maxScan, double x, double y, double width, double height) {
	// type: 0 for rectangular
	// Scanning is 2*radius, so we move 4*scan sideways, and scan downwards
		if (type == 0) {
			int xScans = (int)Math.ceil(width/(maxScan*4));
			int yScans = (int)Math.ceil(height/(maxScan*2));
			int totalScans = xScans * yScans;
			double lX, lY;
			
			for (int yS = yScans-1; yS >= 0; yS--) {
				lY = ((maxScan*2) * yS) + y;
				for (int xS = xScans-1; xS >= 0; xS--) {
					lX = ((maxScan*4) * xS) + x;
					Location l = new Location(lX, lY);
					moves.push(l);
				}
			}			
		}
	}

	public Location getNextMove() {
		if (!moves.empty()) {
			return (Location)moves.pop();
		} else {
			return null;
		}
	}

	public Location checkNextMove() {
		if (!moves.empty()) {
			return (Location)moves.peek();
		} else {
			return null;
		}
	}

	public void discardNextMove() {
		if (!moves.empty()) {
			moves.pop();
		}
	}

	public void addMove(Location l) {
		moves.push(l);
	}
}