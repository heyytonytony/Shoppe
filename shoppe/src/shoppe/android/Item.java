package shoppe.android;

public class Item extends GridElement
{

	int elementSubtype = -1;
	int value = -1;
	/** On a scale of 10 **/
	int rarity = -1;
	int productionCost = -1;
	String name;

	public Item()
	{
		super();
	}

	public Item(int xpos, int ypos, int elementType, int elementSubtype, int value, int rarity, int productionCost, String name)
	{
		super(xpos, ypos, elementType);
		this.elementSubtype = elementSubtype;
		this.value = value;
		this.rarity = rarity;
		this.productionCost = productionCost;
		this.name = name;
	}

}
