package com.example.memolang;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// If someone is reading this code or I am older and decide to look at my old app. I know... many things could me make better xD
public class MainActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener
{
    ///  CONST
    final static int MAX_PLAYERS = 6;
    final static int BASE_TIMER = 10;
    final static int TIMER_MAX = 60;
    /// VIEWS
    TextView txtSelectedPlayers;
    TextView txtSelectedPairs;
    TextView txtSelectedTime;
    TextView txtLang;
    ImageView imgL1;
    ImageView imgL2;
    Switch switchTimer;
    Switch switchLang;
    ViewPager viewPager;
    SharedPreferences shPref;
    /// VARIABLES
    String[] decks; //all decks from folder by name without num of max pairs
    String[] decks_path; //all decks from folder full path
    int[] actSelectedDeck; //index of actual selected deck
    int[] actSelectedLang1 = {0}; //index of actual selected deck
    int[] actSelectedLang2 = {0}; //index of actual selected deck
    String[] languages;
    String[] langCode;
    String[] countryCode;
    int actSelectedPlayer = 1; //actual number of players
    int actSelectedPairs; //actual number of players
    int[] maxPairs;
    byte trackNumberPick = 0; // 0 - players, 1 - pairs, 2 - time
    int actSelectedTime = -1;
    boolean trackLang1; //True - lang1 chosen, false - lang 2

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        /// Load Views
        ImageButton imageButton = findViewById(R.id.imgButSetttings);
        AssetManager assetManager = getAssets();
        try
        {
            InputStream ims = assetManager.open("Icons/geer.png");
            Drawable d = Drawable.createFromStream(ims, null);
            imageButton.setImageDrawable(d);
        }
        catch (Exception ignored)
        {
            Log.e("NoImage", "Couldn't lod image from path: Icons/geer.png");
        }

        txtSelectedPlayers = findViewById(R.id.txtSelectedPlayers);
        txtSelectedPairs = findViewById(R.id.txtSelectedPairs);
        txtSelectedTime = findViewById(R.id.txtSelectedTime);
        txtLang = findViewById(R.id.txtLang);
        switchTimer = findViewById(R.id.switchTimer);
        switchLang = findViewById(R.id.switchLangs);
        imgL1 = findViewById(R.id.imgL1);
        imgL2 = findViewById(R.id.imgL2);
        viewPager = findViewById(R.id.vpDeckPicker);
        shPref = this.getSharedPreferences("com.example.memolang", Context.MODE_PRIVATE);
        /// Start functions
        setGuideLine();
        hideNavigationBar();
        getDeckList();
        loadSharedPrecedences();

        actSelectedPlayer = shPref.getInt("playersPref", 2);
        actSelectedDeck = new int[]{shPref.getInt("lastDeck", decks_path.length / 2)};
        if (actSelectedDeck[0] > decks_path.length)
        {
            actSelectedDeck[0] = decks_path.length / 2;
        }
        actSelectedPairs = shPref.getInt("pairsPref", 16);

        if (shPref.getBoolean("timerOnPref", false))
        {
            switchTimer.setChecked(true);
            actSelectedTime = BASE_TIMER;
            txtSelectedTime.setText(String.format(getResources().getConfiguration().locale, "%d", actSelectedTime));
        }

        getLangList();
        if (maxPairs[actSelectedDeck[0]] < actSelectedPairs)
            actSelectedPairs = maxPairs[actSelectedDeck[0]];

        if (shPref.getBoolean("langOnPref", true))
        {
            switchLang.setChecked(true);
        }
        loadLanguages();

        /// Setting base startup
        txtSelectedPlayers.setText(String.format(getResources().getConfiguration().locale, "%d", actSelectedPlayer));
        txtSelectedPairs.setText(String.format(getResources().getConfiguration().locale, "%d", actSelectedPairs));

        setDeckPicker();
    }

    private void loadLanguages()
    {
        actSelectedLang1[0] = foundLanguageIndex(shPref.getString("lang1Pref", "English"));
        if (actSelectedLang1[0] < 0)
            actSelectedLang1[0] = 0;
        actSelectedLang2[0] = foundLanguageIndex(shPref.getString("lang2Pref", "X"));
        if (actSelectedLang2[0] < 0)
            actSelectedLang2[0] = 0;
        switchLangsClick(null);
    }

    private int foundLanguageIndex(String l)
    {
        for (int i = 0; i < languages.length; i++)
        {
            if (languages[i].equals(l))
                return i;
        }
        return -1;
    }

    private void setGuideLine()
    {
        final float appNameHeight = 0.075f;
        final float weightOptions = 1f;
        final float weightPlay = 1.5f;
        final float weightSettings = 0.75f;
        final int numbersOfOptions = 4;
        final float viewPagerMargin = 0.06f;
        Guideline gdV1 = findViewById(R.id.gdV1);
        Guideline gdV2 = findViewById(R.id.gdV2);

        gdV1.setGuidelinePercent(viewPagerMargin);
        gdV2.setGuidelinePercent(1f - viewPagerMargin);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Guideline guideline1 = findViewById(R.id.gl1);
        guideline1.setGuidelinePercent(appNameHeight);
        float occupied = (displayMetrics.widthPixels * (1f - 2 * viewPagerMargin)) / displayMetrics.heightPixels + appNameHeight;
        Guideline guideline2 = findViewById(R.id.gl2);
        guideline2.setGuidelinePercent(occupied);

        float free = 1f - occupied;
        float weightSum = weightOptions * numbersOfOptions + weightPlay + weightSettings;

        occupied += (free * weightOptions / weightSum);
        Guideline guideline4 = findViewById(R.id.gl4);
        guideline4.setGuidelinePercent(occupied);

        occupied += (free * weightOptions / weightSum);
        Guideline guideline5 = findViewById(R.id.gl5);
        guideline5.setGuidelinePercent(occupied);

        occupied += (free * weightOptions / weightSum);
        Guideline guideline6 = findViewById(R.id.gl6);
        guideline6.setGuidelinePercent(occupied);
        int imgLangSize = (int) (displayMetrics.heightPixels * (free * weightOptions / weightSum));
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) imgL1.getLayoutParams();
        params.width = imgLangSize;
        imgL1.setLayoutParams(params);
        params = (ConstraintLayout.LayoutParams) imgL2.getLayoutParams();
        params.width = imgLangSize;
        imgL2.setLayoutParams(params);

        occupied += (free * weightOptions / weightSum);
        Guideline guideline7 = findViewById(R.id.gl7);
        guideline7.setGuidelinePercent(occupied);

        occupied += (free * weightPlay / weightSum);
        Guideline guideline8 = findViewById(R.id.gl8);
        guideline8.setGuidelinePercent(occupied);
    }

    private void loadSharedPrecedences()
    {
        if (!shPref.contains("firstRun"))
        {
            shPref.edit().putBoolean("firstRun", true).apply();
            shPref.edit().putString("revers", "default.png").apply();
            shPref.edit().putString("back", "default.png").apply();
            shPref.edit().putInt("primBack", SettingsActivity.COLORS_CARD_DEF[0]).apply();
            shPref.edit().putInt("secBack", SettingsActivity.COLORS_CARD_DEF[1]).apply();
            shPref.edit().putInt("playersPref", 2).apply();
            shPref.edit().putInt("pairsPref", 16).apply();
            shPref.edit().putInt("lastDeck", decks_path.length / 2 - 1).apply();
            shPref.edit().putBoolean("timerOnPref", false).apply();
            shPref.edit().putInt("timePref", BASE_TIMER).apply();
            shPref.edit().putBoolean("langOnPref", true).apply();
            shPref.edit().putString("lang1Pref", "English").apply();
            shPref.edit().putString("lang2Pref", "Polski").apply();
            shPref.edit().putBoolean("firstGame", true).apply();
            welcomeMsg();
        }
    }

    public void welcomeMsg()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.welcome_msg, GameBoard.getEmojiByUnicode(0x2764), GameBoard.getEmojiByUnicode(0x1F605), getString(R.string.email)));
        builder.setCancelable(false);

        builder.setPositiveButton(
                getString(R.string.lets_start),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });

        final AlertDialog welcome = builder.create();
        welcome.show();
        Objects.requireNonNull(welcome.getWindow()).setBackgroundDrawableResource(R.drawable.gradient_background_msg);
    }

    private void setDeckPicker()
    {
        String[] deckList = new String[decks_path.length];
        for (int i = 0; i < deckList.length; i++)
        {
            deckList[i] = "DecksList/" + decks_path[i] + ".png";
        }

        ImageAdapter deckPicker = new ImageAdapter(this, deckList, ImageView.ScaleType.FIT_CENTER);
        viewPager.setAdapter(deckPicker);
        viewPager.setCurrentItem(actSelectedDeck[0]);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                actSelectedDeck[0] = position;
                changeDeck();
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });
    }

    /// BACKGROUND ANDROID FUNCTIONS

    @Override
    protected void onResume()
    {
        super.onResume();
        hideNavigationBar();
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

    /// onClick FUNCTIONS

    public void txtLang1Click(View view)
    {
        trackLang1 = true;
        showSelectDialog(languages, actSelectedLang1, getString(R.string.select_lang1));
    }

    public void txtLang2Click(View view)
    {
        trackLang1 = false;
        showSelectDialog(languages, actSelectedLang2, getString(R.string.select_lang2));
    }

    public void butSelectPlayersClick(View view)
    {
        trackNumberPick = 0;
        showNumberPicker(1, MAX_PLAYERS, getString(R.string.num_of_players), actSelectedPlayer);
    }

    public void butSelectPairsClick(View view)
    {
        trackNumberPick = 1;
        showNumberPicker(2, maxPairs[actSelectedDeck[0]], getString(R.string.num_pairs), actSelectedPairs);
    }

    public void butSettingsClick(View view)
    {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);

        startActivity(intent);
    }

    public void butPlayClick(View view)
    {
        Intent intent = new Intent(getApplicationContext(), GameBoard.class);
        shPref.edit().putInt("lastDeck", actSelectedDeck[0]).apply();
        intent.putExtra("Deck", decks_path[actSelectedDeck[0]]);
        intent.putExtra("Players", actSelectedPlayer);

        if (switchLang.isChecked())//GM learning mode
        {
            intent.putExtra("Lang1", languages[actSelectedLang1[0]]);
            intent.putExtra("Lang2", languages[actSelectedLang2[0]]);
            intent.putExtra("LangCode1", langCode[actSelectedLang1[0]]);
            intent.putExtra("LangCode2", langCode[actSelectedLang2[0]]);
            intent.putExtra("CountryCode2", countryCode[actSelectedLang2[0]]);
            intent.putExtra("CountryCode1", countryCode[actSelectedLang1[0]]);
        }

        intent.putExtra("Lang", switchLang.isChecked());
        intent.putExtra("Cards", actSelectedPairs * 2);
        intent.putExtra("Time", switchTimer.isChecked() ? actSelectedTime : -1);

        startActivity(intent);
    }

    public void switchTimerClick(View view)
    {

        if (switchTimer.isChecked())
        {
            if (actSelectedTime == -1)
            {
                actSelectedTime = BASE_TIMER;
            }
            txtSelectedTime.setText(String.format(getResources().getConfiguration().locale, "%d", actSelectedTime));
        }
        else
        {
            txtSelectedTime.setText(getResources().getString(R.string.off));
        }
    }

    public void txtTimerClick(View view)
    {
        if (switchTimer.isChecked())
        {
            trackNumberPick = 2;
            showNumberPicker(BASE_TIMER / 2, TIMER_MAX, getString(R.string.select_time), actSelectedTime);
        }
    }

    public void switchLangsClick(View view)
    {
        shPref.edit().putBoolean("langOnPref", switchLang.isChecked()).apply();
        if (!switchLang.isChecked())//normal mode
        {
            imgL2.setVisibility(View.INVISIBLE);
            imgL1.setVisibility(View.INVISIBLE);
        }
        else
        {
            switchLang.isChecked();
            imgL1.setVisibility(View.VISIBLE);
            imgL2.setVisibility(View.VISIBLE);
            txtLang.setVisibility(View.VISIBLE);
            setImageFromAssets(imgL1, "Langs/" + languages[actSelectedLang1[0]] + ".png");
            setImageFromAssets(imgL2, "Langs/" + languages[actSelectedLang2[0]] + ".png");
        }
    }

    /// NUMBER PICKER

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1)
    {
        if (trackNumberPick == 0)
        {
            actSelectedPlayer = i;
            txtSelectedPlayers.setText(String.format(getResources().getConfiguration().locale, "%d", i));
            shPref.edit().putInt("playersPref", i).apply();
        }
        else if (trackNumberPick == 1)
        {
            actSelectedPairs = i;
            txtSelectedPairs.setText(String.format(getResources().getConfiguration().locale, "%d", i));
            shPref.edit().putInt("pairsPref", i).apply();
        }
        else if (trackNumberPick == 2)
        {
            actSelectedTime = i;
            txtSelectedTime.setText(String.format(getResources().getConfiguration().locale, "%d", i));
            shPref.edit().putInt("timerPref", i).apply();
        }
        hideNavigationBar();
    }

    public void showNumberPicker(int min, int max, String title, int current)
    {
        NumberPickerDialog newFragment = new NumberPickerDialog(min, max, title, current);
        newFragment.setValueChangeListener(this);
        newFragment.setCancelable(false);
        newFragment.show(getSupportFragmentManager(), "Number picker");
    }

    /// OTHER FUNCTIONS

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
            Log.e("NoImage", "Couldn't lod image from path: " + path);
        }
    }

    private void getDeckList() //load decks from folder and set
    {
        Pattern pattern = Pattern.compile("([a-zA-Z]+)_(\\d+)_(\\d)");
        Matcher matcher;
        try
        {
            decks = getAssets().list("Decks");
            decks_path = getAssets().list("Decks");
            maxPairs = new int[decks.length];
        }
        catch (IOException e)
        {
            maxPairs = new int[0];
            decks = new String[0];
            decks_path = new String[0];
        }
        for (int i = 0; i < decks.length; i++)
        {
            matcher = pattern.matcher(decks[i]);
            matcher.matches();
            decks[i] = matcher.group(1);
            maxPairs[i] = Integer.parseInt(Objects.requireNonNull(matcher.group(2)));
        }
    }

    private void getLangList()
    {
        Pattern pattern = Pattern.compile("(.*)_([a-z]+)_([A-Z]+)\\.[a-z]+");
        Matcher matcher;
        try
        {
            languages = getAssets().list("Decks/" + decks_path[actSelectedDeck[0]] + "/Lang");
            assert languages != null;
            langCode = new String[languages.length];
            countryCode = new String[languages.length];
        }
        catch (IOException e)
        {
            languages = new String[0];
        }
        for (int i = 0; i < languages.length; i++)
        {
            matcher = pattern.matcher(languages[i]);
            matcher.matches();
            languages[i] = matcher.group(1);
            langCode[i] = matcher.group(2);
            countryCode[i] = matcher.group(3);
        }
    }

    private void showSelectDialog(final String[] list, final int[] actualSelectedItem, String title)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setSingleChoiceItems(list, actualSelectedItem[0], new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int index)
            {
                actualSelectedItem[0] = index;
            }
        });
        builder.setPositiveButton(getString(R.string.select), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int index)
            {
                hideNavigationBar();
                itemSelected(list, actualSelectedItem);
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                hideNavigationBar();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        //Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.gradient_background_picker);
    }

    private void itemSelected(String[] list, int[] index) /// functions executed after picking value in dialog
    {
        if (list == decks)
        {
            getLangList();
            changeDeck();

            viewPager.setCurrentItem(index[0]);
        }

        else if (list == languages)
        {
            if (trackLang1) // first language picked
            {
                setImageFromAssets(imgL1, "Langs/" + languages[actSelectedLang1[0]] + ".png");
                shPref.edit().putString("lang1Pref", languages[actSelectedLang1[0]]).apply();
            }
            else // second language picked
            {
                setImageFromAssets(imgL2, "Langs/" + languages[actSelectedLang2[0]] + ".png");
                shPref.edit().putString("lang2Pref", languages[actSelectedLang2[0]]).apply();
            }
        }
    }

    private void changeDeck()
    {
        getLangList();
        if (switchLang.isChecked())//language mode
        {
            loadLanguages();
        }
        actSelectedPairs = shPref.getInt("pairsPref", 16);
        if (maxPairs[actSelectedDeck[0]] < actSelectedPairs)
            actSelectedPairs = maxPairs[actSelectedDeck[0]];

        txtSelectedPairs.setText(String.format(getResources().getConfiguration().locale, "%d", actSelectedPairs));
    }

    public static class NumberPickerDialog extends DialogFragment
    {
        private NumberPicker.OnValueChangeListener valueChangeListener;
        int min, max, current;
        String title;

        NumberPickerDialog(int min, int max, String title, int current)
        {
            this.min = min;
            this.max = max;
            this.title = title;
            this.current = current;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {

            final NumberPicker numberPicker = new NumberPicker(getActivity());

            numberPicker.setMinValue(min);
            numberPicker.setMaxValue(max);
            numberPicker.setValue(current);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(title);
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    valueChangeListener.onValueChange(numberPicker,
                            numberPicker.getValue(), numberPicker.getValue());
                }
            });

            builder.setView(numberPicker);
            return builder.create();
        }

        void setValueChangeListener(NumberPicker.OnValueChangeListener valueChangeListener)
        {
            this.valueChangeListener = valueChangeListener;
        }
    }
}
