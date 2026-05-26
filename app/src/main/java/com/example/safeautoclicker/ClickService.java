package com.example.safeautoclicker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ClickService extends AccessibilityService {

    private WindowManager windowManager;
    private LinearLayout panel;
    private TextView crosshair;

    private Handler handler = new Handler();
    private boolean running = false;

    private final String targetPackage = "com.magic.plant.evolumerge";

    private ArrayList<int[]> points = new ArrayList<>();
    private int currentIndex = 0;

    private int crossX = 360;
    private int crossY = 800;

    private WindowManager.LayoutParams crossParams;
    private WindowManager.LayoutParams panelParams;

    private Runnable clickLoop = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            if (!isGameOpen()) {
                handler.postDelayed(this, 1000);
                return;
            }

            if (points.size() == 0) {
                Toast.makeText(ClickService.this, "No saved points yet", Toast.LENGTH_SHORT).show();
                running = false;
                return;
            }

            int[] point = points.get(currentIndex);
            tap(point[0], point[1]);

            currentIndex = (currentIndex + 1) % points.size();
            handler.postDelayed(this, 1800);
        }
    };

    @Override
    protected void onServiceConnected() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        showFloatingControls();
        Toast.makeText(this, "Crosshair Auto Clicker ON", Toast.LENGTH_SHORT).show();
    }

    private void showFloatingControls() {
        crosshair = new TextView(this);
        crosshair.setText("+");
        crosshair.setTextSize(38);
        crosshair.setTextColor(Color.RED);
        crosshair.setGravity(Gravity.CENTER);
        crosshair.setBackgroundColor(Color.argb(80, 255, 255, 255));

        crossParams = new WindowManager.LayoutParams(
                90,
                90,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        crossParams.gravity = Gravity.TOP | Gravity.LEFT;
        crossParams.x = crossX;
        crossParams.y = crossY;

        crosshair.setOnTouchListener(new View.OnTouchListener() {
            int startX, startY;
            float touchX, touchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = crossParams.x;
                        startY = crossParams.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        crossParams.x = startX + (int) (event.getRawX() - touchX);
                        crossParams.y = startY + (int) (event.getRawY() - touchY);
                        windowManager.updateViewLayout(crosshair, crossParams);

                        crossX = crossParams.x + 45;
                        crossY = crossParams.y + 45;
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(crosshair, crossParams);

        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(10, 10, 10, 10);
        panel.setBackgroundColor(Color.argb(180, 0, 0, 0));

        Button saveBtn = new Button(this);
        saveBtn.setText("Save Point");

        Button startBtn = new Button(this);
        startBtn.setText("Start");

        Button stopBtn = new Button(this);
        stopBtn.setText("Stop");

        Button clearBtn = new Button(this);
        clearBtn.setText("Clear");

        TextView info = new TextView(this);
        info.setTextColor(Color.WHITE);
        info.setText("Saved: 0");

        saveBtn.setOnClickListener(v -> {
            points.add(new int[]{crossX, crossY});
            info.setText("Saved: " + points.size());
            Toast.makeText(this, "Saved point: " + crossX + ", " + crossY, Toast.LENGTH_SHORT).show();
        });

        startBtn.setOnClickListener(v -> {
            if (!isGameOpen()) {
                Toast.makeText(this, "Open Magic Merge first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (points.size() == 0) {
                Toast.makeText(this, "Save points first", Toast.LENGTH_SHORT).show();
                return;
            }

            running = true;
            currentIndex = 0;
            handler.postDelayed(clickLoop, 1000);
            Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();
        });

        stopBtn.setOnClickListener(v -> {
            running = false;
            Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
        });

        clearBtn.setOnClickListener(v -> {
            running = false;
            points.clear();
            currentIndex = 0;
            info.setText("Saved: 0");
            Toast.makeText(this, "Cleared saved points", Toast.LENGTH_SHORT).show();
        });

        panel.addView(saveBtn);
        panel.addView(startBtn);
        panel.addView(stopBtn);
        panel.addView(clearBtn);
        panel.addView(info);

        panelParams = new WindowManager.LayoutParams(
                260,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        panelParams.gravity = Gravity.TOP | Gravity.LEFT;
        panelParams.x = 20;
        panelParams.y = 200;

        windowManager.addView(panel, panelParams);
    }

    private boolean isGameOpen() {
        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null || root.getPackageName() == null) {
            return false;
        }

        String packageName = root.getPackageName().toString();
        return packageName.equals(targetPackage);
    }

    private void tap(int x, int y) {
        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(path, 0, 120);

        GestureDescription gesture =
                new GestureDescription.Builder()
                        .addStroke(stroke)
                        .build();

        dispatchGesture(gesture, null, null);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // No automatic screen reading.
    }

    @Override
    public void onInterrupt() {
        running = false;
    }

    @Override
    public void onDestroy() {
        running = false;

        if (windowManager != null) {
            if (crosshair != null) windowManager.removeView(crosshair);
            if (panel != null) windowManager.removeView(panel);
        }

        super.onDestroy();
    }
}
