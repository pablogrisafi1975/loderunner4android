package org.loderunner;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class LodeRunnerActivity extends Activity {

	private static final String TEXT_RIGHT = "\u2192";
	private static final String TEXT_LEFT = "\u2190";
	private static final String TEXT_DOWN = "\u2193";
	private static final String TEXT_UP = "\u2191";
	private static final String TEXT_DIG_RIGTH = "\u2198";
	private static final String TEXT_DIG_LEFT = "\u2199";
	private static final String TEXT_MENU = "Menu";
	private static final String TEXT_PLAY = "Play";
	private static int LODE_RUNNER_VIEW_WIDTH = 336;
	private static int LODE_RUNNER_VIEW_HEIGHT = 176;
	private static int MARGIN = 2;
	private RelativeLayout relativeLayout;
	private List<Button> actionButtons;
	private List<Button> menuButtons;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionButtons = new ArrayList<Button>();
		menuButtons = new ArrayList<Button>();
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

				int squareButtonSize = (drawingHeight - LODE_RUNNER_VIEW_HEIGHT - MARGIN * 3) / 2;
				Log.d(LodeRunnerActivity.class.getCanonicalName(), "squareButtonSize:" + squareButtonSize);
				int menuButtonSize = (drawingWidth - LODE_RUNNER_VIEW_WIDTH) / 2 - 2 * MARGIN;
				int lastButtonLine = drawingHeight - squareButtonSize - MARGIN;

				LodeRunnerView lodeRunnerView = new LodeRunnerView(LodeRunnerActivity.this.getApplicationContext(),
						null);
				addView(lodeRunnerView, (drawingWidth - LODE_RUNNER_VIEW_WIDTH) / 2, 0, LODE_RUNNER_VIEW_WIDTH,
						LODE_RUNNER_VIEW_HEIGHT);

				createActionButtons(drawingWidth, squareButtonSize, menuButtonSize, lastButtonLine);
				
				createMenuButtons(drawingWidth, squareButtonSize, menuButtonSize, lastButtonLine);

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

	private void addButton(String text, int x, int y, int size, boolean isActionButton, View.OnClickListener onClickListener) {
		Button button = new Button(this);
		button.setText(text);
		button.setFocusable(false);
		if (isActionButton) {
			actionButtons.add(button);
		} else {
			button.setVisibility(View.INVISIBLE);
			menuButtons.add(button);
		}
		LayoutParams layoutParams = new LayoutParams(size, size);
		layoutParams.leftMargin = x;
		layoutParams.topMargin = y;
		button.setLayoutParams(layoutParams);
		button.setOnClickListener(onClickListener);
		relativeLayout.addView(button);
	}
	
	private void onMenu() {
		if(!LodeRunnerDrawingThread.getInstance().isPaused){
			LodeRunnerDrawingThread.getInstance().pause();
			showSomeButtons(true);
		}
		
	}
	
	private void onPlay() {	
		if(LodeRunnerDrawingThread.getInstance().isPaused){
			showSomeButtons(false);
			LodeRunnerDrawingThread.getInstance().play();
		}
		
	}

	private void showSomeButtons(boolean showMenuButtons) {
		int actionButtonsVisibility = !showMenuButtons ? View.VISIBLE :View.INVISIBLE;
		int menuButtonsVisibility = showMenuButtons ? View.VISIBLE :View.INVISIBLE;
		for (Button button : actionButtons) {
			button.setVisibility(actionButtonsVisibility);
		}
		for (Button button : menuButtons) {
			button.setVisibility(menuButtonsVisibility);
		}		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_W:
		case KeyEvent.KEYCODE_DPAD_UP:
			LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_CLIMB_UP);
			return true;
		case KeyEvent.KEYCODE_S:
		case KeyEvent.KEYCODE_DPAD_DOWN:
			LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_CLIMB_DOWN);
			return true;
		case KeyEvent.KEYCODE_A:
		case KeyEvent.KEYCODE_DPAD_LEFT:
			LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_RUN_LEFT);
			return true;
		case KeyEvent.KEYCODE_D:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_RUN_RIGHT);
			return true;
		case KeyEvent.KEYCODE_Q:
			LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerHero.MOVE_DIG_LEFT);
			return true;
		case KeyEvent.KEYCODE_E:
			LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerHero.MOVE_DIG_RIGHT);
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

	private void createActionButtons(int drawingWidth, int squareButtonSize, int menuButtonSize, int lastButtonLine) {
		//menu
		addButton(TEXT_MENU, MARGIN, MARGIN, menuButtonSize, true, new View.OnClickListener() {
			public void onClick(View v) {
				onMenu();
			}		
		});

		// dig left
		addButton(TEXT_DIG_LEFT, MARGIN, lastButtonLine, squareButtonSize, true,
				new View.OnClickListener() {
					public void onClick(View v) {
						LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerHero.MOVE_DIG_LEFT);
					}
				});

		// dig right
		addButton(TEXT_DIG_RIGTH, 2 * MARGIN + squareButtonSize, lastButtonLine, squareButtonSize, true,
				new View.OnClickListener() {
					public void onClick(View v) {
						LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerHero.MOVE_DIG_RIGHT);
					}
				});

		// up
		int upDownX = (drawingWidth + LODE_RUNNER_VIEW_WIDTH) / 2 + MARGIN;
		addButton(TEXT_UP, upDownX, lastButtonLine - 2 * MARGIN - 2 * squareButtonSize, squareButtonSize,
				true, new View.OnClickListener() {
					public void onClick(View v) {
						LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_CLIMB_UP);
					}
				});
		// down
		addButton(TEXT_DOWN, upDownX, lastButtonLine, squareButtonSize, true, new View.OnClickListener() {
			public void onClick(View v) {
				LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_CLIMB_DOWN);
			}
		});

		// left

		int leftRighY = lastButtonLine - MARGIN - squareButtonSize;

		addButton(TEXT_LEFT, drawingWidth - 2 * (MARGIN + squareButtonSize), leftRighY, squareButtonSize,
				true, new View.OnClickListener() {
					public void onClick(View v) {
						LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_RUN_LEFT);
					}
				});
		// Right
		addButton(TEXT_RIGHT, drawingWidth - (MARGIN + squareButtonSize), leftRighY, squareButtonSize,
				true, new View.OnClickListener() {
					public void onClick(View v) {
						LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_RUN_RIGHT);
					}
				});
	}
	
	private void createMenuButtons(int drawingWidth, int squareButtonSize, int menuButtonSize, int lastButtonLine) {
		//menu
		addButton(TEXT_PLAY, MARGIN, MARGIN, menuButtonSize, false, new View.OnClickListener() {
			public void onClick(View v) {
				onPlay();
			}		
		});
/*
		// dig left
		addButton(TEXT_DIG_LEFT, MARGIN, lastButtonLine, squareButtonSize, true,
				new View.OnClickListener() {
					public void onClick(View v) {
						LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerHero.MOVE_DIG_LEFT);
					}
				});

		// dig right
		addButton(TEXT_DIG_RIGTH, 2 * MARGIN + squareButtonSize, lastButtonLine, squareButtonSize, true,
				new View.OnClickListener() {
					public void onClick(View v) {
						LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerHero.MOVE_DIG_RIGHT);
					}
				});

		// up
		int upDownX = (drawingWidth + LODE_RUNNER_VIEW_WIDTH) / 2 + MARGIN;
		addButton(TEXT_UP, upDownX, lastButtonLine - 2 * MARGIN - 2 * squareButtonSize, squareButtonSize,
				true, new View.OnClickListener() {
					public void onClick(View v) {
						LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_CLIMB_UP);
					}
				});
		// down
		addButton(TEXT_DOWN, upDownX, lastButtonLine, squareButtonSize, true, new View.OnClickListener() {
			public void onClick(View v) {
				LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_CLIMB_DOWN);
			}
		});

		// left

		int leftRighY = lastButtonLine - MARGIN - squareButtonSize;

		addButton(TEXT_LEFT, drawingWidth - 2 * (MARGIN + squareButtonSize), leftRighY, squareButtonSize,
				true, new View.OnClickListener() {
					public void onClick(View v) {
						LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_RUN_LEFT);
					}
				});
		// Right
		addButton(TEXT_RIGHT, drawingWidth - (MARGIN + squareButtonSize), leftRighY, squareButtonSize,
				true, new View.OnClickListener() {
					public void onClick(View v) {
						LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_RUN_RIGHT);
					}
				});
				*/
	}	

}
