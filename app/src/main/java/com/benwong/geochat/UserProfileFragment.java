package com.benwong.geochat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * A placeholder fragment containing a simple view.
 */
public class UserProfileFragment extends Fragment {


    private EditText emailET;
    private EditText usernameET;
    private EditText userDescriptionET;
    private EditText passwordET;
    private Button saveProfileBtn;
    private ImageView attachmentBtn;
    String profileImage;
    private ImageView profilePic;


    Firebase ref;



    public UserProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        ref = new Firebase("https://originchat.firebaseio.com/users/" + Constant.USERID);
        emailET = (EditText)view.findViewById(R.id.emailET);

        usernameET = (EditText)view.findViewById(R.id.usernameET);
        userDescriptionET = (EditText)view.findViewById(R.id.userDescriptionET);
        attachmentBtn = (ImageView)view.findViewById(R.id.attachment);
        profilePic = (ImageView)view.findViewById(R.id.profilePic);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                String image = String.valueOf(snapshot.child("profilePic").getValue());

                byte[] imageAsBytes = Base64.decode(image, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

                profilePic.setImageBitmap(bmp);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        attachmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(i,Constant.REQUEST_PICK_IMAGE );

            }
        });

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot);
                usernameET.setText(String.valueOf(snapshot.child("username").getValue()) );
                userDescriptionET.setText(String.valueOf(snapshot.child("description").getValue()) );
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        emailET.setText(Constant.USEREMAIL);

        passwordET = (EditText)view.findViewById(R.id.passwordET);
        passwordET.setText(Constant.USERPASSWORD);
        saveProfileBtn = (Button)view.findViewById(R.id.saveProfileButton);

        saveProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // change email

                if (! (Constant.USEREMAIL.equals(emailET.getText().toString()))){
                    ref = new Firebase("https://originchat.firebaseio.com/");
                    ref.changeEmail(Constant.USEREMAIL, passwordET.getText().toString(), emailET.getText().toString(), new Firebase.ResultHandler() {
                        @Override
                        public void onSuccess() {
                            // email changed
                            Toast.makeText(getActivity(), "Email Successfully Changed", Toast.LENGTH_LONG).show();
                        }
                        @Override
                        public void onError(FirebaseError firebaseError) {
                            Toast.makeText(getActivity(), "Error saving email", Toast.LENGTH_LONG).show();
                        }
                    });
                }


                if (! (Constant.USERPASSWORD.equals(passwordET.getText().toString()))) {
                    ref = new Firebase("https://originchat.firebaseio.com/");
                    ref.changePassword(passwordET.getText().toString(), Constant.USERPASSWORD, passwordET.getText().toString(), new Firebase.ResultHandler() {
                        @Override
                        public void onSuccess() {
                            // password changed
                            Toast.makeText(getActivity(), "Password Successfully Changed", Toast.LENGTH_LONG).show();
                        }
                        @Override
                        public void onError(FirebaseError firebaseError) {
                            // error encountered
                            Toast.makeText(getActivity(), "Error saving password", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                //Changing other fields

                ref = new Firebase("https://originchat.firebaseio.com/users/" +  Constant.USERID );

                ref.child("username").setValue(usernameET.getText().toString());
                ref.child("description").setValue(userDescriptionET.getText().toString());
                Toast.makeText(getActivity(), "Profile Info Successfully Saved", Toast.LENGTH_LONG).show();
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

            System.out.println("Made it pass the if statement");

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
                        Toast.makeText(getActivity(), "Profile Picture Saved Successfully", Toast.LENGTH_LONG).show();

                        byte[] imageAsBytes = Base64.decode(profileImage, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

                        profilePic.setImageBitmap(bmp);

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(getActivity(), "Error Uploading File", Toast.LENGTH_LONG).show();
        }

    }
}
