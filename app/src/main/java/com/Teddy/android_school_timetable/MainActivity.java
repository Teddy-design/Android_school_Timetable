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
import android.os.Trace;
import android.util.Log;
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
            boolean Pointed=false;//防止多指混乱
            float x=0f;
            float y=0f;
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                int w = (int) event.getX() / table.OneW;
                int t = (int) event.getY() / table.OneH;
                Log.d("action:",""+event.getAction());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if(!Pointed)
                            Pointed= true;
                        else
                            break;
                        if (table.Is_Open()) {
                            if (table.Should_Close_it(event.getX(), event.getY())) {
                                table.Close_class();
                                table.edit_over();

                            //} else if (table.Should_Go(event.getX(), event.getY())) {

                            //} else if (table.Should_Note(event.getX(), event.getY())) {

                            } else if (table.Should_Edit_it(event.getX(), event.getY())) {

                            }
                        }else{
                            table.get_Pointed(w,t);
                            table.Press_classs();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if ((Math.abs(event.getX()-x)<5)&&(Math.abs(event.getY()-y)<5))
                            break;
                        else{
                            x=event.getX();
                            y=event.getY();
                        }
                        if (table.get_One() != null && (w != table.get_One().week || (t < table.get_One().time1 || t > table.get_One().time2))) {
                            table.Release_class();
                        }else if(table.get_One() != table.get_Pointed(w,t)) {
                            table.Press_classs();
                        }
                        else
                            return false;
                        break;
                    case MotionEvent.ACTION_UP:
                        if(Pointed)
                            Pointed= false;
                        else
                            break;
                        if (table.get_One() != null&&!table.Is_Open()) {
                            if (table.get_One().week != w || table.get_One().time1 > t || table.get_One().time2 < t)
                                table.Release_class();
                            else
                                table.Open_class();
                        }
                        table.performClick();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        if (table.get_One() != null)
                            table.Release_class();
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
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                table.addClass(0,0,1,"126663","123","123", Color.GRAY,false);

                table.addClass(1,1,1,"123","123","123", Color.WHITE,false);
                table.addClass(2,1,1,"测试4","测试老师","一个教室", Color.DKGRAY,false);
                table.addClass(3,1,1,"测试4","测试老师","一个教室", Color.GREEN,false);
                table.addClass(4,1,1,"测试1","测试老师","一个教室", Color.GRAY,false);
                table.addClass(5,1,1,"测试1","测试老师","一个教室", Color.BLUE,false);

                table.addClass(1,2,2,"测试2","测试老师","一个教室", Color.WHITE,false);
                table.addClass(2,2,2,"测试3","测试老师","一个教室", Color.DKGRAY,false);
                table.addClass(3,2,2,"测试0","测试老师","一个教室", Color.GREEN,false);
                table.addClass(4,2,2,"测试1","测试老师","一个教室", Color.GRAY,false);
                table.addClass(5,2,2,"测试1","测试老师","一个教室", Color.BLUE,false);

                table.addClass(1,3,3,"测试2","测试老师","一个教室", Color.WHITE,false);
                table.addClass(2,3,3,"测试3","测试老师","一个教室", Color.DKGRAY,false);
                table.addClass(3,3,3,"测试0","测试老师","一个教室", Color.GREEN,false);
                table.addClass(4,3,3,"测试1","测试老师","一个教室", Color.GRAY,false);
                table.addClass(5,3,3,"测试1","测试老师","一个教室", Color.BLUE,false);

                table.addClass(1,4,4,"测试2","测试老师","一个教室", Color.WHITE,false);
                table.addClass(2,4,4,"测试3","测试老师","一个教室", Color.DKGRAY,false);
                table.addClass(3,4,4,"测试0","测试老师","一个教室", Color.GREEN,false);
                table.addClass(4,4,4,"测试1","测试老师","一个教室", Color.GRAY,false);
                table.addClass(5,4,4,"测试1","测试老师","一个教室", Color.BLUE,false);


                table.flash();
            }
        }).start();

    }
}