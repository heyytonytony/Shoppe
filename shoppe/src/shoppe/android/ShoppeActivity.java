package shoppe.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
	private Dialog dia = null;
	private ImageAdapter inventoryAdapter;
	private int[] artisanButtons;
	
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
				viewFlipper.showNext();
				viewFlipper.showPrevious();
				Log.d("hired artisan button", artisanButtons[msg.arg1] + "     " + R.id.artisan0Button);
				return;
				
			case ShoppeConstants.FIRE_ARTISAN:
				artisanButton = (Button)findViewById(artisanButtons[msg.arg1]);
				artisanButton.setVisibility(View.INVISIBLE);
				viewFlipper.showNext();
				viewFlipper.showPrevious();
				
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
	    

	}

	public void pauseButton(View view)
	{
		Button button = (Button)view;
		if(button.getText().toString().equals(getResources().getString(R.string.pause)))
		{
			//showing paused dialog
			showDialog(0);
			shoppeThread.setRunning(false);
		}
	}

	public void artisanButton(View view)
	{
		Button button = (Button)view;
		if(button.getId() == R.id.artisan0Button)
		{
			Log.d("Activity", "Artisan 1 pressed");
			showDialog(1);
			shoppeThread.setRunning(false);
		}
		if(button.getId() == R.id.artisan1Button)
		{
			Log.d("Activity", "Artisan 2 pressed");
			showDialog(2);
			shoppeThread.setRunning(false);
		}
		if(button.getId() == R.id.artisan2Button)
		{
			Log.d("Activity", "Artisan 3 pressed");
			showDialog(3);
			shoppeThread.setRunning(false);
		}
		if(button.getId() == R.id.artisan3Button)
		{
			Log.d("Activity", "Artisan 4 pressed");
			showDialog(4);
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
		Context mContext = shoppeView.getContext();
		Button artCreateItem, artCancelItem, artDone;
		switch(id)
		{
			case 0:
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

			case 1:
				// artisan 1
				dia = new Dialog(mContext);
				
				dia.setContentView(R.layout.artisan_dialog);
				dia.setTitle("Artisan 1 Management");
				
				artCreateItem = (Button)dia.findViewById(R.id.artCreateItem);
				artCreateItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//create item stuff
						dia.dismiss();
					}
				});
				
				artCancelItem = (Button)dia.findViewById(R.id.artCancelItem);
				artCancelItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//dismiss dialog and unpause game
						shoppeThread.setRunning(true);
						dia.dismiss();
					}
				});
				
				artDone = (Button)dia.findViewById(R.id.artDone);
				artDone.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//dismiss dialog and unpause game
						shoppeThread.setRunning(true);
						dia.dismiss();
					}
				});

				break;
				
			case 2:
				// artisan 2
				dia = new Dialog(mContext);
				
				dia.setContentView(R.layout.artisan_dialog);
				dia.setTitle("Artisan 2 Management");
				
				artCreateItem = (Button)dia.findViewById(R.id.artCreateItem);
				artCreateItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//create item stuff
						dia.dismiss();
					}
				});
				
				artCancelItem = (Button)dia.findViewById(R.id.artCancelItem);
				artCancelItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//dismiss dialog and unpause game
						shoppeThread.setRunning(true);
						dia.dismiss();
					}
				});
				
				artDone = (Button)dia.findViewById(R.id.artDone);
				artDone.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//dismiss dialog and unpause game
						shoppeThread.setRunning(true);
						dia.dismiss();
					}
				});

				break;
				
			case 3:
				// artisan 3
				dia = new Dialog(mContext);
				
				dia.setContentView(R.layout.artisan_dialog);
				dia.setTitle("Artisan 3 Management");
				
				artCreateItem = (Button)dia.findViewById(R.id.artCreateItem);
				artCreateItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//create item stuff
						dia.dismiss();
					}
				});
				
				artCancelItem = (Button)dia.findViewById(R.id.artCancelItem);
				artCancelItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//dismiss dialog and unpause game
						shoppeThread.setRunning(true);
						dia.dismiss();
					}
				});
				
				artDone = (Button)dia.findViewById(R.id.artDone);
				artDone.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//dismiss dialog and unpause game
						shoppeThread.setRunning(true);
						dia.dismiss();
					}
				});

				break;
				
			case 4:
				// artisan 4
				dia = new Dialog(mContext);
				
				dia.setContentView(R.layout.artisan_dialog);
				dia.setTitle("Artisan 4 Management");
				
				artCreateItem = (Button)dia.findViewById(R.id.artCreateItem);
				artCreateItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//create item stuff
						dia.dismiss();
					}
				});
				
				artCancelItem = (Button)dia.findViewById(R.id.artCancelItem);
				artCancelItem.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//dismiss dialog and unpause game
						shoppeThread.setRunning(true);
						dia.dismiss();
					}
				});
				
				artDone = (Button)dia.findViewById(R.id.artDone);
				artDone.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						//dismiss dialog and unpause game
						shoppeThread.setRunning(true);
						dia.dismiss();
					}
				});

				break;
		}

		return dia;

	}
}