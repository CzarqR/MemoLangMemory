package com.example.memolang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
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
import java.util.Locale;
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
    /// In game init variables
    int CARD_SIZE_PIXEL;
    int MARGIN_SIZE_PIXELS;
    int players;
    String deck;
    byte gm; // 0 - casual , 1 - learning mode
    String language1, language2, countryCode1, countryCode2, langCode1, langCode2;
    int cards;
    int time;
    ///Views
    TextView txtTimer;
    TextView txtLang1;
    TextView txtLang2;
    GridLayout gridBoard;
    GridLayout gridPlayers;
    ///Lists
    ArrayList<Player> finalPlayers;
    ArrayList<String> listImages;
    Card[][] cardBoard;
    Drawable revers;
    /// In game playing variables
    CountDownTimer timer;
    int playerIndex = 0;
    int matchedPairs = 0;
    boolean firstPickStatus = true;
    int[] firstPick = {-1, -1};
    int[] secondPick = {-1, -1};
    Locale locale1;
    Locale locale2;
    private TextToSpeech reader;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        Intent intent = getIntent();
        Functions.hideNavigationBar(this);

        players = intent.getIntExtra("Players", 1);
        deck = intent.getStringExtra("Deck");
        gm = (byte) intent.getIntExtra("GM", 0);

        if (gm == 1)
        {
            language1 = intent.getStringExtra("Lang1");
            language2 = intent.getStringExtra("Lang2");
            countryCode1 = intent.getStringExtra("CountryCode1");
            countryCode2 = intent.getStringExtra("CountryCode2");
            langCode1 = intent.getStringExtra("LangCode1");
            langCode2 = intent.getStringExtra("LangCode2");
        }


        cards = intent.getIntExtra("Cards", 4);
        time = intent.getIntExtra("Time", -1) * 1000;
        txtTimer = findViewById(R.id.txtTimer);
        txtLang1 = findViewById(R.id.txtLang1);
        txtLang2 = findViewById(R.id.txtLang2);

        gridBoard = findViewById(R.id.gridBoard);
        gridPlayers = findViewById(R.id.gridPlayers);
        revers = loadImageFromAssets("Revers/back_dark.png");
        setBoard();
        loadImages();
        initPlayersFinalList();
        setCardLayout();
        setPlayers();
        txtTimer.setBackgroundColor(finalPlayers.get(0).color);

        if (time > 0)
        {
            startTimer();
        }
        else
        {
            txtTimer.setText("Turn");
        }

        if (gm == 1) // learning mode
        {
            locale1 = new Locale(intent.getStringExtra("LangCode1"), intent.getStringExtra("CountryCode1"));
            locale2 = new Locale(intent.getStringExtra("LangCode2"), intent.getStringExtra("CountryCode2"));
            reader = new TextToSpeech(this, new TextToSpeech.OnInitListener()
            {
                @Override
                public void onInit(int status)
                {
                    if (status == TextToSpeech.SUCCESS) //CHECK language compatibility
                    {
                        int result = reader.setLanguage(locale1);

                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                        {
                            Log.e("TTS", "Language 1 not supported");
                        }
                        result = reader.setLanguage(locale2);

                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                        {
                            Log.e("TTS", "Language 2 not supported");
                        }
                    }
                    else
                    {
                        Log.e("TTS", "Initialization failed");
                    }
                }
            });
            reader.setPitch(1f);
            reader.setSpeechRate(1.1f);
        }
    }

    @Override
    protected void onDestroy()
    {
        if (reader != null)//TURN off all voices when shutting down app
        {
            reader.stop();
            reader.shutdown();
        }
        super.onDestroy();
    }

    private void startTimer()
    {
        timer = new CountDownTimer(time, 100)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                txtTimer.setText((millisUntilFinished / 1000) + ":" + ((millisUntilFinished % 1000) / 10));
            }

            @Override
            public void onFinish()
            {
                hideMatchedPair();
                if (firstPick[0] != -1)
                {
                    cardBoard[firstPick[0]][firstPick[1]].img.setImageDrawable(revers);
                    cardBoard[firstPick[0]][firstPick[1]].stateHide = true;
                    txtLang1.setText("");
                }
                if (secondPick[0] != -1)
                {
                    cardBoard[secondPick[0]][secondPick[1]].img.setImageDrawable(revers);
                    cardBoard[secondPick[0]][secondPick[1]].stateHide = true;
                    txtLang2.setText("");
                }
                firstPickStatus = true;
                nextPlayerPairsDontMatch();
                startTimer();
            }
        }.start();
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

        View.OnClickListener onClickListener = null;

        if (gm == 0) // on click listener GM CASUAL
        {
            onClickListener = new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int r = Integer.parseInt(v.getTag().toString().substring(0, v.getTag().toString().indexOf("_")));
                    int c = Integer.parseInt(v.getTag().toString().substring(v.getTag().toString().indexOf("_") + 1));

                    if (!cardBoard[r][c].stateMatch) // clicking card that is unmatched
                    {
                        ImageView curImg = (ImageView) v;
                        if (cardBoard[r][c].stateHide) // clicking hidden card
                        {

                            if (firstPickStatus)// clicking first card in round
                            {
                                hideMatchedPair();

                                setImageFromAssets(curImg, "Decks/" + deck + "/Cards/" + cardBoard[r][c].fileExt());
                                firstPickStatus = false;
                                firstPick[0] = r;
                                firstPick[1] = c;
                            }
                            else // clicking second card in round
                            {
                                if (time > 0)
                                {
                                    timer.cancel();
                                }
                                setImageFromAssets(curImg, "Decks/" + deck + "/Cards/" + cardBoard[r][c].fileExt());

                                firstPickStatus = true;
                                secondPick[0] = r;
                                secondPick[1] = c;
                                if (cardBoard[firstPick[0]][firstPick[1]].matches(cardBoard[r][c]))// pairs matched
                                {
                                    cardBoard[r][c].stateMatch = true;
                                    cardBoard[firstPick[0]][firstPick[1]].stateMatch = true;
                                    finalPlayers.get(playerIndex).score++;
                                    finalPlayers.get(playerIndex).setScore();

                                    matchedPairs++;

                                    if (matchedPairs == cards / 2) // END of the game
                                    {
                                        //todo end of the game
                                        cardBoard[firstPick[0]][firstPick[1]].img.setVisibility(View.INVISIBLE);
                                        cardBoard[secondPick[0]][secondPick[1]].img.setVisibility(View.INVISIBLE);
                                    }
                                    else  // NOT END of the game
                                    {
                                        if (time > 0)
                                        {
                                            startTimer();
                                        }
                                    }
                                }
                                else //pairs don't match
                                {
                                    nextPlayerPairsDontMatch();
                                    if (time > 0)
                                    {
                                        startTimer();
                                    }
                                }
                            }

                            cardBoard[r][c].stateHide = false;
                        }
                        else //clicking card which is already shown
                        {
                            //todo animation click same card
                        }
                    }
                }
            };
        }
        else if (gm == 1) // on click listener GM LEARNING MODE
        {
            onClickListener = new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int r = Integer.parseInt(v.getTag().toString().substring(0, v.getTag().toString().indexOf("_")));
                    int c = Integer.parseInt(v.getTag().toString().substring(v.getTag().toString().indexOf("_") + 1));

                    if (!cardBoard[r][c].stateMatch) // clicking card that is unmatched
                    {
                        ImageView curImg = (ImageView) v;
                        if (cardBoard[r][c].stateHide) // clicking hidden card
                        {

                            if (firstPickStatus)// clicking first card in round
                            {
                                hideMatchedPair();

                                setImageFromAssets(curImg, "Decks/" + deck + "/Cards/" + cardBoard[r][c].fileExt());
                                txtLang1.setText(cardBoard[r][c].language1);
                                reader.stop();
                                reader.setLanguage(locale1);
                                reader.speak(cardBoard[r][c].language1, TextToSpeech.QUEUE_ADD, null, null);
                                if (secondPick[0] != -1)
                                {
                                    txtLang2.setText("");
                                }
                                firstPickStatus = false;
                                firstPick[0] = r;
                                firstPick[1] = c;
                            }
                            else // clicking second card in round
                            {
                                if (time > 0)
                                {
                                    timer.cancel();
                                }
                                setImageFromAssets(curImg, "Decks/" + deck + "/Cards/" + cardBoard[r][c].fileExt());
                                txtLang2.setText(cardBoard[r][c].language2);
                                reader.setLanguage(locale2);
                                reader.speak(cardBoard[r][c].language2, TextToSpeech.QUEUE_ADD, null, null);
                                firstPickStatus = true;
                                secondPick[0] = r;
                                secondPick[1] = c;
                                if (cardBoard[firstPick[0]][firstPick[1]].matches(cardBoard[r][c]))// pairs matched
                                {
                                    cardBoard[r][c].stateMatch = true;
                                    cardBoard[firstPick[0]][firstPick[1]].stateMatch = true;
                                    finalPlayers.get(playerIndex).score++;
                                    finalPlayers.get(playerIndex).setScore();
                                    matchedPairs++;
                                    if (matchedPairs == cards / 2) // END of the game
                                    {
                                        //todo end of the game
                                        cardBoard[firstPick[0]][firstPick[1]].img.setVisibility(View.INVISIBLE);
                                        cardBoard[secondPick[0]][secondPick[1]].img.setVisibility(View.INVISIBLE);
                                        txtLang2.setText("");
                                        txtLang1.setText("");
                                    }
                                    else  // NOT END of the game
                                    {
                                        if (time > 0)
                                        {
                                            startTimer();
                                        }
                                    }
                                }
                                else //pairs don't match
                                {
                                    nextPlayerPairsDontMatch();
                                    if (time > 0)
                                    {
                                        startTimer();
                                    }
                                }
                            }

                            cardBoard[r][c].stateHide = false;
                        }
                        else //clicking card which is already shown
                        {

                        }
                    }
                }
            };
        }

        for (int i = 0; i < cards; i++)
        {
            ImageView imageView = new ImageView(this);
            imageView.setImageDrawable(revers);
            imageView.setLayoutParams(layoutParams);
            imageView.setTag((i / wc) + "_" + (i % wc));
            imageView.setOnClickListener(onClickListener);
            cardBoard[i / wc][i % wc].img = imageView;
            gridBoard.addView(imageView);
        }
    }

    private void hideMatchedPair()
    {
        if (secondPick[0] != -1) // in the first round pRR can't be checked
        {
            if (cardBoard[firstPick[0]][firstPick[1]].stateMatch) // previous round card matched
            {
                cardBoard[firstPick[0]][firstPick[1]].img.setVisibility(View.INVISIBLE);
                cardBoard[secondPick[0]][secondPick[1]].img.setVisibility(View.INVISIBLE);
            }
            else // previous round card didn't matched
            {
                cardBoard[firstPick[0]][firstPick[1]].img.setImageDrawable(revers);
                cardBoard[secondPick[0]][secondPick[1]].img.setImageDrawable(revers);
                cardBoard[firstPick[0]][firstPick[1]].stateHide = true;
                cardBoard[secondPick[0]][secondPick[1]].stateHide = true;
            }
        }
    }

    private void nextPlayerPairsDontMatch()
    {
        playerIndex++;
        playerIndex %= players;
        txtTimer.setBackgroundColor(finalPlayers.get(playerIndex).color);
    }

    private void setPlayers()
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int w = displayMetrics.widthPixels - 10; // minus 4 to set little bigger margin
        w /= players;
        gridPlayers.setColumnCount(players);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(w - convertDpToPixel(2 * DP_MARGIN_PLAYERS, this), convertDpToPixel(DP_PLAYERS, this));
        layoutParams.setMargins(convertDpToPixel(DP_MARGIN_PLAYERS, this), 0, convertDpToPixel(DP_MARGIN_PLAYERS, this), 0);
        Collections.shuffle(finalPlayers);
        for (int i = 0; i < players; i++)
        {
            TextView textView = new TextView(this);
            textView.setText(finalPlayers.get(i).name + "\n0");
            finalPlayers.get(i).txtV = textView;
            textView.setBackgroundColor(finalPlayers.get(i).color);
            textView.setGravity(Gravity.CENTER);
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
        TextView txtV;

        public Player(int color, String name)
        {
            this.color = color;
            this.name = name;
        }

        public void setScore()
        {
            txtV.setText(name + "\n" + score);
        }
    }

    private void initPlayersFinalList()
    {
        finalPlayers = new ArrayList<>();
        finalPlayers.add(new Player(Color.rgb(191, 25, 25), "Red"));
        finalPlayers.add(new Player(Color.rgb(0, 196, 207), "Blue"));
        finalPlayers.add(new Player(Color.rgb(10, 191, 52), "Green"));
        finalPlayers.add(new Player(Color.rgb(255, 255, 5), "Yellow"));
        finalPlayers.add(new Player(Color.rgb(255, 105, 180), "Pink"));
        finalPlayers.add(new Player(Color.rgb(255, 101, 0), "Orange"));
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

        for (int i = 0; i < cardBoard.length - 1; i++)
        {
            cardBoard[i] = new Card[gridBoard.getColumnCount()];
        }
        cardBoard[cardBoard.length - 1] = new Card[cards % gridBoard.getColumnCount() == 0 ? gridBoard.getColumnCount() : cards % gridBoard.getColumnCount()];

        if (gm == 0) // GM CASUAL
        {
            for (int i = 0; i < cardBoard.length; i++)
            {
                for (int j = 0; j < cardBoard[i].length; j++)
                {
                    boolean zeroIndex = listImages.get(i * gridBoard.getColumnCount() + j).substring(listImages.get(i * gridBoard.getColumnCount() + j).indexOf("_") + 1, listImages.get(i * gridBoard.getColumnCount() + j).indexOf(".")).equals("0");
                    short id = Short.parseShort(listImages.get(i * gridBoard.getColumnCount() + j).substring(0, listImages.get(i * gridBoard.getColumnCount() + j).indexOf("_")));

                    cardBoard[i][j] = new Card(zeroIndex, null, null, id);
                }
            }
        }
        else if (gm == 1)// GM LEARN MODE
        {
            ArrayList<String> lang1st = readLines("Decks/" + deck + "/Lang/" + language1 + "_" + langCode1 + "_" + countryCode1 + ".txt");
            ArrayList<String> lang2nd = readLines("Decks/" + deck + "/Lang/" + language2 + "_" + langCode2 + "_" + countryCode2 + ".txt");

            for (int i = 0; i < cardBoard.length; i++)
            {
                for (int j = 0; j < cardBoard[i].length; j++)
                {
                    boolean zeroIndex = listImages.get(i * gridBoard.getColumnCount() + j).substring(listImages.get(i * gridBoard.getColumnCount() + j).indexOf("_") + 1, listImages.get(i * gridBoard.getColumnCount() + j).indexOf(".")).equals("0");
                    short id = Short.parseShort(listImages.get(i * gridBoard.getColumnCount() + j).substring(0, listImages.get(i * gridBoard.getColumnCount() + j).indexOf("_")));

                    cardBoard[i][j] = new Card(zeroIndex, lang1st.get(id), lang2nd.get(id), id);
                }
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

    private Drawable loadImageFromAssets(String path)
    {
        AssetManager assetManager = getAssets();
        try
        {
            InputStream ims = assetManager.open(path);
            Drawable d = Drawable.createFromStream(ims, null);
            return d;
        }
        catch (Exception ex)
        {
            return null;
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
        ImageView img;

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

        public String fileExt()//return file/image name with extension .png
        {
            return id + "_" + (zeroIndex ? 0 : 1) + ".png";
        }
    }
}
