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
	private boolean running;
	private SurfaceHolder holder;

	/** Rendering frequency used by the animation thread */
	protected final static int FRAMERATE_MILLISEC = 66;
	/** "In pause" status of the game */
	protected volatile boolean isPaused = true;
	/** All game events are scheduled and sequenced by a single Timer thread */
	protected Timer timer = null;
	/**
	 * Flag set to false if this game canvas was never rendered, to true
	 * otherwise
	 */
	private volatile boolean hasBeenShown = false;
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

	/** Game name use for store persistency */
	private static final String GAME_NAME = "LodeRunner";
	/** Game splash screen */
	private GameSprite splashScreen = null;
	/** Message to display when game is paused */
	private String pauseMessage = null;
	/**
	 * Maximum number of lifes given to the player when starting the game
	 * (constant)
	 */
	private static final int MAX_LIFES = 5;
	/** Lifes left to the player */
	private int lives = MAX_LIFES;
	/** Level number of the current stage */
	public int level = 0;
	/** Current stage, when game is in progress */
	private LodeRunnerStage stage = null;

	private byte[] levelStatuses = new byte[LodeRunnerStage.MAX_LEVELS];

	private static final byte STATUS_DONE = 1;
	private static final byte STATUS_NOT_DONE = 0;
	private int spaceBetweenLines;
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
		InputStream tilesInputStream0 = this.context.getResources().openRawResource(R.raw.tiles12x11);
		InputStream tilesInputStream1 = this.context.getResources().openRawResource(R.raw.tiles4x4);
		InputStream[] tilesInputStreams = new InputStream[] { tilesInputStream0, tilesInputStream1 };
		stage = new LodeRunnerStage(fontInputStream, tilesInputStreams);
		stage.loadFromResource(openBinInputStream());
		recoverStatus();
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
		Font font = null;
		// There is a message to render
		if (pauseMessage != null) {
			font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD | Font.STYLE_ITALIC, Font.SIZE_LARGE);
			g.setFont(font);
			g.setColor(0x00ffffff);
			g.drawString(pauseMessage, w / 2, (h - font.getHeight()) / 2, Graphics.HCENTER | Graphics.TOP);
		} // No message but a splash screen
		else if (splashScreen != null) {
			if ((level / LodeRunnerStage.GAME_LEVELS) % 2 == 0) {
				// Lode Runner splash screen
				splashScreen.paint(g, 0, w / 2, h / 2, Graphics.HCENTER | Graphics.VCENTER);
			} else {
				// Championship splash screen
				splashScreen.paint(g, 0, w / 2, h / 2 - 6, Graphics.HCENTER | Graphics.VCENTER);
				splashScreen.paint(g, 1, w / 2, h / 2 + 6, Graphics.HCENTER | Graphics.VCENTER);
			}
		} // No message, no splash screen
		else {
			// Simulate a splash screen with text
			if ((level / LodeRunnerStage.GAME_LEVELS) % 2 == 0) {
				// Lode Runner splash screen
				font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);
				g.setFont(font);
				g.setColor(0x00ffffff);
				g.drawString("Lode Runner", w / 2, (h - font.getHeight()) / 2, Graphics.HCENTER | Graphics.TOP);
			} else {
				// Championship splash screen
				font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);
				g.setFont(font);
				g.setColor(0x00ffffff);
				g.drawString("Lode Runner", w / 2, (h - font.getHeight()) / 2 - 6, Graphics.HCENTER | Graphics.TOP);
				font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, Font.SIZE_LARGE);
				g.setFont(font);
				g.setColor(0x00ff0000);
				g.drawString("Championship", w / 2, (h - font.getHeight()) / 2 + 6, Graphics.HCENTER | Graphics.TOP);
			}
		}
	}

	boolean clearAfterPause = true;

	/** Render the game canvas */
	public void paint(Graphics g) {
		// Render the stage
		if (stage != null) {
			stage.spriteSize = LodeRunnerStage.SPRITE_NORMAL;
			stage.paint(g);
			if (isPaused || !stage.isLoaded) {
				clearAfterPause = true;

				// clean the stage
				w0 = g.getClipWidth();
				h0 = g.getClipHeight();
				g.setColor(0x000000);
				g.fillRect(0, 0, w0, h0);

				// Render the stage mini-map
				stage.spriteSize = LodeRunnerStage.SPRITE_SMALL;
				stage.paint(g);

				int cx = LodeRunnerStage.STAGE_WIDTH * LodeRunnerStage.SPRITE_WIDTH[stage.spriteSize];
				int cy = LodeRunnerStage.STAGE_HEIGHT * LodeRunnerStage.SPRITE_HEIGHT[stage.spriteSize];
				int x = (w0 - cx) / 2, y = (h0 - cy) / 2;
				if (x > 10) {
					x = 10;
				}

				// Render the message or splash screen
				paintMessage(g, w0, y);
				// Display game information
				Font font = Font.getDefaultFont();
				g.setFont(font);
				g.setColor(0x00ffffff);
				spaceBetweenLines = LodeRunnerStage.SPRITE_HEIGHT[LodeRunnerStage.SPRITE_NORMAL];
				if (spaceBetweenLines < font.getHeight()) {
					spaceBetweenLines = font.getHeight();
				}
				int startY = y + cy + 3;
				if (getHeight() - startY - 4 * spaceBetweenLines < 0) {
					g.setColor(0x00888888);
					for (int i = getHeight() - 4 * spaceBetweenLines; i < startY; i += 2) {
						g.drawLine(0, i, getWidth(), i);
					}
					startY = getHeight() - 4 * spaceBetweenLines;
					g.setColor(0x00ffffff);
				}

				// Display stage information
				if (stage.isLoaded) {
					if (levelStatuses[level] == STATUS_DONE) {
						paintCenter(g, "Done!", startY + spaceBetweenLines, w0);
					}
				} else {
					g.setColor(0x00ffff00);
					g.drawString("Loading...", x + cx / 2, y + (cy - font.getHeight()) / 2, Graphics.TOP
							| Graphics.HCENTER);
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
		levelInfo.setVilains(stage.vilains.size());
		levelInfo.setDone(levelStatuses[level] == STATUS_DONE);
		return levelInfo;
	}

	/**
	 * 
	 * @param newLevel
	 *            0-based level
	 */
	public void loadNewLevel(int newLevel) {
		pauseMessage = null;
		this.level = newLevel;
		try {
			this.stage.loadFromResource(openBinInputStream());
			updateLevelInfo();
		} catch (Exception e) {
			Log.e(LodeRunnerDrawingThread.class.getCanonicalName(), "Error loading level", e);
			throw new RuntimeException(e);
		}

	}

	private void paintCenter(Graphics g, String string, int y, int screenWidth) {

		int stringWidth = Font.getDefaultFont().stringWidth(string);
		int height = Font.getDefaultFont().getHeight();
		g.setColor(0x008800);
		g.fillRoundRect((screenWidth - stringWidth) / 2 - 2, y - 2, stringWidth + 4, height + 4, 8, 800);
		g.setColor(0xffffff);
		g.drawString(string, screenWidth / 2, y, Graphics.TOP | Graphics.HCENTER);
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
				if (stage.endCompleted || stage.endHeroDied) {
					Log.d(HeroHeartbeatTask.class.getCanonicalName(), "Hero died! endCompleted:" + stage.endCompleted
							+ " endHeroDied:" + stage.endHeroDied);
				}
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
	 * Define the vilains' heartBeat as a game event task.
	 */
	protected class VilainsHeartbeatTask extends RepaintTask {

		/** Scheduled every 2 frames */
		public final static int PERIOD = 2 * FRAMERATE_MILLISEC;

		/** Heartbeat */
		public void run() {
			// Loop on every vilain
			if (stage != null && stage.isLoaded) {
				for (LodeRunnerVilain lodeRunnerVilain : stage.vilains) {
					lodeRunnerVilain.heartBeat();
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

	public void nextLevelNotDone() {
		pauseMessage = null;
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
		if (nextLevelNotDone == -1) {
			pauseMessage = "All levels done!";
		} else {
			pauseMessage = null;
			loadNewLevel(nextLevelNotDone);
		}
	}

	/**
	 * Stage is complete, or hero has died
	 */
	public void stageOver(boolean hasCompleted) {
		if (!isPaused) {
			levelStatuses[level] = hasCompleted ? STATUS_DONE : STATUS_NOT_DONE;
			pause();
		}
		// Adjust lives and level

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
			updateLevelInfo();
		} catch (Exception e) {
		}
		needsRepaint = REPAINT_ALL;
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
		timer.schedule(new VilainsHeartbeatTask(), 0, VilainsHeartbeatTask.PERIOD);
		// Schedule the stage's heartBeat
		timer.schedule(new StageHeartbeatTask(), 0, StageHeartbeatTask.PERIOD);
		updateLevelInfo();
	}

	public void setLevelInfoChangedListener(final LevelInfoChangedListener levelInfoChangedListener) {
		this.levelInfoChangedListener = levelInfoChangedListener;
		this.stage.setLevelInfoChangedListener(new LevelInfoChangedListener() {
			@Override
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

}
