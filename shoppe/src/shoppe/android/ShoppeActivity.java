package shoppe.android;

import android.app.Activity;
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
			button.setText(getResources().getString(R.string.play));
			shoppeThread.setRunning(false);
		}
		else
		{
			button.setText(getResources().getString(R.string.pause));
			shoppeThread.setRunning(true);
		}
	}

	public void artisanButton(View view)
	{
		// TODO:Implement artisanButton
	}

	private boolean onButtonTouchEvent(MotionEvent mEvent)
    {
        if (inv == null)
            return false;
        switch (mEvent.getAction())
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
                if ((diff < -100) && (inv.getId() == R.id.hide_inv))
                {
                    //Up --> Bottom
                    viewFlipper.setInAnimation(AccordionAnimation.inFromTopAnimation());
                    viewFlipper.setOutAnimation(AccordionAnimation.outToBottomAnimation());
                    viewFlipper.showNext();
                }
                else if ((diff > 100) && (inv.getId() == R.id.show_inv))
                {
                    //Bottom --> Up
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
}