package com.Teddy.android_school_timetable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import com.Teddy.android_school_timetable.view.Timetable;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = getSharedPreferences("theme", Context.MODE_PRIVATE);
        if (preferences.getBoolean("dark", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }


        Timetable table = findViewById(R.id.Time_table);
        table.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int w = (int) event.getX() / table.OneW;
                int t = (int) event.getY() / table.OneH;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (table.Is_Open()) {
                            if (table.Should_Close_it(event.getX(), event.getY())) {
                                table.Close_it();
                                table.edit_over();

                            //} else if (table.Should_Go(event.getX(), event.getY())) {

                            //} else if (table.Should_Note(event.getX(), event.getY())) {

                            } else if (table.Should_Edit_it(event.getX(), event.getY())) {

                            }
                        }else{
                            table.get_Pointed(w,t);
                            table.Make_small();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (table.get_One() != null && (w != table.get_One().week || (t < table.get_One().time1 || t > table.get_One().time2))) {
                            table.Make_nol();
                        } else
                            return false;
                        break;
                    case MotionEvent.ACTION_UP:
                        if (table.get_One() != null) {
                            if (table.get_One().week != w || table.get_One().time1 > t || table.get_One().time2 < t)
                                table.Make_nol();
                            else
                                table.Open_it();
                        }
                        table.performClick();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        if (table.get_One() != null)
                            table.Make_nol();
                        break;
                    default:
                        table.Quick_Close_it();
                        //findViewById(R.id.editbu).setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                table.addClass(0,0,1,"126663","123","123", Color.GRAY,false);

                table.addClass(6,2,3,"123","123","123", Color.GRAY,false);

                table.addClass(3,2,2,"测试","测试老师","一个教室", Color.DKGRAY,false);
                table.flash();
            }
        }).start();

    }
}