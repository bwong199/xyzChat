package com.benwong.geochat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;


/**
 * A placeholder fragment containing a simple view.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {


    private TextView registerLink;

    private EditText emailLogin;
    private EditText passwordLogin;
    private Firebase ref;
    private Button loginButton;

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.goOffline();
        Firebase.setAndroidContext(getContext());
        ref = new Firebase("https://originchat.firebaseio.com");
        ref.unauth();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_main, container, false);

        registerLink = (TextView)view.findViewById(R.id.registerLink);

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        emailLogin = (EditText)view.findViewById(R.id.emailLogin);
        passwordLogin = (EditText)view.findViewById(R.id.passwordLogin);
        loginButton = (Button)view.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);


        return view;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loginButton:

                LoginUser();


        }
    }

    private void LoginUser() {
        ref.unauth();
        ref.authWithPassword(emailLogin.getText().toString(),passwordLogin.getText().toString(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                System.out.println("User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
                Toast.makeText(getContext(), "Logged in Successfully", Toast.LENGTH_LONG).show();
                Constant.USERID = authData.getUid();
                Constant.USEREMAIL = emailLogin.getText().toString();
                Constant.USERPASSWORD = passwordLogin.getText().toString();

                Intent intent = new Intent(getActivity(), UserListActivity.class);
                intent.putExtra("loginEmail", emailLogin.getText().toString());
                intent.putExtra("userId", authData.getUid());
                startActivity(intent);
            }
            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                // there was an error
                System.out.println(firebaseError.getMessage());
                System.out.println(firebaseError.getDetails());

                switch (firebaseError.getCode()) {
                    case FirebaseError.USER_DOES_NOT_EXIST:
                        Toast.makeText(getContext(), "User does not exist", Toast.LENGTH_LONG).show();
                        // handle a non existing user
                        break;
                    case FirebaseError.INVALID_PASSWORD:
                        Toast.makeText(getContext(), "Invalid password entered", Toast.LENGTH_LONG).show();
                        // handle an invalid password
                        break;
                      default:
                        // handle other errors
                          Toast.makeText(getContext(), firebaseError.getMessage().toString(), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }
}
