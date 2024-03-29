package shoppe.android;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class ShoppeThread extends Thread
{
	/** Indicate whether the surface has been created and is ready to draw */
	private boolean mRun = false;
	
	/** indicates end of activity */
	private boolean alive;

	/** Width of the grid of gridElements **/
	private static int gridWidth = 8;
	/** Height of the grid of gridElements **/
	private static int gridHeight = 5;
	/** Pixel width of tiles **/
	private static int tileWidth = 100;
	/** Pixel height of tiles **/
	private static int tileHeight = 100;
	/** The proportions of the grid width with respect to screen size **/
	private static double gridWidthProportion = 0.75;
	/** The proportions of the grid height with respect to screen size **/
	private static double gridHeightProportion = 0.75;

	/** The offset (spacing) of the grid drawing from the left of the screen **/
	private static int offsetX;
	/** The offset (spacing) of the grid drawing from the top of the screen **/
	private static int offsetY;

	/** Width of the screen **/
	private static int screenWidth;
	/** Height of the screen **/
	private static int screenHeight;

	/** The drawable to use as the background of the animation canvas */
	private Bitmap backgroundBitmap;

	/** Array of tile images used for the grid background **/
	int[][] tiles = new int[gridHeight][gridWidth];

	/** Drawable representing a patron **/
	private Drawable patronDrawable;
	/** Drawable representing the artisans **/
	private Drawable artisanDrawable;

	/** Drawable representing an open tile **/
	private Drawable plainTile;
	/** Drawable representing a countertop tile **/
	private Drawable counterTile;
	/** Drawable representing an exclamation bubble **/
	private Drawable exclamationBubble;

	/**
	 * Current height of the surface/canvas.
	 * 
	 * @see #setSurfaceSize
	 */
	private int canvasHeight = 1;

	/**
	 * Current width of the surface/canvas.
	 * 
	 * @see #setSurfaceSize
	 */
	private int canvasWidth = 1;

	/** The available funds of the shop **/
	private int funds = 1000;

	/** The list of patrons in the shop **/
	private LinkedList<Patron> patronList = new LinkedList<Patron>();
	
	/** The list of employable but not hired artisans in the shop **/
	private LinkedList<Artisan> employableArtisanList = new LinkedList<Artisan>();

	/** The list of artisans working in the shop **/
	private LinkedList<Artisan> artisanList = new LinkedList<Artisan>();
	
	/** which slots have employed an artisan */
	private boolean[] employedArtisan = new boolean[4];

	/** The list of items owned by the player **/
	private LinkedList<Item> inventoryList = new LinkedList<Item>();

	private SurfaceHolder surfaceHolder = null;

	/** Used to keep track of update cycles for NPCs visible on shop floor. Used for movement and such. **/
	private long NPCLastUpdate;
	
	/** Used to keep track of update cycles for generating new NPCs entering the shop **/
	private long NPCLastGenerate;
	
	/** Used to keep track of update cycles for item production **/
	private long artisanBeginTime;

	private boolean[][] tileOccupied = new boolean[gridHeight][gridWidth];

	/** The amount of time in seconds that patron positions are updated **/
	private static final int NPCUpdateInterval = 2;
	
	/** The amount of time in seconds that artisans are updated **/
	private static final int artisanUpdateInterval = 10;

	/** Global list of possible items **/
	private LinkedList<Item> itemList = new LinkedList<Item>();

	/** Defines how input is handled based on integer states **/
	private int inputMode;
	/** inputMode state where the user selects patrons **/
	private static final int DEFAULT_INPUT = 0;
	/** inputMode state where the user is placing an item on the shop floor **/
	private static final int PLACEITEM_INPUT = 1;

	/** Paint to draw text **/
	private Paint textPaint = new Paint();

	/** Paint to draw text background box **/
	private Paint boxPaint = new Paint();

	/** Paint to draw border highlight **/
	private Paint borderPaint = new Paint();

	/** Selected tile from user input to be highlighted **/
	private int selectedX,
			selectedY;

	/** The patron currently engaged by the user **/
	private Patron interactingPatron;
	
	/** The item the patron is interested in **/
	private Item patronsItem;

	/** Number of artisans ever hired. Used for assigning ids (is this still true?) **/
	private static int artisanCount;
	
	/** Maximum number of artisans employed at any time **/
	private static int maxArtisans = 4;
	
	/** Maximum number of patrons in the shop at any time **/
	private static int maxNPCs = 10;
	
	/** Upper limit of patron wealth when randomly generating the value **/
	private static final int maxPatronWealth = 5000;
	
	/** Probability for new patrons entering the shop **/
	private static double NPCEnterProbability = 0.25;
	
	/** The patron-to-artisan ratio for generating a new NPC **/
	private static final double generatePatronArtisanRatio = 0.9;
	
	/** Describes if an employable artisan is in the shop. The shop can only have one prospective employee at a time **/
	private boolean employableArtisanPresent = false;
	
	/** Shop reputation determines how frequent new patrons enter **/
	private int reputation = 0;
	
	/** Random number generator **/
	private Random randomGenerator;
	
	/** Current inventory */
	private ImageAdapter inventoryAdapter;
	
	/** Handler reference */
	private Handler handler;
	
	/** Context reference */
	private Context context;

	public ShoppeThread(SurfaceHolder surfaceHolder, Context context, Handler handler)
	{
		this.surfaceHolder = surfaceHolder;
		Resources res = context.getResources();
		backgroundBitmap = BitmapFactory.decodeResource(res, R.drawable.background);
		patronDrawable = context.getResources().getDrawable(R.drawable.patron);
		artisanDrawable = context.getResources().getDrawable(R.drawable.artisan);
		plainTile = context.getResources().getDrawable(R.drawable.tile);
		counterTile = context.getResources().getDrawable(R.drawable.countertile);
		exclamationBubble = context.getResources().getDrawable(R.drawable.exclamation);
		inventoryAdapter = new ImageAdapter(context);
		this.handler = handler;
		this.context = context;
		selectedX = selectedY = -1;
		alive = true;
		init();
	}

	public void init()
	{
		randomGenerator = new Random();
		NPCLastUpdate = System.currentTimeMillis();
		// initialize occupied tiles on grid
		for(int i = 0; i < gridHeight; i++)
		{
			for(int j = 0; j < gridWidth; j++)
			{
				tileOccupied[i][j] = false;
			}
		}
		// initialize textPaint
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(tileHeight / 4);

		// initialize boxPaint
		boxPaint.setARGB(255, 220, 220, 120);

		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(2);
		borderPaint.setColor(Color.BLACK);

		// testing draw method
		// set counter tiles
		tiles[1][1] = 1;
		tiles[1][2] = 1;
		tiles[4][4] = 1;

		// add patrons
		addPatron(new Patron(3, 4, ShoppeConstants.potion, 100));
		addPatron(new Patron(2, 3, ShoppeConstants.armor, 100));
		
		// adding first artisan
		employableArtisanList.add(new Artisan(0, 7, 0, randomGenerator.nextInt(ShoppeConstants.numSubtypes.length)));
		employableArtisanPresent = true;
		tileOccupied[0][7] = true;

		// test
		patronList.getLast().exclamation = true;

		// let's make some items
		itemList.add(new Item(-1, -1, ShoppeConstants.weapon, ShoppeConstants.flail, 20, 4, 3, "Flail of Truthiness"));
		itemList.add(new Item(-1, -1, ShoppeConstants.armor, ShoppeConstants.kite, 55, 5, 2, "Mangled Kite Armor"));
		itemList.add(new Item(-1, -1, ShoppeConstants.potion, ShoppeConstants.poison, 100, 6, 5, "PBDE"));
		itemList.add(new Item(-1, -1, ShoppeConstants.armor, ShoppeConstants.blah, 102, 4, 5, "Chipped Blah"));
		
		//get an employee
//		artisanList.add(new Artisan(artisanCount++));

	}

	public boolean addPatron(Patron patron)
	{
		if(patron.xpos >= 0 && patron.xpos < gridWidth && patron.ypos >= 0 && patron.ypos < gridHeight && !tileOccupied[patron.ypos][patron.xpos])
		{
			patronList.add(patron);
			tileOccupied[patron.ypos][patron.xpos] = true;
			return true;
		}
		// else
		return false;
	}

	/* Callback invoked when the surface dimensions change. */
	public void setSurfaceSize(int width, int height)
	{
		// synchronized to make sure these all change atomically
		synchronized(surfaceHolder)
		{
			canvasWidth = width;
			canvasHeight = height;

			// don't forget to resize the background image
			backgroundBitmap = Bitmap.createScaledBitmap(backgroundBitmap, width, height, true);
			tileHeight = (int)(height * gridHeightProportion / gridHeight);
			tileWidth = tileHeight;
			screenWidth = width;
			screenHeight = height;
			offsetX = (screenWidth - tileWidth * gridWidth) / 2;
			offsetY = (screenHeight - tileHeight * gridHeight) / 2;

			textPaint.setTextSize(tileHeight / 4);
		}
	}
	
	private void artisanUpdate() {
		Iterator<Artisan> iterator = artisanList.iterator();
		Artisan artisan;
		Item item;
		if (System.currentTimeMillis() > artisanBeginTime + artisanUpdateInterval * 1000) {
			while (iterator.hasNext()) {
				artisan = iterator.next();
				if (artisan.update() == true) {
					item = artisan.producedItem;
					inventoryList.add(item);
					inventoryAdapter.addItem(item.getDrawableID());
					Message msg = handler.obtainMessage();
					msg.arg1 = artisan.getID();
					msg.what = ShoppeConstants.ITEM_PRODUCED;
					msg.obj = item;
					handler.sendMessage(msg);
					Log.d("artisan update","artisan " + msg.arg1 + " created " + item.getItemName());
					Log.d("artisan update","inventory now has " + inventoryAdapter.getCount() + " items");
				}
			}
			artisanBeginTime = System.currentTimeMillis();
		}
	}
	
	public boolean addProduction(int id, Item item) {
		Artisan artisan;
		Iterator<Artisan> iterator = artisanList.iterator();
		while (iterator.hasNext()) {
			artisan = iterator.next();
			if (id == artisan.id) {
				Log.d("add item to production queue", "artisan" + id + " making item " + item.getItemName());
				return artisan.addProduction(item);
			}
		}
		//if artisan not found
		return false;
	}
	
	public boolean removeProduction(int id, Item item) {
		Artisan artisan;
		Iterator<Artisan> iterator = artisanList.iterator();
		while (iterator.hasNext()) {
			artisan = iterator.next();
			if (id == artisan.id) {
				Log.d("remove item from production queue", "artisan" + id + " scrapping item " + item.getItemName());
				return artisan.removeProduction(item);
			}
		}
		//if artisan not found
		return false;
	}
	
	public boolean hireArtisan(Artisan artisan)
	{
		if (artisanList.size() < maxArtisans)
		{
		    int iArt = artisan.id;
		        if(!employedArtisan[iArt])
		        {
		            artisanCount++;
		            employedArtisan[iArt] = true;
		            Message msg = handler.obtainMessage();
		            msg.arg1 = iArt;
		            msg.what = ShoppeConstants.HIRE_ARTISAN;
		            handler.sendMessage(msg);
		            Log.d("hired an artisan", "artisan number:" + msg.arg1 + ", total:" + artisanCount);
		            return artisanList.add(artisan);
		        }
		}
		//else
		return false;
	}
	
	public boolean fireArtisan(int id) {
		Artisan artisan;
		Iterator<Artisan> iterator = artisanList.iterator();
		while (iterator.hasNext()) {
			artisan = iterator.next();
			if (id == artisan.id) {
			    artisanCount--;
				iterator.remove();
				employedArtisan[id] = false;
				Message msg = handler.obtainMessage();
				msg.arg1 = id;
				msg.what = ShoppeConstants.FIRE_ARTISAN;
				handler.sendMessage(msg);
				Log.d("fired an artisan", "artisan number:" + msg.arg1 + ", total:" + artisanCount);
				return true;
			}
		}
		//if artisan not found
		return false;
	}

	private void NPCUpdate(LinkedList<? extends NPC> npcList) {
		Iterator<? extends NPC> iterator = npcList.iterator();
		NPC npc;
		boolean[] availableDirections = new boolean[4];
		int xpos, ypos;
		while (iterator.hasNext()) {
			npc = iterator.next();
			// figure out if we want to have this patron exit
			if (Math.random() < npc.exitProbability) {
				// exit this patron
				if (npc instanceof Artisan) {
					employableArtisanPresent = false;
				}
				tileOccupied[npc.ypos][npc.xpos] = false;
				iterator.remove();
			}
			else {
				for (int i = 0; i < 4; i++) {
					availableDirections[i] = false;
				}
				// only potentially move patrons that are not interacting
				// with
				// the user
				if (npc.interacting == false) {
					xpos = npc.xpos;
					ypos = npc.ypos;
					if (ypos - 1 >= 0) {
						availableDirections[ShoppeConstants.up] = !tileOccupied[ypos - 1][xpos];
					}
					if (ypos + 1 < gridHeight) {
						availableDirections[ShoppeConstants.down] = !tileOccupied[ypos + 1][xpos];
					}
					if (xpos - 1 >= 0) {
						availableDirections[ShoppeConstants.left] = !tileOccupied[ypos][xpos - 1];
					}
					if (xpos + 1 < gridWidth) {
						availableDirections[ShoppeConstants.right] = !tileOccupied[ypos][xpos + 1];
					}
					int moveDirection = npc.move(availableDirections);
					if (moveDirection > -1) { // update tileOccupied
						tileOccupied[ypos][xpos] = false;
						switch (moveDirection) {
						case ShoppeConstants.up:
							tileOccupied[ypos - 1][xpos] = true;
							break;
						case ShoppeConstants.down:
							tileOccupied[ypos + 1][xpos] = true;
							break;
						case ShoppeConstants.left:
							tileOccupied[ypos][xpos - 1] = true;
							break;
						case ShoppeConstants.right:
							tileOccupied[ypos][xpos + 1] = true;
							break;
						}
					}
				}
				// else do nothing
			}
		}
	}
	
	public void generateNPCs() {
		if (patronList.size() + employableArtisanList.size() < maxNPCs && Math.random() < NPCEnterProbability) {
			// determine if a new NPC is to enter
			// determine the initial position, assuming the entrance is at the bottom center of the screen
			boolean openTile1, openTile2;
			openTile1 = !tileOccupied[gridHeight - 1][gridWidth / 2];
			openTile2 = !tileOccupied[gridHeight - 1][gridWidth / 2 - 1];
			if (!employableArtisanPresent && generatePatronArtisanRatio < Math.random() && artisanCount < 4) {
				// make a new artisan
				employableArtisanPresent = true;
				// find first available position
				int iArt;
				for (iArt = 0; iArt < maxArtisans; iArt++) {
					if (!employedArtisan[iArt]) {
						break;
					}
				}
				// find an open tile near the entrance
				if (openTile1) {
					employableArtisanList.add(new Artisan(iArt, gridWidth / 2, gridHeight - 1, randomGenerator
							.nextInt(ShoppeConstants.numSubtypes.length)));
					tileOccupied[gridHeight - 1][gridWidth / 2] = true;
					Log.v("generateNPCs", "Artisan entered shop");
				}
				else if (openTile2) {
					employableArtisanList.add(new Artisan(iArt, gridWidth / 2 - 1, gridHeight - 1, randomGenerator
							.nextInt(ShoppeConstants.numSubtypes.length)));
					tileOccupied[gridHeight - 1][gridWidth / 2 - 1] = true;
					Log.v("generateNPCs", "Artisan entered shop");
				}
				else {
					Log.v("generateNPCs", "New artisan can't enter shop");
				}
			}
			else {
				// make a new patron
				if (openTile1) {
					patronList.add(new Patron(gridHeight - 1, gridWidth / 2, randomGenerator.nextInt(ShoppeConstants.numSubtypes.length),
							randomGenerator.nextInt(maxPatronWealth)));
					tileOccupied[gridHeight - 1][gridWidth / 2] = true;
				}
				else if (openTile2) {
					patronList.add(new Patron(gridHeight - 1, gridWidth / 2 - 1, randomGenerator.nextInt(ShoppeConstants.numSubtypes.length),
							randomGenerator.nextInt(maxPatronWealth)));
					tileOccupied[gridHeight - 1][gridWidth / 2 - 1] = true;

				}
				else {
					Log.v("generateNPCs", "New patron can't enter shop");
				}
			}
		}
	}

	public boolean buyItem(Item item)
	{
		if(funds - item.value >= 0)
		{
			funds -= item.value;
			inventoryList.add(item);
			inventoryAdapter.addItem(item.getDrawableID());
			return true;
		}
		// else
		return false;
	}
	
	public boolean sellItem(Item item)
	{
		if(inventoryList.contains(item))
		{
			funds += item.value;
			inventoryList.remove(item);
			inventoryAdapter.removeItem(item.getDrawableID());
			return true;
		}
		// else
		return false;
	}

	@Override
	public void run()
	{
		while(true && alive)
		{
			if(mRun)
			{

				if (System.currentTimeMillis() > NPCLastUpdate + NPCUpdateInterval * 1000) {
					NPCUpdate(patronList);
					NPCUpdate(employableArtisanList);
					generateNPCs();
					NPCLastUpdate = System.currentTimeMillis();
				}
				artisanUpdate();
				Canvas canvas = null;
				try
				{
					canvas = surfaceHolder.lockCanvas(null);
					synchronized(surfaceHolder)
					{
						draw(canvas);
					}
				}
				finally
				{
					if(canvas != null)
					{
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}
	}

	public void setRunning(boolean b)
	{
		mRun = b;
	}
	
	public void killThread()
	{
		alive = false;
		artisanCount = 0;
	}

	private void drawGrid(Canvas canvas)
	{
		int tileX, tileY;
		for(int i = 0; i < gridHeight; i++)
		{
			for(int j = 0; j < gridWidth; j++)
			{
				// calculates the tile position within the grid, then adds an
				// offset to center in screen
				tileX = j * tileWidth + offsetX;
				tileY = i * tileHeight + offsetY;
				// Log.v("stuffs","i: " + i + " j: " + j + " tile: " +
				// tiles[j][i] + " tileX: " + tileX + " tileY: " + tileY);
				switch(tiles[i][j])
				{
					case ShoppeConstants.plainTile:
						plainTile.setBounds(tileX, tileY, tileX + tileWidth, tileY + tileHeight);
						plainTile.draw(canvas);
						break;
					case ShoppeConstants.counterTile:
						counterTile.setBounds(tileX, tileY, tileX + tileWidth, tileY + tileHeight);
						counterTile.draw(canvas);
						break;
				}
			}
		}
		// draw selected tile highlight
		if(selectedX > -1 && selectedY > -1)
		{
			tileX = selectedX * tileWidth + offsetX;
			tileY = selectedY * tileHeight + offsetY;
			borderPaint.setColor(Color.YELLOW);
			canvas.drawRect(tileX, tileY, tileX + tileWidth, tileY + tileHeight, borderPaint);
		}
	}

	private void drawPatrons(Canvas canvas)
	{
		int xloc, yloc;
		Iterator<Patron> iterator = patronList.iterator();
		Patron p;
		// draw the patrons
		while(iterator.hasNext())
		{
			p = iterator.next();
			xloc = p.xpos * tileWidth + offsetX;
			yloc = p.ypos * tileHeight + offsetY;
			patronDrawable.setBounds(xloc, yloc, xloc + tileWidth, yloc + tileHeight);
			patronDrawable.draw(canvas);
		}
		// draw the patron's exclamations
		iterator = patronList.iterator();
		while(iterator.hasNext())
		{
			p = iterator.next();
			if(p.exclamation == true)
			{
				xloc = p.xpos * tileWidth + offsetX;
				yloc = p.ypos * tileHeight + offsetY;
				exclamationBubble.setBounds(xloc + tileWidth / 2, yloc, xloc + tileWidth, yloc + tileHeight / 2);
				exclamationBubble.draw(canvas);
			}
		}
	}

	private void drawEmployableArtisans(Canvas canvas)
	{
		int xloc, yloc;
		Iterator<Artisan> iterator = employableArtisanList.iterator();
		Artisan a;
		// draw the patrons
		while(iterator.hasNext())
		{
			a = iterator.next();
			xloc = a.xpos * tileWidth + offsetX;
			yloc = a.ypos * tileHeight + offsetY;
			artisanDrawable.setBounds(xloc, yloc, xloc + tileWidth, yloc + tileHeight);
			artisanDrawable.draw(canvas);
		}
	}
	private void draw(Canvas canvas)
	{
		canvas.drawBitmap(backgroundBitmap, 0, 0, null);

		drawGrid(canvas);
		drawPatrons(canvas);
		drawEmployableArtisans(canvas);
		// border
		borderPaint.setColor(Color.BLACK);
		canvas.drawRect(8, 8, tileWidth * 2 + 2, 20 + tileHeight / 2 + 2, borderPaint);
		// background
		canvas.drawRect(10, 10, tileWidth * 2, 20 + tileHeight / 2, boxPaint);
		// text
		canvas.drawText("Funds: " + funds, 20, tileHeight / 2, textPaint);
	}

	public boolean onTouch(MotionEvent event)
	{
		float inputX, inputY;
		int tileX, tileY;
		Iterator<Patron> pIterator;
		Iterator<Artisan> aIterator;
		Patron patron;
		Artisan artisan;
		// Log.v("onTouch","Input event " + event.getAction());
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{ // pressing input is received
			inputX = event.getX();
			inputY = event.getY();
			// Log.v("onTouch","getX: " + inputX + " getY: " + inputY);
			// determine if input event is within the bounds of the grid
			if(interactingPatron != null)
			{
				interactingPatron.endInteraction();
				interactingPatron = null;
				// TODO: call interactingPatron.endInteraction() in the
				// appropriate place
				selectedX = -1;
				selectedY = -1;
			}
			if(inputX >= offsetX && inputY > offsetY && inputX < offsetX + gridWidth * tileWidth && inputY < offsetY + gridHeight * tileHeight)
			{
				// find the corresponding tile location
				tileX = (int)((1.0 * inputX - offsetX) / tileWidth);
				tileY = (int)((1.0 * inputY - offsetY) / tileHeight);
				// Log.v("onTouch","x: " + tileX + " y: " + tileY);
				switch(inputMode)
				{
					case DEFAULT_INPUT:
						// determine if there might exist a NPC at the given
						// tile
						if(tileOccupied[tileY][tileX])
						{
							//search through potential artisan hires
							aIterator = employableArtisanList.iterator();
							while (aIterator.hasNext()) {
								artisan = aIterator.next();
								if (artisan.xpos == tileX && artisan.ypos == tileY) {
									hireArtisan(artisan);
									employableArtisanPresent = false;
									aIterator.remove();
									tileOccupied[tileY][tileX] = false;
									break;
								}
							}
							// search through patron list for potential patron
							// at this location
							pIterator = patronList.iterator();
							while(pIterator.hasNext())
							{
								patron = pIterator.next();
								if(patron.xpos == tileX && patron.ypos == tileY)
								{
									// highlight the tile
									selectedX = tileX;
									selectedY = tileY;
									// start interaction
									interactingPatron = patron;
									//grab a psuedo-random item from the itemList
									//potentially not in store inventory
									int itemIndex = randomGenerator.nextInt(itemList.size() - 1);
									Log.v("item generation", "itemIndex :" + itemIndex + " listSize: " + itemList.size());
									
									patronsItem = itemList.get(itemIndex);
									Log.v("patronUpdate", "Added item " + itemList.get(itemIndex).name + " at index " + itemIndex);
									
									//interact with patron!
									if(patron.startInteraction())
									{
										interactingPatron = patron;
							            Message msg = handler.obtainMessage();
							            msg.obj = patronsItem;
							            msg.what = interactingPatron.getInteractionType();
							            handler.sendMessage(msg);
										
									}
									break;
								}
							}
						}
						break;
					case PLACEITEM_INPUT:
						// TODO: implement item placement on shop floor
						break;
				}
			}
			return true;
		}
		// else
		return false;
	}

	public ImageAdapter getInventoryAdapter()
	{
		return inventoryAdapter;
	}
	
	public boolean[] getEmployed()
	{
		return employedArtisan;
	}
	
	public void setHandler(Handler handler)
	{
		this.handler = handler;
	}
	
	public LinkedList<Item> getItemList()
	{
		return itemList;
	}
	
	public CharSequence[] getItemCS()
	{
		CharSequence[] itemCS = new CharSequence[itemList.size()];
		for(int i = 0; i < itemList.size(); i++)
		{
			itemCS[i] = itemList.get(i).getItemName();
		}
		return itemCS;
	}
	
	public LinkedList<Item> getArtisanProductionQueue(int artisanID)
	{
		Artisan artisan;
		Iterator<Artisan> iterator = artisanList.iterator();
		while(iterator.hasNext())
		{
			artisan = iterator.next();
			if(artisan.id == artisanID)
			{
				return artisan.getProductionQueue();
			}
		}
		return null;
	}
	
	public CharSequence[] getArtisantPQCS(int artisanID)
	{
		Artisan artisan;
		Iterator<Artisan> iterator = artisanList.iterator();
		while(iterator.hasNext())
		{
			artisan = iterator.next();
			if(artisan.id == artisanID)
			{
				return artisan.getPQCS();
			}
		}
		return null;
	}
	
}
