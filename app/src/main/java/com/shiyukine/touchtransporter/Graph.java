package com.shiyukine.touchtransporter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class Graph extends AppCompatActivity {

    //left, top, right, bottom
    static int[] margin = {-1, -1, -1, -1};
    static int pressI = 0;
    public static boolean isSet = false;
    public static Socket socket;
    public static DatagramSocket socketU;
    public static InetAddress address;
    public static byte[] buf;
    public static PrintWriter output = null;
    public static boolean isMove = false;
    public static boolean keyPressed = false;
    public static boolean errorShowed = false;
    public static Graph g;
    public static boolean isUsb = true;

    @SuppressLint({"SourceLockedOrientationActivity", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            errorShowed = false;
            super.onCreate(savedInstanceState);
            setContentView(R.layout.tgraphic);
            //change screen
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            if(!MainActivity.revert) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
            //setting pressure
            ((SeekBar)findViewById(R.id.pressbar)).setProgress(pressI);
            ((SeekBar)findViewById(R.id.pressbar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    pressI = progress;
                    MainActivity.setSetting("press", progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            g = this;
            //settings tgraph
            //ancient setting
            if (margin[0] != -1) {
                PaintGrid g = findViewById(R.id.tgraph);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) g.getLayoutParams();
                layoutParams.leftMargin = margin[0];
                layoutParams.topMargin = margin[1];
                layoutParams.rightMargin = margin[2];
                layoutParams.bottomMargin = margin[3];
                g.setLayoutParams(layoutParams);
                replaceAllEGraph();
            }
            ViewGroup vg = findViewById(R.id.graph);
            //new setting
            if (isSet) {
                findViewById(R.id.egraph1).setOnTouchListener(touchListener);
                findViewById(R.id.egraph2).setOnTouchListener(touchListener);
                findViewById(R.id.egraph3).setOnTouchListener(touchListener);
                findViewById(R.id.egraph4).setOnTouchListener(touchListener);
            } else {
                findViewById(R.id.egraph1).setVisibility(View.INVISIBLE);
                findViewById(R.id.egraph2).setVisibility(View.INVISIBLE);
                findViewById(R.id.egraph3).setVisibility(View.INVISIBLE);
                findViewById(R.id.egraph4).setVisibility(View.INVISIBLE);
                PaintGrid g = findViewById(R.id.tgraph);
                g.setOnTouchListener(touch);
                vg.setOnTouchListener(touch);
                findViewById(R.id.rclick).setOnTouchListener(clicks);
                findViewById(R.id.lclick).setOnTouchListener(clicks);
            }
            if (MainActivity.osuM) findViewById(R.id.settingst).setVisibility(View.GONE);
            for (int i = 0; i < vg.getChildCount(); i++) {
                View v = vg.getChildAt(i);
                if (v.getTag() != null && ((String) v.getTag()).contains("key-")) {
                    if (!isSet) v.setOnTouchListener(touchkey);
                    if (MainActivity.osuM) v.setVisibility(View.INVISIBLE);
                }
            }
            ViewGroup vg2 = findViewById(R.id.gr_ib);
            for (int i = 0; i < vg2.getChildCount(); i++) {
                View v = vg2.getChildAt(i);
                if (v.getTag() != null && ((String) v.getTag()).contains("key-")) {
                    if (!isSet) v.setOnTouchListener(touchkey);
                    if (MainActivity.osuM) v.setVisibility(View.GONE);
                }
            }
            if(!isSet) {
                //send info when activity loaded
                MessageQueue.IdleHandler handler = new MessageQueue.IdleHandler() {
                    @Override
                    public boolean queueIdle() {
                        PaintGrid grid = findViewById(R.id.tgraph);
                        lp = (RelativeLayout.LayoutParams) grid.getLayoutParams();
                        new SendInfo().execute("Max:" + grid.getMeasuredWidth() + ";" + grid.getMeasuredHeight() + (!MainActivity.osuM ? ";pen" : ";mouse") + ";" + MainActivity.verCode + ";" + MainActivity.automc + "|", g);
                        return false;
                    }
                };
                Looper.myQueue().addIdleHandler(handler);
            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage(), e);
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        try {
            if(socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onResume() {
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        super.onResume();
    }

    View.OnTouchListener touchListener =  new View.OnTouchListener() {
        @SuppressLint({"ClickableViewAccessibility", "NonConstantResourceId"})
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                PaintGrid grid = findViewById(R.id.tgraph);
                    RelativeLayout main = findViewById(R.id.graph);
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) grid.getLayoutParams();
                    switch (view.getId()) {
                        case R.id.egraph1:
                            layoutParams.leftMargin = X - 35;
                            layoutParams.topMargin = Y - 35;
                            break;
                        case R.id.egraph2:
                            layoutParams.rightMargin = main.getWidth() - X - 35;
                            layoutParams.topMargin = Y - 35;
                            break;
                        case R.id.egraph3:
                            layoutParams.rightMargin = main.getWidth() - X - 35;
                            layoutParams.bottomMargin = main.getHeight() - Y - 35;
                            break;
                        case R.id.egraph4:
                            layoutParams.leftMargin = X - 35;
                            layoutParams.bottomMargin = main.getHeight() - Y - 35;
                            break;
                    }
                    grid.setLayoutParams(layoutParams);
                    setMarginGraph();
                    replaceAllEGraph();
            }
            return true;
        }
    };

    View.OnTouchListener touchkey =  new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            try {
                if (!isSet) {
                    ImageButton ib = (ImageButton) view;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            new SendInfo().execute(ib.getTag() + "-kdown|", g);
                            isMove = ((String)view.getTag()).contains("key-8");
                            keyPressed = true;
                            return true;

                        case MotionEvent.ACTION_UP:
                            new SendInfo().execute(ib.getTag() + "-kup|", g);
                            if(((String)view.getTag()).contains("key-8")) isMove = false;
                            keyPressed = false;
                            return true;
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.e("Error", "E" + e.getMessage());
            }
            return true;
        }
    };

    View.OnTouchListener clicks =  new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            try {
                if (!isSet) {
                    Button ib = (Button) view;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            new SendInfo().execute((((String)ib.getTag()).contains("lcl") ? "mouse_clickb|" :  "mouse_rclickb|"), g);
                            keyPressed = true;
                            break;

                        case MotionEvent.ACTION_UP:
                            new SendInfo().execute((((String)ib.getTag()).contains("lcl") ? "mouse_upb|" :  "mouse_rupb|"), g);
                            keyPressed = false;
                            break;
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.e("Error", "E" + e.getMessage());
            }
            return true;
        }
    };

    private void setEvent(View view, int X, int Y, float press)
    {
        //change mouse position + change pressure
        new SendInfo().execute("Pos:" + X + ";" + Y + (!MainActivity.osuM ? ";" + press : "") + "|", g);
        //draw
        if(!isSet && !MainActivity.osuM && !isMove)
        {
            ((PaintGrid) view).getCurrentPath().lineTo(X, Y);
            view.invalidate();
        }
    }

    int firstPres = -1;
    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(0,0);

    View.OnTouchListener touch =  new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            try {
                for (int i = 0; i < event.getPointerCount(); i++) {
                    final int id = event.getPointerId(i);
                    final int X = (int) event.getX(i);
                    final int Y = (int) event.getY(i);
                    PaintGrid grid = null;
                    boolean isGrid = view.getId() == R.id.tgraph;
                    boolean condition = false;
                    if(isGrid)
                    {
                        grid = (PaintGrid)view;
                        condition = X > -1 && Y > -1 && X <= grid.getMeasuredWidth() && Y <= grid.getMeasuredHeight();
                    }
                    else
                    {
                        grid = findViewById(R.id.tgraph);
                        condition = X >= lp.leftMargin && Y >= lp.topMargin && X <= lp.leftMargin + grid.getMeasuredWidth() && Y <= lp.topMargin + grid.getMeasuredHeight();
                    }
                    //Log.d("Touch", X + " " + Y + " " + view.toString());
                    if (condition) {
                        if (firstPres == -1) firstPres = id;
                        if (id == firstPres) {
                            //Log.d("Touch", "id " + id + " is " + firstPres + " on grid ");
                            float press = event.getPressure();
                            float pp = ((SeekBar) findViewById(R.id.pressbar)).getProgress();
                            if (pp < 1) press = press * (1 + press);
                            if (pp > 1) press = press * pp;
                            if (press > 1) press = 1;
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_MOVE:
                                    //historic
                                    if (MainActivity.histEv) {
                                        try {
                                            final int historySize = event.getHistorySize();
                                            for (int h = 0; h < historySize; h++) {
                                                //change mouse position + change pressure
                                                int hist = (!isMove && !keyPressed ? firstPres : 0);
                                                if (hist == id && event.getHistoricalSize(hist, h) > 0) {
                                                    int aX = (int) event.getHistoricalX(hist, h);
                                                    int aY = (int) event.getHistoricalY(hist, h);
                                                    setEvent(grid, (isGrid ? aX : aX - lp.leftMargin), (isGrid ? aY : aY - lp.topMargin), press);
                                                }
                                            }
                                        }
                                        catch (Exception ignored) {
                                            Log.d("Touch", "Error history");
                                        }
                                    }
                                    setEvent(grid, (isGrid ? X : X - lp.leftMargin), (isGrid ? Y : Y - lp.topMargin), press);
                                    return true;

                                case MotionEvent.ACTION_DOWN:
                                    //send coords + mouse click
                                    if(isGrid) new SendInfo().execute("Pos:" + X + ";" + Y + (!MainActivity.osuM ? ";" + press : "") + "|", g);
                                    else new SendInfo().execute("Pos:" + (X - lp.leftMargin) + ";" + (Y - lp.topMargin) + (!MainActivity.osuM ? ";" + press : "") + "|", g);
                                    if (MainActivity.automc)
                                        new SendInfo().execute("mouse_click|", g);
                                    //draw
                                    if (!isSet && !MainActivity.osuM && !isMove) {
                                        //((PaintGrid) view).resetCanvas();
                                        ((PaintGrid) grid).newPath(X, Y);
                                    }
                                    return true;

                                case MotionEvent.ACTION_UP:
                                    //mouse up
                                    if (MainActivity.automc)
                                        new SendInfo().execute("mouse_up|", g);
                                    firstPres = -1;
                                    return true;

                                default:
                                    return false;
                            }
                        } else {
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                if (MainActivity.automc)
                                    new SendInfo().execute("mouse_up|", g);
                                firstPres = -1;
                            }
                        }
                    } else {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (MainActivity.automc)
                                new SendInfo().execute("mouse_up|", g);
                            firstPres = -1;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("Error", "E " + e.getMessage());
                e.printStackTrace();
                try {
                    new SendInfo().execute("TABLET ERROR : " + e.getMessage(), g);
                } catch (Exception ignored) { }
            }
            return true;
        }
    };

    public void setMarginGraph() {
        //set margins + margins egraph
        PaintGrid g = findViewById(R.id.tgraph);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) g.getLayoutParams();
        margin[0] = layoutParams.leftMargin;
        margin[1] = layoutParams.topMargin;
        margin[2] = layoutParams.rightMargin;
        margin[3] = layoutParams.bottomMargin;
        Profiles.modifProfile(MainActivity.settings.getString("CurProfile", "Default"), margin);
    }

    public void replaceAllEGraph() {
        //egraph1
        RelativeLayout.LayoutParams l1 = (RelativeLayout.LayoutParams) findViewById(R.id.egraph1).getLayoutParams();
        l1.leftMargin = margin[0] + 35;
        l1.topMargin = margin[1] + 35;
        findViewById(R.id.egraph1).setLayoutParams(l1);
        //egraph2
        RelativeLayout.LayoutParams l2 = (RelativeLayout.LayoutParams) findViewById(R.id.egraph2).getLayoutParams();
        l2.rightMargin = margin[2] + 35;
        l2.topMargin = margin[1] + 35;
        findViewById(R.id.egraph2).setLayoutParams(l2);
        //egraph3
        RelativeLayout.LayoutParams l3 = (RelativeLayout.LayoutParams) findViewById(R.id.egraph3).getLayoutParams();
        l3.rightMargin = margin[2] + 35;
        l3.bottomMargin = margin[3] + 35;
        findViewById(R.id.egraph3).setLayoutParams(l3);
        //egraph4
        RelativeLayout.LayoutParams l4 = (RelativeLayout.LayoutParams) findViewById(R.id.egraph4).getLayoutParams();
        l4.bottomMargin = margin[3] + 35;
        l4.leftMargin = margin[0] + 35;
        findViewById(R.id.egraph4).setLayoutParams(l4);
    }

    public void set_click(View view) {
        if(!isSet) {
            if (findViewById(R.id.setg).getVisibility() == View.VISIBLE)
                findViewById(R.id.setg).setVisibility(View.GONE);
            else findViewById(R.id.setg).setVisibility(View.VISIBLE);
        }
    }
}
