package com.azwinckteam.androidmultichoicequiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class HalamanUtamaActivity extends AppCompatActivity {

    ImageButton pindah;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_halaman_utama);

        pindah = (ImageButton) findViewById(R.id.btn_tips);
        pindah.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(HalamanUtamaActivity.this,TipsActivity.class);
                startActivity(intent);
            }
        });

        pindah = (ImageButton) findViewById(R.id.btn_simulasi);
        pindah.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(HalamanUtamaActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        pindah = (ImageButton) findViewById(R.id.btn_tentang);
        pindah.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(HalamanUtamaActivity.this,TentangActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("exit");
        builder.setMessage("do you want to exit");

        builder.setPositiveButton("IYA", new DialogInterface.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(DialogInterface dialogInterface, int i){
                //finish();
                //System.exit(0);
                //moveTaskToBack(true);
               finishAffinity();
            }
        });
        builder.setNegativeButton("TIDAK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i){

            }
        });
        AlertDialog dialog =  builder.show();
    }

}
