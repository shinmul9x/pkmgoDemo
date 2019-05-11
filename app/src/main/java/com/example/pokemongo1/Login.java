package com.example.pokemongo1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private String id,name;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager=CallbackManager.Factory.create();
        loginButton=(LoginButton)findViewById(R.id.login_button);
        setLoginButton();

    }

    private void setLoginButton() {
        loginButton.setReadPermissions("public_profile");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(Login.this, "Success", Toast.LENGTH_LONG).show();
                Log.d("Login","Success!");
                Intent intent = new Intent(Login.this, MapsActivity.class);
                //intent.setClass(Login.this, MapsActivity.class);
                intent.putExtra("idfb",loginResult.getAccessToken().getUserId());
                startActivity(intent);

                //resuilt();
                // Lấy id của user loginResult.getAccessToken().getUserId()
                /*
                Intent intent1 = new Intent();
                intent1.putExtra("idfb",loginResult.getAccessToken().getUserId());
                setResult(RESULT_OK,intent1);
                finish();
                */
                ///////////////////////////////////////////////////////
                //ParseUser.getCurrentUser().setUsername(name);
                ParseUser.getCurrentUser().put("id",id);
                if (ParseUser.getCurrentUser() == null) {
                    ParseAnonymousUtils.logIn(new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (e == null) {
                                Log.i("Info", "Annonymous hero..");
                            } else {
                                Log.i("..", "false");
                            }

                            //redirectActivity();

                        }
                    });
                }
            }

            @Override
            public void onCancel() {
                Toast.makeText(Login.this,"facebook cancel",Toast.LENGTH_LONG).show();

                Intent intent = new Intent();
                intent.setClass(Login.this, Maps_Test.class);
                intent.putExtra("idfb", "100036382504484");
                startActivity(intent);
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(Login.this,"Lỗi không vào được facebook",Toast.LENGTH_LONG).show();

                Intent intent = new Intent();
                intent.setClass(Login.this, Maps_Test.class);
                //intent.putExtra("idfb", "100036382504484");
                startActivity(intent);
            }
        });
    }
    /*
    private void resuilt() {
        GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Log.d("JSON",response.getJSONObject().toString());
                try {
                    id=object.getString("id");
                    name=object.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields","name,id");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
    }*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
//    protected void onStart(){
//        LoginManager.getInstance().logOut();
//        super.onStart();
//    }
}
