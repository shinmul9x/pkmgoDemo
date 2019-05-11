package com.example.pokemongo1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Guide extends AppCompatActivity {
    TextView txtGuide;
    Button btnReturn,btnReturn2;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide);
        txtGuide =(TextView) findViewById(R.id.txtGuide);
        btnReturn =(Button) findViewById(R.id.btnReturn);
        btnReturn2=(Button) findViewById(R.id.btnReturn2);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Guide.this,Main.class);
                startActivity(intent);
            }
        });
        btnReturn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Guide.this,MapsActivity.class);
                startActivity(intent);
            }
        });
    }
}
