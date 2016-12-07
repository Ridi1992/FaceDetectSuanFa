package com.FRD.cn;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
/**
 * �������������ʾ��
 * @author �޷�
 * @datetime 2016-05-03
 */
public class DrawFaceRect extends View {
    private final static String TAG = "DrawFaceRect";
    private final static int len = 20;
    // ��������ɫ
    private int mcolorfill;
    // ͼƬ��С
    private int pWidth;
    private int pHeight;
    // ��ʾ�ؼ���С
    private int vWidth;
    private int vHeight;
    private float left;
    private float top;
    private float bottom;
    private float right;
    private float wRate;
    private float hRate;
    
    public DrawFaceRect(Context context, int color) {
        super(context);
        this.mcolorfill = color;
        this.pWidth = 0;
        this.pHeight = 0;
        this.vWidth = 0;
        this.vHeight = 0;
    }

    /**
     * ��������������
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setPosition(float left, float top, float right, float bottom) {
        this.wRate = vWidth;
        this.wRate = wRate / pWidth;
        this.hRate = vHeight;
        this.hRate = hRate / pHeight;

        this.left = wRate * left;
        this.right = wRate * right;
        this.top = hRate * top;
        this.bottom = hRate * bottom;
        this.invalidate();
    }

    /**
     *  ��������������
     * @param rect
     */
    public void setPosition(Rect rect) {
        this.wRate = vWidth;
        this.wRate = wRate / pWidth;
        this.hRate = vHeight;
        this.hRate = hRate / pHeight;

        this.left = wRate * rect.left;
        this.right = wRate * rect.right;
        this.top = hRate * rect.top;
        this.bottom = hRate * rect.bottom;
        this.invalidate();
    }
    
    /**
     * ����ͼƬ��ԭʼ��С
     * @param width
     * @param height
     */
    public void setViewSize(int width, int height) {
        this.vWidth = width;
        this.vHeight = height;
    }

    /**
     * ������ʾ�ؼ��Ĵ�С
     * @param width
     * @param height
     */
    public void setImageSize(int width, int height) {
        this.pWidth = width;
        this.pHeight = height;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
            Paint mpaint = new Paint();
            mpaint.setColor(mcolorfill);
            mpaint.setStyle(Paint.Style.STROKE);
            mpaint.setStrokeWidth(3.0f);
            // ��������
            //log("onDraw() left:" + left + " top:" + top + " right:" + right + " buttom:" + buttom);
            canvas.drawRect(new RectF(left, top, right, bottom), mpaint);
            /*
            canvas.drawLine(left, top, left + len, top, mpaint);
            canvas.drawLine(left, top, left, top + len, mpaint);
            canvas.drawLine(left, bottom, left + len, bottom, mpaint);
            canvas.drawLine(left, bottom, left , bottom - len, mpaint);
            canvas.drawLine(right, top, right + len, top, mpaint);
            canvas.drawLine(right, top, right, top + len, mpaint);
            canvas.drawLine(right, bottom, right + len, bottom, mpaint);
            canvas.drawLine(right, bottom, right, bottom - len, mpaint);
            */
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
