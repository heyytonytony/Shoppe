package shoppe.android;

public class NPC extends GridElement {
	/** Probability that the patron will exit the shop **/
	double exitProbability = 0.05;
	/** Describes if the patron is currently interacting with the user **/
	boolean interacting = false;
	/** Describes if the patron is interested in leaving the shop **/
	boolean exiting = false;
	/** The probability that a patron will move from their current tile **/
	double movementProbability = 0.5;
	/** Determines if a NPC is seeking player attention **/
	boolean exclamation = false;

	public NPC() {
		//default
	}

	public NPC(int xpos, int ypos, int elementType) {
		super(xpos, ypos, elementType);
	}

	public NPC(int xpos, int ypos, int elementType, int drawableID) {
		super(xpos, ypos, elementType, drawableID);
	}

	public int forceMove(boolean[] availableDirections) { 
		double savedMovementProbability = movementProbability;
		movementProbability = 1;
		int chosenDirection = move(availableDirections);
		movementProbability = savedMovementProbability;
		return chosenDirection;
	}
	public int move(boolean[] availableDirections) {
		//System.out.println("Movement from " + xpos + "," + ypos);
		double[] directionProbability = new double[4];
		int numDirections = 0, currentAvailableDirections;
		double random = Math.random();
		int chosenDirection = -1;
		//determine if it is time to move
		if (Math.random() < movementProbability) {
			//System.out.println("updating patrons");
			for (int i = 0; i < 4; i++) {
				if (availableDirections[i] == true) {
					numDirections++;
				}
			}
			currentAvailableDirections = numDirections-1;
			if (currentAvailableDirections < 0) {
				currentAvailableDirections = 0;
			}
			for (int i = 0; i < 4; i++) {
				if (availableDirections[i] == true) {
					directionProbability[i] = 1.0*currentAvailableDirections/numDirections;
					currentAvailableDirections--;
				}
				else {
					directionProbability[i] = 2;
				}
			}
			//determine number of options
			//determine a direction to move
			for (int i = 0; i < 4; i++) {
				if (directionProbability[i] < random) {
					chosenDirection = i;
					break;
				}
			}
			//System.out.println("Moved " + chosenDirection);
			switch (chosenDirection) {
			case ShoppeConstants.up:
				ypos--;
				break;
			case ShoppeConstants.down:
				ypos++;
				break;
			case ShoppeConstants.left:
				xpos--;
				break;
			case ShoppeConstants.right:
				xpos++;
				break;
			default:
				System.out.println("Error in Patron movement");
			}
			return chosenDirection;
		}
		//else
		return -1;
	}

}
