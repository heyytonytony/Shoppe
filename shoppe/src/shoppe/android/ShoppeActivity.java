package shoppe.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class ShoppeActivity extends Activity implements OnTouchListener
{

	private float oldTouchValue;
	private ShoppeView shoppeView;
	private ShoppeThread shoppeThread;
	private ViewFlipper viewFlipper;
	private ImageView inv;

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
		shoppeThread = shoppeView.getThread();
		viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper);
		findViewById(R.id.hide_inv).setOnTouchListener(this);
		findViewById(R.id.show_inv).setOnTouchListener(this);
	}

	public void pauseButton(View view)
	{
		// TODO:Complete pauseButton
		// setContentView(R.layout.menuactivity);
		Button button = (Button)view;
		// toggle text displayed
		if(button.getText().toString().equals(getResources().getString(R.string.pause)))
		{
			showDialog(0);
			shoppeThread.setRunning(false);
		}
	}

	public void artisanButton(View view)
	{
		// TODO:Implement artisanButton
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
					viewFlipper.setInAnimation(AccordionAnimation.inFromTopAnimation());
					viewFlipper.setOutAnimation(AccordionAnimation.outToBottomAnimation());
					viewFlipper.showNext();
				}
				else if((diff > 100) && (inv.getId() == R.id.show_inv))
				{
					// Bottom --> Up
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
		Dialog dia = null;
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

				TextView text = (TextView) dia.findViewById(R.id.text);
				text.setText("Hello, this is a custom dialog!");


				break;
				
			case 2:
				// artisan 2
				dia = new Dialog(mContext);
				
				dia.setContentView(R.layout.artisan_dialog);
				dia.setTitle("Artisan 2 Management");
				TextView text1 = (TextView) dia.findViewById(R.id.text);
				text1.setText("Hello, this is a custom dialog!");
				break;
				
			case 3:
				// artisan 3
				dia = new Dialog(mContext);
				
				dia.setContentView(R.layout.artisan_dialog);
				dia.setTitle("Artisan 3 Management");
				TextView text2 = (TextView) dia.findViewById(R.id.text);
				text2.setText("Hello, this is a custom dialog!");
				break;
				
			case 4:
				// artisan 4
				dia = new Dialog(mContext);
				
				dia.setContentView(R.layout.artisan_dialog);
				dia.setTitle("Artisan 4 Management");
				TextView text3 = (TextView) dia.findViewById(R.id.text);
				text3.setText("Hello, this is a custom dialog!");
				break;
		}

		return dia;

	}
}