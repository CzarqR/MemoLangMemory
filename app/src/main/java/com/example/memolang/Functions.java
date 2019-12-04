package com.example.memolang;

import android.app.Activity;
import android.view.View;

public class Functions
{
    public static void hideNavigationBar(Activity activity)
    {
        activity.getWindow().getDecorView().
                setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
    }
}
