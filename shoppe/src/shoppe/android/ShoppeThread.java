package shoppe.android;

import java.util.Iterator;
import java.util.LinkedList;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
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
	private Drawable[] artisanDrawable;

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

	/** The list of artisans working in the shop **/
	private LinkedList<Artisan> artisanList = new LinkedList<Artisan>();

	/** The list of items owned by the player **/
	private LinkedList<Item> inventoryList = new LinkedList<Item>();

	private SurfaceHolder surfaceHolder = null;

	/** Used to keep track of update cycles **/
	private long patronBeginTime;
	
	/** Used to keep track of update cycles **/
	private long artisanBeginTime;

	private boolean[][] tileOccupied = new boolean[gridHeight][gridWidth];

	/** The amount of time in seconds that patron positions are updated **/
	private static final int patronUpdateInterval = 2;
	
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
	
	/** Current inventory */
	ImageAdapter inventoryAdapter;

	public ShoppeThread(SurfaceHolder surfaceHolder, Context context, Handler handler)
	{
		this.surfaceHolder = surfaceHolder;
		Resources res = context.getResources();
		backgroundBitmap = BitmapFactory.decodeResource(res, R.drawable.background);
		patronDrawable = context.getResources().getDrawable(R.drawable.patron);
		plainTile = context.getResources().getDrawable(R.drawable.tile);
		counterTile = context.getResources().getDrawable(R.drawable.countertile);
		exclamationBubble = context.getResources().getDrawable(R.drawable.exclamation);
		inventoryAdapter = new ImageAdapter(context);
		selectedX = selectedY = -1;
		init();
	}

	public void init()
	{
		patronBeginTime = System.currentTimeMillis();
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

		// test
		patronList.getLast().exclamation = true;

		// let's make some items
		itemList.add(new Item(-1, -1, ShoppeConstants.weapon, ShoppeConstants.flail, 20, 4, 3, "Flail of Truthiness"));
		itemList.add(new Item(-1, -1, ShoppeConstants.armor, ShoppeConstants.kite, 55, 5, 2, "Mangled Kite Armor"));
		itemList.add(new Item(-1, -1, ShoppeConstants.potion, ShoppeConstants.poison, 100, 6, 5, "PBDE"));
		itemList.add(new Item(-1, -1, ShoppeConstants.armor, ShoppeConstants.blah, 102, 4, 5, "Chipped Blah"));
		
		//get an employee
		artisanList.add(new Artisan());

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
				return artisan.removeProduction(item);
			}
		}
		//if artisan not found
		return false;
	}

	private void patronUpdate()
	{
		Iterator<Patron> iterator = patronList.iterator();
		Patron patron;
		boolean[] availableDirections = new boolean[4];
		int xpos, ypos;
		if(System.currentTimeMillis() > patronBeginTime + patronUpdateInterval * 1000)
		{
			while(iterator.hasNext())
			{
				for(int i = 0; i < 4; i++)
				{
					availableDirections[i] = false;
				}
				patron = iterator.next();
				// only potentially move patrons that are not interacting with
				// the user
				if(patron.interacting == false)
				{
					xpos = patron.xpos;
					ypos = patron.ypos;
					if(ypos - 1 >= 0)
					{
						availableDirections[ShoppeConstants.up] = !tileOccupied[ypos - 1][xpos];
					}
					if(ypos + 1 < gridHeight)
					{
						availableDirections[ShoppeConstants.down] = !tileOccupied[ypos + 1][xpos];
					}
					if(xpos - 1 >= 0)
					{
						availableDirections[ShoppeConstants.left] = !tileOccupied[ypos][xpos - 1];
					}
					if(xpos + 1 < gridWidth)
					{
						availableDirections[ShoppeConstants.right] = !tileOccupied[ypos][xpos + 1];
					}
					int moveDirection = patron.move(availableDirections);
					if(moveDirection > -1)
					{ // update tileOccupied
						tileOccupied[ypos][xpos] = false;
						switch(moveDirection)
						{
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
			patronBeginTime = System.currentTimeMillis();
		}
	}

	public boolean buyItem(Item item)
	{
		if(funds - item.value >= 0)
		{
			funds -= item.value;
			inventoryList.add(item);
			return true;
		}
		// else
		return false;
	}

	@Override
	public void run()
	{
		while(true)
		{
			if(mRun)
			{
				patronUpdate();
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

	private void draw(Canvas canvas)
	{
		canvas.drawBitmap(backgroundBitmap, 0, 0, null);

		drawGrid(canvas);
		drawPatrons(canvas);
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
		Iterator<Patron> iterator;
		Patron patron;
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
						// determine if there might exist a patron at the given
						// tile
						if(tileOccupied[tileY][tileX])
						{
							// search through patron list for potential patron
							// at this location
							iterator = patronList.iterator();
							while(iterator.hasNext())
							{
								patron = iterator.next();
								if(patron.xpos == tileX && patron.ypos == tileY)
								{
									// highlight the tile
									selectedX = tileX;
									selectedY = tileY;
									// start interaction
									interactingPatron = patron;
									//grab a psuedo-random item from the itemList
									//potentially not in store inventory
									int itemIndex = itemList.size() % (int)(10*Math.random());
									//Log.v("item generation", "itemIndex :" + itemIndex + " listSize: " + itemList.size());
									
									patronsItem = itemList.get(itemIndex);
									Log.v("patronUpdate", "Added item " + itemList.get(itemIndex).name + " at index " + itemIndex);
									if(patron.startInteraction())
									{
										interactingPatron = patron;
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
}
