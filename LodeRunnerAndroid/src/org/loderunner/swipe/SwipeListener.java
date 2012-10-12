package org.loderunner.swipe;

import android.view.View;

public interface SwipeListener {

	void bottom2top(View v);

	void left2right(View v);

	void right2left(View v);

	void top2bottom(View v);

	void tap(View v);

}
