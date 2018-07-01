package co.wisne.mapme.ui.main.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import co.wisne.mapme.databinding.DialogPopupBinding;
import co.wisne.mapme.ui.main.model.LocationItem;

public class PopUpDialog extends DialogFragment {

    DialogPopupBinding binding;

    boolean isImage;

    boolean hasAudio;

    Uri audioUri;

    Uri fileUri;

    MediaPlayer mediaPlayer;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        binding = DialogPopupBinding.inflate(getActivity().getLayoutInflater());


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(binding.getRoot());

        if(isImage){
            binding.videoView.setVisibility(View.GONE);
            binding.imageView.setImageURI(fileUri);

            if(hasAudio){

                try{
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(getActivity().getApplicationContext(), audioUri);
                    mediaPlayer.setLooping(true);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }catch (IOException e){
                    Log.e("D", "PopUpDialog onCreateDialog: ",e);
                }

            }
        }else{
            binding.imageView.setVisibility(View.GONE);
            binding.videoView.setVideoURI(fileUri);
            binding.videoView.start();
        }

        return builder.create();
    }

    public void setFilename(String filename, boolean isImage){

        File sourceFolder  = new File(Environment.getExternalStoragePublicDirectory("")+"/MapMe");

        File sourceFile = new File(sourceFolder,filename);

        if(sourceFile.exists()){

            fileUri = Uri.fromFile(sourceFile);

            this.isImage = isImage;

        }


    }

    public void setLocationItem(LocationItem locationItem){

        File sourceFolder  = new File(Environment.getExternalStoragePublicDirectory("")+"/MapMe");

        File sourceFile = new File(sourceFolder,locationItem.getFileName());

        File audioFile = null;

        if(locationItem.hasAudio()){
            audioFile = new File(sourceFolder,locationItem.getAudioFileName());
        }

        if(sourceFile.exists()){

            fileUri = Uri.fromFile(sourceFile);

            this.isImage = locationItem.isImage();

        }

        if( audioFile != null && audioFile.exists() ){

            this.hasAudio = locationItem.hasAudio();

            audioUri = Uri.fromFile(audioFile);

        }

    }

    @Override
    public void onPause() {
        mediaPlayer.stop();
        super.onPause();
    }
}
