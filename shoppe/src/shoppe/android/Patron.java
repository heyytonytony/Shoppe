package shoppe.android;

public class Patron extends NPC
{
	/** The amount of money this patron has **/
	int wealth;
	/** A factor that determines how influenced a patron may be when bartering **/
	int tenacity;
	/** Probability describing how likely a patron is to become interested in an item **/
	double buyProbability = 0.1;
	/** whether the patron is buying or selling an item */
	int interactionType = ShoppeConstants.BUY_PATRON;

	public Patron()
	{
		// TODO Auto-generated constructor stub
	}

	public Patron(int xpos, int ypos, int elementType, int wealth)
	{
		super(xpos, ypos, elementType);
		this.wealth = wealth;
	}
	
	public boolean interestedBuying() {

		//check for probability hit and if the patron wasn't previously interested
		if (!exclamation && Math.random() < buyProbability) {
			exclamation = !exclamation;
		}
		if (exclamation) {
			return true;
		}
		//else
		return false;
	}
	
	/**
	 * invoked when a patron is selected by the user
	 * @return determines if the patron is interested in interacting with the user, i.e. if the patron wants to barter
	 */
	public boolean startInteraction() {
		if (exclamation)
		{
			//TODO: implement patron interaction/bartering
			interacting = true;
			exclamation = false;
			return true;
		}
		return false;
	}
	
	/**
	 * invoked when a patron has finished interaction with user
	 */
	public void endInteraction() {
		interacting = false;
		//TODO: implement
	}
	
	/**
	 * get interaction type (patron wants to buy an item, or sell an item)
	 */
	public int getInteractionType()
	{
		return interactionType;
	}
	
}
