package com.benwong.geochat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class RegisterFragment extends Fragment implements View.OnClickListener{

    private EditText usernameRegister;
    private EditText emailRegister;
    private EditText passwordRegister;
    private EditText passwordConfirmationRegister;
    private EditText countryRegister;
    private Button registerButton;
    Firebase ref;

    public RegisterFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);


        emailRegister = (EditText)view.findViewById(R.id.emailLogin);
        passwordRegister = (EditText)view.findViewById(R.id.passwordLogin);


        registerButton = (Button)view.findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(getContext());
        ref = new Firebase("https://originchat.firebaseio.com");
        Log.i("firebase", ref.toString());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.registerButton:
            createUser();


        }
    }

    private void createUser(){


        ref.createUser(emailRegister.getText().toString(),passwordRegister.getText().toString(), new Firebase.ValueResultHandler<Map<String, Object>>()  {


            @Override
            public void onSuccess(Map<String, Object> result) {
                System.out.println("Successfully created user account with uid: " + result.get("uid"));

                Firebase userRef = ref.child("users").child(result.get("uid").toString());

                userRef.child("email").setValue(emailRegister.getText().toString());


                Toast.makeText(getContext(), "User successfully created.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("loginEmail", emailRegister.getText().toString());
                startActivity(intent);
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                Toast.makeText(getContext(), "Registration Error", Toast.LENGTH_LONG).show();
                System.out.println(firebaseError);
            }


        });
    }
}
