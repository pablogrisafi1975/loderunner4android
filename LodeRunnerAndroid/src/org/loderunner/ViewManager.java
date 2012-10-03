package org.loderunner;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class ViewManager {

	private RelativeLayout relativeLayout;
	private LodeRunnerView lodeRunnerView;
	private LodeRunnerActivity lodeRunnerActivity;
	
	private static final String TEXT_RIGHT = "\u2192";
	private static final String TEXT_LEFT = "\u2190";
	private static final String TEXT_DOWN = "\u2193";
	private static final String TEXT_UP = "\u2191";
	private static final String TEXT_DIG_RIGTH = "\u2198";
	private static final String TEXT_DIG_LEFT = "\u2199";
	private static final String TEXT_MENU = "Menu";
	private static final String TEXT_PLAY = "Play";
	private static final String TEXT_SUICIDE = "Suicide";
	private static final String TEXT_CLEAR_DONE = "Clear\nDone";
	private static final String TEXT_FIRST = "First";
	private static final String TEXT_PREV = "Prev.";
	private static final String TEXT_NEXT = "Next";
	private static final String TEXT_NEXT_NOT_DONE = "Next not\nDone";
	private static int LODE_RUNNER_VIEW_WIDTH = 336;
	private static int LODE_RUNNER_VIEW_HEIGHT = 176;
	private static int MARGIN = 2;
	private List<Button> actionButtons;
	private List<Button> menuButtons;
	private GameManager gameManager;
	private TextView levelTextView;
		

	public ViewManager(LodeRunnerActivity lodeRunnerActivity, GameManager gameManager, RelativeLayout relativeLayout, LodeRunnerView lodeRunnerView) {
		this.relativeLayout = relativeLayout;
		this.lodeRunnerView = lodeRunnerView;
		this.lodeRunnerActivity = lodeRunnerActivity;
		this.gameManager = gameManager;
		
		this.actionButtons = new ArrayList<Button>();
		this.menuButtons = new ArrayList<Button>();		
		this.levelTextView = new TextView(lodeRunnerActivity);
	}
	
	public Runnable init(){
		return new Runnable() {
			public void run() {
				Rect outRect = new Rect();
				relativeLayout.getWindowVisibleDisplayFrame(outRect);
				int drawingWidth = outRect.width();
				int drawingHeight = calculateDrawingHeigth(outRect);

				Log.d(LodeRunnerActivity.class.getCanonicalName(), "width:" + drawingWidth + "height:" + drawingHeight);

				int smallButtonSize = (drawingHeight - LODE_RUNNER_VIEW_HEIGHT - MARGIN * 3) / 2;
				Log.d(LodeRunnerActivity.class.getCanonicalName(), "squareButtonSize:" + smallButtonSize);
				int bigButtonSize = (drawingWidth - LODE_RUNNER_VIEW_WIDTH) / 2 - 2 * MARGIN;
				int lastButtonLine = drawingHeight - smallButtonSize - MARGIN;


				addView(lodeRunnerView, (drawingWidth - LODE_RUNNER_VIEW_WIDTH) / 2, 0, LODE_RUNNER_VIEW_WIDTH,
						LODE_RUNNER_VIEW_HEIGHT);

				createActionWidgets(drawingWidth, smallButtonSize, bigButtonSize, lastButtonLine);
				
				createMenuWidgets(drawingWidth, smallButtonSize, bigButtonSize, lastButtonLine);
				
				gameManager.updateLevelInfo();
				

			};
		};
	}
	
	private View addView(View view, int x, int y, int width, int heigth) {
		LayoutParams layoutParams = new LayoutParams(width, heigth);
		layoutParams.leftMargin = x;
		layoutParams.topMargin = y;
		view.setLayoutParams(layoutParams);
		relativeLayout.addView(view);
		return view;
	}

	private Button addButton(String text, int x, int y, int size, boolean isActionButton, View.OnClickListener onClickListener) {
		Button button = new Button(lodeRunnerActivity);
		button.setText(text);
		button.setFocusable(false);
		if (isActionButton) {
			button.setVisibility(View.INVISIBLE);
			actionButtons.add(button);
		} else {			
			menuButtons.add(button);
		}
		button.setOnClickListener(onClickListener);
		return (Button) addView(button, x, y, size, size);
	}
	
	public void showMenuWidgets() {
		showSomeButtons(true);
		
	}	
	
	public void showActionWidgets() {
		showSomeButtons(false);
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
	
	private int calculateDrawingHeigth(Rect outRect) {
		int statusBarHeight = outRect.top;
		// Get the height occupied by the decoration contents
		int contentViewTop = lodeRunnerActivity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
		// Calculate titleBarHeight by deducting statusBarHeight from
		// contentViewTop
		int titleBarHeight = contentViewTop - statusBarHeight;

		// By now we got the height of titleBar & statusBar
		// Now lets get the screen size
		DisplayMetrics metrics = new DisplayMetrics();
		lodeRunnerActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int screenHeight = metrics.heightPixels;
		// int screenWidth = metrics.widthPixels;

		// Now calculate the height that our layout can be set
		// If you know that your application doesn't have statusBar added, then
		// don't add here also. Same applies to application bar also
		int layoutHeight = screenHeight - (titleBarHeight + statusBarHeight);
		return layoutHeight;
	}

	private void createActionWidgets(int drawingWidth, int smallButtonSize, int bigButtonSize, int lastButtonLine) {
		//menu
		addButton(TEXT_MENU, MARGIN, MARGIN, bigButtonSize, true, new View.OnClickListener() {
			public void onClick(View v) {
				lodeRunnerActivity.onMenu();
			}		
		});

		// dig left
		addButton(TEXT_DIG_LEFT, MARGIN, lastButtonLine, smallButtonSize, true,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.digLeft();
					}
				});

		// dig right
		addButton(TEXT_DIG_RIGTH, 2 * MARGIN + smallButtonSize, lastButtonLine, smallButtonSize, true,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.digRight();
					}
				});

		// up
		int upDownX = (drawingWidth + LODE_RUNNER_VIEW_WIDTH) / 2 + MARGIN;
		addButton(TEXT_UP, upDownX, lastButtonLine - 2 * MARGIN - 2 * smallButtonSize, smallButtonSize,
				true, new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.up();
					}
				});
		// down
		addButton(TEXT_DOWN, upDownX, lastButtonLine, smallButtonSize, true, new View.OnClickListener() {
			public void onClick(View v) {
				gameManager.down();
			}
		});

		// left

		int leftRighY = lastButtonLine - MARGIN - smallButtonSize;

		addButton(TEXT_LEFT, drawingWidth - 2 * (MARGIN + smallButtonSize), leftRighY, smallButtonSize,
				true, new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.left();
					}
				});
		// Right
		addButton(TEXT_RIGHT, drawingWidth - (MARGIN + smallButtonSize), leftRighY, smallButtonSize,
				true, new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.right();
					}
				});
		//level info	
		levelTextView.setText("level?");
		addView(levelTextView, (drawingWidth - bigButtonSize) / 2, leftRighY, bigButtonSize, smallButtonSize);
		gameManager.setLevelChangeListener(new LevelChangeListener() {
			@Override
			public void levelChanged(final String levelInfo) {
				Log.d(ViewManager.class.getCanonicalName(), "levelInfo: " + levelInfo);
				lodeRunnerActivity.runOnUiThread(new Runnable() {					
					public void run() {
						levelTextView.setText(levelInfo);
						levelTextView.invalidate();						
					}
				});
							
			}
		});				
		
		
	}
	
	private void createMenuWidgets(int drawingWidth, int squareButtonSize, int menuButtonSize, int lastButtonLine) {
		//menu
		addButton(TEXT_PLAY, MARGIN, MARGIN, menuButtonSize, false, new View.OnClickListener() {
			public void onClick(View v) {
				lodeRunnerActivity.onPlay();
			}		
		});

		// suicide
		addButton(TEXT_SUICIDE, drawingWidth - MARGIN - menuButtonSize, MARGIN, menuButtonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.suicide();
					}
				});
		
		addButton(TEXT_CLEAR_DONE, drawingWidth - MARGIN - menuButtonSize, MARGIN * 2 + menuButtonSize, menuButtonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.clearDone();
					}
				});		
		
		int afterViewY = LODE_RUNNER_VIEW_HEIGHT + MARGIN;
		
		addButton(TEXT_FIRST, (drawingWidth - menuButtonSize)/2 - 2 * (menuButtonSize + MARGIN), afterViewY, menuButtonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.firstLevel();
					}
				});		
		Button prevButton = addButton(TEXT_PREV, (drawingWidth - menuButtonSize)/2 - (menuButtonSize + MARGIN), afterViewY, menuButtonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.prevLevel();
					}
				});		
		
		makeRepeatingButton(prevButton,  new View.OnLongClickListener() {			
			public boolean onLongClick(View view) {
				gameManager.back10Levels();
				return true;
			}
		});		
		
		final Button nextButton = addButton(TEXT_NEXT, (drawingWidth + menuButtonSize )/2 + MARGIN, afterViewY, menuButtonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.nextLevel();
					}
				});	
		
		
		
		makeRepeatingButton(nextButton,  new View.OnLongClickListener() {			
			public boolean onLongClick(View view) {
				gameManager.skip10Levels();
				return true;
			}
		});
		
		
		addButton(TEXT_NEXT_NOT_DONE, (drawingWidth + menuButtonSize )/2 + 2 * MARGIN + menuButtonSize , afterViewY, menuButtonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.unsolvedLevel();
					}
				});

	}

	//horrible hack to create a repeating button class
	//too lazy to create a class
	//I suck
	
	private void makeRepeatingButton(final Button button, final OnLongClickListener longClickListener) {
		button.setLongClickable(true);
		View.OnLongClickListener filteredLongClickListener = new View.OnLongClickListener() {			
			public boolean onLongClick(View view) {
				longClickListener.onLongClick(view);
				if(button.isPressed() && button.isClickable()){
					reClickStart(button);
				}
				return true;
			}
		};
		button.setOnLongClickListener(filteredLongClickListener);
	}
	

	
	
	private void reClickStart(final Button button){
		Thread t = new Thread(new Runnable() {			
			public void run() {
				button.setClickable(false);
				while(button.isPressed()){
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}					
					if(button.isPressed()){
						button.performLongClick();
					}
				}
				button.setClickable(true);
			}
		});
		t.start();
	}


	public TextView getLevelTextView() {
		return levelTextView;
	}

	

}
