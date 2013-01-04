package com.androidegris.loderunner.errorhandling;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Process;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

public class ExceptionHandler implements UncaughtExceptionHandler {

    private final Activity myContext;

    public ExceptionHandler(Activity context) {
        myContext = context;
    }

    public void uncaughtException(Thread thread, Throwable exception) {
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        String message = stackTrace.toString();
        
		final AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
		builder.setMessage(message).setTitle("Crash and Burn!!!");		
		Log.e(ExceptionHandler.class.getCanonicalName(), message);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               Process.killProcess(Process.myPid());
	               System.exit(10);
	           }
	       });
		myContext.runOnUiThread(new Runnable() {			
			public void run() {
				builder.show();				
			}
		});
		


    }

}
