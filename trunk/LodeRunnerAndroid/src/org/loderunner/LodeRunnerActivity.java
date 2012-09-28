package org.loderunner;


import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class LodeRunnerActivity extends Activity {
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lode_runner);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_lode_runner, menu);
        return true;
    }
    
    public void onUp(View view) {
    	LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_CLIMB_UP);
    }
    
    public void onDown(View view) {
    	LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_CLIMB_DOWN);
    }
    
    public void onLeft(View view) {
    	LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_RUN_LEFT);
    }
    
    public void onRigth(View view) {
    	LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerCharacter.MOVE_RUN_RIGHT);
    }
    
    public void onFireLeft(View view) {
    	LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerHero.MOVE_DIG_LEFT);
    }
    
    public void onFireRigth(View view) {
    	LodeRunnerDrawingThread.getInstance().gameAction(LodeRunnerHero.MOVE_DIG_RIGHT);
    }
    
    
}
