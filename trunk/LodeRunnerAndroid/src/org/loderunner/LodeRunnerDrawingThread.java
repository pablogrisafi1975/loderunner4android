package org.loderunner;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class LodeRunnerDrawingThread extends Thread {
	
	private Graphics g = new Graphics();
	private boolean running;
	private SurfaceHolder holder;
	
	/** Rendering frequency used by the animation thread */
	protected final static int FRAMERATE_MILLISEC = 66;
	/** "In pause" status of the game */
	protected volatile boolean isPaused = false;
	/** All game events are scheduled and sequenced by a single Timer thread */
	protected Timer timer = null;

	/** Rendering is done in a dedicated animation thread */
	private volatile Thread animationThread = null;
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
	private int lifes = MAX_LIFES;
	/** Level number of the current stage */
	public int level = 0;
	/** Current stage, when game is in progress */
	private LodeRunnerStage stage = null;
	private int newLevel;
	private byte[] levelStatuses = new byte[LodeRunnerStage.MAX_LEVELS];

	private static final byte STATUS_DONE = 1;
	private static final byte STATUS_NOT_DONE = 0;
	private static final int BG_COLOR_FOR_SOFTKEYS = 0x1E90FF;
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
		this.context = context;
		this.width = width;
		this.height = heigth;
		this.holder = holder;
		binInputStream = this.context.getResources().openRawResource(R.raw.loderunnerbin);
		InputStream fontInputStream = this.context.getResources().openRawResource(R.raw.font);
		InputStream tilesInputStream0 = this.context.getResources().openRawResource(R.raw.tiles12x11);
		InputStream tilesInputStream1 = this.context.getResources().openRawResource(R.raw.tiles4x4);
		InputStream[] tilesInputStreams = new InputStream[]{tilesInputStream0, tilesInputStream1} ;
		stage = new LodeRunnerStage(fontInputStream, tilesInputStreams);
		stage.loadFromResource(binInputStream);
		timer = new Timer();
        
		timer.schedule(new HeroHeartbeatTask(), 0, HeroHeartbeatTask.PERIOD);
        // Schedule the vilains' heartBeat
        timer.schedule(new VilainsHeartbeatTask(), 0, VilainsHeartbeatTask.PERIOD);
        // Schedule the stage's heartBeat
        timer.schedule(new StageHeartbeatTask(), 0, StageHeartbeatTask.PERIOD);
        instance = this;		
	}
	
	public static LodeRunnerDrawingThread getInstance(){
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
		
		//canvas.drawRGB(0, 0, 0);
		/*
		Paint paintBlack = new Paint();
		paintBlack.setARGB(255, 0, 0, 0);
		paintBlack.setStyle(Style.STROKE);
		canvas.drawRect(0, 0, 200, 100, paintBlack );
		*/
		g.setCanvas(canvas);
		stage.paint(g);
		/*
		InputStream inputStream = this.context.getResources().openRawResource(R.raw.tiles12x11);
		//esto lo dibuja en 
		Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
		canvas.drawBitmap(bitmap, 10,  10, null);
		*/
		
		
		/* seems like working
		InputStream inputStream = this.context.getResources().openRawResource(R.raw.tiles12x11);
		try {
			GameSprite gameSprite = new GameSprite(inputStream, 12, 11, 0, 0);
			gameSprite.paint(g, 30, 100, 50);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		/*
		InputStream inputStream = this.context.getResources().openRawResource(R.raw.font);
		
		try {
			GameFont gameFont = new GameFont(inputStream, 3, 5, "0123456789/");
			gameFont.paint(g, 5, 100, 50);
			gameFont.drawString(g, "987", 100, 70, Graphics.BOTTOM | Graphics.RIGHT);
			gameFont.drawString(g, "012", 100, 70, Graphics.BOTTOM | Graphics.LEFT);
			gameFont.drawString(g, "543", 100, 70, Graphics.TOP | Graphics.RIGHT);
			gameFont.drawString(g, "123", 100, 70, Graphics.TOP | Graphics.LEFT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} looks like workinf perfect!
			*/	

	}
	
	private void painTopMessage(Graphics g) {
		paintLeft(g, -1, "Level: " + format3(level + 1), 0);
		paintRight(g, -1, "0=Menu", 0);
	}

	/** Render the message or splash screen */
	private void paintMessage(Graphics g, int w, int h) {
		Font font = null;
		// There is a message to render
		if (pauseMessage != null) {
			font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD
					| Font.STYLE_ITALIC, Font.SIZE_LARGE);
			g.setFont(font);
			g.setColor(0x00ffffff);
			g.drawString(pauseMessage, w / 2, (h - font.getHeight()) / 2,
					Graphics.HCENTER | Graphics.TOP);
		} // No message but a splash screen
		else if (splashScreen != null) {
			if ((level / LodeRunnerStage.GAME_LEVELS) % 2 == 0) {
				// Lode Runner splash screen
				splashScreen.paint(g, 0, w / 2, h / 2, Graphics.HCENTER
						| Graphics.VCENTER);
			} else {
				// Championship splash screen
				splashScreen.paint(g, 0, w / 2, h / 2 - 6, Graphics.HCENTER
						| Graphics.VCENTER);
				splashScreen.paint(g, 1, w / 2, h / 2 + 6, Graphics.HCENTER
						| Graphics.VCENTER);
			}
		} // No message, no splash screen
		else {
			// Simulate a splash screen with text
			if ((level / LodeRunnerStage.GAME_LEVELS) % 2 == 0) {
				// Lode Runner splash screen
				font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD,
						Font.SIZE_LARGE);
				g.setFont(font);
				g.setColor(0x00ffffff);
				g.drawString("Lode Runner", w / 2, (h - font.getHeight()) / 2,
						Graphics.HCENTER | Graphics.TOP);
			} else {
				// Championship splash screen
				font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD,
						Font.SIZE_LARGE);
				g.setFont(font);
				g.setColor(0x00ffffff);
				g.drawString("Lode Runner", w / 2,
						(h - font.getHeight()) / 2 - 6, Graphics.HCENTER
								| Graphics.TOP);
				font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC,
						Font.SIZE_LARGE);
				g.setFont(font);
				g.setColor(0x00ff0000);
				g.drawString("Championship", w / 2,
						(h - font.getHeight()) / 2 + 6, Graphics.HCENTER
								| Graphics.TOP);
			}
		}
	}

	boolean clearAfterPause = false;
	private InputStream binInputStream;

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

				int cx = LodeRunnerStage.STAGE_WIDTH
						* LodeRunnerStage.SPRITE_WIDTH[stage.spriteSize];
				int cy = LodeRunnerStage.STAGE_HEIGHT
						* LodeRunnerStage.SPRITE_HEIGHT[stage.spriteSize];
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
				paintLeft(g, LodeRunnerStage.TILE_LADDER, "Level "
						+ format3(level + 1), startY);
				paintRight(g, LodeRunnerStage.TILE_HERO,
						"x" + Integer.toString(lifes), startY);

				// Display stage information
				if (stage.isLoaded) {
					paintLeft(
							g,
							LodeRunnerStage.TILE_CHEST,
							Integer.toString(stage.hero == null ? 0
									: stage.hero.nChests)
									+ "/"
									+ Integer.toString(stage.nChests), startY
									+ spaceBetweenLines);
					paintRight(g, LodeRunnerStage.TILE_MONK,
							"x" + Integer.toString(stage.vilains.size()),
							startY + spaceBetweenLines);
					if (levelStatuses[level] == STATUS_DONE) {
						paintCenter(g, "Done!", startY + spaceBetweenLines, w0);
					}
				} else {
					g.setColor(0x00ffff00);
					g.drawString("Loading...", x + cx / 2,
							y + (cy - font.getHeight()) / 2, Graphics.TOP
									| Graphics.HCENTER);
					g.setColor(0x001463af);
					g.drawString("� 2006 - Fabien GIGANTE", w0 / 2, h0 - 2,
							Graphics.HCENTER | Graphics.BOTTOM);
				}
				paintLeft(g, -1, "Fire=Play", startY + spaceBetweenLines * 2);
				paintRight(g, -1, "#=Exit", startY + spaceBetweenLines * 2);
				if (getWidth() > 160) {
					paintSoftLeft(g, -1, "Next Level");
					paintSoftRight(g, -1, "Suicide");
				} else {
					paintSoftLeft(g, -1, "Next");
					paintSoftRight(g, -1, "Suic.");
				}

			} else {
				if (clearAfterPause) {
					clearAfterPause = false;
					g.setColor(0x000000);
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(0x00ffffff);
					painTopMessage(g);
					paintSoftMenu(g);
				}
				if (stage != null && stage.isMessageAtTop()
						&& getHeight() >= 208 && getHeight() < 320) {
					g.setColor(0x00ffffff);
					painTopMessage(g);
					paintSoftMenu(g);

				}
			}
		}

	}

	private void paintSoftMenu(Graphics g) {
		paintSoftLeft(g, -1, "<= Fire ");
		paintSoftRight(g, -1, "Fire =>");
	}

	private void loadNewLevel() {
		pauseMessage = null;
		this.level = newLevel - 1;
		try {
			this.stage.loadFromResource(binInputStream);
		} catch (Exception e) {
		}

	}

	private void paintSoftLeft(Graphics g, int tileIndex, String string) {
		g.setColor(BG_COLOR_FOR_SOFTKEYS);
		g.fillRect(0, h0 - spaceBetweenLines, w0 / 2 - 1, spaceBetweenLines);
		g.setColor(0xffffff);
		paintLeft(g, tileIndex, string, h0 - spaceBetweenLines);
	}

	private void paintSoftRight(Graphics g, int tileIndex, String string) {
		g.setColor(BG_COLOR_FOR_SOFTKEYS);
		g.fillRect(w0 / 2 + 1, h0 - spaceBetweenLines, w0 / 2 + 1,
				spaceBetweenLines);
		g.setColor(0xffffff);
		paintRight(g, tileIndex, string, h0 - spaceBetweenLines);
	}

	private void paintLeft(Graphics g, int tileIndex, String string, int y) {
		if (tileIndex >= 0) {
			int ySprite = y
					+ (Font.getDefaultFont().getHeight() - LodeRunnerStage.SPRITE_HEIGHT[LodeRunnerStage.SPRITE_NORMAL])
					/ 2;
			stage.sprites[LodeRunnerStage.SPRITE_NORMAL].paint(g,
					LodeRunnerStage.spriteMap[tileIndex], 4, ySprite);
			g.drawString(
					string,
					5 + LodeRunnerStage.SPRITE_WIDTH[LodeRunnerStage.SPRITE_NORMAL],
					y, Graphics.TOP | Graphics.LEFT);
		} else {
			g.drawString(string, 5, y, Graphics.TOP | Graphics.LEFT);
		}
	}

	private void paintRight(Graphics g, int tileIndex, String string, int y) {
		if (w0 == 0) {
			w0 = getWidth();
		}

		paintRight(g, tileIndex, string, y, w0);
	}

	private void paintRight(Graphics g, int tileIndex, String string, int y,
			int screenWidth) {
		if (tileIndex >= 0) {
			int ySprite = y
					+ (Font.getDefaultFont().getHeight() - LodeRunnerStage.SPRITE_HEIGHT[LodeRunnerStage.SPRITE_NORMAL])
					/ 2;
			g.drawString(string, screenWidth - 4, y, Graphics.TOP
					| Graphics.RIGHT);
			int textWidth = Font.getDefaultFont().stringWidth(string);
			stage.sprites[LodeRunnerStage.SPRITE_NORMAL]
					.paint(g,
							LodeRunnerStage.spriteMap[tileIndex],
							screenWidth
									- 4
									- LodeRunnerStage.SPRITE_WIDTH[LodeRunnerStage.SPRITE_NORMAL]
									- textWidth, ySprite);
		} else {
			g.drawString(string, screenWidth - 4, y, Graphics.TOP
					| Graphics.RIGHT);
		}
	}

	private void paintCenter(Graphics g, String string, int y, int screenWidth) {

		int stringWidth = Font.getDefaultFont().stringWidth(string);
		int height = Font.getDefaultFont().getHeight();
		g.setColor(0x008800);
		g.fillRoundRect((screenWidth - stringWidth) / 2 - 2, y - 2,
				stringWidth + 4, height + 4, 8, 800);
		g.setColor(0xffffff);
		g.drawString(string, screenWidth / 2, y, Graphics.TOP
				| Graphics.HCENTER);
	}

	private String format3(int number) {
		if (number < 10) {
			return "00" + number;
		}
		if (number < 100) {
			return "0" + number;
		}
		return Integer.toString(number);
	}

	private void clearDoneLevels() {
		/*
		 * Alert alertClear = new Alert("Clear solved levels",
		 * "Are you sure you want to clear all solved levels?", null,
		 * AlertType.CONFIRMATION); final Command commandYes = new
		 * Command("Yes", Command.OK, 1); final Command commandNo = new
		 * Command("No", Command.CANCEL, 2); alertClear.addCommand(commandNo);
		 * alertClear.addCommand(commandYes); final Displayable
		 * currenDisplayable = Display.getDisplay(midlet).getCurrent();
		 * alertClear.setCommandListener(new CommandListener() {
		 * 
		 * public void commandAction(Command c, Displayable d) { if (c ==
		 * commandYes) { for (int i = 0; i < levelStatuses.length; i++) {
		 * levelStatuses[i] = STATUS_NOT_DONE; } try { saveToStore(GAME_NAME); }
		 * catch (IOException ex) { ex.printStackTrace(); } catch
		 * (RecordStoreException ex) { ex.printStackTrace(); } }
		 * Display.getDisplay(midlet).setCurrent(currenDisplayable); } });
		 * 
		 * Display.getDisplay(midlet).setCurrent(alertClear);
		 */
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
			newLevel = nextLevelNotDone + 1;
			loadNewLevel();
		}
	}

	/**
	 * Stage is complete, or hero has died
	 */
	public void stageOver(boolean hasCompleted) {
		if (!isPaused) {
			levelStatuses[level] = hasCompleted ? STATUS_DONE : STATUS_NOT_DONE;
			// pause();
		}
		// Adjust lifes and level

		if (hasCompleted) {
			level++;
			if (level == LodeRunnerStage.MAX_LEVELS) {
				level = 0;
			}
			pauseMessage = (level % LodeRunnerStage.GAME_LEVELS == 0) ? null
					: "Congratulations !";
		} else {
			lifes--;
			if (lifes < 0) {
				lifes = MAX_LIFES;
				level = 0;
				pauseMessage = "Game Over";
			} else {
				pauseMessage = "Try again...";
			}
		}
		// Load appropriate stage
		try {
			stage.loadFromResource(binInputStream);
		} catch (Exception e) {
		}
		needsRepaint = REPAINT_ALL;
	}

	public void gameAction(int actionCode) {
		stage.hero.requestMove(actionCode);
		
	}	

}
