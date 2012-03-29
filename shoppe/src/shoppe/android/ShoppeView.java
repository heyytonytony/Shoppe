package shoppe.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class ShoppeView extends SurfaceView implements Callback
{

	private ShoppeThread thread;

	public ShoppeView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		// register interest in hearing about changes to surface
		SurfaceHolder surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setSizeFromLayout();

		// pre-create thread
		thread = new ShoppeThread(surfaceHolder, getContext(), getHandler());

		setFocusable(true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent mEvent)
	{
		// the finger, it poketh!
		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		// TODO Auto-generated method stub
		thread.setSurfaceSize(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		// start the thread here so that we don't busy-wait in run()
		try
		{
			thread.start();
		}
		catch(Exception ex)
		{
			thread = new ShoppeThread(getHolder(), getContext(), getHandler());
			thread.start();
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{

		// we have to tell thread to shut down & wait for it to finish, or else
		// it might touch the Surface after we return and explode

		boolean retry = true;
		while(retry)
		{
			try
			{
				thread.join();
				retry = false;
			}
			catch(InterruptedException ie)
			{
			}
		}

	}

}