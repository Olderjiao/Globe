package com.guanwei.globe.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.guanwei.globe.data.PoiEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MyGlobeViewGroup extends FrameLayout {

    /**
     * 画笔
     */
    private Paint paint;

    /**
     * 是否开始画
     */
    private boolean isDraw = false;

    private float mX;
    private float mY;

    //存储坐标的集合
    private ArrayList<PoiEntity> poiList = new ArrayList<>();

    public void setDraw(boolean draw) {
        isDraw = draw;
    }

    /**
     * 获取做标集合
     */
    public ArrayList<PoiEntity> getPoiList() {
        return poiList;
    }

    /**
     * 清楚面板
     */
    public void cleanDraw() {
        if (poiList.size() > 0) {
            poiList.clear();
            invalidate();
        }
    }

    public MyGlobeViewGroup(@NonNull Context context) {
        super(context);
        initPaint();
    }

    public MyGlobeViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public MyGlobeViewGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint() {
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setAntiAlias(true);
        paint.setAlpha(155);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isDraw;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDraw) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //获取点击位置
                    mX = event.getX();
                    mY = event.getY();
                    poiList.add(new PoiEntity(mX, mY));
                    break;
            }
        }
        invalidate();
        return isDraw;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Path path = new Path();
        for (int i = 0; i < poiList.size(); i++) {
            paint.setColor(Color.GREEN);
            //先绘制圆点
            canvas.drawCircle((float) poiList.get(i).getX(), (float) poiList.get(i).getY(), 5, paint);
            paint.setColor(Color.BLUE);
            if (i == 0) {
                path.moveTo((float) poiList.get(i).getX(), (float) poiList.get(i).getY());
            } else {
                path.lineTo((float) poiList.get(i).getX(), (float) poiList.get(i).getY());
            }
        }
        path.close();
        canvas.drawPath(path, paint);
    }

}
