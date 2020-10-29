package com.android.accelerometergraph;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class myGLSurfaceView extends GLSurfaceView {
     //programmatic instantiation--
    public myGLSurfaceView(Context context)
        {
            this(context, null);
        }

        //XML inflation/instantiation
    public myGLSurfaceView(Context context, AttributeSet attrs)
        {
            this(context, attrs, 0);
        }

    public myGLSurfaceView(Context context, AttributeSet attrs, int defStyle)
        {
            super(context, attrs);

            // Tell EGL to use a ES 2.0 Context
            setEGLContextClientVersion(2);

        }
}
