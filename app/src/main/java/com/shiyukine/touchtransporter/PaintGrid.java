package com.shiyukine.touchtransporter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridLayout;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

public class PaintGrid extends GridLayout {

    Path path = new Path();
    public Paint brush = new Paint();
    public ArrayList<Path> paths = new ArrayList<>();

    public PaintGrid(Context context) {
        super(context);
        initBrush();
    }

    public PaintGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBrush();
    }

    public PaintGrid(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBrush();
    }

    public void initBrush()
    {
        //inflate(context, R.layout.sample_paint_grid, this);
        //settings
        brush.setAntiAlias(true);
        brush.setColor(Color.WHITE);
        brush.setStyle(Paint.Style.STROKE);
        brush.setStrokeJoin(Paint.Join.ROUND);
        brush.setStrokeCap(Paint.Cap.ROUND);
        brush.setStrokeWidth(8f);
    }

    @Deprecated
    public void resetCanvas()
    {
        path = new Path();
        invalidate();
    }

    public void newPath(int X, int Y)
    {
        if(paths.size() > 4) paths.remove(0);
        Path p = new Path();
        p.moveTo(X, Y);
        path = p;
        paths.add(p);
        invalidate();
    }

    public Path getCurrentPath()
    {
        return path;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(Path p : paths) canvas.drawPath(p, brush);
    }
}
