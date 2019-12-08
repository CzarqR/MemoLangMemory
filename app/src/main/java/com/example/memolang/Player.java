package com.example.memolang;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class Player
{
    public String name;
    public String color;
    public int points;
    public double time;

    public Player(String name, String color, int points, double time)
    {
        this.name = name;
        this.color = color;
        this.points = points;
        this.time = time;
    }

}
