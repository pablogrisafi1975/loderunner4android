package com.androidegris.loderunner.midp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.SparseArray;

public class Graphics {

	public static final int HCENTER = 1;
	public static final int VCENTER = 2;
	public static final int LEFT = 4;
	public static final int RIGHT = 8;
	public static final int TOP = 16;
	public static final int BOTTOM = 32;

	private Canvas canvas;
	private Paint nextPaint;
	private SparseArray<Paint> paints = new SparseArray<Paint>();
	private float scale = 1.0f;
	private boolean paning = false;

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}

	public int getClipX() {
		return canvas.getClipBounds().left;
	}

	public int getClipY() {
		return canvas.getClipBounds().top;
	}

	public int getClipWidth() {
		return canvas.getClipBounds().right - canvas.getClipBounds().left;
	}

	public int getClipHeight() {
		return canvas.getClipBounds().bottom - canvas.getClipBounds().top;
	}

	/*
	 * g.drawRegion(frames, (frameNumber % framesCountX) * frameWidth,
	 * (frameNumber / framesCountX) * frameHeight, frameWidth, frameHeight, 0,
	 * x, y, Graphics.TOP | Graphics.LEFT);
	 */
	public void drawRegion(Image src, int x_src, int y_src, int width, int height, int transform, int x_dest,
			int y_dest, int anchor) {
		Bitmap bitmap = src.getBitmap(x_src, y_src, width, height);		
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int)(width * scale), (int)(height * scale), false);
		canvas.drawBitmap(scaledBitmap, x_dest * scale, y_dest * scale , null);
	}



	public void setColor(int color) {
		color += 0xff000000;
		nextPaint = findPaint(color);

	}

	private Paint findPaint(int color) {		
		Paint paintFound = paints.get(color);
		if (paintFound == null) {
			paintFound = new Paint();
			paintFound.setColor(color);
			paints.put(color, paintFound);
		}
		return paintFound;
	}

	public void drawLine(int startX, int startY, int stopX, int stopY) {
		nextPaint.setStyle(Style.STROKE);
		canvas.drawLine(startX, startY, stopX, stopY, nextPaint);
	}

	public void fillRect(int x, int y, int width, int height) {
		nextPaint.setStyle(Style.FILL);
		canvas.drawRect(x, y, width, height, nextPaint);

	}

	public void drawRect(int x, int y, int width, int height) {
		nextPaint.setStyle(Style.STROKE);
		canvas.drawRect(x, y, width, height, nextPaint);

	}
	
	public void translate(int x, int y) {
		if(paning){
			canvas.translate(x, y);
		}
	}	

	public int getTranslateX() {
		return 0;
	}

	public int getTranslateY() {
		return 0;
	}

	// g.drawImage(backgroundImage, 0, 0, Graphics.TOP | Graphics.LEFT);
	public void drawImage(Image img, int x, int y, int anchor) {
		Bitmap bitmap = img.getBitmap();
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth() * scale), (int)(bitmap.getHeight() * scale), false);
		canvas.drawBitmap(scaledBitmap, x , y , null);
	}


	public void drawString(String pauseMessage, int i, int j, int k) {
		// TODO Auto-generated method stub

	}

	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		nextPaint.setStyle(Style.FILL);
		canvas.drawRect(x, y, width, height, nextPaint);
	}

	public void setScale(float scale) {
		if(this.scale != scale){
			this.scale = scale;	
			this.canvas.scale(scale,  scale);
		}
	}
	
	
	public void setPaning(boolean paning){
		this.paning = paning;
	}

}
