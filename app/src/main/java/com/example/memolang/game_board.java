package com.example.memolang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

public class game_board extends AppCompatActivity
{
    final static int MAX_PLAYERS = 6;
    LinearLayout linLayPlay;
    LinearLayout linLayStats;
    ImageView playersImg[];
    TextView playersStats[];

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);
        Intent intent = getIntent();
        Functions.hideNavigationBar(this);
        System.out.println(intent.getStringExtra("Players"));
        System.out.println(intent.getStringExtra("Deck"));
        System.out.println(intent.getStringExtra("GM"));


        TextView txt1 = new TextView(this);





//        linLayPlay = findViewById(R.id.linLayPlay);
//        //linLayStats = findViewById(R.id.linLayStats);
//
//
//        //added LayoutParams
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//        //linLayPlay.setOrientation(LinearLayout.VERTICAL);
//
//        //add textView
//        TextView textView = new TextView(this);
//        textView.setText("The developer world is yours");
//        textView.setLayoutParams(params);
//
//        // added Button
//        TextView button = new TextView(this);
//        button.setText("Whedeveloperworldisyours");
//
//        //added the textView and the Button to LinearLayout
//        linLayPlay.addView(textView);
//        linLayPlay.addView(button);


//        //linLayPlay.addView(makeImageView());
//        ImageView imageView1 = makeImageView(100,50,200);
//        ImageView imageView2 = makeImageView(20,110,160);
//
//        //imageView.setColorFilter(Color.GREEN);
//
//        linLayPlay.addView(imageView1);
//        linLayPlay.addView(imageView2);
//
//        imageView2.requestLayout();
//        imageView1.requestLayout();
//
//        linLayPlay.requestLayout();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Functions.hideNavigationBar(this);
    }

    private ImageView makeImageView(int r, int g, int b)
    {
        ImageView img = new ImageView(this);
        img.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        img.setImageResource(R.drawable.def);
        img.setColorFilter(Color.rgb(r, g, b));
//        img.
        img.setScaleType(ImageView.ScaleType.FIT_XY);

        return img;
    }

    private void fillImg()
    {
        playersImg = new ImageView[MAX_PLAYERS];
    }
}
