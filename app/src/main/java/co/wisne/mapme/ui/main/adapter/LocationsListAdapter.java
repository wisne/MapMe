package co.wisne.mapme.ui.main.adapter;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import co.wisne.mapme.databinding.LocationsListitemBinding;
import co.wisne.mapme.interfaces.SelectedItem;
import co.wisne.mapme.ui.main.MainActivityViewModel;
import co.wisne.mapme.ui.main.dialog.ChooseFileDialog;
import co.wisne.mapme.ui.main.dialog.EditLocationDialog;
import co.wisne.mapme.ui.main.model.LocationItem;

public class LocationsListAdapter extends RecyclerView.Adapter<LocationsListAdapter.ViewHolder> {

    ArrayList<LocationItem> items;

    MainActivityViewModel viewModel;


    public static class ViewHolder extends RecyclerView.ViewHolder{

        TextView name;

        TextView lat;

        TextView longi;

        ImageButton editButton;

        ImageButton selectFileButton;

        int position;

        MainActivityViewModel viewModel;

        public void setViewModel(MainActivityViewModel viewModel) {
            this.viewModel = viewModel;
        }

        public ViewHolder(final LocationsListitemBinding binding) {

            super(binding.getRoot());

            name = binding.textLocationName;

            lat = binding.textLatitude;

            longi = binding.textLongitude;

            editButton = binding.imageButtonEdit;

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditLocationDialog dialog = new EditLocationDialog();
                    dialog.setSelectedPosition(position);
                    dialog.show(((AppCompatActivity)binding.getRoot().getContext()).getSupportFragmentManager(),"EditDialog");
                }
            });

            selectFileButton = binding.imageButtonChooseFile;

            selectFileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewModel.setSelectedItem(position);
                    ChooseFileDialog chooseFileDialog = new ChooseFileDialog();
                    chooseFileDialog.show(((AppCompatActivity)binding.getRoot().getContext()).getSupportFragmentManager(),"ChooseFileDialog");
                }
            });

        }

        public void setPostion(int position) {
            this.position = position;
        }
    }

    public LocationsListAdapter(MainActivityViewModel viewModel){
        this.viewModel = viewModel;
        items = viewModel.getLocationItems();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        LocationsListitemBinding binding = LocationsListitemBinding.inflate(layoutInflater,parent,false);

        ViewHolder viewHolder = new ViewHolder(binding);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //set name
        holder.name.setText(items.get(position).getName());

        //set lat and long
        holder.lat.setText(String.valueOf(items.get(position).getLatitude()));

        holder.longi.setText(String.valueOf(items.get(position).getLongitude()));

        holder.setPostion(position);

        holder.setViewModel(viewModel);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }


}
