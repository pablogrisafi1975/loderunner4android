package org.loderunner;

import android.graphics.drawable.Drawable;

/**
 * This is just a wrapper to expose a simpler interface for
 * LodeRunnerdrawingThread
 * 
 * @author arpxxgr
 * 
 */
public class GameManager {
	// TODO make a class create all instances of everything
	public GameManager() {
		this.lodeRunnerDrawingThread = LodeRunnerDrawingThread.getInstance();
	}

	private final LodeRunnerDrawingThread lodeRunnerDrawingThread;

	public void pause() {
		lodeRunnerDrawingThread.pause();
	}

	public void play() {
		lodeRunnerDrawingThread.play();
	}

	public void up() {
		lodeRunnerDrawingThread.gameAction(LodeRunnerCharacter.MOVE_CLIMB_UP);
	}

	public void down() {
		lodeRunnerDrawingThread.gameAction(LodeRunnerCharacter.MOVE_CLIMB_DOWN);
	}

	public void left() {
		lodeRunnerDrawingThread.gameAction(LodeRunnerCharacter.MOVE_RUN_LEFT);
	}

	public void right() {
		lodeRunnerDrawingThread.gameAction(LodeRunnerCharacter.MOVE_RUN_RIGHT);
	}
	
	public void digLeft() {
		lodeRunnerDrawingThread.gameAction(LodeRunnerHero.MOVE_DIG_LEFT);
	}

	public void digRight() {
		lodeRunnerDrawingThread.gameAction(LodeRunnerHero.MOVE_DIG_RIGHT);
	}	
	
	public void setLevelChangeListener(LevelInfoChangedListener levelChangeListener){
		lodeRunnerDrawingThread.setLevelInfoChangedListener(levelChangeListener);
	}

	public void suicide() {
		lodeRunnerDrawingThread.stageOver(false);		
	}

	public void clearDone() {
		// TODO Auto-generated method stub
		
	}

	public void firstLevel() {
		lodeRunnerDrawingThread.loadNewLevel(0);
		
	}

	public void prevLevel() {
		advanceLevel(-1);			
	}

	public void nextLevel() {
		advanceLevel(1);			
	}
	
	public void skip10Levels(){
		advanceLevel(10);			
	}
	
	public void back10Levels(){
		advanceLevel(-10);			
	}
		
	
	private void advanceLevel(int levelsToMove){
		int newLevel = lodeRunnerDrawingThread.level + levelsToMove;
		if(newLevel >= LodeRunnerStage.MAX_LEVELS){
			newLevel = LodeRunnerStage.MAX_LEVELS - 1;
		}
		if(newLevel < 0){
			newLevel = 0;
		}		
		lodeRunnerDrawingThread.loadNewLevel(newLevel);		
		
	}

	public void unsolvedLevel() {
		lodeRunnerDrawingThread.nextLevelNotDone();		
	}

	public void updateLevelInfo() {
		lodeRunnerDrawingThread.updateLevelInfo();		
	}
}
