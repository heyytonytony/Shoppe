package shoppe.android;

public class Patron extends GridElement
{
	/** The amount of money this patron has **/
	int wealth;
	/** A factor that determines how influenced a patron may be when bartering **/
	int tenacity;
	/** Determines if a patron is seeking player attention **/
	boolean exclamation = false;
	/** The probability that a patron will move from their current tile **/
	double movementProbability = 0.5;

	public Patron()
	{
		// TODO Auto-generated constructor stub
	}

	public Patron(int xpos, int ypos, int elementType, int wealth)
	{
		super(xpos, ypos, elementType);
		this.wealth = wealth;
	}
	public int move(boolean[] availableDirections) {
		//System.out.println("Movement from " + xpos + "," + ypos);
		double[] directionProbability = new double[4];
		int numDirections = 0, currentAvailableDirections;
		double random = Math.random();
		int chosenDirection = -1;
		//determine if it is time to move
		if (Math.random() < movementProbability) {
			System.out.println("updating patrons");
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
