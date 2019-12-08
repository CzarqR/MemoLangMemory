package com.example.memolang;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.fragment.app.DialogFragment;

public class NumberPickerDialog extends DialogFragment
{
    private NumberPicker.OnValueChangeListener valueChangeListener;
    int min, max;
    String title;

    public NumberPickerDialog(int min, int max, String title)
    {
        this.min = min;
        this.max = max;
        this.title = title;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {

        final NumberPicker numberPicker = new NumberPicker(getActivity());

        numberPicker.setMinValue(min);
        numberPicker.setMaxValue(max);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                valueChangeListener.onValueChange(numberPicker,
                        numberPicker.getValue(), numberPicker.getValue());
            }
        });

//        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener()
//        {
//            @Override
//            public void onClick(DialogInterface dialog, int which)
//            {
//                valueChangeListener.onValueChange(numberPicker,
//                        numberPicker.getValue(), numberPicker.getValue());
//            }
//        });

        builder.setView(numberPicker);
        return builder.create();
    }

    public NumberPicker.OnValueChangeListener getValueChangeListener()
    {
        return valueChangeListener;
    }

    public void setValueChangeListener(NumberPicker.OnValueChangeListener valueChangeListener)
    {
        this.valueChangeListener = valueChangeListener;
    }
}