package org.loderunner;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class LodeRunnerDrawingThread extends Thread {

	private Graphics g = new Graphics();
	private LevelInfoChangedListener levelInfoChangedListener;
	private PauseRequestedListener pauseRequestedListener;
	private boolean running;
	private SurfaceHolder holder;

	/** Rendering frequency used by the animation thread */
	protected final static int FRAMERATE_MILLISEC = 66;
	/** "In pause" status of the game */
	protected volatile boolean isPaused = true;
	/** All game events are scheduled and sequenced by a single Timer thread */
	protected Timer timer = null;
	/** No repaint is needed, display is up to date */
	protected static final int REPAINT_NONE = 0;
	/** Repaint requested by a timer event (generally minor changes) */
	protected static final int REPAINT_TIMER = 1;
	/** Repaint requested by a key event (generally more important changes) */
	protected static final int REPAINT_KEY = 2;
	/** Full repaint is needed, display must be rendered again entirely */
	protected static final int REPAINT_ALL = 3;
	/** Tells the animation threads what elements should be rendered again */
	protected volatile int needsRepaint;

	/** Game name use for store persistence */
	private static final String GAME_NAME = "LodeRunner";
	/** Game splash screen */
	private GameSprite splashScreen = null;

	/**
	 * Maximum number of lives given to the player when starting the game
	 * (constant)
	 */
	private static final int MAX_LIFES = 5;
	/** Lives left to the player */
	private int lives = MAX_LIFES;
	/** Level number of the current stage */
	public int level = 0;
	/** Current stage, when game is in progress */
	private LodeRunnerStage stage = null;

	private byte[] levelStatuses = new byte[LodeRunnerStage.MAX_LEVELS];

	private static final byte STATUS_DONE = 1;
	private static final byte STATUS_NOT_DONE = 0;
	private int w0;
	private int h0;

	private int width;
	private int height;
	private Context context;

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private static LodeRunnerDrawingThread instance;

	public LodeRunnerDrawingThread(SurfaceHolder holder, Context context, int width, int heigth) {
		instance = this;
		this.context = context;
		this.width = width;
		this.height = heigth;
		this.holder = holder;
		InputStream fontInputStream = this.context.getResources().openRawResource(R.raw.font);
		InputStream tilesInputStream = this.context.getResources().openRawResource(R.raw.tiles12x11);
		stage = new LodeRunnerStage(fontInputStream, tilesInputStream);		
		recoverStatus();
		stage.loadFromResource(openBinInputStream());
	}

	private InputStream openBinInputStream() {
		return this.context.getResources().openRawResource(R.raw.loderunnerbin);
	}

	public static LodeRunnerDrawingThread getInstance() {
		return instance;
	}

	public void setRunning(boolean running) {
		this.running = running;

	}

	@Override
	public void run() {
		while (running) {
			Canvas canvas = null;
			try {
				canvas = holder.lockCanvas(null);

				synchronized (holder) {
					doDraw(canvas);
				}
			} finally {
				// do this in a finally so that if an exception is thrown
				// during the above, we don't leave the Surface in an
				// inconsistent state
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

	private void doDraw(Canvas canvas) {
		g.setCanvas(canvas);
		stage.paint(g);
	}

	/** Render the message or splash screen */
	private void paintMessage(Graphics g, int w, int h) {
		// There is no message to render
		if (splashScreen != null) {
			if ((level / LodeRunnerStage.GAME_LEVELS) % 2 == 0) {
				// Lode Runner splash screen
				splashScreen.paint(g, 0, w / 2, h / 2, Graphics.HCENTER | Graphics.VCENTER);
			} else {
				// Championship splash screen
				splashScreen.paint(g, 0, w / 2, h / 2 - 6, Graphics.HCENTER | Graphics.VCENTER);
				splashScreen.paint(g, 1, w / 2, h / 2 + 6, Graphics.HCENTER | Graphics.VCENTER);
			}
		}
	}

	boolean clearAfterPause = true;
	

	/** Render the game canvas */
	public void paint(Graphics g) {
		// Render the stage
		if (stage != null) {
			stage.paint(g);
			if (isPaused || !stage.isLoaded) {
				clearAfterPause = true;

				// clean the stage
				w0 = g.getClipWidth();
				h0 = g.getClipHeight();
				g.setColor(0x000000);
				g.fillRect(0, 0, w0, h0);

				// Render the stage mini-map
				//stage.spriteSize = LodeRunnerStage.SPRITE_SMALL;
				//stage.paint(g);

				int cx = LodeRunnerStage.STAGE_WIDTH * LodeRunnerStage.SPRITE_WIDTH;
				int cy = LodeRunnerStage.STAGE_HEIGHT * LodeRunnerStage.SPRITE_HEIGHT;
				int x = (w0 - cx) / 2, y = (h0 - cy) / 2;
				if (x > 10) {
					x = 10;
				}

				// Render the message or splash screen
				paintMessage(g, w0, y);
				// Display game information
				g.setColor(0x00ffffff);

				if (!stage.isLoaded) {
					g.setColor(0x00ffff00);
					//g.drawString("Loading...", x + cx / 2, y + (cy - font.getHeight()) / 2, Graphics.TOP  | Graphics.HCENTER);
					g.setColor(0x001463af);
					g.drawString("� 2006 - Fabien GIGANTE", w0 / 2, h0 - 2, Graphics.HCENTER | Graphics.BOTTOM);
				}

			} else {
				if (clearAfterPause) {
					clearAfterPause = false;
					g.setColor(0x000000);
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(0x00ffffff);
				}
				if (stage != null) {
					g.setColor(0x00ffffff);
				}
			}
		}
	}
	
	public void pauseRequest(String pauseMessage) {
		this.pauseRequestedListener.pauseRequest(pauseMessage);
	}

	public void updateLevelInfo() {
		if (this.levelInfoChangedListener != null) {
			LevelInfo levelInfo = createLevelInfo();
			this.levelInfoChangedListener.levelInfoChanged(levelInfo);
		}
	}
	

	private LevelInfo createLevelInfo() {
		LevelInfo levelInfo = new LevelInfo();
		levelInfo.setNumber(level);
		levelInfo.setLives(lives);
		levelInfo.setCoinsPicked(stage.hero == null ? 0 : stage.hero.nChests);
		levelInfo.setCoinsTotal(stage.nChests);
		levelInfo.setVilains(stage.villains.size());
		levelInfo.setDone(levelStatuses[level] == STATUS_DONE);
		return levelInfo;
	}

	/**
	 * 
	 * @param newLevel
	 *            0-based level
	 */
	public void loadNewLevel(int newLevel) {
		this.level = newLevel;
		try {
			this.stage.loadFromResource(openBinInputStream());
			updateLevelInfo();
		} catch (Exception e) {
			Log.e(LodeRunnerDrawingThread.class.getCanonicalName(), "Error loading level", e);
			throw new RuntimeException(e);
		}

	}

	public void clearDoneLevels() {
		for (int i = 0; i < levelStatuses.length; i++) {
			levelStatuses[i] = STATUS_NOT_DONE;
		}
		persistStatus();
		updateLevelInfo();
	}

	protected abstract class GameTask extends TimerTask {
	}

	/**
	 * Game event tasks that require a display rendering refresh should inherit
	 * from this class
	 */
	protected class RepaintTask extends GameTask {

		/**
		 * Triggered by the Timer. Ask the animation thread for a repaint (due
		 * to timer).
		 */
		public void run() {
			needsRepaint |= REPAINT_TIMER;
		}
	}

	/**
	 * Define the hero's heartBeat as a game event task.
	 */
	protected class HeroHeartbeatTask extends RepaintTask {

		/** Scheduled every frame */
		public final static int PERIOD = FRAMERATE_MILLISEC;

		/** Heartbeat */
		public void run() {
			if (stage != null && stage.isLoaded && stage.hero != null) {
				stage.hero.heartBeat();
				if (stage.endCompleted) {
					stageOver(true);
				} else if (stage.endHeroDied) {
					stageOver(false);
				}
			}
			super.run();
		}
	}

	/**
	 * Define the villains' heartBeat as a game event task.
	 */
	protected class VillainsHeartbeatTask extends RepaintTask {

		/** Scheduled every 2 frames */
		public final static int PERIOD = 2 * FRAMERATE_MILLISEC;

		/** Heartbeat */
		public void run() {
			// Loop on every villain
			if (stage != null && stage.isLoaded) {
				for (LodeRunnerVillain lodeRunnerVillain : stage.villains) {
					lodeRunnerVillain.heartBeat();
				}
			}
			super.run();
		}
	}

	/**
	 * Define the stage's heartBeat as a game event task.
	 */
	protected class StageHeartbeatTask extends RepaintTask {

		/** Scheduled every 2 frames */
		public final static int PERIOD = 2 * FRAMERATE_MILLISEC;

		/** Heartbeat */
		public void run() {
			// Loop on every hole
			if (stage != null && stage.isLoaded) {
				for (LodeRunnerHole lodeRunnerHole : stage.holes) {
					lodeRunnerHole.heartBeat();
				}
			}
			super.run();
		}
	}

	public int nextLevelNotDone() {
		int nextLevelNotDone = -1;
		for (int i = level + 1; i < LodeRunnerStage.MAX_LEVELS; i++) {
			if (levelStatuses[i] == STATUS_NOT_DONE) {
				nextLevelNotDone = i;
				break;
			}
		}
		if (nextLevelNotDone == -1) {
			for (int i = 0; i <= level; i++) {
				if (levelStatuses[i] == STATUS_NOT_DONE) {
					nextLevelNotDone = i;
					break;
				}
			}
		}
		if (nextLevelNotDone != -1) {
			loadNewLevel(nextLevelNotDone);
		}
		return nextLevelNotDone;
	}

	/**
	 * Stage is complete, or hero has died
	 */
	public void stageOver(boolean hasCompleted) {
		if (!isPaused) {
			levelStatuses[level] = hasCompleted ? STATUS_DONE : STATUS_NOT_DONE;
		}
		// Adjust lives and level
		String pauseMessage = "";
		if (hasCompleted) {
			level++;
			if (level == LodeRunnerStage.MAX_LEVELS) {
				level = 0;
			}
			pauseMessage = (level % LodeRunnerStage.GAME_LEVELS == 0) ? null : "Congratulations !";
		} else {
			lives--;
			if (lives < 0) {
				lives = MAX_LIFES;
				level = 0;
				pauseMessage = "Game Over";
			} else {
				pauseMessage = "Try again...";
			}
		}
		// Load appropriate stage
		try {
			stage.loadFromResource(openBinInputStream());
		} catch (Exception e) {
		}
		needsRepaint = REPAINT_ALL;
		updateLevelInfo();
		if (!isPaused) {
			pauseRequest(pauseMessage);
		}
	}

	public void gameAction(int actionCode) {
		if (actionCode == LodeRunnerHero.MOVE_DIG) {
			if (stage.hero.lookLeft) {
				stage.hero.requestMove(LodeRunnerHero.MOVE_DIG_LEFT);
			} else {
				stage.hero.requestMove(LodeRunnerHero.MOVE_DIG_RIGHT);
			}
		} else {
			stage.hero.requestMove(actionCode);
		}
	}

	public synchronized void pause() {
		persistStatus();
		needsRepaint = REPAINT_ALL;
		isPaused = true;
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	public synchronized void play() {
		needsRepaint = REPAINT_ALL;
		isPaused = false;
		timer = new Timer();
		levelStatuses[level] = STATUS_NOT_DONE;
		
		timer.schedule(new HeroHeartbeatTask(), 0, HeroHeartbeatTask.PERIOD);
		// Schedule the villains' heartBeat
		timer.schedule(new VillainsHeartbeatTask(), 0, VillainsHeartbeatTask.PERIOD);
		// Schedule the stage's heartBeat
		timer.schedule(new StageHeartbeatTask(), 0, StageHeartbeatTask.PERIOD);
		updateLevelInfo();
	}

	public void setLevelInfoChangedListener(final LevelInfoChangedListener levelInfoChangedListener) {
		this.levelInfoChangedListener = levelInfoChangedListener;
		this.stage.setLevelInfoChangedListener(new LevelInfoChangedListener() {
			public void levelInfoChanged(LevelInfo dontCare) {
				LevelInfo levelInfo = createLevelInfo();
				levelInfoChangedListener.levelInfoChanged(levelInfo);
			}
		});
	}

	private void recoverStatus() {
		SharedPreferences settings = context.getSharedPreferences(GAME_NAME, Context.MODE_PRIVATE);
		this.level = settings.getInt("level", 0);
		this.lives = settings.getInt("lifes", MAX_LIFES);
		String[] levels = settings.getString("levelStatuses", "").split("L");
		for (int i = 0; i < levelStatuses.length; i++) {
			byte status = STATUS_NOT_DONE;
			if (i < levels.length) {
				Byte newStatus = STATUS_NOT_DONE;
				try {
					newStatus = Byte.valueOf(levels[i]);
				} catch (Exception ex) {
					newStatus = STATUS_NOT_DONE;
				}
				if (newStatus.equals(STATUS_DONE)) {
					status = newStatus;
				}
			}
			//just for testing remove!!!!
			//status = i == 0 ? STATUS_NOT_DONE: STATUS_DONE;

			levelStatuses[i] = status;
		}

	}

	private void persistStatus() {
		SharedPreferences settings = context.getSharedPreferences(GAME_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("level", level);
		editor.putInt("lifes", lives);
		StringBuffer levels = new StringBuffer();
		for (byte b : levelStatuses) {
			levels.append(String.valueOf(b)).append('L');
		}
		editor.putString("levelStatuses", levels.toString());

		// Commit the edits!
		editor.commit();
	}

	public void setPauseRequestedListener(PauseRequestedListener pauseRequestedListener) {
		this.pauseRequestedListener = pauseRequestedListener;		
	}

}
