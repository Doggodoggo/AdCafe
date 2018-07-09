package com.bry.adcafe.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.TargetedUser;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class AdvertiserMapFragment extends DialogFragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,View.OnClickListener {
    private final String TAG = "AdvertiserMapFragment";
    private Context mContext;
    private Activity mActivity;


    private GoogleMap map;
    private double CBD_LAT = -1.2805;
    private double CBD_LONG = 36.8163;
    private LatLng CBD = new LatLng(CBD_LAT, CBD_LONG);

    private List<Marker> markers = new ArrayList<>();
    private HashMap<Marker,Circle> markAndCirc = new HashMap<>();
    private Button setButton;
    private View rootView;
    private ImageButton searchBtn;

    MapFragment mapFragment;
    PlaceAutocompleteFragment autocompleteFragment;
    private boolean didUserPressSetBtn = false;
    private List<TargetedUser> targetedUserData;



    public void setfragcontext(Context context) {
        mContext = context;
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    public void setUsersThatCanBeReached(List<TargetedUser> users){
        this.targetedUserData = users;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        if (rootView != null) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) parent.removeView(rootView);
        }
        try {
            rootView = inflater.inflate(R.layout.map_fragment, container, false);
            mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            setButton = rootView.findViewById(R.id.setLocations);
            setButton.setOnClickListener(this);
        } catch (InflateException e) {
            e.printStackTrace();
        }

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        googleMap.addMarker(new MarkerOptions().position(new LatLng(CBD_LAT, CBD_LONG))
                .title("Nairobi-CBD").snippet("Your Point Of Reference.").flat(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setIndoorEnabled(false);
        googleMap.setBuildingsEnabled(false);
        googleMap.setOnMarkerClickListener(this);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CBD, 14));

        if(!Variables.locationTarget.isEmpty()){
            for(LatLng latLng:Variables.locationTarget) {
                Marker mark = map.addMarker(new MarkerOptions().position(latLng)
                        .draggable(true));
                CircleOptions circleOptions = new CircleOptions().center(latLng).radius(Constants.MAX_DISTANCE_IN_METERS);
                circleOptions.strokeWidth(2).strokeColor(Color.rgb(177, 185, 188))
                        .fillColor(Color.argb(70,128,203,196 ));
                Circle circle = map.addCircle(circleOptions);
                markers.add(mark);
                markAndCirc.put(mark,circle);
            }
            Variables.locationTarget.clear();
        }

        if(!targetedUserData.isEmpty()){
            for(TargetedUser user: targetedUserData){
                for(LatLng loc:user.getUserLocations()){
                    CircleOptions myOptions = new CircleOptions().center(loc).radius(10);
                    myOptions.strokeWidth(1).strokeColor(Color.rgb(0, 137, 48))
                            .fillColor(Color.argb(80,0,137,48 ));
                    Circle circle = map.addCircle(myOptions);
                }
            }
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(markers.size()<4){
                    Marker mark = map.addMarker(new MarkerOptions().position(latLng)
                            .draggable(true));
                    CircleOptions circleOptions = new CircleOptions().center(latLng).radius(Constants.MAX_DISTANCE_IN_METERS);
                    circleOptions.strokeWidth(2).strokeColor(Color.rgb(177, 185, 188))
                            .fillColor(Color.argb(70,128,203,196 ));
                    Circle circle = map.addCircle(circleOptions);
                    markers.add(mark);
                    markAndCirc.put(mark,circle);
                }else{
                    Toast.makeText(mContext,"Only a max of 4 locations are allowed.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker arg0) {
                Log.d("System out", "onMarkerDragStart..."+arg0.getPosition().latitude+"..."+arg0.getPosition().longitude);
                Circle c = markAndCirc.get(arg0);
                c.remove();

            }

            @SuppressWarnings("unchecked")
            @Override
            public void onMarkerDragEnd(Marker arg0) {
                Log.d("System out", "onMarkerDragEnd..."+arg0.getPosition().latitude+"..."+arg0.getPosition().longitude);
                map.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
                Circle c = markAndCirc.get(arg0);

                CircleOptions circleOptions = new CircleOptions().center(new LatLng(arg0.getPosition().latitude,arg0.getPosition().longitude))
                        .radius(Constants.MAX_DISTANCE_IN_METERS);
                circleOptions.strokeWidth(2).strokeColor(Color.rgb(177, 185, 188))
                        .fillColor(Color.argb(70,128,203,196 ));

                Circle newC = map.addCircle(circleOptions);
                markAndCirc.remove(arg0);
                markAndCirc.put(arg0,newC);
            }

            @Override
            public void onMarkerDrag(Marker arg0) {
                Log.i("System out", "onMarkerDrag...");
            }
        });

        LatLng topBnd = new LatLng(-4.543295, 40.331370);
        LatLng botBnd = new LatLng(4.259043, 33.959300);
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setBoundsBias(new LatLngBounds(topBnd,botBnd));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
                LatLng searchedPlace = place.getLatLng();
                if(markers.size()<4){
                    Marker mark = map.addMarker(new MarkerOptions().position(searchedPlace)
                            .draggable(true));
                    CircleOptions circleOptions = new CircleOptions().center(searchedPlace).radius(Constants.MAX_DISTANCE_IN_METERS);
                    circleOptions.strokeWidth(2).strokeColor(Color.rgb(177, 185, 188))
                            .fillColor(Color.argb(70,128,203,196 ));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(searchedPlace, 14));
                    Circle circle = map.addCircle(circleOptions);
                    markers.add(mark);
                    markAndCirc.put(mark,circle);
                }else{
                    Toast.makeText(mContext,"Only a max of 4 locations are allowed.",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

//        searchBtn.setVisibility(View.VISIBLE);
//        searchBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
//                            .setBoundsBias(new LatLngBounds(new LatLng(4.259043, 33.959300)
//                                    ,new LatLng(-4.543295, 40.331370))).build(mActivity);
//                    startActivityForResult(intent, 693301);
//                } catch (GooglePlayServicesRepairableException e) {
//                    // TODO: Handle the error.
//                } catch (GooglePlayServicesNotAvailableException e) {
//                    // TODO: Handle the error.
//                }
//            }
//        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 693301) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(mContext, data);
                LatLng searchedPlace = place.getLatLng();
                if(markers.size()<4){
                    Marker mark = map.addMarker(new MarkerOptions().position(searchedPlace)
                            .draggable(true));
                    CircleOptions circleOptions = new CircleOptions().center(searchedPlace).radius(Constants.MAX_DISTANCE_IN_METERS);
                    circleOptions.strokeWidth(2).strokeColor(Color.rgb(177, 185, 188))
                            .fillColor(Color.argb(70,128,203,196 ));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(searchedPlace, 14));
                    Circle circle = map.addCircle(circleOptions);
                    markers.add(mark);
                    markAndCirc.put(mark,circle);
                }else{
                    Toast.makeText(mContext,"Only a max of 4 locations are allowed.",Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(mContext, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(!markers.isEmpty()) {
            try{
                for (Marker m: markers){
                    if(m.equals(marker)){
                        Log.d(TAG,"Removing Marker: "+m.getPosition());
                        m.remove();
                        markers.remove(m);
                        Circle c = markAndCirc.get(marker);
                        c.remove();
                        markAndCirc.remove(m);

                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        return false;
    }

    @Override
    public void onClick(View view) {
        if(view.equals(setButton)){
            setPreferredLocations();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(!didUserPressSetBtn){
            onMyBackPressed();
        }
       try{
           if (mapFragment != null){
               Log.d(TAG,"Map fragment is not null, attempting to remove it ....");
               getFragmentManager().beginTransaction().remove(mapFragment).commit();
           }
           if(autocompleteFragment!=null){
               Log.d(TAG,"Autocomplete fragment is not null, attempting to remove it");
               getFragmentManager().beginTransaction().remove(autocompleteFragment).commit();
           }
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    private void setPreferredLocations(){
        if(markers.isEmpty()){
            Toast.makeText(mContext,"Select at least one location.",Toast.LENGTH_SHORT).show();
        }else{
            setMerkerLocationsForTargeting();
        }
    }


    private void setMerkerLocationsForTargeting(){
        didUserPressSetBtn = true;
        for(Marker m: markers){
            LatLng selLatLng = new LatLng(m.getPosition().latitude,m.getPosition().longitude);
            Variables.locationTarget.add(selLatLng);
        }
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.SET_LOCATION_DATA));
        Toast.makeText(mContext,"Target Locations set.",Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void onMyBackPressed(){
        if(markers.isEmpty()){
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.SET_LOCATION_DATA));
            Toast.makeText(mContext,"No Locations set.",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(mContext,"Locations edit cancelled.",Toast.LENGTH_SHORT).show();
        }
    }

}
