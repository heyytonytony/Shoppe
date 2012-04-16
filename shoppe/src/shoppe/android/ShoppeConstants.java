package shoppe.android;

import java.util.Random;

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
	
	//Defines movement directions
	public static final int up = 0;
	public static final int down = 1;
	public static final int left = 2;
	public static final int right = 3;
	
	/** message types */
	public static final int HIRE_ARTISAN = 100;
	public static final int FIRE_ARTISAN = 101;
	public static final int BUY_PATRON = 102;
	public static final int SELL_PATRON = 103;
	
	/** dialog IDs */
	public static final int DIALOG_PAUSE = 0;
	public static final int DIALOG_ARTISAN_1 = 1;
	public static final int DIALOG_ARTISAN_2 = 2;
	public static final int DIALOG_ARTISAN_3 = 3;
	public static final int DIALOG_ARTISAN_4 = 4;
	public static final int DIALOG_BUY_PATRON = 5;
	public static final int DIALOG_SELL_PATRON = 6;
	public static final int DIALOG_PROSPECTIVE_ARTISAN = 7;
	
	/** patron buy texts */
	private static final int CHAR_SEQUENCE_COUNT = 2;
	private static final Random RN_GEN = new Random();
	private static final CharSequence[] PATRON_BUY_TEXT0 = {
		"Why hello there!  I would like to purchase your ",
		"Good day, shopkeep.  Do you have a "
	};
	private static final CharSequence[] PATRON_BUY_TEXT1 = {
		" please.",
		" in stock today?"
	};
	
	public static int getSubtypes(int elementType) {
		return numSubtypes[elementType];
	}
	
	public static CharSequence getBuyText(String itemName)
	{
		int index = RN_GEN.nextInt(CHAR_SEQUENCE_COUNT);
		CharSequence cs = PATRON_BUY_TEXT0[index] + itemName + PATRON_BUY_TEXT1[index];
		return cs;
	}
}
