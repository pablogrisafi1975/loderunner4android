package org.loderunner;

public class LevelInfo {
	/**
	 * 0 based number of level
	 */
	private int number;
	private int lifes;
	private int vilains;
	private int coinsTotal;
	private int coinsPicked;
	
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getLifes() {
		return lifes;
	}

	public void setLifes(int lives) {
		this.lifes = lives;
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

	@Override
	public String toString() {
		return "LevelInfo [number=" + number + ", lifes=" + lifes + ", vilains=" + vilains + ", coinsTotal="
				+ coinsTotal + ", coinsPicked=" + coinsPicked + "]";
	}
	
	
	
	
	
}
