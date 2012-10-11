package org.loderunner;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class LodeRunnerActivity extends Activity {


	private GameManager gameManager;
	private ViewManager viewManager;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);


		RelativeLayout relativeLayout = new RelativeLayout(this);
		relativeLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setContentView(relativeLayout);
		
		final LodeRunnerView lodeRunnerView = new LodeRunnerView(LodeRunnerActivity.this.getApplicationContext(),
				null);	
		gameManager = new GameManager();
		viewManager = new ViewManager(this, gameManager, relativeLayout, lodeRunnerView);
		// this needs to be post'ed because the actual size of the window
		// is not known until it is drawn
		relativeLayout.post(viewManager.init());


	}


	
	public void onMenu() {
		gameManager.pause();
		viewManager.showMenuWidgets();
	}
	
	public void onPlay() {	
		viewManager.showActionWidgets();
		gameManager.play();
	}



	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_W:
		case KeyEvent.KEYCODE_DPAD_UP:
			gameManager.up();			
			return true;
		case KeyEvent.KEYCODE_S:
		case KeyEvent.KEYCODE_DPAD_DOWN:
			gameManager.down();
			return true;
		case KeyEvent.KEYCODE_A:
		case KeyEvent.KEYCODE_DPAD_LEFT:
			gameManager.left();
			return true;
		case KeyEvent.KEYCODE_D:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			gameManager.right();
			return true;
		case KeyEvent.KEYCODE_Q:
			gameManager.digLeft();
			return true;
		case KeyEvent.KEYCODE_E:
			gameManager.digRight();
			return true;
		case KeyEvent.KEYCODE_DPAD_CENTER:
			gameManager.dig();
			return true;			
		case KeyEvent.KEYCODE_M:
			onMenu();			
			return true;
		case KeyEvent.KEYCODE_P:
			onPlay();			
			return true;			
			
		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}



}
