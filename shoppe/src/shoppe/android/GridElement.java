package shoppe.android;

public abstract class GridElement
{
	// Represents the grid coordinates for element
	int xpos = -1;
	int ypos = -1;

	// The type of element represented eg. Armor, Potion...
	int elementType = -1;

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
}
