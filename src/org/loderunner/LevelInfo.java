package org.loderunner;

public class LevelInfo {
	/**
	 * 0 based number of level
	 */
	private int number;
	private int lives;
	private int vilains;
	private int coinsTotal;
	private int coinsPicked;
	private boolean done;
	
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getLives() {
		return lives;
	}

	public void setLives(int lives) {
		this.lives = lives;
	}

	public int getVilains() {
		return vilains;
	}

	public void setVilains(int vilains) {
		this.vilains = vilains;
	}

	public int getCoinsTotal() {
		return coinsTotal;
	}

	public void setCoinsTotal(int coinsTotal) {
		this.coinsTotal = coinsTotal;
	}

	public int getCoinsPicked() {
		return coinsPicked;
	}

	public void setCoinsPicked(int coinsPicked) {
		this.coinsPicked = coinsPicked;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	@Override
	public String toString() {
		return "LevelInfo [number=" + number + ", lives=" + lives + ", vilains=" + vilains + ", coinsTotal="
				+ coinsTotal + ", coinsPicked=" + coinsPicked + ", done=" + done + "]";
	}


	
	
	
	
}
