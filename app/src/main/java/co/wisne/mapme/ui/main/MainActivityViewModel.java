package co.wisne.mapme.ui.main;

import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import co.wisne.mapme.databinding.DialogPopupBinding;
import co.wisne.mapme.interfaces.SelectedItem;
import co.wisne.mapme.ui.main.model.LocationItem;

public class MainActivityViewModel extends ViewModel implements co.wisne.mapme.interfaces.SelectedItem{

    MutableLiveData<Double> latitude;

    MutableLiveData<Double> longitude;

    ArrayList<LocationItem> locationItems;

    LocationItem currentDetectedLocation;

    MutableLiveData<Boolean> addedLocation;

    MutableLiveData<Integer> itemEdited;

    MutableLiveData<String> toastEvent;

    private boolean detectionEnabled;

    private int selectedItem;

    private MutableLiveData<Boolean> distanceVisibility;

    public MainActivityViewModel(){

        locationItems = new ArrayList<>();

    }

    public MutableLiveData<Double> getLatitude() {
        if(latitude == null){
            latitude = new MutableLiveData<>();
        }

        return latitude;
    }

    public MutableLiveData<Double> getLongitude() {
        if(longitude == null){
            longitude = new MutableLiveData<>();
        }
        return longitude;
    }

    public ArrayList<LocationItem> getLocationItems() {
        return locationItems;
    }

    public MutableLiveData<Boolean> getAddedLocation() {
        if(addedLocation == null){
            addedLocation = new MutableLiveData<>();
        }
        return addedLocation;
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }

    public MutableLiveData<Integer> getItemEdited() {
        if(itemEdited == null){
            itemEdited = new MutableLiveData<>();
        }
        return itemEdited;
    }

    public boolean isDetectionEnabled() {
        return detectionEnabled;
    }

    public void setDetectionEnabled(boolean detectionEnabled) {
        this.detectionEnabled = detectionEnabled;
    }

    public LocationItem getCurrentDetectedLocation() {
        return currentDetectedLocation;
    }

    public void setCurrentDetectedLocation(LocationItem currentDetectedLocation) {
        this.currentDetectedLocation = currentDetectedLocation;
    }

    public MutableLiveData<String> getToastEvent() {
        if(toastEvent == null){
            toastEvent = new MediatorLiveData<>();
        }
        return toastEvent;
    }

    public MutableLiveData<Boolean> getDistanceVisibility() {
        if(distanceVisibility == null){
            distanceVisibility = new MutableLiveData<>();
        }
        return distanceVisibility;
    }

    public void addLocationToList(){

        getLocationItems().add(
                new LocationItem(
                    "Location "+locationItems.size(),
                    getLatitude().getValue(),
                    getLongitude().getValue()
                )
        );

        if(getAddedLocation().hasObservers()){
            getAddedLocation().postValue(true);
        }
        if(getToastEvent().hasObservers()){
            getToastEvent().setValue("Add new location to list.");
        }

        Log.d("D", "addLocationToList: added item at "+getLocationItems().size());

    }

    public void editSelectedLocation(int position, String name){

        getLocationItems().get(position).setName(name);

        if(getItemEdited().hasActiveObservers()){
            getItemEdited().postValue(position);
        }

    }

    public void navigate(){

        if(!isDetectionEnabled()){

            if(locationItems.size() <= 0){
                getToastEvent().setValue("No locations were added for navigation.");
                return;
            }

            for(LocationItem item: locationItems){
                if(TextUtils.isEmpty(item.getFileName())){
                    getToastEvent().setValue("Not all files are set, please choose pictures/videos for all locations.");
                    return;
                }
            }
            getToastEvent().setValue("Navigation has started.");
            setDetectionEnabled(true);
        }else{

            setCurrentDetectedLocation(null);
            getToastEvent().setValue("Navigation has stopped.");
            setDetectionEnabled(false);
        }


    }





}
