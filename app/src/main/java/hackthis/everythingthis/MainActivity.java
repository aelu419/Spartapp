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

    //schedule:0 announcement:1 post:2
    private int pageMode = 0;
    private int pageModeHistory = 0;

    // schedule page vars
    private int screenWidth, screenHeight;
    private static ScheduleRefresher scheduleRefresher = null;
    private static boolean scheduleRefresherRunning = true;
    private static ScheduleBlock sb;

    //main frame vars
    private LinearLayout body;
    private ImageView scheduleIcon, announcementIcon, postIcon;

    //announcement page vars
    private static AnnouncementBlock ab;
    private static AnnouncementRefresher announcementRefresher = null;
    private static boolean announcementRefresherRunning = false;

    //post page vars
    private static PostBlock pb;

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
        footer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //footer.setPadding(0,(int)(0.01*screenHeight),0,(int)(0.01*screenHeight));

        //initialize icon buttons

        LinearLayout.LayoutParams footerButtonParams = new LinearLayout.LayoutParams((int)(0.15*screenWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
        footerButtonParams.setMargins((int)(0.01*screenWidth),0,(int)(0.01*screenWidth),0);

        scheduleIcon = findViewById(R.id.scheduleIcon);
        scheduleIcon.setLayoutParams(footerButtonParams);
        scheduleIcon.setPadding(20,20,20,20);
        scheduleIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        scheduleIcon.setAdjustViewBounds(true);


        announcementIcon = findViewById(R.id.announcementIcon);
        announcementIcon.setLayoutParams(footerButtonParams);
        announcementIcon.setPadding(20,20,20,20);
        announcementIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        announcementIcon.setAdjustViewBounds(true);

        postIcon = findViewById(R.id.postIcon);
        postIcon.setLayoutParams(footerButtonParams);
        postIcon.setPadding(20,20,20,20);
        postIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        postIcon.setAdjustViewBounds(true);


        scheduleIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageMode = 0;
                updatePageMode();
            }
        });

        announcementIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageMode = 1;
                updatePageMode();
            }
        });
        postIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageMode = 2;
                updatePageMode();
            }
        });

        sb = new ScheduleBlock(getApplication(), screenHeight, screenWidth, preferences, editor);
        Log.d("Demo","schedule block created");
        ab = new AnnouncementBlock(getApplication(), new LinearLayout.LayoutParams(screenWidth, (int)(0.85*screenHeight)), preferences, editor);
        Log.d("Demo","announcement block created");
        pb = new PostBlock(getApplication(), new LinearLayout.LayoutParams(screenWidth, (int)(0.85*screenHeight)), preferences, editor);
        Log.d("Demo","post block created");

        pageMode = 0;
        pageModeHistory = 1;

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

            if (pageMode == 0) {
                body.addView(sb);

                scheduleIcon.setImageResource(R.drawable.schedule_selected);
                announcementIcon.setImageResource(R.drawable.announcement);
                postIcon.setImageResource(R.drawable.post);
                //setContentView(R.layout.activity_main);
                //getSupportActionBar().hide();
                startScheduleRefresher();
            } else if(pageMode == 1) {
                body.addView(ab);

                scheduleIcon.setImageResource(R.drawable.schedule);
                announcementIcon.setImageResource(R.drawable.announcement_selected);
                postIcon.setImageResource(R.drawable.post);
                startAnnouncementRefresher();
            } else if(pageMode == 2) {
                body.addView(pb);

                scheduleIcon.setImageResource(R.drawable.schedule);
                announcementIcon.setImageResource(R.drawable.announcement);
                postIcon.setImageResource(R.drawable.post_selected);
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
                    Thread.sleep(30000);
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
                    Thread.sleep(30000);
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
