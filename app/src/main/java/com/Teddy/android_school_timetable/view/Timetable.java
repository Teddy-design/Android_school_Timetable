package com.Teddy.android_school_timetable.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Looper;
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
import io.reactivex.rxjava3.schedulers.Schedulers;

import static android.os.SystemClock.sleep;



public class Timetable extends View {
    public ArrayList<Integer> Cnum;
    Context context;
    public ShowClass[] Has;


    private int Height;//高
    private int Width;//宽
    private int Mclass;//上午课
    private int Aclass;//下午课
    public int OneH;//一个高
    public int OneW;//一个宽

    private int CH;//点击时绘图变化
    public boolean chicking;//点击开始
    public boolean opening;//打开开始
    public boolean moving;
    private boolean edit;

    private boolean bur_first;//只模糊一次

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

    //动画更新次数,越大越慢
    private final int C_speed = 180;

    //移动距离
    private float[] Move;
    //剩余更新次数
    int C_times;
    //单次4边速度
    float[] speed;

    private int tempt1;
    private int tempt2;


    int back_color;

    private int N_text_color;//normal文字颜色

    private int P_text_color;//pointed文字颜色

    private int N_text_size;//normal文字大小

    private int P_text_size;//pointed文字大小

    private TextPaint N_text_paint;//normal

    private TextPaint P_text_paint;

    Bitmap editbp;

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
     *
     * @author 20535
     * @time 2021/3/9 11:41
     * 初始化变量
     */
    private void init(Context context) {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        night= currentNightMode == Configuration.UI_MODE_NIGHT_YES;
        this.context=context;
        setCnum(4, 4);
        setLayerType(LAYER_TYPE_HARDWARE,null);

        opening=false;
        moving = false;



        /////////////////////////////////////////////
        Drawable drawable = ContextCompat.getDrawable(context, R.mipmap.rr);
        editbp = Bitmap.createBitmap(1080, 1920,
                Bitmap.Config.ARGB_8888);
        Canvas bp = new Canvas(editbp);
        drawable.setBounds(0, 0, bp.getWidth(), bp.getHeight());
        drawable.draw(bp);

        //setCnum(4, 4);

    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Height = getMeasuredHeight();
        Width = getMeasuredWidth();
        try {
            OneH = Height / (Aclass + Mclass);
            OneW = Width / 7;
        }catch (Exception e){
            Toast.makeText(context,"课程数量设置异常",Toast.LENGTH_LONG).show();
        }

        init_Paint();
        init_N_class();
        //invalidate();

    }

    public void flash(){
        invalidate();
    }

    /**
     *
     * @author 20535
     * @time 2021/3/9 15:27
     * 初始化normal课程大小
     */
    private void init_N_class(){
        SharedPreferences preferences = context.getSharedPreferences("class_num", Context.MODE_PRIVATE);

        Mclass=preferences.getInt("morning", 2);
        Aclass=preferences.getInt("afternoon", 3);

        Has = new ShowClass[(Mclass + Aclass) * 7];
        Cnum = new ArrayList<>();
        OneH = Height / (Aclass + Mclass);
        OneW = Width / 7;

        EL = (float) Width / 5;
        ER = (float) (Width * 0.8);
        ET = (float) (Height *0.25);
        EB = (float) (Height *0.75);
    }


    /**
     *
     * @author 20535
     * @time 2021/3/9 11:39
     * 初始化Paint
     */
    private void init_Paint(){
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

        auto_set_textsize();
    }

    /**
     *
     * @author 20535
     * @time 2021/3/9 11:18
     * 根据屏幕宽度选字的大小
     */
    private void auto_set_textsize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(displayMetrics);//获取屏幕参数
        N_text_size=displayMetrics.widthPixels/27;
        P_text_size=displayMetrics.widthPixels/20;
        N_text_paint.setTextSize(N_text_size);
        P_text_paint.setTextSize(P_text_size);
        
    }


    /**
     *
     * @author 20535
     * @time 2021/3/9 11:11
     * color设置课表 文字的颜色
     *
     */
    public int set_N_TextColor(int color){
        N_text_color=color;
        N_text_paint.setColor(N_text_color);
        return color;
    }

    /**
     *
     * @author 20535
     * @time 2021/3/9 11:13
     * color设置点击 文字的颜色
     *
     */
    public int set_P_TextColor(int color){
        P_text_color=color;
        P_text_paint.setColor(P_text_color);
        return color;
    }

    /**
     *
     * @author 20535
     * @time 2021/3/9 11:42
     * 设置上午下午课程数量
     */
    public void setCnum(int M, int A) {
        if(Mclass==M&&Aclass==A)
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
     *
     * @author 20535
     * @time 2021/3/10 15:28
     * 确定点击的课程,初始化起始坐标
     */
    public boolean get_Pointed(int w, int t) {


        one=null;
        if (Has[t * 7 + w]!=null){
            one = Has[t * 7 + w];
            SL = one.week * OneW;
            ST = one.time1 * OneH;
            SR = (one.week + 1) * OneW;
            SB = (one.time2 + 1) * OneH;
            return true;
        }

        return false;
    }
    /**
     *
     * @author 20535
     * @time 2021/3/11 8:37
     * 获取点开的课程
     */
    public ShowClass get_One() {
        return one;
    }

    /**
     *
     * @author 20535
     * @time 2021/3/10 14:04
     * 点开某课程的过程
     */
    public void Open_class() {
        moving = true;//移动状态
        opening=true;

        ////////////////////
        ML=EL-SL;
        MT=ET-ST;
        MR=ER-SR;
        MB=EB-SB;

        float[] Dis={ML,MT,MR,MB};
        float temp= Dis[3];
        for(int i=0;i<3;i++){
            if(temp<Dis[i])
                temp=Dis[i];
        }
        C_times= C_speed;
        Move= new float[]{0, 0, 0, 0};
        speed= new float[]{ML / C_speed, MT / C_speed, MR / C_speed, MB / C_speed};

        // 第一步：初始化Observable
        Observable.create((ObservableOnSubscribe<Integer>) e -> {
            if(Move==null||speed==null||one==null){
                Throwable Terro=new Throwable();
                e.onError(Terro);
            }
            for (;C_times>=0;C_times--) {
                if(C_times>(C_speed/2)) {
                    for (int n = 0; n < 4; n++) {
                        Move[n] += 3 * speed[n];
                    }
                    C_times -= 2;
                }
                else{
                    for(int n=0; n<4;n++){
                        Move[n]+=speed[n];
                    }
                }

                e.onNext(C_times);
                sleep((C_speed-C_times)/130);
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
                            Quick_Close_it();
                        }

                        invalidate();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("open", "onError : value : " + e.getMessage() + "\n");
                        moving = false;
                    }

                    @Override
                    public void onComplete() {
                        mDisposable.dispose();
                        moving = false;
                        //opening=false;
                        invalidate();
                        Log.d("open", "onComplete" + "\n");
                    }
                });

    }

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
            int r = 0xFF & tempcolor;
            int g = 0xFF00 & tempcolor;
            g >>= 8;
            int b = 0xFF0000 & tempcolor;
            b >>= 16;
            r /= 2;
            g /= 2;
            b /= 2;
            Addclass.color = Color.argb(255, r, g, b);
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
        for(int i=t1;i<=t2;i++){
            Has[i * 7 + week]=Addclass;
        }



    }

    public void delClass(int w, int t) {//需要改
        ShowClass a = Has[t * 7 + w];
        for (int i = a.time1; i <= a.time2; i++)
            Has[i] = null;
        for (int i = 0; i < Cnum.size(); i++) {
            if (Cnum.get(i) == a.time1 * 7 + a.week)
                Cnum.remove(i);
        }
    }





    public void stop_chick() {
        CH = 1;

        if(one!=null)
            one.onchick = false;
        one=null;
        opening = false;
        moving = false;
    }

    public void Make_small() {
        if (moving)
            return;
        moving = true;
        Observable.create(new ObservableOnSubscribe<Integer>() { // 第一步：初始化Observable
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                for (int i = 1; i < 7; i++) {
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
                    public void onNext(@NonNull Integer integer) {
                        CH = integer;
                        invalidate();
                        if (CH > 6) {
                            mDisposable.dispose();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("small", "onError : value : " + e.getMessage() + "\n");
                        moving = false;
                    }

                    @Override
                    public void onComplete() {
                        mDisposable.dispose();
                        moving = false;
                        Log.d("small", "onComplete" + "\n");
                    }
                });
    }

    public void Make_nol() {

        if (one == null)
            return;
        moving = true;
        Observable.create(new ObservableOnSubscribe<Integer>() { // 第一步：初始化Observable
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                for (int i = 7; i > 0; i--) {
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
                    public void onNext(@NonNull Integer integer) {
                        CH = integer;
                        invalidate();
                        if (CH < 1) {
                            mDisposable.dispose();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("nol", "onError : value : " + e.getMessage() + "\n");
                        moving = false;
                    }

                    @Override
                    public void onComplete() {
                        stop_chick();
                        mDisposable.dispose();
                        moving = false;
                        invalidate();
                        Log.d("nol", "onComplete" + "\n");
                    }
                });

    }

    private float CC;
    private float CCC;



    public void Closei() {
        if (moving)
            return;
        moving = true;

        // 第一步：初始化Observable
        Observable.create((ObservableOnSubscribe<Float>) e -> {
            CC = 1;

            for (CCC = (float) 0.025; CC < 4; CC += CCC) {
                e.onNext(CC);
                if (CCC < 4)
                    CCC *= 1.3;
                sleep((int) (CC * 2) + 2);
            }
            e.onComplete();
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Float>() { // 第三步：订阅

                    // 第二步：初始化Observer
                    private Disposable mDisposable;

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(@NonNull Float f) {
                        SL = EL / f;
                        SR = ER / f;
                        ST = ET / f;
                        SB = EB / f;
                        invalidate();
                        if (f > 10) {
                            mDisposable.dispose();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("close", "onError : value : " + e.getMessage() + "\n");
                        moving = false;
                    }

                    @Override
                    public void onComplete() {
                        stop_chick();
                        mDisposable.dispose();
                        moving = false;
                        invalidate();
                        //setLayerType(View.LAYER_TYPE_NONE, null);
                        Log.d("close", "onComplete" + "\n"+opening);
                    }
                });

    }


    public void Open_it() {

        if (one!=null) {

            one.onchick=true;
            bur_first=true;
            Open_class();
        }
    }

    public void Close_it() {
        Closei();
    }
    public void Quick_Close_it() {
        Log.d("VIEW","关闭所有已打开课程");
        CH = 1;

        if(one!=null){
            one.onchick = false;
            one=null;
        }

        invalidate();
    }
    public void edit_it() {
        edit=true;
        invalidate();
    }
    public void edit_over() {
        edit=false;
    }

    public boolean Should_Close_it(float x, float y) {
        return !(Width / 6 < x) || !(x < (Width * 5) / 6) || !((Height) / 4 < y) || !(y < (Height * 3) / 4);
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




    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Height = getMeasuredHeight();
        auto_set_textsize();
    }


    private Bitmap mBitmapToBlur, mBlurredBitmap;

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
       // canvas.drawLine(0, OneH * Mclass, Width, OneH * Mclass, paint);//中午

        canvas.save();
        prepare();
        canvas.drawBitmap(editbp, 0,0,Main_Paint);
        if(opening&&bur_first)
            bur_canvas.drawBitmap(editbp, 0,0,Main_Paint);
        for (int i = 0; i < Cnum.size(); i++) {

            ShowClass a = Has[Cnum.get(i)];
            if (a.onchick)
                continue;
            if (!opening) {
                Main_Paint.setColor(a.color);
                canvas.drawRoundRect(a.week * OneW, a.time1 * OneH, (a.week + 1) * OneW, (a.time2 + 1) * OneH, 15, 15, Main_Paint);
                canvas.translate(a.week * OneW + 1, (a.time1) * OneH);
                a.n_name.draw(canvas);
                canvas.translate(0, (OneH * (7 + a.time2 - a.time1) / 11));
                a.n_room.draw(canvas);
                canvas.translate(0, -(OneH * (7 + a.time2 - a.time1) / 11));
                canvas.translate(-(a.week * OneW + 1), -(a.time1) * OneH);
            }
            ///////////////////
            if (opening&&bur_first) {
                Main_Paint.setColor(a.color);
                bur_canvas.drawRoundRect(a.week * OneW, a.time1 * OneH, (a.week + 1) * OneW, (a.time2 + 1) * OneH, 15, 15, Main_Paint);
                bur_canvas.translate(a.week * OneW + 1, (a.time1) * OneH);
                a.n_name.draw(bur_canvas);
                bur_canvas.translate(0, (OneH * (7 + a.time2 - a.time1) / 11));
                a.n_room.draw(bur_canvas);
                bur_canvas.translate(0, -(OneH * (7 + a.time2 - a.time1) / 11));
                bur_canvas.translate(-(a.week * OneW + 1), -(a.time1) * OneH);

            }


        }

        //mBitmapToBlur.eraseColor(((ColorDrawable) mBlurredView.getBackground()).getColor());



        ////////////////


        if (chicking) {
            Main_Paint.setColor(one.color);
            canvas.drawRoundRect(Math.max(one.week * OneW + CH, 0), Math.max(one.time1 * OneH + CH, 0), Math.min((one.week + 1) * OneW - CH, Width), Math.min((one.time2 + 1) * OneH - CH, Height), 15, 15, Main_Paint);
            float dx=Math.max(one.week * OneW, 0);
            float dy=Math.max(one.time1 * OneH + CH, 0);
            canvas.translate(dx, dy);
            N_text_paint.setTextSize(N_text_size-CH/2);
            one.n_name.draw(canvas);
            canvas.translate(0, (OneH * (7 + one.time2 - one.time1) / 11));
            one.n_room.draw(canvas);
            canvas.translate(0, -(OneH * (7 + one.time2 - one.time1) / 11));
            canvas.translate(-dx, -dy);
            N_text_paint.setTextSize(N_text_size);
        }
        if (opening) {
            canvas.restore();
            blur();

            canvas.drawBitmap(mBlurredBitmap, 0, 0, null);
            float lx=SL + Move[0];

            float ty=ST + Move[1];

            float rx=SR + Move[2];

            float by=SB + Move[3];
            int db=Width/18;
            //Opaint.setColor( o_text_color);
            //canvas.drawRoundRect(Math.min(lx + db, Width / 2)-2, Math.min(ty + db, Height / 2)-2, Math.max(rx - db, Width / 2)+2, Math.max(by - db, Height / 2)+2, 8, 8, Opaint);
            Main_Paint.setColor(one.color);
            canvas.drawRoundRect(lx , ty, rx, by , 8, 8, Main_Paint);
            Main_Paint.setColor(Color.WHITE);
            canvas.drawRoundRect(lx , ty, rx, by-2*db, 8, 8, Main_Paint);
            Main_Paint.setColor(one.color);

            //Opaint.setColor(one.color);
            //setLayerType(View.LAYER_TYPE_HARDWARE, null);

            float dx=lx+ db;

            if(C_times<(C_speed/2)){
                canvas.translate( dx, ty + Width / 11);
                one.p_name.draw(canvas);
                canvas.translate(0, (by-ty)*3/10);
                one.p_teacher.draw(canvas);
                canvas.translate(0, (by-ty)*3/10);
                one.p_room.draw(canvas);
                canvas.translate(0, -(by-ty)*3/5);
                canvas.translate(-dx, -(tempt1 * OneH + ST + Width / 11));
            }
            else{
                canvas.translate(one.week * OneW + 1 + Move[0], (one.time1) * OneH+Move[1]);
                one.n_name.draw(canvas);
                canvas.translate(0, (OneH * (7 + one.time2 - one.time1) / 11));
                one.n_room.draw(canvas);
                canvas.translate(0, -(OneH * (7 + one.time2 - one.time1) / 11));
                canvas.translate(-(one.week * OneW + 1 + Move[0]), -(one.time1) * OneH+Move[1]);
            }




            int www=1- (Width/1000);
            //canvas.drawBitmap(daohang, (lx  + daohang.getWidth()*2-30*(www)), (tempt2 + 1) * OneH + SD - Width / 7-6, paint);
            //canvas.drawBitmap(beiwang, (rx  - beiwang.getWidth()*3+30*(www)), (tempt2 + 1) * OneH + SD - Width / 7-6, paint);
            //if(!edit){
                //canvas.drawBitmap(editbp, (rx+lx  - editbp.getWidth())/2, (tempt2 + 1) * OneH + SD - Width / 7-6-4*www, paint);
            //}


        }
    }

    private void prepare(){
        if(mBitmapToBlur==null){
            mRenderScript = RenderScript.create(context);
            mBlurScript = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
            mBlurScript.setRadius(8);

            mBitmapToBlur = Bitmap.createBitmap(Width, Height,
                    Bitmap.Config.ARGB_8888);


            mBlurredBitmap = Bitmap.createBitmap(Width, Height,
                    Bitmap.Config.ARGB_8888);


            mBlurInput = Allocation.createFromBitmap(mRenderScript, mBitmapToBlur,
                    Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
            mBlurOutput = Allocation.createTyped(mRenderScript, mBlurInput.getType());
            bur_canvas = new Canvas(mBitmapToBlur);
        }
    }

    protected void blur() {
        if(bur_first) {
            mBlurInput.copyFrom(mBitmapToBlur);
            mBlurScript.setInput(mBlurInput);
            mBlurScript.forEach(mBlurOutput);
            mBlurOutput.copyTo(mBlurredBitmap);
            bur_first=!bur_first;
        }

    }


    private RenderScript mRenderScript;
    private ScriptIntrinsicBlur mBlurScript;
    private Allocation mBlurInput, mBlurOutput;
    private Canvas bur_canvas;

}
