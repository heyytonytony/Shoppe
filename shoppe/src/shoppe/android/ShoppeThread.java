package shoppe.android;

import android.content.Context;
import android.os.Handler;
import android.view.SurfaceHolder;

public class ShoppeThread extends Thread
{

	private SurfaceHolder surfaceHolder = null;
	public ShoppeThread(SurfaceHolder surfaceHolder, Context context, Handler handler)
	{
		this.surfaceHolder = surfaceHolder;
	}
	
}
