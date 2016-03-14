package com.benwong.geochat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by benwong on 2016-03-04.
 */
public class UserListFragment extends Fragment implements View.OnClickListener, LocationListener {
    private TextView usernameTV;
    private TextView country;
    private Firebase ref;
    private Firebase queryPreviousLocation;
    private Intent intent;
    private LocationManager locationManager;
    private String provider;
    private Location location;
    private GeoFire geoFire;
    private TextView addressTV;
    private List<User> nearByUsersList;
    private ListView myListView;
    ArrayAdapter<User> arrayAdapter;
    private RecyclerView mPhotoRecyclerView;
    private Location userLocation;
    float distanceToUser;
    private ImageView profileImageView;



    private GeoQuery geoQuery;
    private int distanceProgress;

    int value = 0;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
//

        usernameTV = (TextView) view.findViewById(R.id.usernameTV);





        // get value from the distance sort
        SharedPreferences prefs = getContext().getSharedPreferences("locationSortPreference", Context.MODE_PRIVATE);
        value = prefs.getInt("seekBarValue", 0); // 0 is default


        geoFire = new GeoFire(new Firebase("https://originchat.firebaseio.com/locations/"));


        intent = getActivity().getIntent();

        country = (TextView) view.findViewById(R.id.country);
        addressTV = (TextView) view.findViewById(R.id.addressTV);
//        myListView = (ListView) view.findViewById(R.id.listView);
        profileImageView = (ImageView) view.findViewById(R.id.imageView);

        nearByUsersList = new ArrayList<User>();


        mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));

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

        // check to see if location is turned on
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (enabled && location != null) {
            try {
                onLocationChanged(location);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 0.0f, this);
            Toast.makeText(getActivity(), "Please turn on GPS for the most accurate location", Toast.LENGTH_LONG).show();
            // query for signed-in user's last location if currrent location doesn't exist so app can still find nearby users if GPS is turned off
            queryPreviousLocation = new Firebase("https://originchat.firebaseio.com/locations/" + Constant.USERID);
            queryPreviousLocation.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
//                    System.out.println("user location " + dataSnapshot);
//                    System.out.println("user location " + dataSnapshot.child("l").child("0").getValue());
//                    System.out.println("user location " + dataSnapshot.child("l").child("1").getValue());
                    try{
                        userLocation = new Location("");
                        userLocation.setLatitude((Double) dataSnapshot.child("l").child("0").getValue());
                        userLocation.setLongitude((Double) dataSnapshot.child("l").child("1").getValue());
                        setAddress(userLocation);
                    }catch (Exception e){
                        e.printStackTrace();
                    }



                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

        }

        ref = new Firebase("https://originchat.firebaseio.com/users/" + Constant.USERID);
        //query for user's home country

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.child("country").getValue() == null || dataSnapshot.child("username").getValue() == null) {
                        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                        startActivity(intent);
                    } else {
                        country.setText(String.valueOf(dataSnapshot.child("country").getValue()));
                        usernameTV.setText(String.valueOf(dataSnapshot.child("username").getValue()));
                        Constant.USERCOUNTRY = String.valueOf(dataSnapshot.child("country").getValue());
                        queryForUsers(String.valueOf(dataSnapshot.child("country").getValue()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        return view;
    }

    private void queryForUsers(final String userCountry) {
        nearByUsersList.clear();
        //query geolocation to find nearbyUsers
        GeoLocation center;
        if (location != null) {
            // use user's GPS to determine location if GPS it turned on
            center = new GeoLocation(location.getLatitude(), location.getLongitude());
        } else {
            // use user's previous location to determine location if GPS is turned off
            center = new GeoLocation(userLocation.getLatitude(), userLocation.getLongitude());
        }
//
//        //query geolocation to find nearbyUsers
        geoQuery = geoFire.queryAtLocation(center, value);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String username, final GeoLocation nearbyLocation) {

//                System.out.println(username + nearbyLocation);
                //query by country within the list of nearby users
                ref = new Firebase("https://originchat.firebaseio.com/users/" + username);

                ref.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

//                        System.out.println(dataSnapshot.child("country").getValue());
                        try {
                            if (dataSnapshot.child("country").getValue().toString().equals(userCountry) && !(dataSnapshot.getKey().toString().equals(Constant.USERID))) {
                                final User usersFromHome = new User();
                                usersFromHome.setCountry(String.valueOf(dataSnapshot.child("country").getValue()));
                                usersFromHome.setEmail(String.valueOf(dataSnapshot.child("email").getValue()));
                                usersFromHome.setUserId(dataSnapshot.getKey());
                                usersFromHome.setImage(String.valueOf(dataSnapshot.child("profilePic").getValue()));
                                usersFromHome.setUsername(String.valueOf(dataSnapshot.child("username").getValue()));
                                usersFromHome.setUserLatitude(String.valueOf(nearbyLocation.latitude));
                                usersFromHome.setUserLongitude(String.valueOf(nearbyLocation.longitude));

                                //calculates distance to from the current user to the list of other nearby users
                                if (location != null) {
//                                    System.out.println("location if gps is off" + userLocation.getLatitude() + " " + userLocation.getLongitude());
                                    Location loc = new Location("");
                                    loc.setLatitude(nearbyLocation.latitude);
                                    loc.setLongitude(nearbyLocation.longitude);
                                    distanceToUser = userLocation.distanceTo(loc) / 1000;
                                    usersFromHome.setDistanceToUser(distanceToUser);

                                } else {
                                    // if location is not available, query for user's previous location
                                    queryPreviousLocation = new Firebase("https://originchat.firebaseio.com/locations/" + Constant.USERID);
                                    queryPreviousLocation.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            userLocation.setLatitude((Double) dataSnapshot.child("l").child("0").getValue());
                                            userLocation.setLongitude((Double) dataSnapshot.child("l").child("1").getValue());

                                            Location loc = new Location("");
                                            loc.setLatitude(nearbyLocation.latitude);
                                            loc.setLongitude(nearbyLocation.longitude);
                                            distanceToUser = userLocation.distanceTo(loc) / 1000;
                                            usersFromHome.setDistanceToUser(distanceToUser);

//                                            System.out.println("Distance to user " + usersFromHome.getId() + "  " +
//                                                    usersFromHome.getUserLatitude() + " " + usersFromHome.getUserLongitude() + " "
//                                                    + userLocation.getLatitude() + " " + userLocation.getLongitude() + " " +  usersFromHome.getDistanceToUser());
//
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {

                                        }
                                    });

                                }


//                                System.out.println("nearByUsers " + usersFromHome.getEmail() + " is from " + usersFromHome.getCountry() + " is " + distanceToUser + " km away");
                                nearByUsersList.add(usersFromHome);

                                //putting items into an individual holder which gets fed into an adapter
                                class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                                    private User mUser;
                                    TextView mCountryTV;
                                    TextView mDistanceAway;
                                    ImageView mProfilePic;
                                    TextView mUsername;

                                    public UserHolder(View itemView) {
                                        super(itemView);

                                        itemView.setOnClickListener(this);
                                        mCountryTV = (TextView) itemView.findViewById(R.id.usernameTV);

                                        mDistanceAway = (TextView) itemView.findViewById(R.id.distanceAwayTV);
                                        mProfilePic = (ImageView) itemView.findViewById(R.id.imageView);
                                        mUsername = (TextView) itemView.findViewById(R.id.usernameTV);
                                    }

                                    public void bindUserItem(User user) {
                                        mUser = user;
                                        byte[] imageAsBytes = Base64.decode(user.getImage(), Base64.DEFAULT);
                                        Bitmap bmp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
                                        mUsername.setText(user.getUsername());
                                        if (bmp != null) {
                                            mProfilePic.setImageBitmap(bmp);
                                        } else {
                                            Bitmap icon = BitmapFactory.decodeResource(getActivity().getResources(),
                                                    R.drawable.unknownuser);
                                            mProfilePic.setImageBitmap(icon);
                                        }

//                                        System.out.println("Distance to user in bindUserItem " + user.getId() + "  " + user.getUserLatitude() + " " + user.getUserLongitude() + " " + user.getDistanceToUser());

                                        if (user.getDistanceToUser() == null || user.getDistanceToUser().equals(null)) {
                                            mDistanceAway.setText("< 1" + " km away");
                                        } else {
                                            String formattedDistance = String.format("%.2f", user.getDistanceToUser());
                                            mDistanceAway.setText(formattedDistance + " km away");
                                        }

                                    }

                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getActivity(), UserDetailActivity.class);
                                        intent.putExtra("userId", mUser.getId());
                                        startActivity(intent);
                                    }
                                }

                                class UserAdapter extends RecyclerView.Adapter<UserHolder> {

                                    private List<User> mUserItems;

                                    public UserAdapter(List<User> userItems) {
                                        mUserItems = userItems;
                                    }

                                    @Override
                                    public UserHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
//                                TextView textView = new TextView(getActivity());
                                        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                                        View view = layoutInflater.inflate(R.layout.list_item_user, viewGroup, false);
                                        return new UserHolder(view);
                                    }

                                    @Override
                                    public void onBindViewHolder(UserHolder userHolder, int position) {
                                        User userItem = mUserItems.get(position);
                                        userHolder.bindUserItem(userItem);
                                    }

                                    @Override
                                    public int getItemCount() {
                                        return mUserItems.size();
                                    }
                                }

                                if (isAdded()) {
                                    Collections.sort(nearByUsersList);
                                    mPhotoRecyclerView.setAdapter(new UserAdapter(nearByUsersList));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });

            }

            @Override
            public void onKeyExited(String username) {
                nearByUsersList.remove(username);
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
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            try {
//                System.out.println(location.getLatitude() + " " + location.getLongitude());
                userLocation = new Location("");
                userLocation.setLatitude(location.getLatitude());
                userLocation.setLongitude(location.getLongitude());

            } catch (Exception e) {
                e.printStackTrace();
            }

//            Log.i("geoLocation", String.valueOf(location.getLatitude())  + String.valueOf(location.getLongitude()) );
            geoFire = new GeoFire(new Firebase("https://originchat.firebaseio.com/locations/"));
            if (location != null) {
                geoFire.setLocation(Constant.USERID, new GeoLocation(location.getLatitude(), location.getLongitude()));

            }

            setAddress(location);

        }
    }

    private void setAddress(Location geoLocation) {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> listAddresses = null;
        try {
            listAddresses = geocoder.getFromLocation(geoLocation.getLatitude(), geoLocation.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (listAddresses != null && listAddresses.size() > 0) {
//            Log.i("PlaceInfo", listAddresses.get(0).toString());
            String addressHolder = "";
            for (int i = 0; i <= listAddresses.get(0).getMaxAddressLineIndex(); i++) {
                addressHolder += listAddresses.get(0).getAddressLine(i) + "\n";
                Log.i("address", addressHolder);
            }
            addressTV.setText(addressHolder + "\n");
        }else{
            addressTV.setText("Please turn on GPS. Address not found.");
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
