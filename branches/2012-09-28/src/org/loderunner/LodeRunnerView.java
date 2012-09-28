package org.loderunner;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class LodeRunnerView extends SurfaceView implements Callback {

	

	private LodeRunnerDrawingThread drawingThread;

	public LodeRunnerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		drawingThread = new LodeRunnerDrawingThread(holder , context, this.getWidth(), this.getHeight());
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
		boolean retry = true;
		drawingThread.setRunning(false);
		while (retry) {
			try {
				drawingThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}

	

}
