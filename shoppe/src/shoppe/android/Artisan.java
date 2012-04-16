package shoppe.android;

import java.util.LinkedList;

public class Artisan extends GridElement
{

	int skills[];
	LinkedList<Item> production = new LinkedList<Item>();
	int numProduction = 0;
	int productionProgress = 0;
	Item producedItem;
	int id;
	
	public Artisan(int id) {
		this.id = id;
	}

	public Artisan(int id, int xpos, int ypos, int elementType) {
		super(xpos, ypos, elementType);
		this.id = id;
		skills = new int[ShoppeConstants.getSubtypes(elementType)];
	}

	/**
	 * Updates this artisan's progress on an item for every call.
	 * 
	 * @return an Item object if production has finished, Null otherwise.
	 */
	public boolean update()
	{
		if(numProduction > 0)
		{
			productionProgress++;
			if(productionProgress == production.peek().productionCost)
			{
				productionProgress = 0;
				producedItem = production.remove();
				numProduction--;
				return true;
			}
		}
		//else
		return false;
	}

	/**
	 * Add an item to this artisan.
	 * @param item is the Item object to add to the production queue.
	 * @return the success state of adding the item. May return false if incompatible item, or if queue is full.
	 */
	boolean addProduction(Item item)
	{
		if (numProduction < ShoppeConstants.productionLimit)
		{ //&&item.elementType == this.elementType
			numProduction++;
			return production.add(item);
		}
		return false;
	}
	/**
	 * Removes an item from this artisan's production queue.
	 * @param item is the Item object to remove from the production queue.
	 * @return the success state of removing the item. May return false if the item is not found.
	 */
	boolean removeProduction(Item item)
	{
		numProduction--;
		return production.remove(item);
	}
	
	/**
	 * Increases the skill level of this artisan given a subtype.
	 * 
	 * @param subtype
	 *            is the skill type increased.
	 * @return the success state of increasing the specified skill type.
	 */
	boolean levelup(int subtype)
	{
		if(skills[subtype] < ShoppeConstants.skillLimit)
		{
			skills[subtype]++;
			return true;
		}
		// else
		return false;
	}
	
	public LinkedList<Item> getProductionQueue()
	{
		return production;
	}

	public CharSequence[] getPQCS()
	{
		CharSequence[] PQCS = new CharSequence[production.size()];
		for(int i = 0; i < production.size(); i++)
		{
			PQCS[i] = production.get(i).getItemName();
		}
		return PQCS;
	}
	
	public Item getProducedItem()
	{
		return producedItem;
	}
	
	public int getID()
	{
		return id;
	}

}
