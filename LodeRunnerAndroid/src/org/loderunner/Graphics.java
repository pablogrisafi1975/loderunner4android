package org.loderunner;

import java.util.HashMap;
import java.util.Map;

import android.R.integer;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class Graphics {

	public static final int HCENTER = 1;
	public static final int VCENTER = 2;
	public static final int LEFT = 4;
	public static final int RIGHT = 8;
	public static final int TOP = 16;
	public static final int BOTTOM = 32;

	private Canvas canvas;
	private Paint nextPaint;
	private Map<Integer, Paint> paints = new HashMap<Integer, Paint>();

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
	public void drawRegion(Image src, int x_src, int y_src, int width,
			int height, int transform, int x_dest, int y_dest, int anchor) {
		Bitmap bitmap = src.getBitmap(x_src, y_src, width, height);
		canvas.drawBitmap(bitmap, x_dest, y_dest, null);

	}

	public void translate(int tx, int ty) {
		// TODO Auto-generated method stub

	}

	public void setColor(int color) {
		nextPaint = findPaint(color);

	}

	private Paint findPaint(Integer color) {
		if (paints.containsKey(color)) {
			return paints.get(color);
		}
		Paint paint = new Paint();
		paint.setColor(color);
		paints.put(color, paint);
		return paint;
	}

	public void drawLine(int i, int y, int x, int j) {
		// TODO Auto-generated method stub

	}

	public void fillRect(int x, int y, int width, int height) {
		nextPaint.setStyle(Style.FILL);
		canvas.drawRect(x, y, width, height, nextPaint);

	}

	public void drawRect(int x, int y, int width, int height) {
		nextPaint.setStyle(Style.STROKE);
		canvas.drawRect(x, y, width, height, nextPaint);

	}

	public int getTranslateX() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getTranslateY() {
		// TODO Auto-generated method stub
		return 0;
	}

	//g.drawImage(backgroundImage, 0, 0, Graphics.TOP | Graphics.LEFT);
	public void drawImage(Image img, int x, int y, int anchor) {
		canvas.drawBitmap(img.getBitmap(), x, y, null);

	}

	public void setFont(Font font) {
		// TODO Auto-generated method stub

	}

	public void drawString(String pauseMessage, int i, int j, int k) {
		// TODO Auto-generated method stub

	}

	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		nextPaint.setStyle(Style.FILL);
		canvas.drawRect(x, y, width, height, nextPaint);
	}

}
