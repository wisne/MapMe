package co.wisne.mapme.ui.main.dialog;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import co.wisne.mapme.R;

public class ChooseFileDialog extends DialogFragment {

    public static int CHOOSE_PICTURE = 0;

    public static int CHOOSE_VIDEO = 1;

    public static int CHOOSE_AUDIO = 2;

    int PERMISSION_REQUEST_STORAGE;

    @Override
    @NonNull
    public Dialog onCreateDialog( Bundle savedInstanceState) {

        if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //permission not allowed, so ask for it.
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_STORAGE
            );
        }


        //permission allowed for location
        Log.d("D", "onCreate: Permission is already granted");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.choose_file)
                .setItems(R.array.choose_file_options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                Log.d("D", "onClick: choosen Choose Picture");
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                if (getActivity() != null)
                                    getActivity().startActivityForResult(intent, CHOOSE_PICTURE);

                                break;
                            case 1:
                                Log.d("D", "onClick: choosen Choose Video");
                                Intent intentVideoChooser = new Intent(Intent.ACTION_GET_CONTENT);
                                intentVideoChooser.setType("video/*");
                                if (getActivity() != null)
                                    getActivity().startActivityForResult(intentVideoChooser, CHOOSE_VIDEO);
                                break;

                        }
                    }
                });


        return builder.create();


    }

}
