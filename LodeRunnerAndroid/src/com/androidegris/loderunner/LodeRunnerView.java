package com.androidegris.loderunner;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class LodeRunnerView extends SurfaceView implements Callback {

	private LodeRunnerDrawingThread drawingThread;

	public LodeRunnerView(Context context, AttributeSet attrs, UncaughtExceptionHandler uncaughtExceptionHandler) {
		super(context, attrs);
		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		drawingThread = new LodeRunnerDrawingThread(holder , this.getContext(), this.getWidth(), this.getHeight());
		drawingThread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
		holder.addCallback(this);
		setFocusable(true);

	}

	public void surfaceCreated(SurfaceHolder holder) {
		drawingThread.setRunning(true);
		drawingThread.start();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(LodeRunnerView.class.getCanonicalName(), "surfaceDestroyed");
		boolean retry = true;
		drawingThread.setRunning(false);
		while (retry) {
			try {
				drawingThread.join();
				retry = false;
			} catch (InterruptedException e) {
				Log.e(LodeRunnerView.class.getCanonicalName(), "Error detroying surface", e);
			}
		}
	}

	public LodeRunnerDrawingThread getDrawingThread() {
		return drawingThread;
	}

	public void setScale(float scale) {
		drawingThread.setScale(scale);	
	}

	public void setPaning(boolean paning) {
		drawingThread.setPaning(paning);		
	}



	

}
