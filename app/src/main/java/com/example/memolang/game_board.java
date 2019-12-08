package com.example.memolang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.regex.MatchResult;

public class game_board extends AppCompatActivity
{
    /// CONST
    final static int DP_MARGIN = 3;
    final static int DP_VIEW_MARGIN = 8;
    final static int DP_PLAYERS = 45;
    final static int DP_TIMER = 25;


    int players;
    String deck;
    String gm;
    int cards;

    ///Vievs
    TextView playersStats[];
    GridLayout gridBoard;
    ImageView[][] card;

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
        cards = intent.getIntExtra("Cards", 2);

        System.out.println(card);
        System.out.println(players);
        gridBoard = findViewById(R.id.gridBoard);
        setBoard();
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
            imageView.setImageResource(R.drawable.back_au);
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
}
