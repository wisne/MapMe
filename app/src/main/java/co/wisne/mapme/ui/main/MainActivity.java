package co.wisne.mapme.ui.main;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.UUID;

import co.wisne.mapme.R;
import co.wisne.mapme.databinding.ActivityMainBinding;
import co.wisne.mapme.ui.main.adapter.LocationsListAdapter;
import co.wisne.mapme.ui.main.dialog.ChooseFileDialog;
import co.wisne.mapme.ui.main.dialog.PopUpDialog;
import co.wisne.mapme.ui.main.model.LocationItem;


public class MainActivity extends AppCompatActivity {

    final int PERMISSION_REQUEST_LOCATION = 123;

    LocationRequest locationRequest;

    FusedLocationProviderClient locationProviderClient;

    LocationCallback locationCallback;

    ///viewmodel and data binding
    MainActivityViewModel viewModel;

    ActivityMainBinding binding;

    RecyclerView.LayoutManager layoutManager;

    LocationsListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);

        binding.setLifecycleOwner(this);

        binding.setViewModel(viewModel);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            //permission not allowed, so ask for it.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION
                    );
        }else {
            //permission allowed for location
            Log.d("D", "onCreate: Permission is already granted");

            createLocationRequest();
        }

        layoutManager = new LinearLayoutManager(this);

        adapter = new LocationsListAdapter(viewModel);

        binding.recyclerViewItem.setLayoutManager(layoutManager);

        binding.recyclerViewItem.setAdapter(adapter);

        binding.recyclerViewItem.setHasFixedSize(false);

        viewModel.getAddedLocation().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                //notify recycler list that item has been added
                binding.recyclerViewItem.getAdapter().notifyItemInserted(viewModel.getLocationItems().size()-1);
            }
        });

        viewModel.getItemEdited().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer position) {
                if(position!= null)
                binding.recyclerViewItem.getAdapter().notifyItemChanged(position);
            }
        });

        viewModel.getToastEvent().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                Toast.makeText(MainActivity.this,s,Toast.LENGTH_SHORT).show();
            }
        });




    }


    public void createLocationRequest(){

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult==null){
                    Log.d("D", "onLocationResult: locationResult is null");
                    return;
                }else {

                    float shortestDistance = Float.MAX_VALUE;

                    LocationItem nearestLocation = null;

                    for(Location location: locationResult.getLocations()){

                        Log.d("D", "onLocationResult: "+location.toString());

                        viewModel.getLatitude().setValue(location.getLatitude());

                        viewModel.getLongitude().setValue(location.getLongitude());

                            for(LocationItem item: viewModel.locationItems ){

                                float[] results = {0};

                                Location.distanceBetween(location.getLatitude(),location.getLongitude(),item.getLatitude(),item.getLongitude(),results);

                               if( results[0] <= 21 && viewModel.isDetectionEnabled()){

                                        if(viewModel.getCurrentDetectedLocation() != item){

                                            if(TextUtils.isEmpty(item.getFileName())){

                                                Toast.makeText(getApplicationContext(),"Please choose a file for "+item.getName(),Toast.LENGTH_SHORT).show();

                                            }
                                            else {

                                                PopUpDialog popUpDialog = new PopUpDialog();
                                                //popUpDialog.setFilename(item.getFileName(),item.isImage());
                                                popUpDialog.setLocationItem(item);
                                                popUpDialog.show(getSupportFragmentManager(),"popUp");
                                            }

                                            viewModel.setCurrentDetectedLocation(item);
                                        }
                               }

                               if(item == viewModel.getCurrentDetectedLocation()){
                                   Log.d("D", "onLocationResult: already there");
                               }

                               if(results[0] < shortestDistance){
                                    shortestDistance = results[0];
                                    nearestLocation = item;
                               }
                            }


                        Log.d("D", "onLocationResult: Shortest distance is "+shortestDistance);
                        if(nearestLocation!= null){

                            viewModel.getDistanceVisibility().setValue(true);

                            Log.d("D", "onLocationResult: nearest item is "+nearestLocation.getName());

                            binding.textDistance.setText(String.valueOf((int)shortestDistance)+ "m.");

                            binding.textNextLocation.setText(nearestLocation.getName());

                            float maxDistance = 1000;
                            float indicatorDistance;

                            binding.progressBarDistance.setMax((int)maxDistance);

                            if((indicatorDistance = maxDistance - shortestDistance) > -1 ){
                                binding.progressBarDistance.setProgress((int)indicatorDistance);
                            }else {
                                binding.progressBarDistance.setProgress(0);
                            }
                        }




                    }
                }
            }
        };


        Log.d("D", "createLocationRequest: create location request");

        locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //LOCATION SETTING CHECKER
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d("D", "onSuccess: Successfully ");
                startLocationRequest();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("D", "onFailure: location setting failed");
            }
        });
    }

    private void startLocationRequest(){
        Log.d("D", "startLocationRequest: starting location request");
        //request permission if not granted
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION
            );
        }
        locationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);
    }



    //permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST_LOCATION:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Permission for location was granted",Toast.LENGTH_SHORT).show();
                    createLocationRequest();
                }else {
                    Toast.makeText(this,"Permission for location wasn't granted and app will not perform.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //if a picture is chosen
        if(requestCode == ChooseFileDialog.CHOOSE_PICTURE && resultCode == RESULT_OK){

            final Uri originalUri = data.getData();

            binding.progressBar.setVisibility(View.VISIBLE);


            new Thread(new Runnable() {
                @Override
                public void run() {

                    CopyFromUri(originalUri, viewModel.getSelectedItem(),false);

                    binding.progressBar.setVisibility(View.GONE);

                    viewModel.getLocationItems().get(viewModel.getSelectedItem()).setImage(true);

                    binding.getRoot().post(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(getApplicationContext(),"Picture was select for "+ viewModel.getLocationItems().get(viewModel.getSelectedItem()).getName(), Toast.LENGTH_SHORT).show();

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("Do you want to insert audio?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent intentAudioChooser =  new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                                            startActivityForResult(intentAudioChooser, ChooseFileDialog.CHOOSE_AUDIO);
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();


                        }
                    });


                }
            }).run();

        }

        //if a video is chosen
        if(requestCode == ChooseFileDialog.CHOOSE_VIDEO && resultCode == RESULT_OK){

            final Uri originalUri = data.getData();

            binding.progressBar.setVisibility(View.VISIBLE);


            new Thread(new Runnable() {
                @Override
                public void run() {

                    CopyFromUri(originalUri, viewModel.getSelectedItem(),false);
                    binding.progressBar.setVisibility(View.GONE);
                    viewModel.getLocationItems().get(viewModel.getSelectedItem()).setImage(false);
                    binding.getRoot().post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Video was select for "+ viewModel.getLocationItems().get(viewModel.getSelectedItem()).getName(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).run();

        }

        //if an audio is chosen
        if(requestCode == ChooseFileDialog.CHOOSE_AUDIO && resultCode == RESULT_OK){

            final Uri originalUri = data.getData();

            binding.progressBar.setVisibility(View.VISIBLE);


            new Thread(new Runnable() {
                @Override
                public void run() {

                    CopyFromUri(originalUri, viewModel.getSelectedItem(),true);
                    binding.progressBar.setVisibility(View.GONE);
                    viewModel.getLocationItems().get(viewModel.getSelectedItem()).setAudio(true);
                    binding.getRoot().post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Audio was select for "+ viewModel.getLocationItems().get(viewModel.getSelectedItem()).getName(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).run();

        }



    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void CopyFromUri(Uri originalUri, int itemPosition, boolean isAudio){

        String filename = "";

        File file = new File(Environment.getExternalStoragePublicDirectory(""), "MapMe");

        if(file.mkdir()){
            Log.d("D", "onActivityResult: Made dir");
        }else
        {
            if(file.exists()){
                Log.d("D", "onActivityResult: dir already exists");
            }
            else {
                Log.d("D", "onActivityResult: couldn't make file");
            }
        }

        try
        {//copying files

            InputStream inputStream = getContentResolver().openInputStream(originalUri);

            filename = UUID.randomUUID()+"";

            if(originalUri.getScheme().equals("content")){

                Cursor metaCursor = getContentResolver().query(originalUri, null, null, null, null);

                if(metaCursor!= null){
                    try{
                        if(metaCursor.moveToFirst()){
                            int name = metaCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                            filename = metaCursor.getString(name);
                            Log.d("D", "onActivityResult: filename from meta "+filename);
                        }
                    }catch (Exception e){
                        Log.e("D", "onActivityResult: ", e);
                    }finally {
                        metaCursor.close();
                    }
                }

                //set to be copied to file name
                OutputStream outputStream = new FileOutputStream(new File(file,filename));

                byte[] buffer = new byte[1024];
                int len;
                while((len = inputStream.read(buffer))>0){
                    outputStream.write(buffer,0,len);
                }
                outputStream.close();


            }else {
                filename = originalUri.getLastPathSegment();

                //set to be copied to file name
                OutputStream outputStream = new FileOutputStream(new File(file,filename));

                //begin copy
                byte[] buffer = new byte[1024];
                int len;
                while((len = inputStream.read(buffer))>0){
                    outputStream.write(buffer,0,len);
                }
                outputStream.close();
            }


        }catch (Exception e){

        }
        if(isAudio){
            viewModel.getLocationItems().get(itemPosition).setAudioFileName(filename);
            Log.d("D", "CopyFromUri: set audio file url "+filename);
        }
        else {
            viewModel.getLocationItems().get(itemPosition).setFileName(filename);
            Log.d("D", "CopyFromUri: set file url "+filename);
        }


    }



}
