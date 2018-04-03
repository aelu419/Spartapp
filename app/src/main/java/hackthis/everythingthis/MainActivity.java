package hackthis.everythingthis;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    //false = schedule, true = announcement
    private boolean pageMode = false;
    private boolean pageModeHistory = false;

    // schedule page vars
    private int screenWidth, screenHeight;
    private Date currentTime = new Date();
    private ScheduleRefresher scheduleRefresher = null;
    private boolean scheduleRefresherRunning = true;
    private ScheduleBlock sb;
    private LinearLayout body;
    private ImageView scheduleIcon, announcementIcon;

    //announcement page vars




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        //get access to internet
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());

        //initialize leancloud
        // 初始化参数依次为 this, AppId, AppKey
        AVOSCloud.initialize(this,"cRxhzMwEQJ07JfRuztRYFJ5n-gzGzoHsz","kIvYOVL1hGnkS3n1kh76P8NC");
        //使用国内节点
        AVOSCloud.useAVCloudCN();
        // 放在 SDK 初始化语句 AVOSCloud.initialize() 后面，只需要调用一次即可
        AVOSCloud.setDebugLogEnabled(true);

        pageMode = false;
        pageModeHistory = false;

        /*// 测试 SDK 是否正常工作的代码
        已测试，见数据库中androidTestObject项
        AVObject testObject = new AVObject("androidTestObject");
        testObject.put("words", "androidTestObjectMSG");
        testObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if(e == null){
                    Log.d("saved","success!");
                }
            }
        });*/
    }

    @Override
    public void onStart(){

        super.onStart();

        //getScreenDimensions
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        //initialize body
        body = findViewById(R.id.body);
        body.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, (int)(0.85*screenHeight)));

        //initialize footer
        LinearLayout footer = findViewById(R.id.footer);
        footer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)(0.075*screenHeight)));
        footer.setPadding(0,(int)(0.01*screenHeight),0,(int)(0.01*screenHeight));

        //initialize icon button
        scheduleIcon = findViewById(R.id.scheduleIcon);
        LinearLayout.LayoutParams footerButtonParams = new LinearLayout.LayoutParams((int)(0.075*screenHeight),ViewGroup.LayoutParams.MATCH_PARENT);
        footerButtonParams.setMargins((int)(0.04*screenHeight),0,(int)(0.04*screenHeight),0);
        scheduleIcon.setLayoutParams(footerButtonParams);
        scheduleIcon.setPadding((int)(0.005*screenHeight),(int)(0.005*screenHeight),(int)(0.005*screenHeight),(int)(0.005*screenHeight));


        announcementIcon = findViewById(R.id.announcementIcon);
        announcementIcon.setLayoutParams(footerButtonParams);
        announcementIcon.setPadding((int)(0.005*screenHeight),(int)(0.005*screenHeight),(int)(0.005*screenHeight),(int)(0.005*screenHeight));


        scheduleIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageMode = false;
                updatePageMode();
            }
        });

        announcementIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageMode = true;
                updatePageMode();
            }
        });

        pageMode = false;
        pageModeHistory = true;

        updatePageMode();

    }

    public void updatePageMode() {
        if(pageModeHistory != pageMode) {
            if(scheduleRefresherRunning){
                stopScheduleRefresher();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    body.removeAllViews();
                }
            });

            if (!pageMode) {
                scheduleIcon.setImageResource(R.drawable.schedule_selected);
                announcementIcon.setImageResource(R.drawable.announcement);
                startScheduleRefresher();
            } else {
                scheduleIcon.setImageResource(R.drawable.schedule);
                announcementIcon.setImageResource(R.drawable.announcement_selected);
                //Todo: call announcement initialization method
            }
            pageModeHistory = pageMode;
        }
    }

    public void startScheduleRefresher() {
        stopScheduleRefresher();
        scheduleRefresherRunning = true;
        scheduleRefresher = (ScheduleRefresher) new ScheduleRefresher().execute();
    }

    public void stopScheduleRefresher() {
        if (scheduleRefresher != null) {
            scheduleRefresherRunning = false;
            scheduleRefresher.cancel(true);
            scheduleRefresher = null;
        }
    }

    public class ScheduleRefresher extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sb = new ScheduleBlock(getApplicationContext(), screenHeight, screenWidth);
                    body.addView(sb);
                }
            });

            while (scheduleRefresherRunning) {

                publishProgress();
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Log.i("Demo","progress update");
            sb.onSelection();
            super.onProgressUpdate(values);
        }

    }

    //tries to get time from internet, if fails then set time as system time
    private Date getTime() {
        Date date = new Date();
        String URL1 = "http://www.baidu.com";
        String URL2 = "http://www.ntsc.ac.cn";
        String URL3 = "http://www.beijing-time.org";
        Log.i("Demo","current time source :");
        try{
            if (getWebDate(URL1) != null) {
                Log.i("Demo",URL1);
                date = getWebDate(URL1);
            } else if (getWebDate(URL2) != null) {
                Log.i("Demo",URL2);
                date = getWebDate(URL2);
            } else if (getWebDate(URL3) != null) {
                Log.i("Demo",URL3);
                date = getWebDate(URL3);
            }
            else {
                Log.i("Demo", "System");
            }
        }
        catch(Exception e){

        }
        return date;
    }

    //get time from internet with given url
    private Date getWebDate(String url) {
        Date temp;
        Log.i("Demo","getting time from " + url);
        URL u;
        try {
            u = new URL(url);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
        try{
            HttpURLConnection connecter = (HttpURLConnection)u.openConnection();
            connecter.connect();
            temp = new Date(connecter.getDate());
            connecter.disconnect();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
        Log.i("Demo","got time from " + url);
        return temp;
    }

    private int getPixelsFromDp(int size){

        DisplayMetrics metrics =new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return(size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;

    }

    private int getDPFromPixel(int size){
        DisplayMetrics metrics =new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return(size * DisplayMetrics.DENSITY_DEFAULT) / metrics.densityDpi;
    }

}
