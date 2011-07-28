package com.android.launcher;

import android.view.View;

public class DragSourceTarget implements DragSource {

	public void onDropCompleted(View target, boolean success) {
        // This is a bit expensive but safe
		System.out.println("I WORK");
	}	
}
