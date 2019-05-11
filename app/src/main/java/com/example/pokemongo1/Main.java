package com.example.pokemongo1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;

public class Main extends AppCompatActivity {
    RelativeLayout manhinh;
    Button btn, btn2;
    DatabaseReference mData;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);

        //anh xa man hinh de doi background
        manhinh = (RelativeLayout) findViewById(R.id.manhinh);
        manhinh.setBackgroundResource(R.drawable.back2);
        mData = FirebaseDatabase.getInstance().getReference();

        btn = (Button) findViewById(R.id.btn);
        btn2 = (Button) findViewById(R.id.btn2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Main.this, MapsActivity.class);
                startActivity(intent);
                ParseUser.getCurrentUser().put("Guest", "nam");
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Main.this, Guide.class);
                startActivity(intent);
                //mData = FirebaseDatabase.getInstance().getReference();
                mData.child("Pokemon Start").push().setValue("duong minh duc");
            }
        });
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
}