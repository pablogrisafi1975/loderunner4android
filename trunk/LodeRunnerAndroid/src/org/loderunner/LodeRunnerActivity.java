package org.loderunner;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class LodeRunnerActivity extends Activity {

	private static int LODE_RUNNER_VIEW_WIDTH = 336;
	private static int LODE_RUNNER_VIEW_HEIGHT = 176;
	private static int MARGIN = 2;
	private RelativeLayout relativeLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		this.relativeLayout = new RelativeLayout(this);
		relativeLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		setContentView(relativeLayout);
		// this needs to be post'ed because the actual size of the window
		// is not known until it is drawn
		relativeLayout.post(new Runnable() {
			public void run() {
				Rect outRect = new Rect();
				relativeLayout.getWindowVisibleDisplayFrame(outRect);
				int drawingWidth = outRect.width();
				int drawingHeight = calculateDrawingHeigth(outRect);

				Log.d(LodeRunnerActivity.class.getCanonicalName(), "width:" + drawingWidth + "height:" + drawingHeight);

				int squareButtonSize = (drawingWidth - LODE_RUNNER_VIEW_WIDTH - MARGIN * 6) / 4;
				int menuButtonWidth = 2 * squareButtonSize + MARGIN;
				int lastButtonLine = drawingHeight - squareButtonSize - MARGIN;

				LodeRunnerView lodeRunnerView = new LodeRunnerView(LodeRunnerActivity.this.getApplicationContext(),
						null);
				addView(lodeRunnerView, (drawingWidth - LODE_RUNNER_VIEW_WIDTH) / 2,
						(drawingHeight - LODE_RUNNER_VIEW_HEIGHT) / 2, LODE_RUNNER_VIEW_WIDTH, LODE_RUNNER_VIEW_HEIGHT);

				addButton("Menu", MARGIN, MARGIN, menuButtonWidth, squareButtonSize, new View.OnClickListener() {
					public void onClick(View v) {
						onMenu();
					}
				});

				// dig left
				addButton("\u2199", MARGIN, lastButtonLine, squareButtonSize, squareButtonSize,
						new View.OnClickListener() {
							public void onClick(View v) {
								LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerHero.MOVE_DIG_LEFT);
							}
						});

				// dig rigth
				addButton("\u2198", 2 * MARGIN + squareButtonSize, lastButtonLine, squareButtonSize, squareButtonSize,
						new View.OnClickListener() {
							public void onClick(View v) {
								LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerHero.MOVE_DIG_RIGHT);
							}
						});

				// up
				int upDownX = (drawingWidth + LODE_RUNNER_VIEW_WIDTH + MARGIN + squareButtonSize) / 2 + MARGIN;
				addButton("\u2191", upDownX, lastButtonLine - 2 * MARGIN - 2 * squareButtonSize, squareButtonSize,
						squareButtonSize, new View.OnClickListener() {
							public void onClick(View v) {
								LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_CLIMB_UP);
							}
						});
				// down
				addButton("\u2193", upDownX, lastButtonLine, squareButtonSize, squareButtonSize,
						new View.OnClickListener() {
							public void onClick(View v) {
								LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_CLIMB_DOWN);
							}
						});

				// left

				int leftRighY = lastButtonLine - MARGIN - squareButtonSize;

				addButton("\u2190", (drawingWidth + LODE_RUNNER_VIEW_WIDTH) / 2 + MARGIN, leftRighY, squareButtonSize,
						squareButtonSize, new View.OnClickListener() {
							public void onClick(View v) {
								LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_RUN_LEFT);
							}
						});
				// Right
				addButton("\u2192", (drawingWidth + LODE_RUNNER_VIEW_WIDTH) / 2 + 2 * MARGIN + squareButtonSize,
						leftRighY, squareButtonSize, squareButtonSize, new View.OnClickListener() {
							public void onClick(View v) {
								LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_RUN_RIGHT);
							}
						});

			}
		});

	}

	private void addView(View view, int x, int y, int width, int heigth) {
		LayoutParams layoutParams = new LayoutParams(width, heigth);
		layoutParams.leftMargin = x;
		layoutParams.topMargin = y;
		view.setLayoutParams(layoutParams);
		relativeLayout.addView(view);
	}

	private void addButton(String text, int x, int y, int width, int heigth, OnClickListener onClickListener) {
		Button menuButton = new Button(this);
		menuButton.setText(text);
		LayoutParams layoutParams = new LayoutParams(width, heigth);
		layoutParams.leftMargin = x;
		layoutParams.topMargin = y;
		menuButton.setLayoutParams(layoutParams);
		menuButton.setOnClickListener(onClickListener);
		relativeLayout.addView(menuButton);
	}

	private void onMenu() {
		Log.d("menu", "Menu");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
	
	private int calculateDrawingHeigth(Rect outRect) {
		int statusBarHeight = outRect.top;
		// Get the height occupied by the decoration contents
		int contentViewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
		// Calculate titleBarHeight by deducting statusBarHeight from
		// contentViewTop
		int titleBarHeight = contentViewTop - statusBarHeight;

		// By now we got the height of titleBar & statusBar
		// Now lets get the screen size
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int screenHeight = metrics.heightPixels;
		// int screenWidth = metrics.widthPixels;

		// Now calculate the height that our layout can be set
		// If you know that your application doesn't have statusBar added, then
		// don't add here also. Same applies to application bar also
		int layoutHeight = screenHeight - (titleBarHeight + statusBarHeight);
		return layoutHeight;
	}

}
