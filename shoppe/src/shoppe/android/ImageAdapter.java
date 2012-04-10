package shoppe.android;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter
{
	
	 private Context mContext;
	 private int[] items;
	 
	 public ImageAdapter(Context context)
	 {
		 mContext = context;
		 int[] pics = {R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron,
				 R.drawable.patron, R.drawable.patron, R.drawable.patron};
		 items = pics;
	 }

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return items.length;
	}

	@Override
	public Object getItem(int position)
	{
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent)
	{
		ImageView imageView;
		// if it's not recycled, initialize some attributes
        if (view == null)
        {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView)view;
        }

        imageView.setImageResource(items[position]);
        return imageView;

	}

}
