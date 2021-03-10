package com.Teddy.android_school_timetable.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
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
    private ShowClass one;//被点击的
    //展开的移动坐标
    private float SL;
    private float SR;
    private float ST;
    private float SD;
    private float EL;
    private float ER;
    private float ET;
    private float ED;
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
        chicking = false;
        moving = false;
        init_Text();
        init_N_class();
        setCnum(4, 4);
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

        preferences.getInt("morning", false)

        OneH = Height / (Aclass + Mclass);
        OneW = Width / 7;
    }


    /**
     *
     * @author 20535
     * @time 2021/3/9 11:39
     * 初始化文字属性
     */
    private void init_Text(){
        P_text_paint = new TextPaint();
        P_text_paint.setAntiAlias(true);
        P_text_paint.setSubpixelText(true);
        P_text_paint.setStyle(Paint.Style.FILL);

        N_text_paint = new TextPaint();
        N_text_paint.setAntiAlias(true);
        N_text_paint.setSubpixelText(true);
        N_text_paint.setStyle(Paint.Style.FILL);

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
        P_text_size=displayMetrics.widthPixels/26;
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
     * @time 2021/3/10 14:04
     * 点开某课程
     */
    public void Openi() {
        moving = true;
        // 第一步：初始化Observable
        Observable.create((ObservableOnSubscribe<Float>) e -> {
            CC = 4;
            CCC = 2.98f;
            for (; CC > 1.0; CC -= CCC) {
                e.onNext(CC);
                if (CCC > 0.014)
                    CCC = (float) (CCC /2);
                sleep(14 - (int) (CC*1.42));
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
                        SD = ED / f;
                        invalidate();
                        if (f < 1) {
                            mDisposable.dispose();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("open", "onError : value : " + e.getMessage() + "\n");
                        moving = false;
                    }

                    @Override
                    public void onComplete() {
                        mDisposable.dispose();
                        moving = false;
                        invalidate();
                        Log.d("open", "onComplete" + "\n");
                        EL = SL  ;
                        ER = SR  ;
                        ET = ST  ;
                        ED = SD  ;
                    }
                });

    }

    public void addClass(int week, int t1, int t2, String name, String teacher, String room, String color,boolean open)throws ArrayIndexOutOfBoundsException {

        post(new Runnable() {
            @Override
            public void run() {
                ShowClass Addclass = new ShowClass();
                Addclass.week = week;
                Addclass.time1 = t1;
                Addclass.time2 = t2;
                Addclass.name = name;
                Addclass.room = room;
                Addclass.onchick = false;
                if(night){
                    int tempcolor=Color.parseColor(color);
                    int r = 0xFF & tempcolor;
                    int g = 0xFF00 & tempcolor;
                    g >>= 8;
                    int b = 0xFF0000 & tempcolor;
                    b >>= 16;
                    r/=2;
                    g/=2;
                    b/=2;
                    Addclass.color =Color.argb(255,r,g,b);
                }else
                    Addclass.color =Color.parseColor(color);

                int count = 0;
                Pattern p = Pattern.compile("\\d");
                Matcher m = p.matcher(name);
                while(m.find()){
                    count++;
                }
                if((name.length()-count/2)>8)
                    Addclass.n_name = new StaticLayout(name.substring(0,8+count/2), N_text_paint, OneW - 2, Layout.Alignment.ALIGN_CENTER, 1.0F, 0.0F, true);
                else
                    Addclass.n_name = new StaticLayout(name, N_text_paint, OneW - 2, Layout.Alignment.ALIGN_CENTER, 1.0F, 0.0F, true);
                Addclass.n_room = new StaticLayout(room, N_text_paint, OneW - 2, Layout.Alignment.ALIGN_CENTER, 1.0F, 0.0F, true);

                Addclass.p_name = new StaticLayout(name, P_text_paint, Width /2, Layout.Alignment.ALIGN_NORMAL, 1.2F, 0.0F, true);
                Addclass.p_teacher = new StaticLayout(teacher, P_text_paint, Width /2, Layout.Alignment.ALIGN_NORMAL, 1.2F, 0.0F, true);
                Addclass.p_room = new StaticLayout(room, P_text_paint, Width /2, Layout.Alignment.ALIGN_NORMAL, 1.2F, 0.0F, true);

                Cnum.add(t1 * 7 + week);
                if(open){
                    Addclass.onchick=true;
                    opening=true;
                }
            }
        });

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

    public boolean clearClass() {
        Has = new ShowClass[(Mclass + Aclass) * 7];
        Cnum = new ArrayList<>();

        chicking=false;
        moving=false;

        return true;
    }
    public boolean is_clear() {
        return Cnum.size()==0;
    }

    public ShowClass get_Class(int w, int t) {
        one=Has[t * 7 + w];
        if(one==null)
            return null;
        EL = Width / 6 - one.week * OneW;
        ER = (Width * 5) / 6 - (one.week + 1) * OneW;
        ET = (Height) / 4 - one.time1 * OneH;
        ED = (Height *3) / 4 - (one.time2 + 1) * OneH;
        return one;
    }
    public ShowClass get_One() {
        return one;
    }

    public void init_chick() {
        if (!chicking) {
            CH = 1;
            chicking = true;
            one.onchick = true;
            opening = false;
            moving = false;
            Make_small();
        }
    }

    public void stop_chick() {
        CH = 1;
        chicking = false;
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

        if (one == null || !chicking || moving)
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
                        SD = ED / f;
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
        if (chicking && !opening) {
            chicking = false;
            opening = true;
            tempt1=one.time1;
            tempt2=one.time2;
            SL = SR = ST = SD = 0;
            Openi();
        }
    }

    public void Close_it() {
        Closei();
    }
    public void Quick_Close_it() {
        Log.d("VIEW","关闭所有已打开课程");
        CH = 1;
        chicking = false;
        if(one!=null){
            one.onchick = false;
            one=null;
        }
        opening = false;
        moving = false;
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
    public boolean Should_Change_it(float x, float y) {
        return (Width * 7) / 15 < x && x < (Width * 8) / 15 && (Height) * 20 / 31 < y && y < (Height * 22) / 31;
    }
    public boolean Should_Go(float x, float y) {
        return (one.week * OneW + SL  + daohang.getWidth()*2) < x && x < (one.week * OneW + SL  + daohang.getWidth()*3) && (Height) * 20 / 31 < y && y < (Height * 22) / 31;
    }
    public boolean Should_Note(float x, float y) {
        return ((one.week + 1) * OneW + SR - beiwang.getWidth()*3) < x && x < ((one.week + 1) * OneW + SR - beiwang.getWidth()*2) && (Height) * 20 / 31 < y && y < (Height * 22) / 31;
    }
    public boolean Is_Open() {
        return opening;
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
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Height = getMeasuredHeight();
        auto_set_textsize();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
       // canvas.drawLine(0, OneH * Mclass, Width, OneH * Mclass, paint);//中午

        for (int i = 0; i < Cnum.size(); i++) {
            ShowClass a = Has[Cnum.get(i)];
            if (a.onchick)
                continue;
            paint.setColor(a.color);
            canvas.drawRoundRect(a.week * OneW, a.time1 * OneH, (a.week + 1) * OneW, (a.time2 + 1) * OneH, 15, 15, paint);
            canvas.translate(a.week * OneW + 1, (a.time1) * OneH);
            a.Tname.draw(canvas);
            canvas.translate(0, (OneH * (7 + a.time2 - a.time1) / 11));
            a.Troom.draw(canvas);
            canvas.translate(0, -(OneH * (7 + a.time2 - a.time1) / 11));
            canvas.translate(-(a.week * OneW + 1), -(a.time1) * OneH);
        }
        if (chicking) {
            paint.setColor(one.color);
            canvas.drawRoundRect(Math.max(one.week * OneW + CH, 0), Math.max(one.time1 * OneH + CH, 0), Math.min((one.week + 1) * OneW - CH, Width), Math.min((one.time2 + 1) * OneH - CH, Height), 15, 15, paint);
            float dx=Math.max(one.week * OneW, 0);
            float dy=Math.max(one.time1 * OneH + CH, 0);
            canvas.translate(dx, dy);
            paintText.setTextSize(textsize-CH/2);
            one.Tname.draw(canvas);
            canvas.translate(0, (OneH * (7 + one.time2 - one.time1) / 11));
            one.Troom.draw(canvas);
            canvas.translate(0, -(OneH * (7 + one.time2 - one.time1) / 11));
            canvas.translate(-dx, -dy);
            paintText.setTextSize(textsize);
        }
        if (opening) {
            float lx=one.week * OneW + SL;
            float rx=(one.week + 1) * OneW + SR;
            float ty=tempt1 * OneH + ST;
            float by=(tempt2 + 1) * OneH + SD;
            int db=Width/18;
            //Opaint.setColor( o_text_color);
            //canvas.drawRoundRect(Math.min(lx + db, Width / 2)-2, Math.min(ty + db, Height / 2)-2, Math.max(rx - db, Width / 2)+2, Math.max(by - db, Height / 2)+2, 8, 8, Opaint);

            Opaint.setColor(back_color);
            canvas.drawRoundRect(lx + db, ty + db, rx - db, by - 2*db, 8, 8, Opaint);

            Opaint.setColor(one.color);
            canvas.drawRoundRect(lx + db, by - 3*db, rx - db, by - db, 8, 8, Opaint);
            //Opaint.setColor(one.color);
            //setLayerType(View.LAYER_TYPE_HARDWARE, null);
            paintText.setColor(o_text_color);
            paintText.setShadowLayer(0f, 0, 0, Color.GRAY);
            float dx=lx+ 2*db;

            canvas.translate( dx, tempt1 * OneH + ST + Width / 11);
            if(CC<1.2){
                one.Oname.draw(canvas);
                canvas.translate(0, (by-ty)/4);
                one.Oteacher.draw(canvas);
                canvas.translate(0, (by-ty)/4);
                one.Oroom.draw(canvas);
                canvas.translate(0, -(by-ty)/2);
            }
            else
                one.Tname.draw(canvas);

            canvas.translate(-dx, -(tempt1 * OneH + ST + Width / 11));

            paintText.setColor(Color.WHITE);
            paintText.setShadowLayer(2f, 1, 1, Color.GRAY);
            int www=1-(int)(Width/1000);
            canvas.drawBitmap(daohang, (lx  + daohang.getWidth()*2-30*(www)), (tempt2 + 1) * OneH + SD - Width / 7-6, paint);
            canvas.drawBitmap(beiwang, (rx  - beiwang.getWidth()*3+30*(www)), (tempt2 + 1) * OneH + SD - Width / 7-6, paint);
            if(!edit){
                canvas.drawBitmap(editbp, (rx+lx  - editbp.getWidth())/2, (tempt2 + 1) * OneH + SD - Width / 7-6-4*www, paint);
            }


        }
    }


}
