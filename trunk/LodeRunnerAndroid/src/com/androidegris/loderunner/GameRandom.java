package com.androidegris.loderunner;

/* Copyright � 2006 - Fabien GIGANTE */

import java.util.*;

/**
 * Extended version of the standard Random class
 */
@SuppressWarnings("serial")
class GameRandom extends Random {


	/** Returns true if the next pseudorandom between 0 and n-1 egals 0 */
    public boolean nextBoolean(int n) {
        return nextInt(n) == 0;
    }

    /** Returns true if the next pseudorandom between 0 and 1 egals 0 */
    public boolean nextBoolean() {
        return nextBoolean(2);
    }
}
