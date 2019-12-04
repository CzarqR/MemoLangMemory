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
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity
{
    /// VIEWS
    ImageView imgDeck;
    TextView txtGM;
    TextView txtPlayer;


    /// VARIABLES
    String[] decks; //all decks from folder
    int[] actSelectedDeck = {0}; //index of actual selected deck
    String[] gameModes;
    int[] actSelectedGM = {0}; //index of actual selected gameMode
    String[] players = {"1", "2", "3", "4", "5", "6"};
    int[] actSelectedPlayer = {0}; //index of actual selected gameMode


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgDeck = findViewById(R.id.imgDeck);
        txtGM = findViewById(R.id.txtGM);
        txtPlayer = findViewById(R.id.txtPlayers);

        Functions.hideNavigationBar(this);
        decks = getDeckList();
        gameModes = new String[]{getString(R.string.casual), getString(R.string.learnMode)};

        setImageFromAssets(imgDeck, "Decks/" + decks[0] + "/" + decks[0] + ".png");
        txtGM.setText(gameModes[0]);
        txtPlayer.setText(players[0]);
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

    private String[] getDeckList()
    {
        try
        {
            return getAssets().list("Decks");
        }
        catch (IOException e)
        {
            return new String[0];
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
        showSelectDialog(players, actSelectedPlayer, getString(R.string.select_players));
    }

    private void itemSelected(String[] list, int[] index)
    {
        if (list == decks)
        {
            setImageFromAssets(imgDeck, "Decks/" + list[index[0]] + "/" + list[index[0]] + ".png");
        }
        else if (list == gameModes)
        {
            txtGM.setText(list[index[0]]);
        }
        else if (list == players)
        {
            txtPlayer.setText(list[index[0]]);
        }
    }

    public void butPlayClick(View view)
    {
        Intent intent = new Intent(getApplicationContext(), game_board.class);
        intent.putExtra("Deck", decks[actSelectedDeck[0]]);
        intent.putExtra("Players", players[actSelectedPlayer[0]]);
        intent.putExtra("GM", gameModes[actSelectedGM[0]]);
        startActivity(intent);
    }


}
