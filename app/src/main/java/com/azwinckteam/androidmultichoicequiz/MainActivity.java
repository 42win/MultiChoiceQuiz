package com.azwinckteam.androidmultichoicequiz;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;

import com.azwinckteam.androidmultichoicequiz.Adapter.CategoryAdapter;
import com.azwinckteam.androidmultichoicequiz.Common.SpaceDecoration;
import com.azwinckteam.androidmultichoicequiz.DBHelper.DBHelper;


public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyler_category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("TOEIC");
        setSupportActionBar(toolbar);

        recyler_category = (RecyclerView)findViewById(R.id.rycyler_category);
        recyler_category.setHasFixedSize(true);
        recyler_category.setLayoutManager(new GridLayoutManager(this, 2));

        //Get Screen High
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int heigh = displayMetrics.heightPixels / 8;  //Max size of item in category
        CategoryAdapter adapter = new CategoryAdapter(MainActivity.this, DBHelper.getInstance(this).getAllCategories());
        int spaceInPixel = 4;
        recyler_category.addItemDecoration(new SpaceDecoration(spaceInPixel));
        recyler_category.setAdapter(adapter);
    }
}
