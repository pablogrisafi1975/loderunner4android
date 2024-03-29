package com.androidegris.loderunner.swipe;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class SwipeDetector implements View.OnTouchListener {

	private static final String logTag = SwipeDetector.class.getCanonicalName();
	private static final long TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
	
	private SwipeListener swipeListener;	
	private float downX, downY, upX, upY;
	private int halfDrawingWidth;
	private long downT, upT;

	public SwipeDetector(SwipeListener swipeListener) {
		this.swipeListener = swipeListener;
	}

	public void onRightToLeftSwipe(View v) {
		Log.i(logTag, "onRightToLeftSwipe!");
		swipeListener.right2left(v);
	}

	public void onLeftToRightSwipe(View v) {
		Log.i(logTag, "onLeftToRightSwipe!");
		swipeListener.left2right(v);
	}

	public void onTopToBottomSwipe(View v) {
		Log.i(logTag, "onTopToBottomSwipe!");
		swipeListener.top2bottom(v);
	}

	public void onBottomToTopSwipe(View v) {
		Log.i(logTag, "onBottomToTopSwipe!");
		swipeListener.bottom2top(v);
	}
	
	public void onTapLeft(View v) {
		Log.i(logTag, "onTapLeft!");
		swipeListener.tapLeft(v);
	}	

	public void onTapRigth(View v) {
		Log.i(logTag, "onTapRigth!");
		swipeListener.tapRigth(v);
	}	
	
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			downX = event.getX();
			downY = event.getY();
			downT = event.getEventTime();
			return true;
		}
		case MotionEvent.ACTION_UP: {
			upX = event.getX();
			upY = event.getY();
			upT = event.getEventTime();

			float deltaX = downX - upX;
			float deltaY = downY - upY;
			long deltaT = upT - downT;
			Log.i(logTag, "deltaX:" + deltaX + " deltaY:" + deltaY + " deltaT:" + deltaT);
			
			if(deltaT < TAP_TIMEOUT){ //is a TAP
				if(upX > halfDrawingWidth){
					this.onTapRigth(v);
				}else{
					this.onTapLeft(v);
				}
				return true;
			}
			
			//its a swipe

			// swipe horizontal?
			if (Math.abs(deltaX) > Math.abs(deltaY) ) {
				// left or right
				if (deltaX < 0) {
					this.onLeftToRightSwipe(v);
					return true;
				}
				if (deltaX > 0) {
					this.onRightToLeftSwipe(v);
					return true;
				}
			}
			else{
				// top or down
				if (deltaY < 0) {
					this.onTopToBottomSwipe(v);
					return true;
				}
				if (deltaY > 0) {
					this.onBottomToTopSwipe(v);
					return true;
				}
			} 

		}
		}
		return false;
	}

	public void setDrawingWidth(int drawingWidth) {
		this.halfDrawingWidth = drawingWidth / 2;		
	}

}
