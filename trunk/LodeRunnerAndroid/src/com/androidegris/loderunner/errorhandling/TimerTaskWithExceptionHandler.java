package com.androidegris.loderunner.errorhandling;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.TimerTask;

public abstract class TimerTaskWithExceptionHandler extends TimerTask {
	private final UncaughtExceptionHandler handler;

	public TimerTaskWithExceptionHandler( UncaughtExceptionHandler handler) {
		if (handler == null) {
			throw new NullPointerException();
		}
		this.handler = handler;
	}

	@Override
	public void run() {
		try {
			runWrapped();
		} catch (Throwable exc) {
			handler.uncaughtException(Thread.currentThread(), exc);
		}
	}

	public abstract void runWrapped();



}
