package com.Teddy.android_school_timetable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GestureDetectorCompat;

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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.Teddy.android_school_timetable.view.Timetable;

public class MainActivity extends AppCompatActivity{

    private GestureDetectorCompat mDetector;
    private Timetable table;

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

        mDetector = new GestureDetectorCompat(this,new FlingGestureListener());




        table = findViewById(R.id.Time_table);
        table.setOnTouchListener(new View.OnTouchListener() {

            boolean Opened=false;//防止关闭时打开
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int w = (int) event.getX() / table.OneW;
                int t = (int) event.getY() / table.OneH;

                if(event.getActionIndex()!=0)//单指操作
                    return mDetector.onTouchEvent(event);;
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        if (table.Is_Open()) {
                            Opened=true;
                            if (table.Should_Close_it(event.getX(), event.getY())) {
                                table.Close_class();



                            } else if (table.Should_Edit_it(event.getX(), event.getY())) {

                            }
                        }else{
                            Opened=false;
                            table.get_Pointed(w,t);
                            table.Press_classs();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:

                        if (table.get_One() != null && (w != table.get_One().week || (t < table.get_One().time1 || t > table.get_One().time2))) {
                            table.Release_class();
                        }
                        if(table.get_One() != table.get_Pointed(w,t)&&!Opened) {
                            table.Press_classs();
                        }
                        else
                            return mDetector.onTouchEvent(event);;
                        break;
                    case MotionEvent.ACTION_UP:

                        if (table.get_One() != null) {
                            if (table.get_One().week != w || table.get_One().time1 > t || table.get_One().time2 < t)
                                table.Release_class();
                            else if(!Opened)
                                table.Open_class();


                        }
                        //table.performClick();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        if (table.get_One() != null)
                            table.Release_class();
                        break;
                    default:
                        //table.Quick_Close_it();
                        //findViewById(R.id.editbu).setVisibility(View.GONE);
                        break;
                }
                return mDetector.onTouchEvent(event);
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

                table.addClass(1,1,1,"123","123","123", Color.RED,false);
                table.addClass(2,1,1,"测试4","测试老师","一个教室", Color.DKGRAY,false);
                table.addClass(3,1,1,"测试4","测试老师","一个教室", Color.GREEN,false);
                table.addClass(4,1,1,"测试1","测试老师","一个教室", Color.GRAY,false);
                table.addClass(5,1,1,"测试1","测试老师","一个教室", Color.BLUE,false);

                table.addClass(1,2,2,"测试2","测试老师","一个教室", Color.RED,false);
                table.addClass(2,2,2,"测试3","测试老师","一个教室", Color.DKGRAY,false);
                table.addClass(3,2,2,"测试0","测试老师","一个教室", Color.GREEN,false);
                table.addClass(4,2,2,"233","测试老师","一个教室", Color.GRAY,false);
                table.addClass(5,2,2,"123","测试老师","一个教室", Color.BLUE,false);

                table.addClass(1,3,3,"测试2","测试老师","一个教室", Color.RED,false);
                table.addClass(2,3,3,"测试3","测试老师","一个教室", Color.DKGRAY,false);
                table.addClass(3,3,3,"测试0","测试老师","一个教室", Color.GREEN,false);
                table.addClass(4,3,3,"332","测试老师","一个教室", Color.GRAY,false);
                table.addClass(5,3,3,"321","测试老师","一个教室", Color.BLUE,false);

                table.addClass(1,4,4,"测试2","测试老师","一个教室", Color.RED,false);
                table.addClass(2,4,4,"测试333","测试老师","一个教室", Color.DKGRAY,false);
                table.addClass(3,4,4,"测试0","测试老师","一个教室", Color.GREEN,false);
                table.addClass(4,4,4,"测试1","测试老师","一个教室", Color.GRAY,false);
                table.addClass(5,4,4,"测试1","测试老师","一个教室", Color.BLUE,false);


                table.flash();
            }
        }).start();

    }


    class FlingGestureListener extends GestureDetector.SimpleOnGestureListener {


        @Override
        public boolean onDown(MotionEvent event) {

            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            if(table.opening) {
                Log.d("FlingGestureListener", "onFling: 取消");
                return true;
            }
            if((event1.getX()-event2.getX())>300&&velocityX<-200)
                Log.d("FlingGestureListener", "onFling: 向左");
            else if((event2.getX()-event1.getX())>300&&velocityX>200)
                Log.d("FlingGestureListener", "onFling: 向右");

            return true;
        }
    }



}