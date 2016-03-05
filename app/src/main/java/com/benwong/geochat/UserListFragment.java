package com.benwong.geochat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by benwong on 2016-03-04.
 */
public class UserListFragment extends Fragment implements View.OnClickListener, LocationListener {
    private TextView userEmail;
    private TextView country;
    private Firebase ref;
    private Intent intent;
    private LocationManager locationManager;
    private String provider;
    private Location location;
    private GeoFire geoFire;
    private TextView addressTV;
    private List<User> mUsers = new ArrayList<>();
    private String userCountry;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        userEmail = (TextView) view.findViewById(R.id.userEmail);
        intent = getActivity().getIntent();

        userEmail.setText(intent.getStringExtra("loginEmail"));
        ref = new Firebase("https://originchat.firebaseio.com/users/" + intent.getStringExtra("userId"));
        ref.child("email").setValue(intent.getStringExtra("loginEmail"));

        country = (TextView) view.findViewById(R.id.country);
        addressTV = (TextView)view.findViewById(R.id.addressTV);


        ref = new Firebase("https://originchat.firebaseio.com/users/" + intent.getStringExtra("userId") + "/country");

        Query queryRef = ref.orderByChild("country");

        //force user to set country if it's not set in the database
        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("userCountry", String.valueOf(dataSnapshot.getValue()));

                try {
                    if (dataSnapshot.getValue() == null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        selectCountryAlert();
                    } else {
                        country.setText(String.valueOf(dataSnapshot.getValue()));
                        userCountry = String.valueOf(dataSnapshot.getValue());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        country.setOnClickListener(this);

        //setup location manager
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0.0f, this);
        }

        provider = locationManager.getBestProvider(new Criteria(), false);

        location = locationManager.getLastKnownLocation(provider);

        onLocationChanged(location);


        final Set<String> usersNearby = new HashSet<String>();

        //query geolocation
        geoFire = new GeoFire(new Firebase("https://originchat.firebaseio.com/locations/"));

        GeoLocation center = new GeoLocation(location.getLatitude(), location.getLongitude());

        GeoQuery geoQuery = geoFire.queryAtLocation(center, 5);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String username, GeoLocation location) {
                usersNearby.add(username);
                // additional code, like displaying a pin on the map
                // and adding Firebase listeners for this user
                Log.i("nearby", String.valueOf(usersNearby));

                for (String user : usersNearby) {
                    System.out.println(user);
                    Log.i("nearbyUser", user);

                    ref = new Firebase("https://originchat.firebaseio.com/users/" + user );

                    Query queryCountryRef = ref.orderByChild("country").equalTo()
                }

            }

            @Override
            public void onKeyExited(String username) {
                usersNearby.remove(username);
                // additional code, like removing a pin from the map
                // and removing any Firebase listener for this user
            }

            @Override
            public void onKeyMoved(String s, GeoLocation geoLocation) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(FirebaseError firebaseError) {

            }
        });

             return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.country:
                selectCountryAlert();
        }

    }

    private void selectCountryAlert(){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        builderSingle.setTitle("Select Country of Origin");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.select_dialog_singlechoice);

        String[] isoCountries = Locale.getISOCountries();
        System.out.println(Locale.getISOCountries());
        for (String country : isoCountries) {
            Locale locale = new Locale("en", country);

            arrayAdapter.add(locale.getDisplayCountry().toString());
        }


        builderSingle.setNegativeButton(
                "cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        AlertDialog.Builder builderInner = new AlertDialog.Builder(
                                getActivity());
                        builderInner.setMessage(strName);
                        builderInner.setTitle("Your Selected Item is");
                        builderInner.setPositiveButton(
                                "Ok",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                        dialog.dismiss();
                                    }
                                });
                        builderInner.show();

                        country.setText(strName);

                        ref = new Firebase("https://originchat.firebaseio.com/");

                        ref.child("users").child(intent.getStringExtra("userId")).child("country").setValue(strName);
                    }
                });
        builderSingle.show();
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null){

            Log.i("geoLocation", String.valueOf(location.getLatitude())  + String.valueOf(location.getLongitude()) );
            geoFire = new GeoFire(new Firebase("https://originchat.firebaseio.com/locations/"  ));
            geoFire.setLocation(intent.getStringExtra("userId"), new GeoLocation(location.getLatitude(), location.getLongitude()));
        }

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> listAddresses = null;
        try {
            listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (listAddresses != null && listAddresses.size() > 0) {
            Log.i("PlaceInfo", listAddresses.get(0).toString());

            String addressHolder = "";

            for (int i = 0; i <= listAddresses.get(0).getMaxAddressLineIndex(); i++) {

                addressHolder += listAddresses.get(0).getAddressLine(i) + "\n";
                Log.i("address", addressHolder);
            }
            addressTV.setText(addressHolder + "\n" );
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
