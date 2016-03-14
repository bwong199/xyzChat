package com.benwong.geochat;

import android.app.ProgressDialog;
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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


/**
 * A placeholder fragment containing a simple view.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView registerLink;

    private EditText emailLogin;
    private EditText passwordLogin;
    private Firebase ref;
    private Button mLoginButton;
    private ProgressDialog mAuthProgressDialog;

    // Facebook
    private LoginButton loginButton;
    CallbackManager callbackManager;

    /* Data from the authenticated user */
    private AuthData mAuthData;

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Firebase.goOffline();
        Firebase.setAndroidContext(getContext());
        FacebookSdk.sdkInitialize(getContext());

        LoginManager.getInstance().logOut();


        final Firebase authListener = new Firebase("https://originchat.firebaseio.com/");
        authListener.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {

                if (authData != null) {
                    // user is logged in
//                    System.out.println("line 74" + authData);


                } else {
                    // user is not logged in
                }
            }
        });



//        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));
        callbackManager = CallbackManager.Factory.create();


        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        System.out.println(loginResult);

                    }
                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        System.out.println(exception);
                    }
                });


        ref = new Firebase("https://originchat.firebaseio.com");
        ref.unauth();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_main, container, false);

        registerLink = (TextView) view.findViewById(R.id.registerLink);

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        emailLogin = (EditText) view.findViewById(R.id.emailLogin);
        passwordLogin = (EditText) view.findViewById(R.id.passwordLogin);
        mLoginButton = (Button) view.findViewById(R.id.loginButton);
        mLoginButton.setOnClickListener(this);




        //fb login
        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");
        // If using in a fragment
        loginButton.setFragment(this);
        // Other app specific specialization

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code

                onFacebookAccessTokenChange(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });


        return view;
    }

    private void onFacebookAccessTokenChange(AccessToken token) {
        if (token != null) {
            ref.authWithOAuthToken("facebook", token.getToken(), new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(final AuthData authData) {

                    // The Facebook user is now authenticated with your Firebase app
                    for (String x : authData.getProviderData().keySet()) {
                        System.out.println(x);
                    }

                    System.out.println(authData.getProviderData().get("cachedUserProfile"));

                    Constant.FacebookID = String.valueOf(authData.getProviderData().get("id"));

//                    FacebookUser facebookUser = new FacebookUser();
//                    facebookUser.setUserId(String.valueOf(authData.getProviderData().get("id")));
//                    facebookUser.setDisplayName(String.valueOf(authData.getProviderData().get("displayName")));
//                    facebookUser.setProfileImage(String.valueOf(authData.getProviderData().get("profileImageURL")));

                    ref = new Firebase("https://originchat.firebaseio.com/users");

                    Query queryRef = ref.orderByChild("facebookId").equalTo(String.valueOf(authData.getProviderData().get("id")));

                    queryRef.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            System.out.println(dataSnapshot);

                            if (dataSnapshot.getValue() == null) {
                                System.out.println("faceid is null");
                                Constant.FacebookID = (String) authData.getProviderData().get("id");
                                Intent intent = new Intent(getActivity(), RegisterActivity.class);
                                startActivity(intent);
                            } else {

                                Constant.USERID = dataSnapshot.getKey();
                                Intent intent = new Intent(getActivity(), UserListActivity.class);
                                startActivity(intent);
                            }

                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }

                    });
//                    Intent intent = new Intent(getActivity(), UserListActivity.class);
//                    startActivity(intent);


                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    // there was an error
                    System.out.println(firebaseError);
                }
            });
        } else {
        /* Logged out of Facebook so do a logout from the Firebase app */
            ref.unauth();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        System.out.println(data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginButton:
                loginUser();
                break;


        }
    }


    private void loginUser() {
        ref.authWithPassword(emailLogin.getText().toString(), passwordLogin.getText().toString(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                System.out.println("User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
                Toast.makeText(getContext(), "Logged in Successfully", Toast.LENGTH_LONG).show();
                Constant.USERID = authData.getUid();
                Constant.USEREMAIL = emailLogin.getText().toString();
                Constant.USERPASSWORD = passwordLogin.getText().toString();

                //set user's email in the user tree upon registration
                ref = new Firebase("https://originchat.firebaseio.com/users/" + Constant.USERID);
                ref.child("email").setValue(emailLogin.getText().toString());

                Intent intent = new Intent(getActivity(), UserListActivity.class);
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
