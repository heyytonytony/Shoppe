package shoppe.android;

public final class ShoppeConstants {
	/**
	 * Contains global constants for game related identifiers
	 */
	//Describes the different types that correspond to Artisans, Items, Patron desirables
	public static final int weapon = 0;
	public static final int armor = 1;
	public static final int potion = 2;
	
	//Describes the sub-types for general items. Corresponds to item subtype/skill type
	//Weapon-specific
	public static final int weaponSubtypes = 3;
	public static final int sword = 0;
	public static final int flail = 1;
	public static final int etc = 2;
	
	//Armor-specific
	public static final int armorSubtypes = 3;
	public static final int kite = 0;
	public static final int blah = 1;
	public static final int epicbacon = 2;
	
	//Potion-specific
	public static final int potionSubtypes = 3;
	public static final int healing = 0;
	public static final int regeneration = 1;
	public static final int poison = 2;
	
	public static final int[] numSubtypes = {weaponSubtypes, armorSubtypes, potionSubtypes};
	
	//Artisan-related constants
	public static final int productionLimit = 5;
	public static final int skillLimit = 10;
	
	/** Defines tile identities **/
	public static final int plainTile = 0;
	public static final int counterTile = 1;
	public static final int obstructionTile = 2;
	
	public static int getSubtypes(int elementType) {
		return numSubtypes[elementType];
	}
}
