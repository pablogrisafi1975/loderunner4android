package org.loderunner;

import java.util.ArrayList;
import java.util.List;

import org.loderunner.swipe.SwipeDetector;
import org.loderunner.swipe.SwipeListener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class ViewManager {

	private static final String SURE_CLEAR_ALL_LEVELS = "Are you sure about clearing all done levels?";
	private static final String ALL_LEVELS_DONE_WANT_CLEAR = "All levels done! Do you want to clear and start over?";
	private static final String BUTTON_NO = "No";
	private static final String BUTTON_YES = "Yes";
	private static final String TITLE_WARNING = "Warning!";
	private static final String LABEL_VILAINS = "Foes: %d";
	private static final String LABEL_COINS = "$: %d/%d";
	private static final String LABEL_LIVES = "Lives: %d";
	private static final String LABEL_LEVEL = "Level: %03d";
	private RelativeLayout relativeLayout;
	private LodeRunnerView lodeRunnerView;
	private LodeRunnerActivity lodeRunnerActivity;
	
	private static final String TEXT_MENU = "Menu";
	private static final String TEXT_PLAY = "Play";
	private static final String TEXT_SUICIDE = "Suicide";
	private static final String TEXT_CLEAR_DONE = "Clear\nDone";
	private static final String TEXT_FIRST = "First";
	private static final String TEXT_PREV = "Prev.";
	private static final String TEXT_NEXT = "Next";
	private static final String TEXT_NEXT_NOT_DONE = "Next not\nDone";
	private static int MARGIN = 2;
	private List<Button> actionButtons;
	private List<Button> menuButtons;
	private GameManager gameManager;
	private TextView levelTextView;
	private TextView livesTextView;
	private TextView coinsTextView;
	private TextView villainsTextView;
	private TextView doneTextView;
		

	public ViewManager(LodeRunnerActivity lodeRunnerActivity, GameManager gameManager, RelativeLayout relativeLayout, LodeRunnerView lodeRunnerView) {
		this.relativeLayout = relativeLayout;
		this.lodeRunnerView = lodeRunnerView;
		this.lodeRunnerActivity = lodeRunnerActivity;
		this.gameManager = gameManager;
		
		this.actionButtons = new ArrayList<Button>();
		this.menuButtons = new ArrayList<Button>();		
		this.levelTextView = new TextView(lodeRunnerActivity);
		this.livesTextView = new TextView(lodeRunnerActivity);
		this.coinsTextView = new TextView(lodeRunnerActivity);
		this.villainsTextView = new TextView(lodeRunnerActivity);
		
		this.doneTextView = new TextView(lodeRunnerActivity);
		
	}
	
	public Runnable init(){
		return new Runnable() {
			public void run() {
				Rect outRect = new Rect();
				relativeLayout.getWindowVisibleDisplayFrame(outRect);
				
				int drawingWidth = outRect.width();
				int drawingHeight = calculateDrawingHeigth(outRect);
				Log.d(ViewManager.class.getCanonicalName(), "drawingWidth:" + drawingWidth + " drawingHeight:" + drawingHeight);
				
				createSwipeDetector(drawingWidth);
				
				Rect gameRect = createGameView(drawingWidth);

				int buttonSize = (drawingWidth - gameRect.width()) / 2 - 2 * MARGIN;
				
				createPlayWidgets(drawingWidth, buttonSize);
				
				createMenuWidgets(drawingWidth, buttonSize, gameRect);
				
				createInfoLabels(drawingWidth, buttonSize, gameRect);
				
				gameManager.updateLevelInfo();

			}

		};
	}
	
	private Rect createGameView(int drawingWidth) {
		int gameX = 0;
		int gameY = 0;
		int gameWidth = 0;
		int gameHeigth = 0;		
		
		if(drawingWidth <= 600){
			gameWidth = LodeRunnerStage.STAGE_WIDTH_PIXELS;
			gameHeigth = LodeRunnerStage.STAGE_HEIGHT_PIXELS;
			lodeRunnerView.setScale(1);
			Log.d(ViewManager.class.getCanonicalName(), "scale = 1");
		}else if(drawingWidth <= 900){
			gameWidth = LodeRunnerStage.STAGE_WIDTH_PIXELS * 2;
			gameHeigth = LodeRunnerStage.STAGE_HEIGHT_PIXELS * 2;		
			lodeRunnerView.setScale(2);
			Log.d(ViewManager.class.getCanonicalName(), "scale = 2");
		}else{
			int scale =  LodeRunnerStage.STAGE_WIDTH_PIXELS / drawingWidth;
			gameWidth = LodeRunnerStage.STAGE_WIDTH_PIXELS * scale;
			gameHeigth = LodeRunnerStage.STAGE_HEIGHT_PIXELS * scale;		
			lodeRunnerView.setScale(scale);			
			Log.d(ViewManager.class.getCanonicalName(), "scale = " + scale);
		}
		
		gameX = (drawingWidth - gameWidth) / 2;
		gameY = 0;
		
		addView(lodeRunnerView, gameX, gameY, gameWidth, gameHeigth);
		
		addView(doneTextView, gameX, gameY, gameWidth,	gameHeigth);		
		
		return new Rect(gameX, gameY, gameX + gameWidth, gameY + gameHeigth);
	};	
	
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

	private void createPlayWidgets(int drawingWidth, int buttonSize) {
		addButton(TEXT_MENU, MARGIN, MARGIN, buttonSize, true, new View.OnClickListener() {
			public void onClick(View v) {
				lodeRunnerActivity.onMenu();
			}		
		});
		
	}
	private void createInfoLabels(int drawingWidth, int buttonSize, Rect gameRect) {
		
		// left

		int labelTop = gameRect.bottom + MARGIN;
		int labelSize = buttonSize / 4;

		//level info	
		addView(levelTextView, (drawingWidth - buttonSize) / 2, labelTop, buttonSize, labelSize);
		
		addView(livesTextView, (drawingWidth - buttonSize) / 2, labelTop + labelSize, buttonSize, labelSize);
		 
		addView(coinsTextView, (drawingWidth - buttonSize) / 2, labelTop + labelSize * 2 , buttonSize, labelSize);
		
		addView(villainsTextView, (drawingWidth - buttonSize) / 2, labelTop + labelSize * 3, buttonSize, labelSize);
		
		gameManager.setLevelChangeListener(new LevelInfoChangedListener() {
			public void levelInfoChanged(final LevelInfo levelInfo) {
				Log.d(ViewManager.class.getCanonicalName(), "LevelInfo: " + levelInfo);
				lodeRunnerActivity.runOnUiThread(new Runnable() {					
					public void run() {
						levelTextView.setText(String.format(LABEL_LEVEL, levelInfo.getNumber() + 1));						
						levelTextView.invalidate();

						livesTextView.setText(String.format(LABEL_LIVES, levelInfo.getLives()));						
						livesTextView.invalidate();
						
						coinsTextView.setText(String.format(LABEL_COINS, levelInfo.getCoinsPicked(), levelInfo.getCoinsTotal()));						
						coinsTextView.invalidate();
						
						villainsTextView.setText(String.format(LABEL_VILAINS, levelInfo.getVilains()));						
						villainsTextView.invalidate();			
						
						doneTextView.setVisibility(levelInfo.isDone() ? View.VISIBLE : View.INVISIBLE);
						doneTextView.setText("Done!");
						doneTextView.setTextSize(LodeRunnerStage.STAGE_HEIGHT_PIXELS / 2);
						doneTextView.setTextColor(0xFF00FF00);
						doneTextView.invalidate();		
						
					}
				});							
			}
		});			
		
		gameManager.setPauseRequestedListener(new PauseRequestedListener() {			
			public void pauseRequest(final String message) {
				lodeRunnerActivity.runOnUiThread(new Runnable() {
					public void run() {
						if(message == null){
							doneTextView.setVisibility(View.INVISIBLE);
						}else{					
							doneTextView.setText(message);
							doneTextView.setTextSize(LodeRunnerStage.STAGE_HEIGHT_PIXELS / 4);
							doneTextView.setVisibility(View.VISIBLE);
						}
						doneTextView.invalidate();
						lodeRunnerActivity.onMenu();
					};
				});	
			};
		});
	}
	

	private void createMenuWidgets(int drawingWidth, int buttonSize, Rect gameRect) {
		doneTextView.setGravity(Gravity.CENTER);
		doneTextView.setVisibility(View.INVISIBLE);		
		
		//menu
		addButton(TEXT_PLAY, MARGIN, MARGIN, buttonSize, false, new View.OnClickListener() {
			public void onClick(View v) {
				lodeRunnerActivity.onPlay();
			}		
		});

		// suicide
		addButton(TEXT_SUICIDE, drawingWidth - MARGIN - buttonSize, MARGIN, buttonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.suicide();
					}
				});
		
		addButton(TEXT_CLEAR_DONE, drawingWidth - MARGIN - buttonSize, MARGIN * 2 + buttonSize, buttonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						showClearDoneDialog(SURE_CLEAR_ALL_LEVELS);
					}
				});		
		
		int afterViewY = gameRect.bottom + MARGIN;
		
		addButton(TEXT_FIRST, (drawingWidth - buttonSize)/2 - 2 * (buttonSize + MARGIN), afterViewY, buttonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.firstLevel();
					}
				});		
		Button prevButton = addButton(TEXT_PREV, (drawingWidth - buttonSize)/2 - (buttonSize + MARGIN), afterViewY, buttonSize, false,
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
		
		final Button nextButton = addButton(TEXT_NEXT, (drawingWidth + buttonSize )/2 + MARGIN, afterViewY, buttonSize, false,
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
		
		
		addButton(TEXT_NEXT_NOT_DONE, (drawingWidth + buttonSize )/2 + 2 * MARGIN + buttonSize , afterViewY, buttonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						int nextLevelNotDone = gameManager.nextLevelNotDone();
						if(nextLevelNotDone == -1){
							showClearDoneDialog(ALL_LEVELS_DONE_WANT_CLEAR);
						}
					}
				});

	}


	
	private void showClearDoneDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.lodeRunnerActivity);
		builder.setMessage(message).setTitle(TITLE_WARNING);		
		builder.setPositiveButton(BUTTON_YES, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	              gameManager.clearDone();
	           }
	       });
		builder.setNegativeButton(BUTTON_NO, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               dialog.cancel();
	           }
	       });
		builder.show();
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
						Thread.sleep(1500);
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

	private void createSwipeDetector(int drawingWidth) {
		SwipeDetector swipeDetector = new SwipeDetector(new SwipeListener() {
			
			public void top2bottom(View v) {
				gameManager.down();
			}
			
			public void right2left(View v) {
				gameManager.left();
			}
			
			public void left2right(View v) {
				gameManager.right();
			}
			
			public void bottom2top(View v) {
				gameManager.up();
			}

			public void tapLeft(View v) {
				gameManager.digLeft();		
			}

			public void tapRigth(View v) {
				gameManager.digRight();						
			}
		});
		
		swipeDetector.setDrawingWidth(drawingWidth);
		relativeLayout.setOnTouchListener(swipeDetector);
	}


}
