package com.example.memolang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;

public class SettingsActivity extends AppCompatActivity
{
    ViewPager vPgBackground;
    ViewPager vPgRevers;
    String[] backgroundList;
    String[] reversList;
    SharedPreferences shPref;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        shPref = this.getSharedPreferences("com.example.memolang", Context.MODE_PRIVATE);

        vPgBackground = findViewById(R.id.vPgBackground);
        vPgRevers = findViewById(R.id.vPgRevers);
        setBackPicker(shPref.getString("back", "default.png"));
        setReversPicker(shPref.getString("revers", "default.png"));
    }

    private void setBackPicker(String nameToSet)
    {

        try
        {
            backgroundList = getAssets().list("Background");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Drawable[] imgBackgroundList = new Drawable[backgroundList.length];
        AssetManager assetManager = getAssets();

        for (int i = 0; i < imgBackgroundList.length; i++)
        {
            InputStream inputStream = null;
            try
            {
                inputStream = assetManager.open(String.valueOf("Background/" + backgroundList[i]));
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.e("Asset Load", "Couldn't load deck image with index " + i);
                imgBackgroundList[i] = null;
            }
            imgBackgroundList[i] = Drawable.createFromStream(inputStream, null);
        }

        ImageAdapter imageAdapter = new ImageAdapter(this, imgBackgroundList);
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
        catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println(reversList.length);
        Drawable[] imgReversList = new Drawable[reversList.length];
        AssetManager assetManager = getAssets();

        for (int i = 0; i < imgReversList.length; i++)
        {
            InputStream inputStream = null;
            try
            {
                inputStream = assetManager.open("Revers/" + reversList[i]);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.e("Asset Load", "Couldn't load deck image with index " + i);
                imgReversList[i] = null;
            }
            imgReversList[i] = Drawable.createFromStream(inputStream, null);
        }

        ImageAdapter imageAdapter = new ImageAdapter(this, imgReversList);
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
        alertDialog.setMessage("Image used in this app comes from:\nfreepngimg.com\nflaticon.com\npexels.com\npngtree.com\n\nIf You owe right to of pictures used in this project and You don't want it to be used, contact us");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
