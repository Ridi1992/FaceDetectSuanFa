package com.FRL.cn;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

public class FaceView extends android.widget.ImageView {
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mFace = null;
    private float _width = 480.0f;
    private float _height = 640.0f;

    public FaceView(Context c) {
        super(c);         
        init();               
    }             

    public FaceView(Context c, AttributeSet attrs) {
        super(c, attrs);
        init();
    }

    private void init() {                
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(3);
    }

    // set up detected face rectangle for display
    public void setDisplayRect(float left, float top, float right, float bottom, int width, int height) {
        mFace = new RectF(left, top, right, bottom);
        _width = width;
        _height = height;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas); 
        Log.e("FaceView", "onDraw()");
        if (mFace != null) {
            float ratio = Math.min(getWidth() / _width, getHeight() / _height);
            float xOffset = (getWidth() - ratio * _width) / 2.0f;
            float yOffset = (getHeight() - ratio * _height) / 2.0f;
            
            mFace.left *= ratio;
            mFace.top *= ratio;
            mFace.right *= ratio;
            mFace.bottom *= ratio;
            mFace.offset(xOffset, yOffset);
            
            canvas.drawRect(mFace, mPaint);
        }
    }

}
