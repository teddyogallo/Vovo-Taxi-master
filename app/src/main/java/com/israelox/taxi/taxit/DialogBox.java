package com.israelox.taxi.taxit;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

public class DialogBox extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog=new AlertDialog.Builder(getActivity());
        dialog.setTitle("Set Price")
                .setMessage("Choose Operation")
                .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                     DriverMapActivity drive=new DriverMapActivity();
                     drive.starts();
                    }
                })
                .setNegativeButton("Stop", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DriverMapActivity drive=new DriverMapActivity();
                        drive.stops();
                    }
                });

        return dialog.create();

    }
}
