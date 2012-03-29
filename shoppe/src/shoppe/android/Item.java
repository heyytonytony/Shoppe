package shoppe.android;

public class Item extends GridElement
{

	int elementSubtype = -1;
	int value = -1;
	int rarity = -1;
	int productionCost = -1;

	public Item()
	{
		super();
	}

	public Item(int xpos, int ypos, int elementType, int elementSubtype, int value, int rarity, int productionCost)
	{
		super(xpos, ypos, elementType);
		this.elementSubtype = elementSubtype;
		this.value = value;
		this.rarity = rarity;
		this.productionCost = productionCost;
	}

}
