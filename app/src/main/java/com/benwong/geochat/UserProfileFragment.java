package com.benwong.geochat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class UserProfileFragment extends Fragment {
    private EditText emailET;
    private EditText usernameET;
    private EditText userDescriptionET;
    private EditText passwordET;
    private TextView countryTV;
    private TextView countryTV2;


    private Button saveProfileBtn;
    private ImageView attachmentBtn;
    String profileImage;
    private ImageView profilePic;
    Firebase ref;
    private SeekBar distanceControl;

    private TextView sortKM;

    public UserProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        sortKM = (TextView)view.findViewById(R.id.sortKm);

        int value = 0;

        distanceControl = (SeekBar) view.findViewById(R.id.seekBar);

        SharedPreferences prefs = getContext().getSharedPreferences("locationSortPreference", Context.MODE_PRIVATE);

        value = prefs.getInt("seekBarValue", 0); // 0 is default

        distanceControl.setProgress(prefs.getInt("seekBarValue", value));

        sortKM.setText(String.valueOf(prefs.getInt("seekBarValue", value)) );

        distanceControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                Constant.DISTANCE_SORT = progress;


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences prefs = getContext().getSharedPreferences("locationSortPreference", Context.MODE_PRIVATE);
                // Don't forget to call commit() when changing preferences.
                prefs.edit().putInt("seekBarValue", seekBar.getProgress()).commit();

                sortKM.setText(String.valueOf(prefs.getInt("seekBarValue", seekBar.getProgress()))+ " km");
            }
        });

        ref = new Firebase("https://originchat.firebaseio.com/users/" + Constant.USERID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                String image = String.valueOf(snapshot.child("profilePic").getValue());

                byte[] imageAsBytes = Base64.decode(image, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

                profilePic.setImageBitmap(bmp);

                if (!(snapshot.child("country").getValue() == null)) {
                    countryTV2.setText(String.valueOf(snapshot.child("country").getValue()));
                } else {
                    countryTV2.setText("No Country Selected Yet");
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        emailET = (EditText) view.findViewById(R.id.emailET);

        usernameET = (EditText) view.findViewById(R.id.usernameET);
        userDescriptionET = (EditText) view.findViewById(R.id.userDescriptionET);
        attachmentBtn = (ImageView) view.findViewById(R.id.attachment);
        profilePic = (ImageView) view.findViewById(R.id.profilePic);
        countryTV = (TextView) view.findViewById(R.id.countryTV);
        countryTV2 = (TextView) view.findViewById(R.id.countryTV2);

        countryTV2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCountryAlert();
            }
        });


        attachmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(i, Constant.REQUEST_PICK_IMAGE);

            }
        });

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot);
                if (!(snapshot.child("username").getValue() == null)) {
                    usernameET.setText(String.valueOf(snapshot.child("username").getValue()));
                }

                if (snapshot.child("description").getValue() != null) {
                    userDescriptionET.setText(String.valueOf(snapshot.child("description").getValue()));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        emailET.setText(Constant.USEREMAIL);

        passwordET = (EditText) view.findViewById(R.id.passwordET);
        passwordET.setText(Constant.USERPASSWORD);
        saveProfileBtn = (Button) view.findViewById(R.id.saveProfileButton);

        saveProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // change email

                if (!(Constant.USEREMAIL.equals(emailET.getText().toString())) && !(emailET.getText().equals(""))) {
                    ref = new Firebase("https://originchat.firebaseio.com/");
                    ref.changeEmail(Constant.USEREMAIL, passwordET.getText().toString(), emailET.getText().toString(), new Firebase.ResultHandler() {
                        @Override
                        public void onSuccess() {
                            // email changed
                            Toast.makeText(getActivity(), R.string.email_save_success, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            Toast.makeText(getActivity(), R.string.email_save_error, Toast.LENGTH_LONG).show();
                        }
                    });
                }


                if (!(Constant.USERPASSWORD.equals(passwordET.getText().toString()))) {
                    ref = new Firebase("https://originchat.firebaseio.com/");
                    ref.changePassword(passwordET.getText().toString(), Constant.USERPASSWORD, passwordET.getText().toString(), new Firebase.ResultHandler() {
                        @Override
                        public void onSuccess() {
                            // password changed
                            Toast.makeText(getActivity(), R.string.password_save_success, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            // error encountered
                            Toast.makeText(getActivity(), R.string.password_save_error, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                //Changing other fields

                if (!(usernameET.getText().toString().equals("")) &&
                        !(userDescriptionET.getText().toString().equals("")) &&
                        !(countryTV2.getText().toString().toString().equals("No Country Selected Yet"))) {

                    ref = new Firebase("https://originchat.firebaseio.com/users/" + Constant.USERID);
                    ref.child("username").setValue(usernameET.getText().toString());
                    ref.child("description").setValue(userDescriptionET.getText().toString());
                    Toast.makeText(getActivity(), R.string.profile_save_success_msg, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), R.string.fill_in_fields_warning, Toast.LENGTH_LONG).show();
                }


            }
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println("Fragment onActivityResult" + " " + requestCode + " " + resultCode + " " + data);
        System.out.println("constant request code " + Constant.REQUEST_PICK_IMAGE);

        if (resultCode == -1 && data != null) {

            Uri selectedImage = data.getData();

            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);

                bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                byte[] byteArray = stream.toByteArray();

                profileImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                ref = new Firebase("https://originchat.firebaseio.com/users/" + Constant.USERID);

                ref.child("profilePic").setValue(profileImage, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        Toast.makeText(getActivity(), R.string.save_profile_pic_msg, Toast.LENGTH_LONG).show();

                        byte[] imageAsBytes = Base64.decode(profileImage, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

                        profilePic.setImageBitmap(bmp);

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(getActivity(), R.string.error_uploading_profile_pic, Toast.LENGTH_LONG).show();
        }

    }

    private void selectCountryAlert() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());

        builderSingle.setTitle(R.string.select_country);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.select_dialog_singlechoice);

        String[] isoCountries = Locale.getISOCountries();

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
                        builderInner.setTitle(R.string.selected_country_message);
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

                        countryTV2.setText(strName);

                        ref = new Firebase("https://originchat.firebaseio.com/");

                        ref.child("users").child(Constant.USERID).child("country").setValue(strName);

                    }
                });
        builderSingle.show();
    }
}
