package hackthis.everythingthis;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.avos.avoscloud.*;

import java.net.URL;
import java.net.URLConnection;

import static hackthis.everythingthis.utils.testInternetConnection;


public class MainActivity extends Activity {

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
    private static String versionName;

    public boolean firstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //getActionBar().hide();

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

        AVInstallation.getCurrentInstallation().saveInBackground();

        PushService.setDefaultPushCallback(this, MainActivity.class);

        preferences = this.getSharedPreferences(getString(R.string.preferences_key),Context.MODE_PRIVATE);
        editor = preferences.edit();

        firstRun = preferences.getBoolean(getResources().getString(R.string.first_run_key), true);
        //TODO: write any firstRun methods here

        editor.putBoolean(getResources().getString(R.string.first_run_key), false);
        editor.apply();



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

        //getScreenDimensions
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        if(!testInternetConnection(this)){
            //read local version code
            Log.d("VER","failed to connect");
        }
        else{
            try {
                AVQuery versionQuery = new AVQuery("AndroidVersionInfo");
                AVObject versionObj = versionQuery.get("5adf2f749f545433342866ec");
                versionName = versionObj.getString("version_name");
                int versionCode = versionObj.getInt("version_code");

                int localVersionCode = 0;
                try {
                    PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
                    localVersionCode = pInfo.versionCode;
                    Log.d("VER","local version code is " + localVersionCode);

                    if(localVersionCode < versionCode){
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                        alertBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://thisprogrammingclub.github.io/"));
                                startActivity(browserIntent);
                            }
                        }).setNegativeButton("no", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        }).setMessage("A new version (" + versionName + ") is available. Do you want to redirect to its download page?")
                                .setCancelable(true);
                        Log.d("VER","connected and obtained version "+versionName+" with code "+versionCode);
                        AlertDialog dialog = alertBuilder.create();
                        dialog.show();
                    }

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            catch(Exception e){
                //read local version code
            }
        }

        //initialize body
        body = (LinearLayout)findViewById(R.id.body);
        body.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, (int)(0.875*screenHeight)));

        //initialize footer
        LinearLayout footer = (LinearLayout)findViewById(R.id.footer);
        footer.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, (int)(0.10*screenHeight)));
        //footer.setPadding(0,(int)(0.01*screenHeight),0,(int)(0.01*screenHeight));

        //initialize icon buttons

        LinearLayout.LayoutParams footerButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        scheduleIcon = (ImageView)findViewById(R.id.scheduleIcon);
        scheduleIcon.setLayoutParams(footerButtonParams);
        scheduleIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        scheduleIcon.setPadding((int)(0.025*screenHeight),(int)(0.025*screenHeight),(int)(0.025*screenHeight),(int)(0.025*screenHeight));
        scheduleIcon.setAdjustViewBounds(true);


        announcementIcon = (ImageView)findViewById(R.id.announcementIcon);
        announcementIcon.setLayoutParams(footerButtonParams);
        announcementIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        announcementIcon.setPadding((int)(0.025*screenHeight),(int)(0.025*screenHeight),(int)(0.025*screenHeight),(int)(0.025*screenHeight));
        announcementIcon.setAdjustViewBounds(true);

        postIcon = (ImageView)findViewById(R.id.postIcon);
        postIcon.setLayoutParams(footerButtonParams);
        postIcon.setPadding((int)(0.025*screenHeight),(int)(0.025*screenHeight),(int)(0.025*screenHeight),(int)(0.025*screenHeight));
        postIcon.setScaleType(ImageView.ScaleType.FIT_XY);
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


        sb = new ScheduleBlock(getApplication(), (int)(0.88*screenHeight), screenWidth, preferences, editor);
        Log.d("Demo","schedule block created");
        ab = new AnnouncementBlock(getApplication(), new LinearLayout.LayoutParams(screenWidth, (int)(0.88*screenHeight)), preferences, editor);
        Log.d("Demo","announcement block created");
        pb = new PostBlock(getApplication(), new LinearLayout.LayoutParams(screenWidth, (int)(0.88*screenHeight)), preferences, editor);
        Log.d("Demo","post block created");

        registerReceiver(announcementUpdateReceiver, new IntentFilter("updateAnnouncements"));

        pageMode = 0;
        pageModeHistory = 1;

        updatePageMode();
    }

    BroadcastReceiver announcementUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // internet lost alert dialog method call from here...
            try {
                Log.d("pushannon","announcement update receiver received intent with action "+intent.getAction());
                if (intent.getAction().equals("updateAnnouncements"))
                    if (ab != null) {
                        ab.loadAnnouncements();
                    } else {

                    }
            }
            catch (Exception E){
                Log.d("pushannon","announcement update receiver threw exception",E);
            }
        }
    };

    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(announcementUpdateReceiver);
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
