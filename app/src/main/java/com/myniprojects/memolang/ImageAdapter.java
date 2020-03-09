package com.myniprojects.memolang;

import androidx.viewpager.widget.PagerAdapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.InputStream;

public class ImageAdapter extends PagerAdapter
{
    private Context mContext;
    String[] imgListPaths;
    ImageView.ScaleType scaleType;

    ImageAdapter(Context context, String[] imgList, ImageView.ScaleType scaleType)
    {
        mContext = context;
        this.imgListPaths = imgList;
        this.scaleType = scaleType;
    }

    @Override
    public int getCount()
    {
        return imgListPaths.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(scaleType);
        setImageFromAssets(imageView, imgListPaths[position]);
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        container.removeView((ImageView) object);
    }

    private void setImageFromAssets(ImageView img, String path)
    {
        ///SET IMAGE FROM FOLDER
        AssetManager assetManager = mContext.getAssets();
        try
        {
            InputStream ims = assetManager.open(path);
            Drawable d = Drawable.createFromStream(ims, null);
            img.setImageDrawable(d);
        }
        catch (Exception ignored)
        {
            Log.e("NoImage", "Couldn't lod image from path: " + path);
        }
    }
}
