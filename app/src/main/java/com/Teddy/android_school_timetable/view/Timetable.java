package com.Teddy.android_school_timetable.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Looper;
import android.os.Trace;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicBlur;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;


import com.Teddy.android_school_timetable.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static android.os.SystemClock.sleep;


public class Timetable extends View {

    String TAG= "TimeTable";
    boolean debug = true;

    public ArrayList<Integer> Cnum;
    Context context;
    public ShowClass[] Has;


    private int Height;//高
    private int Width;//宽
    private int Mclass;//上午课
    private int Aclass;//下午课
    public int OneH;//一个高
    public int OneW;//一个宽

    public boolean pressing;//按压开始
    public boolean opening;//打开开始
    public boolean moving;
    private boolean edit;

    private boolean blur_first;//只模糊一次
    private RenderScript mRenderScript;
    private ScriptIntrinsicBlur mBlurScript;
    private Allocation mBlurInput, mBlurOutput;
    private Canvas blur_canvas;
    private Bitmap mBitmapToBlur, mBlurredBitmap;

    private ShowClass one;//被点击的
    //起始位置
    private float SL;
    private float SR;
    private float ST;
    private float SB;

    //结束位置
    private float EL;
    private float ER;
    private float ET;
    private float EB;

    //移动坐标
    private float ML;
    private float MR;
    private float MT;
    private float MB;

    //临时坐标
    private float lx;
    private float rx;
    private float ty;
    private float by;

    //动画更新次数,越大越慢
    private final int C_speed = 25;

    //移动距离
    private float[] Move;
    //剩余更新次数
    int C_times;
    //单次4边速度
    float[] speed;

    private int tempt1;
    private int tempt2;


    private boolean press_flag;//打断press标志
    private boolean open_flag;//打断open标志

    int back_color;

    private int N_text_color;//normal文字颜色

    private int P_text_color;//pointed文字颜色

    private int N_text_size;//normal文字大小

    private int P_text_size;//pointed文字大小

    private TextPaint N_text_paint;//normal

    private TextPaint P_text_paint;

    VectorDrawableCompat edit_button;//编辑图标

    private Paint Main_Paint;

    boolean night;

    public Timetable(Context context) {
        super(context);
        init(context);
    }

    public Timetable(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Timetable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * @author 20535
     * @time 2021/3/9 11:41
     * 初始化变量
     */
    private void init(Context context) {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        night = currentNightMode == Configuration.UI_MODE_NIGHT_YES;
        this.context = context;
        setCnum(4, 4);
        setLayerType(LAYER_TYPE_HARDWARE, null);

        pressing=false;
        opening = false;
        moving = false;
        press_flag=false;
        open_flag=false;

        init_image();

        RxJavaPlugins.setErrorHandler(throwable -> {
            //throwable.printStackTrace();
            //啥也不做
        });



    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Height = getMeasuredHeight();
        Width = getMeasuredWidth();

        init_Paint();
        init_N_class();
        init_blur();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Height = getMeasuredHeight();
        Width = getMeasuredWidth();

        init_Paint();
        init_N_class();
        invalidate();
    }

    /**
     *
     * @author 20535
     * @time 2021/3/22 10:43
     * 刷新
     */
    public void flash() {
        invalidate();
    }
    /**
     *
     * @author 20535
     * @time 2021/3/22 10:44
     * 初始化按键的照片
     */
    private void init_image(){

        edit_button = VectorDrawableCompat.create(context.getResources(),R.drawable.ic_edit,context.getTheme());

    }
    /**
     * @author 20535
     * @time 2021/3/9 15:27
     * 初始化normal课程大小
     */
    private void init_N_class() {
        SharedPreferences preferences = context.getSharedPreferences("class_num", Context.MODE_PRIVATE);

        Mclass = preferences.getInt("morning", 2);
        Aclass = preferences.getInt("afternoon", 3);

        Has = new ShowClass[(Mclass + Aclass) * 7];
        Cnum = new ArrayList<>();
        try {
            OneH = Height / (Aclass + Mclass);
            OneW = Width / 7;
        } catch (Exception e) {
            Toast.makeText(context, "课程数量设置异常", Toast.LENGTH_LONG).show();
        }
        EL = (float) Width / 5;
        ER = (float) (Width * 0.8);
        ET = (float) (Height * 0.25);
        EB = (float) (Height * 0.75);
    }


    /**
     * @author 20535
     * @time 2021/3/9 11:39
     * 初始化Paint
     */
    private void init_Paint() {
        P_text_paint = new TextPaint();
        P_text_paint.setAntiAlias(true);
        P_text_paint.setSubpixelText(true);
        P_text_paint.setStyle(Paint.Style.FILL);

        N_text_paint = new TextPaint();
        N_text_paint.setAntiAlias(true);
        N_text_paint.setSubpixelText(true);
        N_text_paint.setStyle(Paint.Style.FILL);

        Main_Paint = new Paint();
        Main_Paint.setAntiAlias(true);
        Main_Paint.setSubpixelText(true);
        Main_Paint.setStyle(Paint.Style.FILL);

        auto_set_text();
    }

    /**
     * @author 20535
     * @time 2021/3/9 11:18
     * 根据屏幕宽度选字的大小
     */
    private void auto_set_text() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(displayMetrics);//获取屏幕参数
        N_text_size = displayMetrics.widthPixels / 27;
        P_text_size = displayMetrics.widthPixels / 20;
        N_text_paint.setTextSize(N_text_size);
        P_text_paint.setTextSize(P_text_size);

        set_N_TextColor(Color.WHITE);
        set_P_TextColor(Color.BLACK);

    }


    /**
     * @author 20535
     * @time 2021/3/9 11:11
     * color设置课表 文字的颜色
     */
    public int set_N_TextColor(int color) {
        N_text_color = color;
        N_text_paint.setColor(N_text_color);
        return color;
    }

    /**
     * @author 20535
     * @time 2021/3/9 11:13
     * color设置点击 文字的颜色
     */
    public int set_P_TextColor(int color) {
        P_text_color = color;
        P_text_paint.setColor(P_text_color);
        return color;
    }

    /**
     * @author 20535
     * @time 2021/3/9 11:42
     * 设置上午下午课程数量
     */
    public void setCnum(int M, int A) {
        if (Mclass == M && Aclass == A)
            return;

        Mclass = M;
        Aclass = A;
        Has = new ShowClass[(Mclass + Aclass) * 7];
        Cnum = new ArrayList<>();
        OneH = Height / (Aclass + Mclass);
        OneW = Width / 7;
        invalidate();
    }


    /**
     * @author 20535
     * @time 2021/3/10 15:28
     * 确定点击的课程,初始化起始坐标
     */
    public ShowClass get_Pointed(int w, int t) {

        if (moving)
            return null;
        one = null;
        if (Has[t * 7 + w] != null) {
            one = Has[t * 7 + w];
            SL = one.week * OneW;
            ST = one.time1 * OneH;
            SR = (one.week + 1) * OneW;
            SB = (one.time2 + 1) * OneH;
            return one;
        }

        return null;
    }

    /**
     * @author 20535
     * @time 2021/3/11 8:37
     * 获取点开的课程
     */
    public ShowClass get_One() {
        return one;
    }

    /**
     * @author 20535
     * @time 2021/3/10 14:04
     * 点开某课程的过程
     */
    public void Open_class() {
        if(opening||(moving&&!pressing))
            return;
        if (debug)
            Log.d("Open_class", "start" + "\n");
        if (moving && pressing) {//打断点击
            press_flag = false;
        }

        if (one != null) {
            pressing = false;
            one.onchick = true;
            moving = true;//移动状态
            opening = true;
            open_flag = true;
            blur_first = true;
        } else
            return;


        ////////////////////
        ML = EL - SL;
        MT = ET - ST;
        MR = ER - SR;
        MB = EB - SB;

        Move = new float[]{0, 0, 0, 0};
        speed = new float[]{ML / C_speed, MT / C_speed, MR / C_speed, MB / C_speed};

        // 第一步：初始化Observable
        Observable.create((ObservableOnSubscribe<Integer>) e -> {

            for (C_times = C_speed; C_times >= 0; C_times--) {

                if (!open_flag) {
                    Throwable Terro = new Throwable("打开打断");
                    e.onError(Terro);
                    break;
                } else {
                    moving = true;
                    if (5>C_times) {
                        for (int n = 0; n < 4; n++) {
                            Move[n] += speed[n];
                        }
                    }else{
                        for (int n = 0; n < 4; n++) {
                            Move[n] += 2*speed[n];
                        }
                        C_times--;
                    }
                    sleep((C_speed - C_times));
                    e.onNext(C_times);

                }

            }


            e.onComplete();
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() { // 第三步：订阅

                    // 第二步：初始化Observer
                    private Disposable mDisposable;

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(@NonNull Integer Temp_times) {

                        if (Temp_times < 0) {
                            mDisposable.dispose();
                        }
                        invalidate();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        moving = false;
                        if (debug)
                            Log.e("open", "onError : value : " + e.getMessage() + "\n");

                    }

                    @Override
                    public void onComplete() {
                        mDisposable.dispose();
                        moving = false;
                        if (debug)
                            Log.d("open", "onComplete" + "\n");
                    }
                });

    }


    /**
     * @author 20535
     * @time 2021/3/17 9:36
     * 课程关闭，可打断打开过程
     */
    public void Close_class() {
        if (!opening)
            return;
        if (open_flag)//打断
            open_flag = false;
        moving = true;
        if (debug)
            Log.d("Close_class", "start" + "\n");

        // 第一步：初始化Observable
        Observable.create((ObservableOnSubscribe<Integer>) e -> {

            for (; C_times <= C_speed; C_times++) {
                for (int n = 0; n < 4; n++) {
                    Move[n] -= speed[n];
                }
                moving = true;

                sleep((C_times) / 10);
                e.onNext(C_times);
            }

            e.onComplete();
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() { // 第三步：订阅

                    // 第二步：初始化Observer
                    private Disposable mDisposable;

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(@NonNull Integer f) {
                        invalidate();
                        if (f > C_speed) {
                            mDisposable.dispose();
                        }

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (debug)
                            Log.e("close", "onError : value : " + e.getMessage() + "\n");
                        moving = false;
                    }

                    @Override
                    public void onComplete() {
                        moving = false;
                        opening = false;
                        if (one != null)
                            one.onchick = false;
                        one = null;
                        opening = false;
                        
                        invalidate();

                        mDisposable.dispose();
                        //setLayerType(View.LAYER_TYPE_NONE, null);
                        if (debug)
                            Log.d("close", "onComplete" + "\n");
                    }
                });

    }

    /**
     * @author 20535
     * @time 2021/3/16 10:36
     * 模拟按压变小
     */
    public void Press_classs() {
        if (moving ||opening||one==null)
            return;

        press_flag = true;

        pressing = true;
        one.onchick = true;
        Move = new float[]{0, 0, 0, 0};
        if (debug)
            Log.d("Press_classs", "start" + "\n");
        Observable.create(new ObservableOnSubscribe<Integer>() { // 第一步：初始化Observable
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                for (int i = 1; i < 8; i++) {
                    if (!press_flag) {
                        e.onError(new Throwable("按压打断"));
                    } else{
                        moving = true;
                        Move[0] = i;
                    }

                    e.onNext(i);
                    sleep(8);
                }
                e.onComplete();

            }
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() { // 第三步：订阅

                    // 第二步：初始化Observer
                    private Disposable mDisposable;

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(@NonNull Integer i) {
                        invalidate();
                        if (i > 8) {
                            mDisposable.dispose();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        moving = false;
                        pressing = false;
                        if (debug)
                            Log.e("Press_classs", "onError : value : " + e.getMessage() + "\n");
                        mDisposable.dispose();
                    }

                    @Override
                    public void onComplete() {
                        mDisposable.dispose();
                        moving = false;
                        if (debug)
                            Log.d("Press_classs", "onComplete" + "\n");
                    }
                });
    }

    /**
     * @author 20535
     * @time 2021/3/16 10:39
     * 松开课程回弹
     */
    public void Release_class() {

        if (one == null || !pressing|| moving||opening)
            return;
        press_flag = false;

        moving = true;
        if (debug)
            Log.d("Release_class", "start" + "\n");
        Observable.create(new ObservableOnSubscribe<Integer>() { // 第一步：初始化Observable
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                for (int i = 8; i > 0; i--) {
                    moving = true;
                    Move[0] = i;
                    e.onNext(i);
                    sleep(12);
                }

                e.onComplete();
            }
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() { // 第三步：订阅

                    // 第二步：初始化Observer
                    private Disposable mDisposable;

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(@NonNull Integer i) {
                        invalidate();
                        if (i < 1) {
                            mDisposable.dispose();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        if (debug)
                            Log.d("Release_class", "onError : value : " + e.getMessage() + "\n");
                        moving = false;
                        pressing = false;
                    }

                    @Override
                    public void onComplete() {
                        moving = false;
                        pressing = false;
                        if (one != null)
                            one.onchick = false;
                        one = null;
                        invalidate();
                        if (debug)
                            Log.d("Release_class", "onComplete" + "\n");
                        mDisposable.dispose();
                    }
                });

    }


    /**
     * @author 20535
     * @time 2021/3/16 10:12
     * 添加待显示课程
     */
    public void addClass(int week, int t1, int t2, String name, String teacher, String room, int tempcolor, boolean open) throws ArrayIndexOutOfBoundsException {

        ShowClass Addclass = new ShowClass();
        Addclass.week = week;
        Addclass.time1 = t1;
        Addclass.time2 = t2;
        Addclass.name = name;
        Addclass.teacher = teacher;
        Addclass.room = room;
        Addclass.onchick = false;
        if (night) {
            Addclass.color=Change_color(tempcolor);
        } else
            Addclass.color = (tempcolor);

        int count = 0;
        Pattern p = Pattern.compile("\\d");
        Matcher m = p.matcher(name);
        while (m.find()) {
            count++;
        }
        if ((name.length() - count / 2) > 8)
            Addclass.n_name = new StaticLayout(name.substring(0, 8 + count / 2), N_text_paint, OneW - 2, Layout.Alignment.ALIGN_CENTER, 1.0F, 0.0F, true);
        else
            Addclass.n_name = new StaticLayout(name, N_text_paint, OneW - 2, Layout.Alignment.ALIGN_CENTER, 1.0F, 0.0F, true);
        Addclass.n_room = new StaticLayout(room, N_text_paint, OneW - 2, Layout.Alignment.ALIGN_CENTER, 1.0F, 0.0F, true);

        Addclass.p_name = new StaticLayout(name, P_text_paint, Width / 2, Layout.Alignment.ALIGN_NORMAL, 1.2F, 0.0F, true);
        Addclass.p_teacher = new StaticLayout(teacher, P_text_paint, Width / 2, Layout.Alignment.ALIGN_NORMAL, 1.2F, 0.0F, true);
        Addclass.p_room = new StaticLayout(room, P_text_paint, Width / 2, Layout.Alignment.ALIGN_NORMAL, 1.2F, 0.0F, true);

        Cnum.add(t1 * 7 + week);
        if (open) {
            Addclass.onchick = true;
            opening = true;
        }
        for (int i = t1; i <= t2; i++) {
            Has[i * 7 + week] = Addclass;
        }

    }


    public void Quick_Close_it() {
        if (debug)
            Log.d("VIEW", "关闭所有已打开课程");

        if (one != null) {
            one.onchick = false;
            one = null;
            opening=false;
            open_flag=false;
            pressing=false;
            press_flag=false;
        }

        invalidate();
    }

    public void edit_it() {
        edit = true;
        invalidate();
    }

    public void edit_over() {
        edit = false;
    }

    public boolean Should_Close_it(float x, float y) {
        return !(EL-5 < x) || !(x < ER+5) || !(ET+5 < y) || !(y < EB-5);
    }

    public boolean Should_Edit_it(float x, float y) {
        return (Width * 7) / 15 < x && x < (Width * 8) / 15 && (Height) * 20 / 31 < y && y < (Height * 22) / 31;
    }

    //public boolean Should_Go(float x, float y) {
    //    return (one.week * OneW + SL  + daohang.getWidth()*2) < x && x < (one.week * OneW + SL  + daohang.getWidth()*3) && (Height) * 20 / 31 < y && y < (Height * 22) / 31;
    //}
    //public boolean Should_Note(float x, float y) {
    //    return ((one.week + 1) * OneW + SR - beiwang.getWidth()*3) < x && x < ((one.week + 1) * OneW + SR - beiwang.getWidth()*2) && (Height) * 20 / 31 < y && y < (Height * 22) / 31;
    //}
    public boolean Is_Open() {
        return opening;
    }
    public boolean Is_Move() {
        return moving;
    }



    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // canvas.drawLine(0, OneH * Mclass, Width, OneH * Mclass, paint);//中午

        blur_canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        for (int i = 0; i < Cnum.size(); i++) {
            ShowClass a = Has[Cnum.get(i)];

            if (a.onchick){
                continue;
            }


            if (!opening) {
                Main_Paint.setColor(a.color);
                canvas.drawRoundRect(a.week * OneW, a.time1 * OneH, (a.week + 1) * OneW, (a.time2 + 1) * OneH, 15, 15, Main_Paint);
                canvas.translate(a.week * OneW + 1, (a.time1) * OneH);
                a.n_name.draw(canvas);
                canvas.translate(0, (OneH * (7 + a.time2 - a.time1) / 11));
                a.n_room.draw(canvas);
                canvas.translate(0, -(OneH * (7 + a.time2 - a.time1) / 11));
                canvas.translate(-(a.week * OneW + 1), -(a.time1) * OneH);
            } else if (blur_first) {

                Main_Paint.setColor(a.color);
                blur_canvas.drawRoundRect(a.week * OneW, a.time1 * OneH, (a.week + 1) * OneW, (a.time2 + 1) * OneH, 15, 15, Main_Paint);
                blur_canvas.translate(a.week * OneW + 1, (a.time1) * OneH);
                a.n_name.draw(blur_canvas);
                blur_canvas.translate(0, (OneH * (7 + a.time2 - a.time1) / 11));
                a.n_room.draw(blur_canvas);
                blur_canvas.translate(0, -(OneH * (7 + a.time2 - a.time1) / 11));
                blur_canvas.translate(-(a.week * OneW + 1), -(a.time1) * OneH);

            }


        }

        //mBitmapToBlur.eraseColor(((ColorDrawable) mBlurredView.getBackground()).getColor());


        ////////////////

        if (one == null) {
            return;
        }

        if (pressing) {
            lx = SL + Move[0];
            ty = ST + Move[0];
            rx = SR - Move[0];
            by = SB - Move[0];

            Main_Paint.setColor(one.color);

            canvas.drawRoundRect(lx, ty, rx, by, 8 + Move[0], 8 + Move[0], Main_Paint);
            N_text_paint.setTextSize(N_text_size - Move[0] / 2);

            canvas.translate(one.week * OneW + Move[0] / 2, (one.time1) * OneH + Move[0] / 2);
            one.n_name.draw(canvas);
            canvas.translate(0, (OneH * (7 + one.time2 - one.time1) / 11f - Move[0]));
            one.n_room.draw(canvas);
            canvas.translate(0, -(OneH * (7 + one.time2 - one.time1) / 11f - Move[0]));
            canvas.translate(-(one.week * OneW + Move[0] / 2), -(one.time1) * OneH - Move[0] / 2);

            N_text_paint.setTextSize(N_text_size);
        } else if (opening) {

            blur();

            canvas.drawBitmap(mBlurredBitmap, 0, 0, null);

            lx = SL + Move[0];
            ty = ST + Move[1];
            rx = SR + Move[2];
            by = SB + Move[3];

            int db = OneH / 6;
            Main_Paint.setColor(one.color);
            canvas.drawRoundRect(lx, ty, rx, by, 8, 8, Main_Paint);
            Main_Paint.setColor(Color.argb((int) (55+1000/(C_times+6)), 255, 255, 255));
            canvas.drawRoundRect(lx, ty, rx, by - 2 * db, 8, 8, Main_Paint);
            Main_Paint.setColor(one.color);


            //Opaint.setColor(one.color);
            //setLayerType(View.LAYER_TYPE_HARDWARE, null);

            float dx = lx + db * 0.7f;

            if (C_times <= (C_speed / 2)) {
                canvas.translate(dx, ty + OneH / 5f);
                one.p_name.draw(canvas);
                canvas.translate(0, (by - ty) * 0.3f);
                one.p_teacher.draw(canvas);
                canvas.translate(0, (by - ty) * 0.3f);
                one.p_room.draw(canvas);
                canvas.translate(0, -(by - ty) * 0.6f);
                canvas.translate(-dx, -(ty + OneH / 5f));
                edit_button.setTint(Button_color(one.color));
                edit_button.setBounds((int)((lx+rx)/2-db),(int)(by-2*db),(int)((lx+rx)/2+db),(int)(by));
                edit_button.draw(canvas);
            } else {
                canvas.translate(one.week * OneW + 1 + Move[0], (one.time1) * OneH + Move[1]);
                one.n_name.draw(canvas);
                canvas.translate(0, (OneH * (7 + one.time2 - one.time1) / 11f));
                one.n_room.draw(canvas);
                canvas.translate(0, -(OneH * (7 + one.time2 - one.time1) / 11f));
                canvas.translate(-(one.week * OneW + 1 + Move[0]), -(one.time1) * OneH + Move[1]);
            }


            //int www = 1 - (Width / 1000);
            //canvas.drawBitmap(daohang, (lx  + daohang.getWidth()*2-30*(www)), (tempt2 + 1) * OneH + SD - Width / 7-6, paint);
            //canvas.drawBitmap(beiwang, (rx  - beiwang.getWidth()*3+30*(www)), (tempt2 + 1) * OneH + SD - Width / 7-6, paint);
            //if(!edit){
            //canvas.drawBitmap(editbp, (rx+lx  - editbp.getWidth())/2, (tempt2 + 1) * OneH + SD - Width / 7-6-4*www, paint);
            //}


        }
    }

    private void init_blur() {
        if (mBitmapToBlur == null) {
            mRenderScript = RenderScript.create(context);
            mBlurScript = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
            mBlurScript.setRadius(12);

            mBitmapToBlur = Bitmap.createBitmap(Width, Height,
                    Bitmap.Config.ARGB_8888);

            mBlurredBitmap = Bitmap.createBitmap(Width, Height,
                    Bitmap.Config.ARGB_8888);

            mBlurInput = Allocation.createFromBitmap(mRenderScript, mBitmapToBlur,
                    Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
            mBlurOutput = Allocation.createTyped(mRenderScript, mBlurInput.getType());
            blur_canvas = new Canvas(mBitmapToBlur);
        }
    }

    protected void blur() {
        if (blur_first) {

            mBlurInput.copyFrom(mBitmapToBlur);
            mBlurScript.setInput(mBlurInput);
            mBlurScript.forEach(mBlurOutput);
            mBlurOutput.copyTo(mBlurredBitmap);
            blur_first = false;
        }

    }

    private int Change_color(int color){
        int r = 0xFF & color;
        int g = 0xFF00 & color;
        int b = 0xFF0000 & color;
        g >>= 8;
        b >>= 16;
        r /=2;
        g /=2;
        b /=2;
        return Color.argb(255, r, g, b);
    }
    private int Button_color(int color){
        int r = 0xFF & color;
        int g = 0xFF00 & color;
        int b = 0xFF0000 & color;
        g >>= 8;
        b >>= 16;
        if(r+g+b<400)
            return Color.WHITE;
        else
            return Color.BLACK;

    }

}
