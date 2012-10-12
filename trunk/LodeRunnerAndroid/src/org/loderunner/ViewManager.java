package org.loderunner;

import java.util.ArrayList;
import java.util.List;

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

	private RelativeLayout relativeLayout;
	private LodeRunnerView lodeRunnerView;
	private LodeRunnerActivity lodeRunnerActivity;
	
	private static final String TEXT_RIGHT = "\u2192";
	private static final String TEXT_LEFT = "\u2190";
	private static final String TEXT_DOWN = "\u2193";
	private static final String TEXT_UP = "\u2191";
	private static final String TEXT_DIG_RIGHT = "\u2198";
	private static final String TEXT_DIG_LEFT = "\u2199";
	private static final String TEXT_DIG = "\u2199\u2198";
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

				Log.d(LodeRunnerActivity.class.getCanonicalName(), "width:" + drawingWidth + "height:" + drawingHeight);

				int smallButtonSize = (drawingHeight - LodeRunnerStage.STAGE_HEIGHT_PIXELS - MARGIN * 3) / 2;
				Log.d(LodeRunnerActivity.class.getCanonicalName(), "squareButtonSize:" + smallButtonSize);
				int bigButtonSize = (drawingWidth - LodeRunnerStage.STAGE_WIDTH_PIXELS) / 2 - 2 * MARGIN;
				int lastButtonLine = drawingHeight - smallButtonSize - MARGIN;


				addView(lodeRunnerView, (drawingWidth - LodeRunnerStage.STAGE_WIDTH_PIXELS) / 2, 0, LodeRunnerStage.STAGE_WIDTH_PIXELS ,
						LodeRunnerStage.STAGE_HEIGHT_PIXELS);
				
				addView(doneTextView, (drawingWidth - LodeRunnerStage.STAGE_WIDTH_PIXELS) / 2, 0, LodeRunnerStage.STAGE_WIDTH_PIXELS,
						LodeRunnerStage.STAGE_HEIGHT_PIXELS);

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
		addButton(TEXT_DIG_RIGHT, 2 * MARGIN + smallButtonSize, lastButtonLine, smallButtonSize, true,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.digRight();
					}
				});
		//dig
		addButton(TEXT_DIG, MARGIN, lastButtonLine - MARGIN - bigButtonSize, bigButtonSize, true,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.dig();
					}
				});		

		// up
		int upDownX = (drawingWidth + LodeRunnerStage.STAGE_WIDTH_PIXELS) / 2 + MARGIN;
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
		addView(levelTextView, (drawingWidth - bigButtonSize) / 2, leftRighY, bigButtonSize, smallButtonSize / 2);
		
		livesTextView.setText("lifes?");
		addView(livesTextView, (drawingWidth - bigButtonSize) / 2, leftRighY + smallButtonSize / 2, bigButtonSize, smallButtonSize / 2);
		 
		coinsTextView.setText("coins?");		
		addView(coinsTextView, (drawingWidth - bigButtonSize) / 2, leftRighY + smallButtonSize , bigButtonSize, smallButtonSize / 2);
		
		villainsTextView.setText("vilains?");
		addView(villainsTextView, (drawingWidth - bigButtonSize) / 2, leftRighY + smallButtonSize * 3 / 2, bigButtonSize, smallButtonSize / 2);
		
		gameManager.setLevelChangeListener(new LevelInfoChangedListener() {
			public void levelInfoChanged(final LevelInfo levelInfo) {
				Log.d(ViewManager.class.getCanonicalName(), "LevelInfo: " + levelInfo);
				lodeRunnerActivity.runOnUiThread(new Runnable() {					
					public void run() {
						levelTextView.setText(String.format("Level: %03d", levelInfo.getNumber() + 1));						
						levelTextView.invalidate();

						livesTextView.setText(String.format("Lives: %d", levelInfo.getLives()));						
						livesTextView.invalidate();
						
						coinsTextView.setText(String.format("$: %d/%d", levelInfo.getCoinsPicked(), levelInfo.getCoinsTotal()));						
						coinsTextView.invalidate();
						
						villainsTextView.setText(String.format("Foes: %d", levelInfo.getVilains()));						
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
	

	private void createMenuWidgets(int drawingWidth, int smallButtonSize, int bigButtonSize, int lastButtonLine) {
		doneTextView.setGravity(Gravity.CENTER);
		doneTextView.setVisibility(View.INVISIBLE);		
		
		//menu
		addButton(TEXT_PLAY, MARGIN, MARGIN, bigButtonSize, false, new View.OnClickListener() {
			public void onClick(View v) {
				lodeRunnerActivity.onPlay();
			}		
		});

		// suicide
		addButton(TEXT_SUICIDE, drawingWidth - MARGIN - bigButtonSize, MARGIN, bigButtonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.suicide();
					}
				});
		
		addButton(TEXT_CLEAR_DONE, drawingWidth - MARGIN - bigButtonSize, MARGIN * 2 + bigButtonSize, bigButtonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						showClearDoneDialog("Are you sure about clearing all done levels?");
					}
				});		
		
		int afterViewY = LodeRunnerStage.STAGE_HEIGHT_PIXELS + MARGIN;
		
		addButton(TEXT_FIRST, (drawingWidth - bigButtonSize)/2 - 2 * (bigButtonSize + MARGIN), afterViewY, bigButtonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						gameManager.firstLevel();
					}
				});		
		Button prevButton = addButton(TEXT_PREV, (drawingWidth - bigButtonSize)/2 - (bigButtonSize + MARGIN), afterViewY, bigButtonSize, false,
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
		
		final Button nextButton = addButton(TEXT_NEXT, (drawingWidth + bigButtonSize )/2 + MARGIN, afterViewY, bigButtonSize, false,
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
		
		
		addButton(TEXT_NEXT_NOT_DONE, (drawingWidth + bigButtonSize )/2 + 2 * MARGIN + bigButtonSize , afterViewY, bigButtonSize, false,
				new View.OnClickListener() {
					public void onClick(View v) {
						int nextLevelNotDone = gameManager.nextLevelNotDone();
						if(nextLevelNotDone == -1){
							showClearDoneDialog("All levels done! Do you want to clear and start over?");
						}
					}
				});

	}


	
	private void showClearDoneDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.lodeRunnerActivity);
		builder.setMessage(message).setTitle("Warning!");		
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	              gameManager.clearDone();
	           }
	       });
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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


}
