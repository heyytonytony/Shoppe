package shoppe.android;

import java.util.Iterator;
import java.util.LinkedList;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class ShoppeThread extends Thread
{
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
    /** Drawable representing an artisan **/
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
    
    /** The list of artisans working in the shop **/
    private LinkedList<Artisan> artisanList = new LinkedList<Artisan>();
    
	private SurfaceHolder surfaceHolder = null;
	public ShoppeThread(SurfaceHolder surfaceHolder, Context context, Handler handler)
	{
		this.surfaceHolder = surfaceHolder;
		Resources res = context.getResources();
		backgroundBitmap = BitmapFactory.decodeResource(res,R.drawable.background);
		patronDrawable = context.getResources().getDrawable(R.drawable.patron);
		plainTile = context.getResources().getDrawable(R.drawable.tile);
		counterTile = context.getResources().getDrawable(R.drawable.countertile);
		exclamationBubble = context.getResources().getDrawable(R.drawable.exclamation);
		init();
	}
	
	public void init() {
		//testing draw method
		tiles[1][1] = 1;
		tiles[1][2] = 1;
		
		tiles[4][4] = 1;
		patronList.add(new Patron(3, 4, ShoppeConstants.potion, 100));
		patronList.add(new Patron(2, 3, ShoppeConstants.armor,100));
		patronList.getLast().exclamation = true;
	}
	
    /* Callback invoked when the surface dimensions change. */
    public void setSurfaceSize(int width, int height) {
        // synchronized to make sure these all change atomically
        synchronized (surfaceHolder) {
            canvasWidth = width;
            canvasHeight = height;

            // don't forget to resize the background image
            backgroundBitmap = backgroundBitmap.createScaledBitmap(
            		backgroundBitmap, width, height, true);
            tileHeight = (int) (height*gridHeightProportion/gridHeight);
            tileWidth = tileHeight;
            screenWidth = width;
            screenHeight = height;
            offsetX = (screenWidth-tileWidth*gridWidth)/2;
            offsetY = (screenHeight-tileHeight*gridHeight)/2;
        }
    }
    
	@Override
	public void run() {
		Canvas canvas = null;
		try {
			canvas = surfaceHolder.lockCanvas(null);
			synchronized (surfaceHolder) {
				draw(canvas);
			}
		}
		finally {
			if (canvas != null) {
				surfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
	}
	
	private void drawGrid(Canvas canvas) {
		int tileX, tileY;
		for (int i=0; i<gridHeight; i++) {
			for (int j=0; j<gridWidth; j++) {
				//calculates the tile position within the grid, then adds an offset to center in screen
				tileX = j*tileWidth+offsetX;
				tileY = i*tileHeight+offsetY;
				//Log.v("stuffs","i: " + i + " j: " + j + " tile: " + tiles[j][i] + " tileX: " + tileX + " tileY: " + tileY);
				switch (tiles[i][j]) {
				case ShoppeConstants.plainTile:
					plainTile.setBounds(tileX,tileY,tileX+tileWidth,tileY+tileHeight);
					plainTile.draw(canvas);
					break;
				case ShoppeConstants.counterTile:
					counterTile.setBounds(tileX,tileY,tileX+tileWidth,tileY+tileHeight);
					counterTile.draw(canvas);
					break;
				}
			}
		}
	}
	
	private void drawPatrons(Canvas canvas) {
		int xloc, yloc;
		Iterator<Patron> iterator = patronList.iterator();
		Patron p;
		//draw the patrons
		while (iterator.hasNext()) {
			p = iterator.next();
			xloc = p.xpos*tileWidth+offsetX;
			yloc = p.ypos*tileHeight+offsetY;
			patronDrawable.setBounds(xloc,yloc,xloc+tileWidth,yloc+tileHeight);
			patronDrawable.draw(canvas);
		}
		//draw the patron's exclamations
		iterator = patronList.iterator();
		while (iterator.hasNext()) {
			p = iterator.next();
			if (p.exclamation == true) {
				xloc = p.xpos*tileWidth+offsetX;
				yloc = p.ypos*tileHeight+offsetY;
				exclamationBubble.setBounds(xloc+tileWidth/2,yloc,xloc+tileWidth,yloc+tileHeight/2);
				exclamationBubble.draw(canvas);
			}
		}
	}
	
	private void draw(Canvas canvas) {
		canvas.drawBitmap(backgroundBitmap,0,0,null);
		
		drawGrid(canvas);
		drawPatrons(canvas);
	}
}
