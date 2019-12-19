package com.example.memolang;

import androidx.viewpager.widget.PagerAdapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageAdapter extends PagerAdapter
{
    private Context mContext;
    Drawable[] imgList;

    ImageAdapter(Context context, Drawable[] imgList)
    {
        mContext = context;
        this.imgList = imgList;
    }

    @Override
    public int getCount()
    {
        return imgList.length;
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
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageDrawable(imgList[position]);
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        container.removeView((ImageView) object);
    }
}
