package com.example.memolang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class game_board extends AppCompatActivity
{
    /// CONST
    final static int DURATION_ANIM = 450;
    final static int VANISH_DELAY = 1100;
    final static int DURATION_ZOOM = 150;
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
    //ArrayList<String> listImages;
    Card[][] cardBoard;
    Drawable revers;
    /// In game playing variables
    CountDownTimer timer;
    int playerIndex = 0;
    int matchedPairs = 0;
    boolean firstPickStatus = true;
    boolean wasMatched = true;
    short[] firstPick = {-1, -1};
    short[] secondPick = {-1, -1};
    Locale locale1;
    Locale locale2;
    private TextToSpeech reader;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        Intent intent = getIntent();
        hideNavigationBar();
        SharedPreferences shPref = this.getSharedPreferences("com.example.memolang", Context.MODE_PRIVATE);

        setBackground("Background/" + shPref.getString("back", "default.png"));

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
        revers = loadImageFromAssets("Revers/" + shPref.getString("revers", "default.png"));
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
            txtTimer.setText(finalPlayers.get(0).name);
        }

        if (gm == 1) // learning mode
        {
            locale1 = new Locale(Objects.requireNonNull(intent.getStringExtra("LangCode1")), Objects.requireNonNull(intent.getStringExtra("CountryCode1")));
            locale2 = new Locale(Objects.requireNonNull(intent.getStringExtra("LangCode2")), Objects.requireNonNull(intent.getStringExtra("CountryCode2")));
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
            reader.setPitch(1.1f);
            reader.setSpeechRate(1.1f);
        }
    }

    private void setBackground(String path)
    {
        ConstraintLayout back = findViewById(R.id.conLayMain);
        ImageView imgBack = new ImageView(this);
        LinearLayout.LayoutParams Params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        imgBack.setLayoutParams(Params1);
        setImageFromAssets(imgBack, path);
        imgBack.setScaleType(ImageView.ScaleType.CENTER_CROP);
        back.addView(imgBack, 0);
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

    @Override
    protected void onResume()
    {
        super.onResume();
        hideNavigationBar();
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

    /// ANIMATIONS
    private void animateShow(final int r, final int c)
    {
        final Handler handler = new Handler();
        cardBoard[r][c].img.animate().rotationX(360).setDuration(2 * DURATION_ANIM);
        cardBoard[r][c].img.animate().scaleX(0.6f).scaleY(0.6f).setDuration(DURATION_ANIM);
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("Decks/" + deck + "/Cards/" + cardBoard[r][c].fileExt());
                setImageFromAssets(cardBoard[r][c].img, "Decks/" + deck + "/Cards/" + cardBoard[r][c].fileExt());
                cardBoard[r][c].img.animate().scaleX(1f).scaleY(1f).setDuration(DURATION_ANIM);
            }
        }, DURATION_ANIM / 2);
    }

    private void animateHide(final int r, final int c)
    {
        final Handler handler = new Handler();
        cardBoard[r][c].img.animate().rotationX(180).setDuration(2 * DURATION_ANIM);
        cardBoard[r][c].img.animate().scaleX(0.6f).scaleY(0.6f).setDuration(DURATION_ANIM);
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                cardBoard[r][c].img.setImageDrawable(revers);
                cardBoard[r][c].img.animate().scaleX(1f).scaleY(1f).setDuration(DURATION_ANIM);
            }
        }, DURATION_ANIM / 2);
    }

    private void animateZoom(final int r, final int c)
    {
        final Handler handler = new Handler();
        cardBoard[r][c].img.animate().scaleX(0.9f).scaleY(0.9f).setDuration(DURATION_ZOOM);
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                cardBoard[r][c].img.animate().scaleX(1).scaleY(1).setDuration(DURATION_ZOOM);
            }
        }, DURATION_ZOOM);
    }

    private void animateVanish(final int r, final int c)
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                cardBoard[r][c].img.animate().rotation(720).scaleX(0).scaleY(0).setDuration(DURATION_ANIM);
            }
        }, VANISH_DELAY);
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
                if (firstPick[0] > -1)
                {
                    if (!cardBoard[firstPick[0]][firstPick[1]].isHide)
                    {
                        cardBoard[firstPick[0]][firstPick[1]].isHide = true;
                        animateHide(firstPick[0], firstPick[1]);
                    }
                }
                if (secondPick[0] > -1)
                {
                    if (!cardBoard[secondPick[0]][secondPick[1]].isHide)
                    {
                        cardBoard[secondPick[0]][secondPick[1]].isHide = true;
                        animateHide(secondPick[0], secondPick[1]);
                    }
                }
                firstPickStatus = true;
                wasMatched = true;
                nextPlayerNoDelay();
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
        int wc = gridBoard.getColumnCount();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(CARD_SIZE_PIXEL - 2 * MARGIN_SIZE_PIXELS, CARD_SIZE_PIXEL - 2 * MARGIN_SIZE_PIXELS);
        layoutParams.setMargins(MARGIN_SIZE_PIXELS, MARGIN_SIZE_PIXELS, MARGIN_SIZE_PIXELS, MARGIN_SIZE_PIXELS);
        View.OnClickListener onClickListener;
        onClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int r = Integer.parseInt(v.getTag().toString().substring(0, v.getTag().toString().indexOf("_")));
                int c = Integer.parseInt(v.getTag().toString().substring(v.getTag().toString().indexOf("_") + 1));
                if (!cardBoard[r][c].isMatched)// CARD ISN'T MATCHED YET
                {
                    if (!wasMatched && firstPickStatus)//previous round card wasn't matched
                    {
                        cardBoard[firstPick[0]][firstPick[1]].isHide = true;
                        cardBoard[secondPick[0]][secondPick[1]].isHide = true;
                        animateHide(firstPick[0], firstPick[1]);
                        animateHide(secondPick[0], secondPick[1]);
                    }
                    if (cardBoard[r][c].isHide)// CARD IS HIDDEN
                    {
                        cardBoard[r][c].isHide = false;
                        animateShow(r, c);
                        if (firstPickStatus) //clicking first card of the round
                        {

                            firstPick[0] = (short) r;
                            firstPick[1] = (short) c;
                            firstPickStatus = false;
                            if (gm == 1)// LEARNING MODE
                            {
                                txtLang1.setText(cardBoard[r][c].language1);
                                reader.stop();
                                reader.setLanguage(locale1);
                                reader.speak(cardBoard[r][c].language1, TextToSpeech.QUEUE_ADD, null, null);
                                txtLang2.setText("");
                            }
                        }
                        else //clicking second card of the round
                        {
                            secondPick[0] = (short) r;
                            secondPick[1] = (short) c;
                            firstPickStatus = true;
                            if (gm == 1) // LEARNING MODE
                            {
                                txtLang2.setText(cardBoard[r][c].language2);
                                reader.setLanguage(locale2);
                                reader.speak(cardBoard[r][c].language2, TextToSpeech.QUEUE_ADD, null, null);
                            }
                            if (time > 0)
                            {
                                timer.cancel();
                            }
                            if (cardBoard[firstPick[0]][firstPick[1]].matches(cardBoard[secondPick[0]][secondPick[1]])) // PAIR MATCHED
                            {
                                finalPlayers.get(playerIndex).score++;
                                finalPlayers.get(playerIndex).setScore();
                                cardBoard[firstPick[0]][firstPick[1]].isMatched = true;
                                cardBoard[secondPick[0]][secondPick[1]].isMatched = true;
                                matchedPairs++;
                                wasMatched = true;
                                animateVanish(firstPick[0], firstPick[1]);
                                animateVanish(secondPick[0], secondPick[1]);
                            }
                            else // pair doesn't match
                            {
                                wasMatched = false;
                                nextPlayer();
                            }
                            if (matchedPairs == cards / 2)//END OF THE GAME
                            {
                                endOfGame();
                            }
                            else // NOT END of the game
                            {
                                if (time > 0)
                                {
                                    startTimer();
                                }
                            }
                        }
                    }
                    else // CARD IS ALREADY SHOWN
                    {
                        animateZoom(r, c);
                    }
                }
            }
        };

        for (int i = 0; i < cards; i++)
        {
            ImageView imageView = new ImageView(this);
            imageView.setImageDrawable(revers);
            imageView.setLayoutParams(layoutParams);
            imageView.setTag((i / wc) + "_" + (i % wc));
            imageView.setOnClickListener(onClickListener);
            imageView.setBackgroundResource(R.drawable.gradient_card);
            imageView.setRotationX(180f);
            cardBoard[i / wc][i % wc].img = imageView;
            gridBoard.addView(imageView);
        }
    }

    private String winner()
    {
        if (players == 1)
        {
            return "The winner is... There was only one player so its only one option: " + finalPlayers.get(0).name;
        }
        int max = finalPlayers.get(0).score;
        ArrayList<Integer> winnersIndexes = new ArrayList<>();
        winnersIndexes.add(0);
        for (int i = 1; i < finalPlayers.size(); i++)
        {
            if (max < finalPlayers.get(i).score)
            {
                winnersIndexes.clear();
                winnersIndexes.add(i);
                max = finalPlayers.get(i).score;
            }
            else if (max == finalPlayers.get(i).score)
            {
                winnersIndexes.add(i);
            }
        }

        if (winnersIndexes.size() == 1)//only one winner
        {
            return "The winner is... " + finalPlayers.get(winnersIndexes.get(0)).name + "! With score " + max;
        }
        else if (winnersIndexes.size() == players && players > 2) //all players with the same score and number of players is more than 2
        {
            return "The winner is... Everyone!!! with score " + max;
        }
        else //many winners
        {
            StringBuilder x = new StringBuilder("The winner is... Draw! ");

            for (int i = 0; i < winnersIndexes.size(); i++)
            {
                x.append(finalPlayers.get(winnersIndexes.get(i)).name).append(" and ");
            }
            x = new StringBuilder(x.substring(0, x.length() - 4));
            x.append("has the same score: ").append(max);
            return x.toString();
        }
    }

    private void endOfGame()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(winner());
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "Play again",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        finish();
                        startActivity(getIntent());
                        dialog.cancel();
                    }
                });

        builder1.setNeutralButton(
                "Menu",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        finish();
                        dialog.cancel();
                    }
                });

        final AlertDialog alert11 = builder1.create();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                alert11.show();
            }
        }, VANISH_DELAY + DURATION_ANIM);
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

    private void nextPlayer()
    {
        playerIndex++;
        playerIndex %= players;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                txtTimer.setBackgroundColor(finalPlayers.get(playerIndex).color);
                txtTimer.setText(finalPlayers.get(playerIndex).name);
            }
        }, DURATION_ANIM);
    }

    private void nextPlayerNoDelay()
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
            textView.setTextColor(Color.BLACK);
            gridPlayers.addView(textView);
        }
    }

    private class Player
    {
        int color;
        String name;
        int score = 0;
        TextView txtV;

        Player(int color, String name)
        {
            this.color = color;
            this.name = name;
        }

        void setScore()
        {
            txtV.setText(name + "\n" + score);
        }
    }

    public String getEmojiByUnicode(int unicode)
    {
        return new String(Character.toChars(unicode));
    }

    private void initPlayersFinalList()
    {
        finalPlayers = new ArrayList<>();
        finalPlayers.add(new Player(Color.rgb(191, 25, 25), getEmojiByUnicode(0x1F42E))); // COW
        finalPlayers.add(new Player(Color.rgb(0, 196, 207), getEmojiByUnicode(0x1F434))); // HORSE
        finalPlayers.add(new Player(Color.rgb(10, 191, 52), getEmojiByUnicode(0x1F436))); // DOG
        finalPlayers.add(new Player(Color.rgb(255, 255, 5), getEmojiByUnicode(0x1F43C))); // PANDA
        finalPlayers.add(new Player(Color.rgb(255, 105, 180), getEmojiByUnicode(0x1F408))); // CAT
        finalPlayers.add(new Player(Color.rgb(255, 101, 0), getEmojiByUnicode(0x1F43B))); // BEAR
    }

    public ArrayList<String> readLines(String filename)
    {
        ArrayList<String> lang = new ArrayList<>();
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(getAssets().open(filename), StandardCharsets.UTF_8));
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

        Pattern pattern = Pattern.compile("([a-zA-Z]+)_(\\d+)_(\\d)");
        Matcher matcher;
        matcher = pattern.matcher(deck);
        matcher.matches();

        if (Objects.equals(matcher.group(3), "2"))
        {
            ArrayList<String> listImages;
            ArrayList<Integer> list = new ArrayList<>();
            for (int i = 0; i < Integer.parseInt(Objects.requireNonNull(matcher.group(2))); i++)
            {
                list.add(i);
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
        else if (Objects.equals(matcher.group(3), "1"))
        {
            String[] listCard = null;
            try
            {
                listCard = getAssets().list("Decks/" + deck + "/Cards");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            assert listCard != null;
            Collections.shuffle(Arrays.asList(listCard));
            String[] listCard2 = new String[cards];

            for (int i = 0; i < cards / 2; i++)
            {
                listCard2[i * 2] = listCard[i];
                listCard2[i * 2 + 1] = listCard[i];
            }
            Collections.shuffle(Arrays.asList(listCard2));

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
                        short id = Short.parseShort(listCard2[i * gridBoard.getColumnCount() + j].substring(0, listCard2[i * gridBoard.getColumnCount() + j].indexOf("_")));

                        cardBoard[i][j] = new Card(true, null, null, id);
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
                        short id = Short.parseShort(listCard2[i * gridBoard.getColumnCount() + j].substring(0, listCard2[i * gridBoard.getColumnCount() + j].indexOf("_")));
                        cardBoard[i][j] = new Card(true, lang1st.get(id), lang2nd.get(id), id);
                    }
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
        catch (Exception ignored)
        {
        }
    }

    private Drawable loadImageFromAssets(String path)
    {
        AssetManager assetManager = getAssets();
        try
        {
            InputStream ims = assetManager.open(path);
            return Drawable.createFromStream(ims, null);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private class Card
    {
        boolean isHide = true;
        boolean isMatched = false;
        boolean zeroIndex;
        String language1;
        String language2;
        short id;
        ImageView img;

        Card(boolean zeroIndex, String language1, String language2, short id)
        {
            this.zeroIndex = zeroIndex;
            this.language1 = language1;
            this.language2 = language2;
            this.id = id;
        }

        boolean matches(Card card)
        {
            return this.id == card.id;
        }

        String fileExt()//return file/image name with extension .png
        {
            return id + "_" + (zeroIndex ? 0 : 1) + ".png";
        }
    }
}
