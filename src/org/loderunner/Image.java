package org.loderunner;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Image {
	private final Bitmap bitmap;

	public Image(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public int getWidth() {
		return this.bitmap.getWidth();
	}

	public int getHeight() {
		return this.bitmap.getHeight();
	}

	public Graphics getGraphics() {
		// TODO Auto-generated method stub
		return null;
	}

	public static Image createImage(InputStream inputStream) {
		Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
		return new Image(bitmap);
	}
	
	public static Image createImage(int i, int j) {
		return null;
	}

	public Bitmap getBitmap(int x, int y, int width, int height) {		
		return Bitmap.createBitmap(this.bitmap, x, y, width, height);
	}

	public Bitmap getBitmap() {
		return this.bitmap;
	}

}
