package hackthis.everythingthis;

import android.content.Context;
import android.content.SharedPreferences;
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

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    //false = schedule, true = announcement
    private boolean pageMode = false;
    private boolean pageModeHistory = false;

    // schedule page vars
    private int screenWidth, screenHeight;
    private ScheduleRefresher scheduleRefresher = null;
    private boolean scheduleRefresherRunning = true;
    private ScheduleBlock sb;

    //main frame vars
    private LinearLayout body;
    private ImageView scheduleIcon, announcementIcon;

    //announcement page vars
    private AnnouncementBlock ab;
    private AnnouncementRefresher announcementRefresher = null;
    private boolean announcementRefresherRunning = false;

    //data related vars
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        preferences = this.getPreferences(Context.MODE_PRIVATE);
        editor = preferences.edit();

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

/**
 *
 * Stuff to change to adapt to database
 * Line 47 mainly
 * the array called nextAnnouncements
 * Line 206
 *
 * just use control and f and search for database
 * most stuff to implement will be in those locations
 *
 * also right now the announcements are moving up by size%8 (which is two) which i have no clue how to fix without adding ghost announcements
 *
 * I was thinking for the filter screen we might as well use a spinner (dropdown) because it's more versatile
 * the spinner is already there we just need to set it to visible (R.id.filterDropdown)
 *
 * Todo:
 * make the next and prev page buttons turn gray or something when announcements run out (like if(announcementsToLoad=0) changeColorToGray)
 * add functions to set the title of an announcement
 * add a function to implement the time function (current time minus that time or whatever)
 * are we gonna do search? (we can just linear search all the announcements or something and show only the ones that have the thing the user's searching for)
 */

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

        //initialize icon buttons
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

        sb = new ScheduleBlock(getApplication(), screenHeight, screenWidth, editor);
        ab = new AnnouncementBlock(getApplicationContext(), new LinearLayout.LayoutParams(screenWidth, (int)(0.85*screenHeight)), editor);

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
                body.addView(sb);
                scheduleIcon.setImageResource(R.drawable.schedule_selected);
                announcementIcon.setImageResource(R.drawable.announcement);
                //setContentView(R.layout.activity_main);
                //getSupportActionBar().hide();
                startScheduleRefresher();
            } else {
                body.addView(ab);
                scheduleIcon.setImageResource(R.drawable.schedule);
                announcementIcon.setImageResource(R.drawable.announcement_selected);
                startAnnouncementRefresher();
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

    public void startAnnouncementRefresher() {
        stopAnnouncementRefresher();
        announcementRefresherRunning = true;
        announcementRefresher = (AnnouncementRefresher) new AnnouncementRefresher().execute();
    }

    public void stopAnnouncementRefresher() {
        if (announcementRefresher != null) {
            announcementRefresherRunning = false;
            announcementRefresher.cancel(true);
            announcementRefresher = null;
        }
    }

    public class AnnouncementRefresher extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            while (announcementRefresherRunning) {

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
            ab.loadAnnouncements();
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
