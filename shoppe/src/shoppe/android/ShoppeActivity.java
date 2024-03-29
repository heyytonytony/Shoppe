package shoppe.android;

import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ShoppeActivity extends Activity implements OnTouchListener
{

	private float oldTouchValue;
	private ShoppeView shoppeView;
	private ShoppeThread shoppeThread;
	private ViewFlipper viewFlipper;
	private ImageView inv;
	private Dialog dia = null, diaNest = null;
	private ImageAdapter inventoryAdapter;
	private boolean first = true;
	
	private int[] artisanButtons;
	
	/** Managing add items to production queue */
	private LinkedList<Item> itemList;
	private CharSequence[] itemCS;
	
	/** Managing removing items from production queue */
	private LinkedList<Item> artisanProductionQueue;
	private CharSequence[] artisanPQCS;
	
	/** item patron wants to buy/sell */
	private Item patronItem;
	
	/** handler */
	final Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Button artisanButton;
			switch(msg.what)
			{
			case ShoppeConstants.HIRE_ARTISAN:
				artisanButton = (Button)findViewById(artisanButtons[msg.arg1]);
				artisanButton.setVisibility(View.VISIBLE);
				if(first)
				{
					viewFlipper.showNext();
					viewFlipper.showPrevious();
				}
//				Log.d("hired artisan button", artisanButtons[msg.arg1] + "");
				return;
				
			case ShoppeConstants.FIRE_ARTISAN:
				artisanButton = (Button)findViewById(artisanButtons[msg.arg1]);
				artisanButton.setVisibility(View.INVISIBLE);
				if(first)
				{
					viewFlipper.showNext();
					viewFlipper.showPrevious();
				}
//				Log.d("fired artisan button", artisanButtons[msg.arg1] + "");
				return;
				
			case ShoppeConstants.BUY_PATRON:
				patronItem = (Item)msg.obj;
				showDialog(ShoppeConstants.DIALOG_BUY_PATRON);
				shoppeThread.setRunning(false);
				return;
				
			case ShoppeConstants.SELL_PATRON:
				patronItem = (Item)msg.obj;
				showDialog(ShoppeConstants.DIALOG_SELL_PATRON);
				shoppeThread.setRunning(false);
				return;
				
			case ShoppeConstants.ITEM_PRODUCED:
				Toast.makeText(shoppeView.getContext(), ((Item)(msg.obj)).getItemName() + " completed by Artisan " + msg.arg1, Toast.LENGTH_SHORT).show();
				return;
				
			default:
				return;
			}
				
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shoppeactivity);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		shoppeView = (ShoppeView)findViewById(R.id.shoppeView);
		shoppeView.setHandler(handler);
		shoppeThread = shoppeView.getThread();
		
		viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper);
		findViewById(R.id.hide_inv).setOnTouchListener(this);
		findViewById(R.id.show_inv).setOnTouchListener(this);
		
		GridView invView = (GridView)findViewById(R.id.invView);
		inventoryAdapter = shoppeThread.getInventoryAdapter();
	    invView.setAdapter(inventoryAdapter);
	    invView.setOnItemClickListener(new OnItemClickListener()
	    {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	        {
	            Toast.makeText(shoppeView.getContext(), "" + position, Toast.LENGTH_SHORT).show();
	        }
	    });

	    int[] temp = {R.id.artisan0Button, R.id.artisan1Button, R.id.artisan2Button, R.id.artisan3Button};
	    artisanButtons = temp;
	    
	    itemList = shoppeThread.getItemList();
	    itemCS = shoppeThread.getItemCS();
	    
	}

	public void pauseButton(View view)
	{
		Button button = (Button)view;
		if(button.getText().toString().equals(getResources().getString(R.string.pause)))
		{
			//showing paused dialog
			showDialog(ShoppeConstants.DIALOG_PAUSE);
			Log.d("dialog info", dia.toString());
			shoppeThread.setRunning(false);
		}
	}

	public void artisanButton(View view)
	{
		Button button = (Button)view;
		if(button.getId() == R.id.artisan0Button)
		{
			Log.d("Activity", "Artisan 1 pressed");
			showDialog(ShoppeConstants.DIALOG_ARTISAN_1);
			Log.d("dialog info", dia.toString());
			shoppeThread.setRunning(false);
		}
		if(button.getId() == R.id.artisan1Button)
		{
			Log.d("Activity", "Artisan 2 pressed");
			showDialog(ShoppeConstants.DIALOG_ARTISAN_2);
			Log.d("dialog info", dia.toString());
			shoppeThread.setRunning(false);
		}
		if(button.getId() == R.id.artisan2Button)
		{
			Log.d("Activity", "Artisan 3 pressed");
			showDialog(ShoppeConstants.DIALOG_ARTISAN_3);
			Log.d("dialog info", dia.toString());
			shoppeThread.setRunning(false);
		}
		if(button.getId() == R.id.artisan3Button)
		{
			Log.d("Activity", "Artisan 4 pressed");
			showDialog(ShoppeConstants.DIALOG_ARTISAN_4);
			Log.d("dialog info", dia.toString());
			shoppeThread.setRunning(false);
		}
	}
	
	private boolean onButtonTouchEvent(MotionEvent mEvent)
	{
		if(inv == null)
			return false;
		switch(mEvent.getAction())
		{
			case MotionEvent.ACTION_DOWN:
			{
				oldTouchValue = mEvent.getY();
				break;
			}
			case MotionEvent.ACTION_UP:
			{
				float currentY = mEvent.getY();
				final float diff = oldTouchValue - currentY;
				if((diff < -100) && (inv.getId() == R.id.hide_inv))
				{
					// Up --> Bottom
					shoppeThread.setRunning(true);
					viewFlipper.setInAnimation(AccordionAnimation.inFromTopAnimation());
					viewFlipper.setOutAnimation(AccordionAnimation.outToBottomAnimation());
					viewFlipper.showNext();
				}
				else if((diff > 100) && (inv.getId() == R.id.show_inv))
				{
					// Bottom --> Up
					shoppeThread.setRunning(false);
					viewFlipper.setInAnimation(AccordionAnimation.inFromBottomAnimation());
					viewFlipper.setOutAnimation(AccordionAnimation.outToTopAnimation());
					viewFlipper.showPrevious();
					if(first)
						first = !first;
				}
				break;
			}
		}
		inv = null;
		return true;
	}

	@Override
	public boolean onTouch(View view, MotionEvent mEvent)
	{
		inv = (ImageView)view;
		final boolean result = this.onButtonTouchEvent(mEvent);
		return result;
	}

	protected Dialog onCreateDialog(int id)
	{
		final Context mContext = shoppeView.getContext();
		Button artCreateItem, artCancelItem, artFire, artDone;
		
		Button patAgree, patDecline;
		ImageView patItemImage;
		TextView patItemText;
		
		switch(id)
		{
			case ShoppeConstants.DIALOG_PAUSE:
				// game paused
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Game paused!").setCancelable(false).setNeutralButton("Unpause!", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int id)
					{
						shoppeThread.setRunning(true);
						dialog.dismiss();
					}
				});
				AlertDialog alert = builder.create();
				dia = alert;
				break;

			case ShoppeConstants.DIALOG_ARTISAN_1:
				// artisan 1
				dia = new Dialog(mContext);
				
				dia.setContentView(R.layout.artisan_dialog);
				dia.setTitle("Artisan 1 Management");
				
				//populate ImageViews with items in production queue, if any
				
				artCreateItem = (Button)dia.findViewById(R.id.artisanCreateItem);
				artCreateItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//create item stuff
						diaNest = null;
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setTitle("Artisan 1 Create Item");
						builder.setItems(itemCS, new DialogInterface.OnClickListener()
						{
						    public void onClick(DialogInterface dialog, int index)
						    {
						        Toast.makeText(mContext, itemList.get(index).getItemName() + " queued for production", Toast.LENGTH_SHORT).show();
						        shoppeThread.addProduction(0, itemList.get(index));
								artisanPQCS = shoppeThread.getArtisantPQCS(0);
								artisanProductionQueue = shoppeThread.getArtisanProductionQueue(0);
						        diaNest.dismiss();

						    }
						});
						AlertDialog alert = builder.create();
						diaNest = alert;
						diaNest.show();
					}
				});
				
				artCancelItem = (Button)dia.findViewById(R.id.artisanCancelItem);
				artCancelItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//cancel item stuff
						diaNest = null;
						artisanPQCS = shoppeThread.getArtisantPQCS(0);
						artisanProductionQueue = shoppeThread.getArtisanProductionQueue(0);
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setTitle("Artisan 1 Cancel Item");
						builder.setItems(artisanPQCS, new DialogInterface.OnClickListener()
						{
						    public void onClick(DialogInterface dialog, int index)
						    {
						        Toast.makeText(mContext, artisanProductionQueue.get(index).getItemName() + " removed from production", Toast.LENGTH_SHORT).show();
						        shoppeThread.removeProduction(0, artisanProductionQueue.get(index));
								artisanPQCS = shoppeThread.getArtisantPQCS(0);
								artisanProductionQueue = shoppeThread.getArtisanProductionQueue(0);
						        diaNest.dismiss();

						    }
						});
						AlertDialog alert = builder.create();
						diaNest = alert;
						diaNest.show();
					}
				});
				
				artFire = (Button)dia.findViewById(R.id.artisanFire);
				artFire.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//fire the bloke
						diaNest = null;
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setMessage("Are you sure you want to fire Artisan 1?");
						builder.setCancelable(false);
						builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
						{
						 	public void onClick(DialogInterface dialog, int id)
						 	{
						 		shoppeThread.fireArtisan(0);
						 		dialog.dismiss();
							}
						});
					    builder.setNegativeButton("No", new DialogInterface.OnClickListener()
					    {
					    	public void onClick(DialogInterface dialog, int id)
					    	{
					    		dialog.cancel();
					    	}
					    });
					    AlertDialog alert = builder.create();
						diaNest = alert;
						diaNest.show();
						
					}
				});
				
				artDone = (Button)dia.findViewById(R.id.artisanDone);
				artDone.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//dismiss dialog and unpause game
						Log.d("artisan1 done", "ARTISAN 1 DONE");
						dia.dismiss();
						shoppeThread.setRunning(true);
					}
				});

				break;
				
			case ShoppeConstants.DIALOG_ARTISAN_2:
				// artisan 2
				dia = new Dialog(mContext);
				
				dia.setContentView(R.layout.artisan_dialog);
				dia.setTitle("Artisan 2 Management");
				
				artCreateItem = (Button)dia.findViewById(R.id.artisanCreateItem);
				artCreateItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//create item stuff
						diaNest = null;
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setTitle("Artisan 2 Create Item");
						builder.setItems(itemCS, new DialogInterface.OnClickListener()
						{
						    public void onClick(DialogInterface dialog, int index)
						    {
						        Toast.makeText(mContext, itemList.get(index).getItemName() + " queued for production", Toast.LENGTH_SHORT).show();
						        shoppeThread.addProduction(1, itemList.get(index));
								artisanPQCS = shoppeThread.getArtisantPQCS(1);
								artisanProductionQueue = shoppeThread.getArtisanProductionQueue(1);
						        diaNest.dismiss();

						    }
						});
						AlertDialog alert = builder.create();
						diaNest = alert;
						diaNest.show();
					}
				});
				
				artCancelItem = (Button)dia.findViewById(R.id.artisanCancelItem);
				artCancelItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//cancel item stuff
						diaNest = null;
						artisanPQCS = shoppeThread.getArtisantPQCS(1);
						artisanProductionQueue = shoppeThread.getArtisanProductionQueue(1);
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setTitle("Artisan 2 Cancel Item");
						builder.setItems(artisanPQCS, new DialogInterface.OnClickListener()
						{
						    public void onClick(DialogInterface dialog, int index)
						    {
						        Toast.makeText(mContext, artisanProductionQueue.get(index).getItemName() + " removed from production", Toast.LENGTH_SHORT).show();
						        shoppeThread.removeProduction(1, artisanProductionQueue.get(index));
								artisanPQCS = shoppeThread.getArtisantPQCS(1);
								artisanProductionQueue = shoppeThread.getArtisanProductionQueue(1);
						        diaNest.dismiss();

						    }
						});
						AlertDialog alert = builder.create();
						diaNest = alert;
						diaNest.show();
					}
				});
				
				artFire = (Button)dia.findViewById(R.id.artisanFire);
				artFire.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//fire the bloke
						diaNest = null;
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setMessage("Are you sure you want to fire Artisan 2?");
						builder.setCancelable(false);
						builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
						{
						 	public void onClick(DialogInterface dialog, int id)
						 	{
						 		shoppeThread.fireArtisan(1);
						 		dialog.dismiss();
							}
						});
					    builder.setNegativeButton("No", new DialogInterface.OnClickListener()
					    {
					    	public void onClick(DialogInterface dialog, int id)
					    	{
					    		dialog.cancel();
					    	}
					    });
					    AlertDialog alert = builder.create();
						diaNest = alert;
						diaNest.show();
						
					}
				});
				
				artDone = (Button)dia.findViewById(R.id.artisanDone);
				artDone.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//dismiss dialog and unpause game
						Log.d("artisan2 done", "ARTISAN 2 DONE");
						dia.dismiss();
						shoppeThread.setRunning(true);
					}
				});

				break;
				
			case ShoppeConstants.DIALOG_ARTISAN_3:
				// artisan 3
				dia = new Dialog(mContext);
				
				dia.setContentView(R.layout.artisan_dialog);
				dia.setTitle("Artisan 3 Management");
				
				artCreateItem = (Button)dia.findViewById(R.id.artisanCreateItem);
				artCreateItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//create item stuff
						diaNest = null;
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setTitle("Artisan 3 Create Item");
						builder.setItems(itemCS, new DialogInterface.OnClickListener()
						{
						    public void onClick(DialogInterface dialog, int index)
						    {
						        Toast.makeText(mContext, itemList.get(index).getItemName() + " queued for production", Toast.LENGTH_SHORT).show();
						        shoppeThread.addProduction(2, itemList.get(index));
								artisanPQCS = shoppeThread.getArtisantPQCS(2);
								artisanProductionQueue = shoppeThread.getArtisanProductionQueue(2);
						        diaNest.dismiss();

						    }
						});
						AlertDialog alert = builder.create();
						diaNest = alert;
						diaNest.show();
					}
				});
				
				artCancelItem = (Button)dia.findViewById(R.id.artisanCancelItem);
				artCancelItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//cancel item stuff
						diaNest = null;
						artisanPQCS = shoppeThread.getArtisantPQCS(2);
						artisanProductionQueue = shoppeThread.getArtisanProductionQueue(2);
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setTitle("Artisan 3 Cancel Item");
						builder.setItems(artisanPQCS, new DialogInterface.OnClickListener()
						{
						    public void onClick(DialogInterface dialog, int index)
						    {
						        Toast.makeText(mContext, artisanProductionQueue.get(index).getItemName() + " removed from production", Toast.LENGTH_SHORT).show();
						        shoppeThread.removeProduction(2, artisanProductionQueue.get(index));
								artisanPQCS = shoppeThread.getArtisantPQCS(2);
								artisanProductionQueue = shoppeThread.getArtisanProductionQueue(2);
						        diaNest.dismiss();

						    }
						});
						AlertDialog alert = builder.create();
						diaNest = alert;
						diaNest.show();
					}
				});
				
				artFire = (Button)dia.findViewById(R.id.artisanFire);
				artFire.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//fire the bloke
						diaNest = null;
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setMessage("Are you sure you want to fire Artisan 3?");
						builder.setCancelable(false);
						builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
						{
						 	public void onClick(DialogInterface dialog, int id)
						 	{
						 		shoppeThread.fireArtisan(2);
						 		dialog.dismiss();
							}
						});
					    builder.setNegativeButton("No", new DialogInterface.OnClickListener()
					    {
					    	public void onClick(DialogInterface dialog, int id)
					    	{
					    		dialog.cancel();
					    	}
					    });
					    AlertDialog alert = builder.create();
						diaNest = alert;
						diaNest.show();
						
					}
				});
				
				artDone = (Button)dia.findViewById(R.id.artisanDone);
				artDone.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//dismiss dialog and unpause game
						Log.d("artisan3 done", "ARTISAN 3 DONE");
						dia.dismiss();
						shoppeThread.setRunning(true);
					}
				});

				break;
				
			case ShoppeConstants.DIALOG_ARTISAN_4:
				// artisan 4
				dia = new Dialog(mContext);
				
				dia.setContentView(R.layout.artisan_dialog);
				dia.setTitle("Artisan 4 Management");
				
				artCreateItem = (Button)dia.findViewById(R.id.artisanCreateItem);
				artCreateItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//create item stuff
						diaNest = null;
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setTitle("Artisan 4 Create Item");
						builder.setItems(itemCS, new DialogInterface.OnClickListener()
						{
						    public void onClick(DialogInterface dialog, int index)
						    {
						        Toast.makeText(mContext, itemList.get(index).getItemName() + " queued for production", Toast.LENGTH_SHORT).show();
						        shoppeThread.addProduction(3, itemList.get(index));
								artisanPQCS = shoppeThread.getArtisantPQCS(3);
								artisanProductionQueue = shoppeThread.getArtisanProductionQueue(3);
						        diaNest.dismiss();

						    }
						});
						AlertDialog alert = builder.create();
						diaNest = alert;
						diaNest.show();
					}
				});
				
				artCancelItem = (Button)dia.findViewById(R.id.artisanCancelItem);
				artCancelItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//cancel item stuff
						diaNest = null;
						artisanPQCS = shoppeThread.getArtisantPQCS(3);
						artisanProductionQueue = shoppeThread.getArtisanProductionQueue(3);
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setTitle("Artisan 4 Cancel Item");
						builder.setItems(artisanPQCS, new DialogInterface.OnClickListener()
						{
						    public void onClick(DialogInterface dialog, int index)
						    {
						        Toast.makeText(mContext, artisanProductionQueue.get(index).getItemName() + " removed from production", Toast.LENGTH_SHORT).show();
						        shoppeThread.removeProduction(3, artisanProductionQueue.get(index));
								artisanPQCS = shoppeThread.getArtisantPQCS(3);
								artisanProductionQueue = shoppeThread.getArtisanProductionQueue(3);
						        diaNest.dismiss();

						    }
						});
						AlertDialog alert = builder.create();
						diaNest = alert;
						diaNest.show();
					}
				});
				
				artFire = (Button)dia.findViewById(R.id.artisanFire);
				artFire.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//fire the bloke
						diaNest = null;
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setMessage("Are you sure you want to fire Artisan 4?");
						builder.setCancelable(false);
						builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
						{
						 	public void onClick(DialogInterface dialog, int id)
						 	{
						 		shoppeThread.fireArtisan(3);
						 		dialog.dismiss();
							}
						});
					    builder.setNegativeButton("No", new DialogInterface.OnClickListener()
					    {
					    	public void onClick(DialogInterface dialog, int id)
					    	{
					    		dialog.cancel();
					    	}
					    });
					    AlertDialog alert = builder.create();
						diaNest = alert;
						diaNest.show();
						
					}
				});
				
				artDone = (Button)dia.findViewById(R.id.artisanDone);
				artDone.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//dismiss dialog and unpause game
						Log.d("artisan4 done", "ARTISAN 4 DONE");
						dia.dismiss();
						shoppeThread.setRunning(true);
					}
				});

				break;
				
			case ShoppeConstants.DIALOG_BUY_PATRON:
				// patron who wants to buy something
				dia = new Dialog(mContext);
				
				dia.setContentView(R.layout.patron_dialog);
				dia.setTitle("Mr. Patron wants to buy something!");
				
				patItemImage = (ImageView)dia.findViewById(R.id.patronItemImage);
				patItemImage.setImageResource(patronItem.getDrawableID());
				
				patItemText = (TextView)dia.findViewById(R.id.patronItemText);
				patItemText.setText(ShoppeConstants.getBuyText(patronItem.getItemName()));
				
				patAgree = (Button)dia.findViewById(R.id.patAgree);
				patAgree.setText("Make sale!");
				patAgree.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						// TODO remove item from inventory, add funds
						// perform sale
						shoppeThread.sellItem(patronItem);
						dia.dismiss();
						shoppeThread.setRunning(true);
						
					}
				});
				
				patDecline = (Button)dia.findViewById(R.id.patDecline);
				patDecline.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						// decline sale
						dia.dismiss();
						shoppeThread.setRunning(true);
						
					}
				});
				
				break;
				
			case ShoppeConstants.DIALOG_SELL_PATRON:
				// patron who wants to sell something
				dia = new Dialog(mContext);
				
				dia.setContentView(R.layout.patron_dialog);
				dia.setTitle("Mr. Patron wants to sell something!");
				
				patAgree = (Button)dia.findViewById(R.id.patAgree);
				patAgree.setText("Buy offer!");
				patAgree.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						// buy item from patron
						shoppeThread.buyItem(patronItem);
						dia.dismiss();
						shoppeThread.setRunning(true);
					}
				});
				
				patDecline = (Button)dia.findViewById(R.id.patDecline);
				patDecline.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						// TODO Auto-generated method stub
						// decline to buy item from patron
						dia.dismiss();
						shoppeThread.setRunning(true);
					}
				});
				break;
		}
		
		return dia;

	}
	
	protected void onPrepareDialog(int id, Dialog dialog)
	{
		if(id > ShoppeConstants.DIALOG_PAUSE && id < ShoppeConstants.DIALOG_BUY_PATRON && artisanProductionQueue != null)
		{
			artisanProductionQueue = shoppeThread.getArtisanProductionQueue(id - 1);
			artisanPQCS = shoppeThread.getArtisantPQCS(id - 1);
			int size = Math.min(4, artisanProductionQueue.size());
			ImageView[] images = new ImageView[4];
			TextView[] text = new TextView[4];
			TableRow prodQItemsImages = (TableRow)dialog.findViewById(R.id.artisanProductionRow);
			TableRow prodQItemsTexts = (TableRow)dialog.findViewById(R.id.artisanProductionRowText);
			for(int index = 0; index < size; index++)
			{
				images[index] = (ImageView)prodQItemsImages.getChildAt(index);
				images[index].setImageResource(artisanProductionQueue.get(index).getDrawableID());
				text[index] = (TextView)prodQItemsTexts.getChildAt(index);
				text[index].setText(artisanPQCS[index]);
			}
			for(int index = size; index < 4; index++)
			{
				images[index] = (ImageView)prodQItemsImages.getChildAt(index);
				images[index].setImageResource(0);
				text[index] = (TextView)prodQItemsTexts.getChildAt(index);
				text[index].setText("");
			}
		}
		
		dia = dialog;
	}
	
	/**
     * Invoked when the Activity loses user focus.
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        finish();
        System.gc();
    }
    
    @Override
    protected void onStop()
    {
        super.onStop();
        System.gc();
    }
}