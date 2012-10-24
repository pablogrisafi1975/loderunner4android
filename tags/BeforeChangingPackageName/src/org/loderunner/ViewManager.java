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

	private static final String TEXT_DONE = "Done!";
	private static final String SURE_CLEAR_ALL_LEVELS = "Are you sure about clearing all done levels?";
	private static final String ALL_LEVELS_DONE_WANT_CLEAR = "All levels done! Do you want to clear and start over?";
	private static final String BUTTON_NO = "No";
	private static final String BUTTON_YES = "Yes";
	private static final String TITLE_WARNING = "Warning!";
	private static final String LABEL_VILAINS = "Enemies: %02d";
	private static final String LABEL_COINS = "Coins: %02d/%02d";
	private static final String LABEL_LIVES = "Lives: %d";
	private static final String LABEL_LEVEL = "Level: %03d";
	private RelativeLayout relativeLayout;
	private LodeRunnerView lodeRunnerView;
	private LodeRunnerActivity lodeRunnerActivity;
	
	private static final String TEXT_PAUSE = "Pause";
	private static final String TEXT_QUIT = "Quit";
	private static final String TEXT_PLAY = "Play";
	private static final String TEXT_SUICIDE = "Suicide";
	private static final String TEXT_CLEAR_DONE = "Clear\nDone";
	private static final String TEXT_FIRST = "First";
	private static final String TEXT_PREV = "Prev.";
	private static final String TEXT_NEXT = "Next";
	private static final String TEXT_NEXT_NOT_DONE = "Next not\nDone";
	private List<Button> actionButtons;
	private List<Button> menuButtons;
	private GameManager gameManager;
	private TextView doneTextView;
		

	public ViewManager(LodeRunnerActivity lodeRunnerActivity, GameManager gameManager, RelativeLayout relativeLayout, LodeRunnerView lodeRunnerView) {
		this.relativeLayout = relativeLayout;
		this.lodeRunnerView = lodeRunnerView;
		this.lodeRunnerActivity = lodeRunnerActivity;
		this.gameManager = gameManager;
		
		this.actionButtons = new ArrayList<Button>();
		this.menuButtons = new ArrayList<Button>();		
	
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
				
				createPlayWidgets(drawingWidth, drawingHeight, gameRect);
				
				createMenuWidgets(drawingWidth, drawingHeight);
				
				createInfoLabels(drawingWidth);
				
				gameManager.updateLevelInfo();

			}

		};
	}
	
	private Rect createGameView(int drawingWidth) {
		int gameX = 0;
		int gameY = 0;
		int gameWidth = 0;
		int gameHeigth = 0;		
		if(drawingWidth <= LodeRunnerStage.STAGE_WIDTH_PIXELS){
			gameWidth = drawingWidth;
			gameHeigth = LodeRunnerStage.STAGE_HEIGHT_PIXELS;			
			lodeRunnerView.setScale(1);
			Log.d(ViewManager.class.getCanonicalName(), "scale = 1, but panning");
		}		
		else if(drawingWidth <= 600){
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
	
	private Button addButton(String text, int x, int y, int width, int heigth, boolean isActionButton, View.OnClickListener onClickListener) {
		Button button = new Button(lodeRunnerActivity);
		button.setText(text);
		button.setFocusable(false);
		button.getBackground().setAlpha(128);
		if (isActionButton) {
			button.setVisibility(View.INVISIBLE);
			actionButtons.add(button);
		} else {			
			menuButtons.add(button);
		}
		button.setOnClickListener(onClickListener);
		return (Button) addView(button, x, y, width, heigth);		
	}

	private Button addSquareButton(String text, int x, int y, int size, boolean isActionButton, View.OnClickListener onClickListener) {
		return addButton(text, x, y, size, size, isActionButton, onClickListener);
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


	private void createInfoLabels(int drawingWidth) {
		
		gameManager.setLevelChangeListener(new LevelInfoChangedListener() {
			public void levelInfoChanged(final LevelInfo levelInfo) {
				Log.d(ViewManager.class.getCanonicalName(), "LevelInfo: " + levelInfo);
				lodeRunnerActivity.runOnUiThread(new Runnable() {					
					public void run() {	
						StringBuffer sb = new StringBuffer(lodeRunnerActivity.getString(R.string.title_activity_lode_runner));
						sb.append("      |");
						sb.append(String.format(LABEL_LEVEL, levelInfo.getNumber() + 1));
						sb.append(" | ");
						
						sb.append(String.format(LABEL_LIVES, levelInfo.getLives()));
						sb.append(" | ");

						sb.append(String.format(LABEL_COINS, levelInfo.getCoinsPicked(), levelInfo.getCoinsTotal()));
						sb.append(" | ");

						sb.append(String.format(LABEL_VILAINS, levelInfo.getVilains()));
						lodeRunnerActivity.setTitle(sb);
						
						doneTextView.setVisibility(levelInfo.isDone() ? View.VISIBLE : View.INVISIBLE);
						doneTextView.setText(TEXT_DONE);
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
	
	private void createPlayWidgets(int drawingWidth, int drawingHeight, Rect gameRect) {
		int buttonWidth =  (int) (((double)drawingWidth )/ 7.1d);
		int margin = (drawingWidth - 7 * buttonWidth) / 8;		
		int middleMinusHalf = (drawingWidth - buttonWidth) / 2;
		
		int buttonHeigth = buttonWidth;
		if(buttonHeigth + gameRect.bottom + 2 * margin > drawingHeight){
			buttonHeigth = drawingHeight - gameRect.bottom - 2 * margin; 
		}
		
		int buttonTop = drawingHeight - margin - buttonHeigth;
		
		
		addButton(TEXT_PAUSE, middleMinusHalf, buttonTop, buttonWidth, buttonHeigth, true, new View.OnClickListener() {
			public void onClick(View v) {
				lodeRunnerActivity.onMenu();
			}		
		});
		
	}	
	

	private void createMenuWidgets(int drawingWidth, int drawingHeight) {
		doneTextView.setGravity(Gravity.CENTER);
		doneTextView.setVisibility(View.INVISIBLE);		
		int buttonSize =  (int) (((double)drawingWidth )/ 7.1d);
		int margin = (drawingWidth - 7 * buttonSize) / 8;
		int buttonTop = drawingHeight - margin - buttonSize;
		int middleMinusHalf = (drawingWidth - buttonSize) / 2;
		
		addSquareButton(TEXT_PLAY, middleMinusHalf , buttonTop, buttonSize, false, new View.OnClickListener() {
			public void onClick(View v) {
				lodeRunnerActivity.onPlay();
			}		
		});
		
		Button prevButton = addSquareButton(TEXT_PREV, middleMinusHalf - (buttonSize + margin), buttonTop, buttonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.prevLevel();
					}
				});			
		
		addSquareButton(TEXT_FIRST, middleMinusHalf - 2 * (buttonSize + margin), buttonTop, buttonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.firstLevel();
					}
				});				

		addSquareButton(TEXT_SUICIDE, middleMinusHalf - 3 * (buttonSize + margin), buttonTop, buttonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.suicide();
					}
				});
		
		int middlePlusHalf = (drawingWidth + buttonSize) / 2 + margin;
		
		final Button nextButton = addSquareButton(TEXT_NEXT, middlePlusHalf, buttonTop, buttonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.nextLevel();
					}
				});			
		
		addSquareButton(TEXT_NEXT_NOT_DONE, middlePlusHalf + (margin + buttonSize), buttonTop, buttonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						int nextLevelNotDone = gameManager.nextLevelNotDone();
						if(nextLevelNotDone == -1){
							showClearDoneDialog(ALL_LEVELS_DONE_WANT_CLEAR);
						}
					}
				});		
		
		addSquareButton(TEXT_CLEAR_DONE,  middlePlusHalf + 2 * (margin + buttonSize), buttonTop, buttonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						showClearDoneDialog(SURE_CLEAR_ALL_LEVELS);
					}
				});		
		
		addSquareButton(TEXT_QUIT, drawingWidth - buttonSize - margin , margin, buttonSize, false, new View.OnClickListener() {
			public void onClick(View v) {
				lodeRunnerActivity.finish();
			}		
		});		
		
		makeRepeatingButton(prevButton,  new View.OnLongClickListener() {			
			public boolean onLongClick(View view) {
				gameManager.back10Levels();
				return true;
			}
		});		
		
		makeRepeatingButton(nextButton,  new View.OnLongClickListener() {			
			public boolean onLongClick(View view) {
				gameManager.skip10Levels();
				return true;
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
