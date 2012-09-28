package org.loderunner;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class Image {
	private final Bitmap bitmap;
	private final Graphics graphics;

	private Image(Bitmap bitmap) {
		this.bitmap = bitmap;
		this.graphics = new Graphics();
		if(bitmap.isMutable()){
			this.graphics.setCanvas(new Canvas(bitmap));
		}
	}

	public int getWidth() {
		return this.bitmap.getWidth();
	}

	public int getHeight() {
		return this.bitmap.getHeight();
	}

	public Graphics getGraphics() {
		return graphics;
	}

	public static Image createImage(InputStream inputStream) {
		Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
		return new Image(bitmap);
	}
	
	public static Image createImage(int width, int height) {
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888 );
		return new Image(bitmap);
	}

	public Bitmap getBitmap(int x, int y, int width, int height) {		
		return Bitmap.createBitmap(this.bitmap, x, y, width, height);
	}

	public Bitmap getBitmap() {
		return this.bitmap;
	}

}
