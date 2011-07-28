package com.android.launcher;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class PokeDragIcon extends ImageView implements DragSource, DropTarget {

	public PokeDragIcon(Context context) {
		super(context);
	}

	public PokeDragIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PokeDragIcon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void onDropCompleted(View target, boolean success) {
		// TODO Auto-generated method stub
		System.out.println("I AM ON DROP COMPLETED");
	}

	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		//Accept all things by default
		System.out.println("I AM ACCEPTING YOUR DROP");
		return true;
	}

	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		// TODO Auto-generated method stub
		System.out.println("YOU ENTERED ME");
	}

	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		// TODO Auto-generated method stub

	}

	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		// TODO Auto-generated method stub

	}

	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		// TODO Auto-generated method stub
		System.out.println("I AM ON DROP");
		PokeDragIcon otherIcon = (PokeDragIcon)dragInfo;
		Drawable drawable = this.getDrawable();
		this.setBackgroundDrawable(otherIcon.getBackground());
		otherIcon.setBackgroundDrawable(drawable);

	}

}
