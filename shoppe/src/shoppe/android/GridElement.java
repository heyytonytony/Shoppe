package shoppe.android;

public abstract class GridElement
{
	/** Represents the grid coordinates for element */
	int xpos = -1;
	int ypos = -1;

	/** The type of element represented, e.g. Armor, Potion... */
	int elementType = -1;
	
	/** reference to drawable id */
	int drawableID = R.drawable.default_item;

	GridElement()
	{
		// default
	}

	GridElement(int xpos, int ypos, int elementType)
	{
		this.xpos = xpos;
		this.ypos = ypos;
		this.elementType = elementType;
	}

	GridElement(int xpos, int ypos, int elementType, int drawableID)
	{
		this.xpos = xpos;
		this.ypos = ypos;
		this.elementType = elementType;
		this.drawableID = drawableID;
	}
	
	public int getDrawableID()
	{
		return drawableID;
	}
}
