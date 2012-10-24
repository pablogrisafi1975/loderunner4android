package com.androidegris.loderunner;

/* Copyright � 2006 - Fabien GIGANTE */

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.androidegris.loderunner.listeners.LevelInfoChangedListener;
import com.androidegris.loderunner.midp.Graphics;
import com.androidegris.loderunner.midp.Image;

import android.util.Log;


/**
 * A Lode Runner stage or level is composed of
 * - a tiles array describing the stage landscape
 * - a Lode Runner hero and an array of Lode Runner villains
 */
public class LodeRunnerStage {

    /** Number of levels per game */
    public static final int GAME_LEVELS = 150;
    /** Maximum number of levels  (for both Lode Runner and Championship) */
    public static final int MAX_LEVELS = 2 * GAME_LEVELS;
    /** Stage width in tiles */
    public static final int STAGE_WIDTH = 28;
    /** Stage height in tiles */
    public static final int STAGE_HEIGHT = 16;
    /** Tile/sprite width in pixels */
    public static final int SPRITE_WIDTH = 12;
    /** Tile/sprite height in pixels */
    public static final int SPRITE_HEIGHT = 11;
    
    public static final int STAGE_WIDTH_PIXELS = STAGE_WIDTH * SPRITE_WIDTH;
    public static final int STAGE_HEIGHT_PIXELS = STAGE_HEIGHT * SPRITE_HEIGHT;
    
    /** Core tile type constant for void/empty tile */
    public static final int TILE_VOID = 0;
    /** Core tile type constant for diggable brick tile */
    public static final int TILE_BRICK = 1;
    /** Core tile type constant for non diggable solid/concrete tile */
    public static final int TILE_CONCRETE = 2;
    /** Core tile type constant for permanent ladder tile */
    public static final int TILE_LADDER = 3;
    /** Core tile type constant for rope tile */
    public static final int TILE_ROPE = 4;
    /** Core tile type constant for trap brick tile */
    public static final int TILE_TRAP = 5;
    /** Core tile type constant for exit ladder tile*/
    public static final int TILE_EXIT = 6;
    /** Core tile type constant for chest tile */
    public static final int TILE_CHEST = 7;
    /** Core tile type constant for villain initial position (only valid when loading from resource file) */
    public static final int TILE_MONK = 8;
    /** Core tile type constant for hero initial position (only valid when loading from resource file) */
    public static final int TILE_HERO = 9;
    /** Volatile tile type constant used for tiles out of stage boundaries */
    public static final int TILE_OUTSIDE = 10;
    /** Volatile tile type constant used for brick just being dug (still considered full) */
    public static final int TILE_HOLE_FULL = 11;
    /** Volatile tile type constant used for brick completely dug (considered empty, can trap villains) */
    public static final int TILE_HOLE_EMPTY = 12;
    /** Tiles array describing the stage landscape. Values are tile type TILE_* constants. */
    private int[] tiles = new int[STAGE_WIDTH * STAGE_HEIGHT];
    /** Lode Runner sprites (for both tiles & characters) */
    public GameSprite sprites;
    /** Lode Runner small sprite font */
    public GameFont font = null;
    /** Mapping table from tile type to sprite index */
    public static final int[] spriteMap = { /*Core*/14, 15, 12, 16, 17, 18, 19, 20, 21, 0, /*Volatile*/ 13, 75, 74};
    /** Sprite size constant for normal stage rendering */
    public static final int SPRITE_NORMAL = 0;
    /** Sprite size constant for small stage overview rendering */
    public static final int SPRITE_SMALL = 1;
    /** Lode Runner game hero */
    public volatile LodeRunnerHero hero = null;
    /** Lode Runner game vilains. Vector of LodeRunnerVilain elements. */
    public List<LodeRunnerVillain> villains = null;
    /** Lode Runner holes in this stage. */
    public List<LodeRunnerHole> holes = null;
    /** Current random number generator for the stage */
    public GameRandom random = new GameRandom();
    /** Total number of chests to be collected in this stage */
    public int nChests = 0;
    /** Flag set if the exit of this stage is enabled */
    public boolean exitEnabled = false;
    /** Flag set if the hero has died and this stage is over */
    public boolean endHeroDied = false;
    /** Flag set if the hero has successfully completed this stage */
    public boolean endCompleted = false;
    /** Stage background pixel image */
    private Image backgroundImage = null;
    private List<Integer> backgroundTilesToRepaint = null;
    /** Game canvas using this stage*/
    //private LodeRunnerView canvas = null;
    /** Stage loading state */
    public boolean isLoaded = false;
    /** Stage loading is done in a separated thread */
    private Thread loadingThread = null;
	private LevelInfoChangedListener levelInfoChangedListener;
	private LodeRunnerDrawingThread drawingThread;

    /** Initialize an empty stage. Load the sprites resources. */
    public LodeRunnerStage(InputStream fontInputStream, InputStream tilesInputStream, LodeRunnerDrawingThread drawingThread) {
        this.drawingThread = drawingThread;
        try {
            // Load game resource images (font and sprites)
            font = new GameFont(fontInputStream, 3, 5, "0123456789/");
            sprites = new GameSprite(tilesInputStream, SPRITE_WIDTH, SPRITE_HEIGHT, 0, 0);
            // If enough memory, use a background image to speed up normal stage rendering
            backgroundImage = Image.createImage(STAGE_WIDTH_PIXELS, STAGE_HEIGHT_PIXELS);
            backgroundTilesToRepaint = Collections.synchronizedList(new ArrayList<Integer>());
        } catch (Exception e) {
            Log.e(LodeRunnerStage.class.getCanonicalName(), "Inicialization error", e);
            throw new Error(e);
        }
    }

    /** Loading thread for asynchronous stage building */
    private class LoadingThread extends Thread {

    	private final InputStream inputStreamBin;
    	private final int level;
    	
        public LoadingThread(InputStream inputStreamBin, int level) {
			this.inputStreamBin = inputStreamBin;
			this.level = level;
		}

		

		/** Entry point of this asynchronous loading thread */
        public void run() {
            try {
                // In the original Apple II version, the levels can be found at offset 0x3000-0xc600
                // (aligned on 0x100 bytes blocks), in "loderunner.dsk" and "ch_lode.dsk"
                // "LodeRunner.bin" contains the usefull extracts of those two files
                // (without the alignment to save more space in the jar archive)
                // Each tile is encoded on 4 bits
                DataInput input = new DataInputStream(inputStreamBin);
                // Read level's buffer
                int level = this.level % MAX_LEVELS;//  canvas.level % MAX_LEVELS;
                byte[] buffer = new byte[STAGE_WIDTH * STAGE_HEIGHT / 2];
                input.skipBytes(buffer.length * level);
                input.readFully(buffer);
                // Decode tiles from buffer content
                for (int i = 0; i < STAGE_WIDTH * STAGE_HEIGHT; i++) {
                    // Exit if thread is canceled
                    if (isLoaded) {
                        break;
                    }
                    // Decode next tile
                    int tile = (i % 2 == 0) ? buffer[i / 2] & 0xf : (buffer[i / 2] >> 4) & 0xf;
                    switch (tile) {
                        // Create hero at his starting point
                        case TILE_HERO:
                            hero = new LodeRunnerHero(LodeRunnerStage.this);
                            hero.moveToTile(i);
                            tile = TILE_VOID;
                            break;
                        // Add villains at their initial positions
                        case TILE_MONK:
                            LodeRunnerVillain vilain = new LodeRunnerVillain(LodeRunnerStage.this);
                            vilain.moveToTile(i);
                            villains.add(vilain);
                            tile = TILE_VOID;
                            break;
                        // Count number of chests
                        case TILE_CHEST:
                            nChests++;
                            break;
                    }
                    tiles[i] = tile;
                    // Track tiles to repaint
                    if (backgroundTilesToRepaint != null) {
                        backgroundTilesToRepaint.add(i);
                    }
                    // Periodically yield to other threads
                    if (i % STAGE_WIDTH == 0) {
                        yield();
                    }
                }
                // The show can go on...
                exitEnabled = (nChests == 0);
                isLoaded = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally{
            	try {
					inputStreamBin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
            loadingThread = null;
            drawingThread.needsRepaint =  LodeRunnerDrawingThread.REPAINT_ALL; 
            updateLevelInfo();
        }
    }

    /** Load a stage from a given level in the levels resource file */
    public void loadFromResource(InputStream binInputStream, int level) {
        // Abort previous loading attempt
        while (loadingThread != null) {
            isLoaded = true;
        }
        // Reset members
        isLoaded = false;
        hero = null;
        villains = Collections.synchronizedList (new ArrayList<LodeRunnerVillain>());
        holes = Collections.synchronizedList (new ArrayList<LodeRunnerHole>());
        nChests = 0;
        exitEnabled = false;
        endHeroDied = false;
        endCompleted = false;
        if (backgroundTilesToRepaint != null) {
            backgroundTilesToRepaint.clear();
        }
        // Asynchronously load the stage
        loadingThread = new LoadingThread(binInputStream, level);
        loadingThread.start();        
    }

    /** Get tile array index from x and y position of the tile. */
    public static int getTileIndex(int xTile, int yTile) {
        // Tiles are stored vertically (as in original game resource file format)
        return yTile * STAGE_WIDTH + xTile;
    }

    /** Get the tile type at a given position (raw value from the array)  */
    public int getTile(int xTile, int yTile) {
        if (xTile < 0 || xTile >= STAGE_WIDTH || yTile < 0 || yTile >= STAGE_HEIGHT) {
            return TILE_OUTSIDE;
        }
        return tiles[getTileIndex(xTile, yTile)];
    }

    /**
     * Get the tile behavior at a given position.
     * A tile behavior is computed based on the tile type as follows:
     * - TILE_CHEST and TILE_HOLE_EMPTY have a TILE_VOID behavior
     * - TILE_OUTSIDE and TILE_HOLE_FULL have a TILE_CONCRETE behavior
     * - TILE_EXIT has either a TILE_LADDER (stage completed) or TILE_VOID (otherwise) behavior
     */
    public int getTileBehavior(int xTile, int yTile) {
        int tile = getTile(xTile, yTile);
        if (tile == TILE_CHEST || tile == TILE_MONK || tile == TILE_HERO || tile == TILE_HOLE_EMPTY) {
            tile = TILE_VOID;
        } else if (tile == TILE_OUTSIDE || tile == TILE_HOLE_FULL) {
            tile = TILE_CONCRETE;
        } else if (tile == TILE_EXIT) {
            tile = exitEnabled ? TILE_LADDER : TILE_VOID;
        }
        return tile;
    }

    /**
     * Get the tile appearance at a given position.
     * A tile appearance is computed based on the tile type as follows:
     * - TILE_TRAP has a TILE_BRICK appearance
     * - TILE_OUTSIDE, TILE_HOLE_EMPTY and TILE_HOLE_FULL have a TILE_VOID appearance
     * - TILE_EXIT has either a TILE_LADDER (stage completed) or TILE_VOID (otherwise) appearance
     */
    public int getTileAppearance(int xTile, int yTile) {
        int tile = getTile(xTile, yTile);
        if (tile == TILE_TRAP) {
            tile = TILE_BRICK;
        } else if (tile == TILE_MONK || tile == TILE_HERO || tile == TILE_OUTSIDE || tile == TILE_HOLE_FULL || tile == TILE_HOLE_EMPTY) {
            tile = TILE_VOID;
        } else if (tile == TILE_EXIT) {
            tile = exitEnabled ? TILE_LADDER : TILE_VOID;
        }
        return tile;
    }

    /** Set the tile type at a given position */
    public void setTile(int xTile, int yTile, int type) {
        if (xTile < 0 || xTile >= STAGE_WIDTH || yTile < 0 || yTile >= STAGE_HEIGHT) {
            return;
        }
        tiles[getTileIndex(xTile, yTile)] = type;
        // Background image (if any) is no more up to date
        if (backgroundTilesToRepaint != null) {
            backgroundTilesToRepaint.add(getTileIndex(xTile, yTile));
        }
        // Keep track of dug holes (for delayed refill)
        if (type == TILE_HOLE_EMPTY) {
            holes.add(new LodeRunnerHole(this, xTile, yTile));
        }
    }

    /** Check if the given tile is occupied by a villain */
    private boolean isVilainAt(int xTile, int yTile, boolean includeRespawning) {
        for (LodeRunnerVillain vilain :villains) {
            if (vilain.xTile == xTile && vilain.yTile == yTile && (includeRespawning || vilain.currentMove != LodeRunnerVillain.MOVE_RESPAWN)) {
                return true;
            }
        }
        return false;
    }

    /** Check if the given tile is occupied by a living villain */
    public boolean isVilainAt(int xTile, int yTile) {
        return isVilainAt(xTile, yTile, false);
    }

    /** Randomly computes a tile index suitable for respawning a villain */
    public int computeRandomRespawnPoint() {
        // Compute possible respawn points (not on top row)
        List<Integer> possiblePoints = new ArrayList<Integer>();
        for (int y = 1; y < STAGE_HEIGHT; y++) {
            for (int x = 0; x < STAGE_WIDTH; x++) {
                if (getTile(x, y) == TILE_VOID && !isVilainAt(x, y, true)) {
                    possiblePoints.add(getTileIndex(x, y));
                }
            }
            if (!possiblePoints.isEmpty()) {
                break;
            }
        }
        // Return a random possible position
        return possiblePoints.get(random.nextInt(possiblePoints.size()));
    }

    /** Enables the exit for this stage */
    public void enableExit() {
        if (!exitEnabled) {
            exitEnabled = true;
            if (backgroundTilesToRepaint != null) {
                for (int i = 0; i < STAGE_WIDTH * STAGE_HEIGHT; i++) {
                    if (tiles[i] == TILE_EXIT) {
                        backgroundTilesToRepaint.add(i);
                    }
                }
            }
        }
    }

    /** Renders the stage's tiles */
    public void paintTiles(Graphics g) {
        boolean screenCleared = backgroundImage == null;
        for (int x = 0; x < STAGE_WIDTH; x++) {
            for (int y = 0; y < STAGE_HEIGHT; y++) {
                // Tiles are drawn according to their appearance
                int tile = getTileAppearance(x, y);
                // If screen has been cleared, empty tiles can be skipped
                if (screenCleared && tile != TILE_VOID) {
                    sprites.paint(g, spriteMap[tile], x * SPRITE_WIDTH, y * SPRITE_HEIGHT);
                }
            }
        }
    }

    /** Renders only the tiles that need repainting */
    public void repaintBackgroundTiles() {
        if (backgroundImage == null) {
            return;
        }
        Graphics g = backgroundImage.getGraphics();
        // Loop on every tile that needs repainting
        for (Integer tileIndex : backgroundTilesToRepaint.toArray(new Integer[backgroundTilesToRepaint.size()])) {
            int xTile = tileIndex % LodeRunnerStage.STAGE_WIDTH;
            int yTile = tileIndex / LodeRunnerStage.STAGE_WIDTH;
            // Tiles are drawn according to their appearance
            int tileAppearance = getTileAppearance(xTile, yTile);
            sprites.paint(g, spriteMap[tileAppearance], xTile * SPRITE_WIDTH, yTile * SPRITE_HEIGHT);
        }
        backgroundTilesToRepaint.clear();
    }

    /** Render the stage's sprites (hero and vilains) */
    public void paintSprites(Graphics g) {
        for (LodeRunnerHole lodeRunnerHole : holes.toArray(new LodeRunnerHole[holes.size()])) {
        	lodeRunnerHole.paint(g);
        }
        for (LodeRunnerVillain lodeRunnerVilain : villains.toArray(new LodeRunnerVillain[villains.size()])) {
        	lodeRunnerVilain.paint(g);
        }
        if (hero != null) {
            hero.paint(g);
        }
    }

    /** Translate to center the screen */
    public void centerScreen(Graphics g) {
        int w0 = g.getClipWidth(), h0 = g.getClipHeight();
        int tx = 0, ty = 0;
        if (isLoaded && hero != null) {
            // Compute screen translation, based on hero's position
            if (w0 >= STAGE_WIDTH_PIXELS) {
                tx = (w0 - STAGE_WIDTH_PIXELS) / 2;
            } else if (hero.getCenterX() < w0 / 2) {
                tx = 0;
            } else if (hero.getCenterX() > STAGE_WIDTH_PIXELS - w0 / 2) {
                tx = w0 - STAGE_WIDTH_PIXELS;
            } else {
                tx = w0 / 2 - hero.getCenterX();
            }
            if (h0 >= STAGE_HEIGHT_PIXELS) {
                ty = (h0 - STAGE_HEIGHT_PIXELS) / 2;
            } else if (hero.getCenterY() < h0 / 2) {
                ty = 0;
            } else if (hero.getCenterY() > STAGE_HEIGHT_PIXELS - h0 / 2) {
                ty = h0 - STAGE_HEIGHT_PIXELS;
            } else {
                ty = h0 / 2 - hero.getCenterY();
            }
        } else {
            // Center stage
            tx = (w0 - STAGE_WIDTH_PIXELS) / 2;
            ty = (h0 - STAGE_HEIGHT_PIXELS) / 2;
        }
        g.translate(tx, ty);
    }

    boolean isMessageAtTop() {
        return hero != null && hero.getY() <= 16 && hero.wasShowingMessage();
    }

    /** Render the stage */
    public void paint(Graphics g) {
        // Prepare the screen (clear, center)
        g.setColor(0);
        if (!isLoaded || backgroundImage == null) {
            g.fillRect(0, 0, g.getClipWidth(), g.getClipHeight());
        }
        centerScreen(g);
        // deleting the message when hero is at the top
        if (isMessageAtTop()) {
            int blockSize = SPRITE_WIDTH ;
            g.fillRect(hero.getCenterX() - blockSize, hero.getY() - blockSize, 2 * blockSize, blockSize);
        }

        if (isLoaded) {
            // Paint tiles, using background Image if available
            if (backgroundImage != null) {
                repaintBackgroundTiles();
                g.drawImage(backgroundImage, 0, 0, Graphics.TOP | Graphics.LEFT);
            } else {
                paintTiles(g);
            }
            // Paint sprites
            paintSprites(g);
        }
        // Revert translation
        g.translate(-g.getTranslateX(), -g.getTranslateY());
    }

	public void setLevelInfoChangedListener(LevelInfoChangedListener levelInfoChangedListener) {
		this.levelInfoChangedListener = levelInfoChangedListener;		
	}

	public void updateLevelInfo() {
		if(this.levelInfoChangedListener != null){
			LevelInfo levelInfo = new LevelInfo();
			//this fires the listener in drawing thread who collects the actual info
			this.levelInfoChangedListener.levelInfoChanged(levelInfo);
		}
		
	}
}
