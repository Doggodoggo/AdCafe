package com.bry.adcafe.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.AdvertiserLocation;
import com.bry.adcafe.models.myLatLng;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class SetAdvertiserLocation extends DialogFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener
        ,View.OnClickListener ,GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener{

    private final String TAG = "myMapFragment";
    private Context mContext;
    private Activity mActivity;

    private GoogleMap map;
    private double CBD_LAT = -1.2805;
    private double CBD_LONG = 36.8163;
    private LatLng CBD = new LatLng(CBD_LAT, CBD_LONG);
    private int ZOOM = 15;

    private List<Marker> markers = new ArrayList<>();
    private Button setButton;
    private View rootView;
    MapFragment mapFragment;
    PlaceAutocompleteFragment autocompleteFragment;

    private int[] myLocationIcons;
    private final int REQUESTCODE = 3301;
    private CardView loc1;
    private CardView loc2;
    private CardView loc3;
    private CardView loc4;


    public void setfragcontext(Context context) {
        mContext = context;
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        try {
            rootView = inflater.inflate(R.layout.map_fragment, container, false);
            mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

            setButton = rootView.findViewById(R.id.setLocations);
            setButton.setOnClickListener(this);

            loc1 = rootView.findViewById(R.id.loc1);
            loc2 = rootView.findViewById(R.id.loc2);
            loc3 = rootView.findViewById(R.id.loc3);
            loc4 = rootView.findViewById(R.id.loc4);
            rootView.findViewById(R.id.loc1img).setOnClickListener(this);
            rootView.findViewById(R.id.loc2img).setOnClickListener(this);
            rootView.findViewById(R.id.loc3img).setOnClickListener(this);
            rootView.findViewById(R.id.loc4img).setOnClickListener(this);

            setButton.setText("CANCEL");
            mapFragment.getMapAsync(this);


        } catch (InflateException e) {
            e.printStackTrace();
        }

        myLocationIcons = new int[]{
                R.id.loc1,
                R.id.loc2,
                R.id.loc3,
                R.id.loc4
        };

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForSetThatMyLocationButtonThingy,
                new IntentFilter("SET_THAT_MY_LOCATION_BUTTON_THINGY"));

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        return rootView;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        googleMap.addMarker(new MarkerOptions().position(new LatLng(CBD_LAT, CBD_LONG))
                .title("Nairobi-CBD").flat(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setIndoorEnabled(false);
        googleMap.setBuildingsEnabled(false);
        googleMap.setOnMarkerClickListener(this);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CBD, ZOOM));

        if(!Variables.advertiserLocations.isEmpty()){
            for(AdvertiserLocation loc:Variables.advertiserLocations) {
                Marker mark = map.addMarker(new MarkerOptions().position(new LatLng(loc.getMyLatLng().latitude,loc.getMyLatLng().longitude))
                        .draggable(true));
                markers.add(mark);
            }
            setButton.setText("SET.");
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(markers.size()<4){
                    Marker mark = map.addMarker(new MarkerOptions().position(latLng)
                            .draggable(true));
                    markers.add(mark);
                    int pos = markers.indexOf(mark);
                    rootView.findViewById(myLocationIcons[pos]).setVisibility(View.VISIBLE);

                    AdvertiserLocation adLctn = new AdvertiserLocation(new myLatLng(latLng.latitude,latLng.longitude),"");
                    Variables.advertiserLocations.add(adLctn);
                    setButton.setText("SET.");
                }else{
                    Toast.makeText(mContext,"Only a max of 4 locations are allowed",Toast.LENGTH_SHORT).show();
                }
            }
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker arg0) {
                Log.d("System out", "onMarkerDragStart..."+arg0.getPosition().latitude+"..."+arg0.getPosition().longitude);
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onMarkerDragEnd(Marker arg0) {
                Log.d("System out", "onMarkerDragEnd..."+arg0.getPosition().latitude+"..."+arg0.getPosition().longitude);
                map.animateCamera(CameraUpdateFactory.newLatLng(arg0.getPosition()));
            }

            @Override
            public void onMarkerDrag(Marker arg0) {
                Log.i("System out", "onMarkerDrag...");
            }
        });

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUESTCODE);
        }else{
            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(this);
            map.setOnMyLocationClickListener(this);
        }

        LatLng botBnd = new LatLng(-4.716667, 27.433333);
        LatLng topBnd = new LatLng(4.883333, 41.8583834826426);
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setBoundsBias(new LatLngBounds(botBnd,topBnd));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
                LatLng searchedPlace = place.getLatLng();
                AdvertiserLocation adLctn = new AdvertiserLocation(new myLatLng(searchedPlace.latitude,searchedPlace.longitude),place.getName().toString());
                if(markers.size()<4){
                    Marker mark = map.addMarker(new MarkerOptions().position(searchedPlace)
                            .draggable(true));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedPlace, ZOOM));
                    markers.add(mark);
                    int pos = markers.indexOf(mark);
                    rootView.findViewById(myLocationIcons[pos]).setVisibility(View.VISIBLE);
                    Variables.advertiserLocations.add(adLctn);
                    setButton.setText("SET");
                }else{
                    Toast.makeText(mContext,"Only a max of 4 locations are allowed.",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        if(!markers.isEmpty()){
            for(Marker loc:markers){
                int pos = markers.indexOf(loc);
                rootView.findViewById(myLocationIcons[pos]).setVisibility(View.VISIBLE);
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
                        int pos = markers.size()-1;
                        rootView.findViewById(myLocationIcons[pos]).setVisibility(View.INVISIBLE);
                        markers.remove(m);
                        for(AdvertiserLocation advLoc:Variables.advertiserLocations){
                            myLatLng loc = advLoc.getMyLatLng();
                            if(m.getPosition().latitude == loc.latitude && m.getPosition().longitude == loc.longitude){
                                Variables.advertiserLocations.remove(advLoc);
                            }
                        }
                    }
                }
                if(Variables.advertiserLocations.isEmpty()){
                    setButton.setText("CANCEL.");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return false;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        LocationManager lm = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled) {
            Toast.makeText(mContext,"Please turn on your GPS-Location.",Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        if(markers.size()<4){
            LatLng myLatLng = new LatLng(location.getLatitude(),location.getLongitude());
            Marker mark = map.addMarker(new MarkerOptions().position(myLatLng)
                    .title("Your Location.").flat(true)
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            markers.add(mark);
            int pos = markers.indexOf(mark);
            rootView.findViewById(myLocationIcons[pos]).setVisibility(View.VISIBLE);

            AdvertiserLocation adLctn = new AdvertiserLocation(new myLatLng(myLatLng.latitude,myLatLng.longitude),"");
            Variables.advertiserLocations.add(adLctn);
            setButton.setText("SET");
        }else{
            Toast.makeText(mContext,"Only a max of 4 locations are allowed",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        if(view.equals(setButton)){
            dismiss();
        }else{
            if(view.equals(rootView.findViewById(R.id.loc1img))){
                Log.d(TAG,"loc btn clicked");
                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(markers.get(0).getPosition(), ZOOM);
                map.animateCamera(location);
            }else if(view.equals(rootView.findViewById(R.id.loc2img))){
                Log.d(TAG,"loc btn clicked");
                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(markers.get(1).getPosition(), ZOOM);
                map.animateCamera(location);
            }else if(view.equals(rootView.findViewById(R.id.loc3img))){
                Log.d(TAG,"loc btn clicked");
                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(markers.get(2).getPosition(), ZOOM);
                map.animateCamera(location);
            }else if(view.equals(rootView.findViewById(R.id.loc4img))){
                Log.d(TAG,"loc btn clicked");
                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(markers.get(3).getPosition(), ZOOM);
                map.animateCamera(location);
            }
        }
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("UPDATE_TEXT_FOR_WHEN_LOCATION_IS_ADDED"));
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForSetThatMyLocationButtonThingy);
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
        super.onDestroyView();
    }


    private BroadcastReceiver mMessageReceiverForSetThatMyLocationButtonThingy = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MyMapFragment","Message received for setting my location button thingy to true");
            try{
                map.setMyLocationEnabled(true);
            }catch (SecurityException e){
                e.printStackTrace();
            }
            map.setOnMyLocationButtonClickListener(SetAdvertiserLocation.this);
            map.setOnMyLocationClickListener(SetAdvertiserLocation.this);
        }
    };
}
