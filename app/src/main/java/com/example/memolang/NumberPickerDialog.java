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
    int min, max, current;
    String title;

    public NumberPickerDialog(int min, int max, String title, int current)
    {
        this.min = min;
        this.max = max;
        this.title = title;
        this.current = current;
    }

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

//        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener()
//        {
//            @Override
//            public void onClick(DialogInterface dialog, int which)
//            {
//                valueChangeListener.onValueChange(numberPicker,
//                        numberPicker.getValue(), numberPicker.getValue());
//            }
//        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                System.out.println("HIDE NAVIGATION BAR");
            }
        });
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