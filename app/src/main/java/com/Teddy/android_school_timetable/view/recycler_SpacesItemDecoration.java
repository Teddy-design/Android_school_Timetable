package com.Teddy.android_school_timetable.view;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class recycler_SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public recycler_SpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
        //outRect.bottom = space;

        if (parent.getChildPosition(view) == 0)
            outRect.top = space/2;
        outRect.top = space;
    }
}
