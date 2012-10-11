package org.loderunner;


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
		if(lodeRunnerDrawingThread.isPaused){
			return;
		}
		lodeRunnerDrawingThread.pause();
	}

	public void play() {
		if(!lodeRunnerDrawingThread.isPaused){
			return;
		}		
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
	
	public void dig() {
		lodeRunnerDrawingThread.gameAction(LodeRunnerHero.MOVE_DIG);
		
	}
	
	public void setLevelChangeListener(LevelInfoChangedListener levelChangeListener){
		lodeRunnerDrawingThread.setLevelInfoChangedListener(levelChangeListener);
	}

	public void suicide() {
		lodeRunnerDrawingThread.stageOver(false);		
	}

	public void clearDone() {
		lodeRunnerDrawingThread.clearDoneLevels();
		
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

	public int nextLevelNotDone() {
		return lodeRunnerDrawingThread.nextLevelNotDone();		
	}

	public void updateLevelInfo() {
		lodeRunnerDrawingThread.updateLevelInfo();		
	}

	public void setPauseRequestedListener(PauseRequestedListener pauseRequestedListener) {
		lodeRunnerDrawingThread.setPauseRequestedListener(pauseRequestedListener);		
	}


}
