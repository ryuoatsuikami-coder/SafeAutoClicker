package com.example.safeautoclicker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class ClickService extends AccessibilityService {
    private Handler handler = new Handler();
    private boolean running = false;

    private int[][] points = {
            {540, 1510},
            {540, 2220}
    };

    private int index = 0;

    private Runnable clickLoop = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            int x = points[index][0];
            int y = points[index][1];
            tap(x, y);

            index = (index + 1) % points.length;
            handler.postDelayed(this, 2500);
        }
    };

    @Override
    protected void onServiceConnected() {
        Toast.makeText(this, "Auto Clicker Service Enabled", Toast.LENGTH_SHORT).show();
        running = true;
        handler.postDelayed(clickLoop, 1500);
    }

    private void tap(int x, int y) {
        Path path = new Path();
        path.moveTo(x, y);

        GestureDescription.StrokeDescription stroke =
                new GestureDescription.StrokeDescription(path, 0, 100);

        GestureDescription gesture =
                new GestureDescription.Builder().addStroke(stroke).build();

        dispatchGesture(gesture, null, null);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {
        running = false;
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
    }
}
