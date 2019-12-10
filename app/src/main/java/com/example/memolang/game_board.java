package com.example.memolang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class game_board extends AppCompatActivity
{
    /// CONST
    final static int DP_MARGIN = 3;
    final static int DP_MARGIN_PLAYERS = 2;
    final static int DP_VIEW_MARGIN = 8;
    final static int DP_PLAYERS = 45;
    final static int DP_TIMER = 25;
    /// In game variables
    int CARD_SIZE_PIXEL;
    int MARGIN_SIZE_PIXELS;
    int players;
    String deck;
    String gm;
    String language1, language2;
    int cards;
    int time;
    ///Views
    TextView playersStats[];
    GridLayout gridBoard;
    GridLayout gridPlayers;
    ///Lists
    ArrayList<Player> finalPlayers;
    ArrayList<String> listImages;
    Card[][] cardBoard; ///todo cards id apply

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
        language1 = intent.getStringExtra("Lang1");
        language2 = intent.getStringExtra("Lang2");
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
        setBoard();
        loadImages();
        initPlayersFinalList();
        setCardLayout();
        setPlayers();
    }

    private void setBoard()
    {
        int w = getFreeWidth();
        int h = getFreeHeight();
        int hc = Distribution.cardsH(w, h, cards);
        int wc = (int) Math.ceil((double) cards / hc);
        gridBoard.setColumnCount(wc);
        gridBoard.setRowCount(hc);

        CARD_SIZE_PIXEL = convertDpToPixel((double) w / wc >= (double) h / hc ? h / hc : w / wc, this);
        MARGIN_SIZE_PIXELS = convertDpToPixel(DP_MARGIN, this);
    }

    private void setCardLayout()
    {
        int hc = gridBoard.getRowCount();
        int wc = gridBoard.getColumnCount();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(CARD_SIZE_PIXEL - 2 * MARGIN_SIZE_PIXELS, CARD_SIZE_PIXEL - 2 * MARGIN_SIZE_PIXELS);
        layoutParams.setMargins(MARGIN_SIZE_PIXELS, MARGIN_SIZE_PIXELS, MARGIN_SIZE_PIXELS, MARGIN_SIZE_PIXELS);

        for (int i = 0; i < cards; i++)
        {
            ImageView imageView = new ImageView(this);
            setImageFromAssets(imageView, "Decks/" + deck + "/Cards/" + listImages.get(i));
            imageView.setLayoutParams(layoutParams);
            imageView.setTag((i / wc) + "_" + (i % wc));
            imageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // TODO card click
                    ImageView actIm = (ImageView) v;
                    setImageFromAssets(actIm, "Decks/" + deck + "/Cards/" + listImages.get(1));
                }
            });
            gridBoard.addView(imageView);
        }
    }

    private void setPlayers()
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int w = displayMetrics.widthPixels - 2; // minus 2 to set little bigger margin
        w /= players;
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
        int score = 0;

        public Player(int color, String name)
        {
            this.color = color;
            this.name = name;
        }
    }

    private void initPlayersFinalList()
    {
        finalPlayers = new ArrayList<>();
        finalPlayers.add(new Player(Color.rgb(191, 25, 25), "Red"));
        finalPlayers.add(new Player(Color.rgb(0, 196, 207), "Blue"));
        finalPlayers.add(new Player(Color.rgb(10, 191, 52), "Green"));
        finalPlayers.add(new Player(Color.rgb(216, 227, 5), "Yellow"));
        finalPlayers.add(new Player(Color.rgb(255, 105, 180), "Pink"));
        finalPlayers.add(new Player(Color.rgb(255, 165, 0), "Orange"));
    }

    public ArrayList<String> readLines(String filename)
    {
        ArrayList<String> lang = new ArrayList<>();
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(getAssets().open(filename), "UTF-8"));
            String mLine;
            while ((mLine = reader.readLine()) != null)
            {
                lang.add(mLine);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return lang;
    }

    private void loadImages()
    {

        ArrayList<String> lang1st = readLines("Decks/" + deck + "/Lang/" + language1 + ".txt");
        ArrayList<String> lang2nd = readLines("Decks/" + deck + "/Lang/" + language2 + ".txt");

        Pattern pattern = Pattern.compile("([a-zA-Z]+)_(\\d+)");
        Matcher matcher;
        matcher = pattern.matcher(deck);
        matcher.matches();

        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < Integer.parseInt(matcher.group(2)); i++)
        {
            list.add(new Integer(i));
        }
        Collections.shuffle(list);
        listImages = new ArrayList<>();

        for (int i = 0; i < cards / 2; i++)
        {
            listImages.add(list.get(i) + "_0.png");
            listImages.add(list.get(i) + "_1.png");
        }
        Collections.shuffle(listImages);

        cardBoard = new Card[(int) Math.ceil(((double) cards) / gridBoard.getColumnCount())][];

        System.out.println();

        for (int i = 0; i < cardBoard.length - 1; i++)
        {
            cardBoard[i] = new Card[gridBoard.getColumnCount()];
        }
        cardBoard[cardBoard.length - 1] = new Card[cards % gridBoard.getColumnCount() == 0 ? gridBoard.getColumnCount() : cards % gridBoard.getColumnCount()];

        for (int i = 0; i < cardBoard.length; i++)
        {
            for (int j = 0; j < cardBoard[i].length; j++)
            {
                boolean zeroIndex = listImages.get(i * gridBoard.getColumnCount() + j).substring(listImages.get(i * gridBoard.getColumnCount() + j).indexOf("_") + 1, listImages.get(i * gridBoard.getColumnCount() + j).indexOf(".")).equals("0");
                short id = Short.parseShort(listImages.get(i * gridBoard.getColumnCount() + j).substring(0, listImages.get(i * gridBoard.getColumnCount() + j).indexOf("_")));
                cardBoard[i][j] = new Card(zeroIndex,lang1st.get(id), lang2nd.get(id), id );
            }
        }
        
    }

    private void setImageFromAssets(ImageView img, String path)
    {
        ///SET IMAGE FROM FOLDER
        AssetManager assetManager = getAssets();
        try
        {
            InputStream ims = assetManager.open(path);
            Drawable d = Drawable.createFromStream(ims, null);
            img.setImageDrawable(d);
        }
        catch (Exception ex)
        {
            return;
        }
    }

    private class Card
    {
        boolean stateHide = true;
        boolean stateMatch = false;
        boolean zeroIndex;
        String language1;
        String language2;
        short id;

        public Card(boolean zeroIndex, String language1, String language2, short id)
        {
            this.zeroIndex = zeroIndex;
            this.language1 = language1;
            this.language2 = language2;
            this.id = id;
        }

        public boolean matches(Card card)
        {
            return this.id == card.id;
        }
    }
}
