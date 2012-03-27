package shoppe.android;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ShoppeActivity extends Activity {

	private ShoppeView shoppeView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shoppeactivity);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}
	
	public void pauseButton(View view) {
		//TODO:Complete pauseButton
		//setContentView(R.layout.menuactivity);
		Button button = (Button) view;
		//toggle text displayed
		if (button.getText().toString().equals(getResources().getString(R.string.pause))) {
			button.setText(getResources().getString(R.string.play));
		}
		else {
			button.setText(getResources().getString(R.string.pause));
		}
	}
	public void artisanButton(View view) {
		//TODO:Implement artisanButton
	}
}