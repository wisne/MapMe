package co.wisne.mapme.ui.main.dialog;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import co.wisne.mapme.R;
import co.wisne.mapme.databinding.DialogLocationEditBinding;
import co.wisne.mapme.ui.main.MainActivityViewModel;
import co.wisne.mapme.ui.main.model.LocationItem;

public class EditLocationDialog extends DialogFragment {

    MainActivityViewModel viewModel;

    DialogLocationEditBinding binding;

    int selectedPostion;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        binding = DialogLocationEditBinding.inflate(LayoutInflater.from(getActivity()));

        builder.setView(binding.getRoot());

        viewModel = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class);

        binding.name.setText(viewModel.getLocationItems().get(selectedPostion).getName());

        binding.latitude.setText(String.valueOf(viewModel.getLocationItems().get(selectedPostion).getLatitude()));

        binding.longitude.setText(String.valueOf(viewModel.getLocationItems().get(selectedPostion).getLongitude()));

        binding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEdit();
            }
        });



        return builder.create();
    }

    public void setSelectedPosition(int Postion){
        selectedPostion = Postion;
    }

    public void saveEdit(){
            viewModel.editSelectedLocation(selectedPostion,binding.name.getText().toString());
    }


}
