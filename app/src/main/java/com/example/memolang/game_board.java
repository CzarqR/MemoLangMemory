package com.example.memolang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class game_board extends AppCompatActivity
{
    /// CONST
    final static int DP_MARGIN = 3;
    final static int DP_MARGIN_PLAYERS = 2;
    final static int DP_VIEW_MARGIN = 8;
    final static int DP_PLAYERS = 45;
    final static int DP_TIMER = 25;
    /// In game variables
    int players;
    String deck;
    String gm;
    int cards;
    int time;
    ///Views
    TextView playersStats[];
    GridLayout gridBoard;
    GridLayout gridPlayers;
    ImageView[][] card;
    ///Lists
    ArrayList<Player> finalPlayers;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        Intent intent = getIntent();
        Functions.hideNavigationBar(this);

        players = intent.getIntExtra("Players", 1);
        deck = intent.getStringExtra("Deck");
        gm = intent.getStringExtra("GM");
        cards = intent.getIntExtra("Cards", 4);
        time = intent.getIntExtra("Time", -1);

        System.out.println("XDDDDDDDDDDDDD");
        System.out.println(cards);
        System.out.println(players);
        System.out.println(gm);
        System.out.println(deck);
        System.out.println(time);
        System.out.println("XDDDDDDDDDDDDD");

        gridBoard = findViewById(R.id.gridBoard);
        gridPlayers = findViewById(R.id.gridPlayers);
        initPlayersFinalList();
        setBoard();
        setPlayers();
    }

    private void setBoard()
    {
        int w = getFreeWidth();
        int h = getFreeHeight();

        int hc = Distribution.cardsH(w, h, cards);
        System.out.println(hc);
        int wc = (int) Math.ceil((double) cards / hc);
        gridBoard.setColumnCount(wc);
        gridBoard.setRowCount(hc);

        final int CARD_SIZE_PIXEL = convertDpToPixel((double) w / wc >= (double) h / hc ? h / hc : w / wc, this);
        final int MARGIN_SIZE_PIXELS = convertDpToPixel(DP_MARGIN, this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(CARD_SIZE_PIXEL - 2 * MARGIN_SIZE_PIXELS, CARD_SIZE_PIXEL - 2 * MARGIN_SIZE_PIXELS);
        layoutParams.setMargins(MARGIN_SIZE_PIXELS, MARGIN_SIZE_PIXELS, MARGIN_SIZE_PIXELS, MARGIN_SIZE_PIXELS);

        for (int i = 0; i < cards; i++)
        {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.back_dark);
            imageView.setLayoutParams(layoutParams);
            imageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // TODO card click
                    System.out.println("CLICK Image");
                }
            });
            gridBoard.addView(imageView);
        }
    }

    private void setPlayers()
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int w = displayMetrics.widthPixels;
        System.out.println(w);
        w /= players;
        System.out.println(w);
        gridPlayers.setColumnCount(players);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(w - convertDpToPixel(2 * DP_MARGIN_PLAYERS, this), convertDpToPixel(DP_PLAYERS, this));
        layoutParams.setMargins(convertDpToPixel(DP_MARGIN_PLAYERS, this), 0, convertDpToPixel(DP_MARGIN_PLAYERS, this), 0);
        Collections.shuffle(finalPlayers);
        for (int i = 0; i < players; i++)
        {
            TextView textView = new TextView(this);
            textView.setText(finalPlayers.get(i).name + "\n0");
            textView.setBackgroundColor(finalPlayers.get(i).color);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setLayoutParams(layoutParams);
            gridPlayers.addView(textView);
        }
    }

    private int getFreeHeight()
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return convertPixelsToDp(displayMetrics.heightPixels, this) - (DP_VIEW_MARGIN * 4 - DP_MARGIN * 2 + DP_PLAYERS + DP_TIMER);
    }

    private int getFreeWidth()
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return convertPixelsToDp(displayMetrics.widthPixels, this) - (DP_VIEW_MARGIN * 2 - DP_MARGIN * 2);
    }

    public static int convertPixelsToDp(float px, Context context)
    {
        return (int) (px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int convertDpToPixel(float dp, Context context)
    {
        return (int) (dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Functions.hideNavigationBar(this);
    }

    private class Player
    {
        int color;
        String name;

        public Player(int color, String name)
        {
            this.color = color;
            this.name = name;
        }
    }

    private void initPlayersFinalList()
    {
        finalPlayers = new ArrayList<Player>();
        finalPlayers.add(new Player(Color.rgb(191, 25, 25), "Red"));
        finalPlayers.add(new Player(Color.rgb(0, 196, 207), "Blue"));
        finalPlayers.add(new Player(Color.rgb(10, 191, 52), "Green"));
        finalPlayers.add(new Player(Color.rgb(216, 227, 5), "Yellow"));
        finalPlayers.add(new Player(Color.rgb(255, 105, 180), "Pink"));
        finalPlayers.add(new Player(Color.rgb(255, 165, 0), "Orange"));
    }
}
