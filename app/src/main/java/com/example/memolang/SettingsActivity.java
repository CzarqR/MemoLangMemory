package com.example.memolang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.util.Objects;

import yuku.ambilwarna.AmbilWarnaDialog;

public class SettingsActivity extends AppCompatActivity
{
    ViewPager vPgBackground;
    ViewPager vPgRevers;
    String[] backgroundList;
    String[] reversList;
    SharedPreferences shPref;
    ImageView imgCardBack;
    final static int[] COLORS_CARD_DEF = {0xFFA7FFEB, 0xFFFF9E80};
    int[] colors;
    int indexColor;

    @Override
    protected void onResume()
    {
        super.onResume();
        hideNavigationBar();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        hideNavigationBar();
        shPref = this.getSharedPreferences("com.example.memolang", Context.MODE_PRIVATE);

        vPgBackground = findViewById(R.id.vPgBackground);
        vPgRevers = findViewById(R.id.vPgRevers);

        setBackPicker(shPref.getString("back", "default.png"));
        setReversPicker(shPref.getString("revers", "default.png"));
        colors = new int[2];
        colors[0] = shPref.getInt("primBack", COLORS_CARD_DEF[0]);
        colors[1] = shPref.getInt("secBack", COLORS_CARD_DEF[1]);

        setCardBack();
    }

    public void hideNavigationBar()
    {
        this.getWindow().getDecorView().
                setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
    }

    private void setCardBack()
    {
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.BR_TL,
                colors);
        imgCardBack = findViewById(R.id.imgCardBack);
        imgCardBack.setImageDrawable(gd);
    }

    private void setBackPicker(String nameToSet)
    {

        try
        {
            backgroundList = getAssets().list("Background");
        }
        catch (IOException ignored)
        {
        }
        assert backgroundList != null;
        String[] backgroundListPaths = new String[backgroundList.length];

        for (int i = 0; i < backgroundListPaths.length; i++)
        {
            backgroundListPaths[i] = "Background/" + backgroundList[i];
        }

        ImageAdapter imageAdapter = new ImageAdapter(this, backgroundListPaths, ImageView.ScaleType.CENTER_CROP);
        vPgBackground.setAdapter(imageAdapter);
        vPgBackground.setCurrentItem(findIndex(backgroundList, nameToSet));
        vPgBackground.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                shPref.edit().putString("back", backgroundList[position]).apply();
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });
    }

    private void setReversPicker(String nameToSet)
    {

        try
        {
            reversList = getAssets().list("Revers");
        }
        catch (IOException ignored)
        {
        }
        assert reversList != null;
        String[] reversListPaths = new String[reversList.length];

        for (int i = 0; i < reversListPaths.length; i++)
        {
            reversListPaths[i] = "Revers/" + reversList[i];
        }

        ImageAdapter imageAdapter = new ImageAdapter(this, reversListPaths, ImageView.ScaleType.FIT_CENTER);
        vPgRevers.setAdapter(imageAdapter);
        vPgRevers.setCurrentItem(findIndex(reversList, nameToSet));
        vPgRevers.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                shPref.edit().putString("revers", reversList[position]).apply();
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });
    }

    private int findIndex(String[] list, String name)
    {
        for (int i = 0; i < list.length; i++)
        {
            if (list[i].equals(name))
            {
                return i;
            }
        }
        return 0;
    }

    public void butCreditsClick(View view)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Credits");
        alertDialog.setMessage(getString(R.string.credits_info, getString(R.string.email)));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        hideNavigationBar();
                    }
                });

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                hideNavigationBar();
            }
        });
        alertDialog.show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.gradient_background_msg);
    }

    public void butHelpClick(View view)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.help));
        alertDialog.setMessage(getString(R.string.help_text));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        hideNavigationBar();
                    }
                });
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                hideNavigationBar();
            }
        });
        alertDialog.show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.gradient_background_msg);
    }

    public void pickColor(View view)
    {
        indexColor = Integer.parseInt(view.getTag().toString());
        openColorPickerDialog();
    }

    public void butDefaultClick(View view)
    {
        colors[0] = COLORS_CARD_DEF[0];
        colors[1] = COLORS_CARD_DEF[1];
        shPref.edit().putInt("primBack", colors[0]).apply();
        shPref.edit().putInt("secBack", colors[1]).apply();
        for (int i = 0; i < backgroundList.length; i++)
        {
            if (backgroundList[i].equals("default.png"))
            {
                vPgBackground.setCurrentItem(i);
                shPref.edit().putString("back", backgroundList[i]).apply();
                break;
            }
        }
        for (int i = 0; i < reversList.length; i++)
        {
            if (reversList[i].equals("default.png"))
            {
                vPgRevers.setCurrentItem(i);
                shPref.edit().putString("back", reversList[i]).apply();
                break;
            }
        }

        setCardBack();
    }

    private void openColorPickerDialog()
    {

        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(SettingsActivity.this, colors[indexColor], true, new AmbilWarnaDialog.OnAmbilWarnaListener()
        {
            @Override
            public void onOk(AmbilWarnaDialog ambilWarnaDialog, int color)
            {

                shPref.edit().putInt(indexColor == 0 ? "primBack" : "secBack", color).apply();
                colors[indexColor] = color;
                setCardBack();
                hideNavigationBar();
            }

            @Override
            public void onCancel(AmbilWarnaDialog ambilWarnaDialog)
            {
                hideNavigationBar();
            }
        });
        ambilWarnaDialog.show();
    }

    public void butApplyClick(View view)
    {
        this.finish();
    }
}
