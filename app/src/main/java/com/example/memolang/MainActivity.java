package com.example.memolang;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener
{
    ///  CONST
    final static int MAX_PLAYERS = 5;
    /// VIEWS
    ImageView imgDeck;
    TextView txtGM;
    TextView txtPlayer;
    TextView txtPairs;
    /// VARIABLES
    String[] decks; //all decks from folder by name without num of max pairs
    String[] decks_path; //all decks from folder full path
    int[] actSelectedDeck = {0}; //index of actual selected deck
    String[] gameModes;
    int[] actSelectedGM = {0}; //index of actual selected gameMode;
    int actSelectedPlayer = 1; //actual number of players
    int actSelectedPairs; //actual number of players
    int maxPairs[];
    byte trackNumberPick = 0; // 0 - players, 1 - pairs, 2 - time

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgDeck = findViewById(R.id.imgDeck);
        txtGM = findViewById(R.id.txtGM);
        txtPlayer = findViewById(R.id.txtPlayers);
        txtPairs = findViewById(R.id.txtPairs);

        Functions.hideNavigationBar(this);
        getDeckList();
        gameModes = new String[]{getString(R.string.casual), getString(R.string.learnMode)};

        setImageFromAssets(imgDeck, "Decks/" + decks_path[0] + "/" + decks[0] + ".png");
        txtGM.setText(gameModes[0]);
        txtPlayer.setText(Integer.toString(actSelectedPlayer));
        txtPairs.setText(Integer.toString(maxPairs[actSelectedDeck[0]]));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Functions.hideNavigationBar(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN))
        {

        }
        return true;
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

    private void getDeckList() //load decks from folder and set
    {
        Pattern pattern = Pattern.compile("([a-zA-Z]+)_(\\d+)");
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
            maxPairs[i] = Integer.parseInt(matcher.group(2));
        }
    }

    private void showSelectDialog(final String[] list, final int[] actualSelectedItem, String title)
    {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        // add a radio button list
        builder.setSingleChoiceItems(list, actualSelectedItem[0], new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialog, int index)
            {
                // user checked an item
                actualSelectedItem[0] = index;
            }
        });
        // add OK and Cancel buttons
        builder.setPositiveButton(getString(R.string.select), new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialog, int index)
            {
                System.out.println(actSelectedDeck[0]);
                hideNavigationBar();
                itemSelected(list, actualSelectedItem);
                // user clicked OK
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    ///FUNCTION from other class, try to set one global function for all
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

    public void butSelectDeckClick(View view)
    {
        showSelectDialog(decks, actSelectedDeck, getString(R.string.select_deck));
    }

    public void butSelectGMClick(View view)
    {
        showSelectDialog(gameModes, actSelectedGM, getString(R.string.select_GM));
    }

    public void butSelectPlayersClick(View view)
    {
        trackNumberPick = 0;
        showNumberPicker(1, MAX_PLAYERS, getString(R.string.num_of_players));
    }

    public void butSelectPairsClick(View view)
    {
        trackNumberPick = 1;
        showNumberPicker(1, maxPairs[actSelectedDeck[0]], getString(R.string.num_pairs));
    }

    private void itemSelected(String[] list, int[] index)
    {
        if (list == decks)
        {
            setImageFromAssets(imgDeck, "Decks/" + decks_path[index[0]] + "/" + list[index[0]] + ".png");
            actSelectedPairs = maxPairs[index[0]];
            txtPairs.setText(Integer.toString(actSelectedPairs));
        }
        else if (list == gameModes)
        {
            txtGM.setText(list[index[0]]);
        }
    }

    public void butPlayClick(View view)
    {
        Intent intent = new Intent(getApplicationContext(), game_board.class);
        intent.putExtra("Deck", decks[actSelectedDeck[0]]);
        intent.putExtra("Players", actSelectedPlayer);
        intent.putExtra("GM", gameModes[actSelectedGM[0]]);
        intent.putExtra("Cards", actSelectedPairs * 2);
        startActivity(intent);
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1)
    {
        if (trackNumberPick == 0)
        {
            actSelectedPlayer = i;
            txtPlayer.setText(Integer.toString(i));
        }
        else if (trackNumberPick == 1)
        {
            actSelectedPairs = i;
            txtPairs.setText(Integer.toString(i));
        }
        else if (trackNumberPick == 2)
        {
            //todo zmiana timera na runde
            System.out.println(1);
        }
        hideNavigationBar();
    }

    public void showNumberPicker(int min, int max, String title)
    {
        NumberPickerDialog newFragment = new NumberPickerDialog(min, max, title);
        newFragment.setValueChangeListener(this);
        newFragment.show(getSupportFragmentManager(), "Number picker");
    }
}
