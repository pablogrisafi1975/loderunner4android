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
	
	public void setLevelChangeListener(LevelChangeListener levelChangeListener){
		lodeRunnerDrawingThread.setLevelChangeListener(levelChangeListener);
	}

}
