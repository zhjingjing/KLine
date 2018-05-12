package com.kchart;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.kchart.aty.KActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button kchart,fchart;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        kchart= (Button) findViewById(R.id.btn_chartk);
        fchart=(Button)findViewById(R.id.btn_chartf);
        kchart.setOnClickListener(this);
        fchart.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_chartf:

                break;
            case R.id.btn_chartk:
                startActivity(new Intent(this, KActivity.class));
                break;

        }
    }
}
