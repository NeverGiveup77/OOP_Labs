package com.android.accelerometergraph;

import android.content.Context;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class GraphViewRenderer implements GLSurfaceView.Renderer
{
    public void GraphViewRenderer(Context context)
    {
        Context mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        AccelerometerGraphJNI.surfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        AccelerometerGraphJNI.surfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        AccelerometerGraphJNI.drawFrame();
    }
}
