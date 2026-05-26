package com.example.safeautoclicker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 80, 40, 40);

        TextView txt = new TextView(this);
        txt.setText(
                "Safe Auto Clicker\n\n" +
                "Steps:\n" +
                "1. Enable Accessibility Service\n" +
                "2. Allow Display Over Apps\n" +
                "3. Open Magic Merge\n" +
                "4. Use floating crosshair to save tap points\n\n" +
                "This clicker works only inside Magic Merge."
        );
        txt.setTextSize(18);

        Button acc = new Button(this);
        acc.setText("Open Accessibility Settings");
        acc.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        );

        Button overlay = new Button(this);
        overlay.setText("Allow Display Over Apps");
        overlay.setOnClickListener(v -> {
            Intent i = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
            );
            startActivity(i);
        });

        layout.addView(txt);
        layout.addView(acc);
        layout.addView(overlay);

        setContentView(layout);
    }
}
