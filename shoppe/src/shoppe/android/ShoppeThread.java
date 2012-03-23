package shoppe.android;

import java.util.Iterator;
import java.util.LinkedList;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
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
    /** Drawable reperesnting a countertop tile **/
    private Drawable counterTile;
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
		init();
	}
	
	public void init() {
		//testing draw method
		tiles[1][1] = 1;
		tiles[1][2] = 1;
		
		tiles[4][4] = 1;
		patronList.add(new Patron());
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
            tileHeight = height/gridHeight;
            tileWidth = tileHeight;
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
	
	private void draw(Canvas canvas) {
		canvas.drawBitmap(backgroundBitmap,0,0,null);
		int tileX, tileY;
		
		for (int i=0; i<gridHeight; i++) {
			for (int j=0; j<gridHeight; j++) {
				tileX = j*tileWidth;
				tileY = i*tileHeight;
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
		
		Iterator<Patron> iterator = patronList.iterator();
		Patron p;
		while (iterator.hasNext()) {
			p = iterator.next();
			patronDrawable.setBounds(200,200,300,300);
			patronDrawable.draw(canvas);
		}
		
	}
}
