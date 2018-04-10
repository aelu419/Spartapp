package hackthis.everythingthis;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.avos.avoscloud.*;

public class MainActivity extends AppCompatActivity{

    //false = schedule, true = announcement
    private boolean pageMode = false;
    private boolean pageModeHistory = false;

    // schedule page vars
    private int screenWidth, screenHeight;
    private static ScheduleRefresher scheduleRefresher = null;
    private static boolean scheduleRefresherRunning = true;
    private static ScheduleBlock sb;

    //main frame vars
    private LinearLayout body;
    private ImageView scheduleIcon, announcementIcon;

    //announcement page vars
    private static AnnouncementBlock ab;
    private static AnnouncementRefresher announcementRefresher = null;
    private static boolean announcementRefresherRunning = false;

    //data related vars
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    public boolean firstRun;

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

        firstRun = preferences.getBoolean(getResources().getString(R.string.first_run_key), true);
        //TODO: write any firstRun methods here

        editor.putBoolean(getResources().getString(R.string.first_run_key), false);

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

        sb = new ScheduleBlock(getApplication(), screenHeight, screenWidth, preferences, editor);
        ab = new AnnouncementBlock(getApplicationContext(), new LinearLayout.LayoutParams(screenWidth, (int)(0.85*screenHeight)), preferences, editor);

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

    public static class ScheduleRefresher extends AsyncTask<Void, Void, Void> {

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

    public static class AnnouncementRefresher extends AsyncTask<Void, Void, Void> {

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
}
