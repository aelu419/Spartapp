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
    public boolean shown = false; //this is for the filter view

    public int announcementsIndex = 2;
    
    public ArrayList<Announcement> announcements = new ArrayList<Announcement>();

    public Announcement[] nextAnnouncements = {/*change this to get the announcement from the database instead of from a set string
    also the time has not really been implemented*/
            //format: content, new Time(time), club (you can use null)
            new Announcement("Dogs", new Time(19700101), null),
            new Announcement("More Dogs", new Time(19700101), null),
            new Announcement("Cats", new Time(19700101), null),
            new Announcement("More Cats", new Time(19700101), null),
            new Announcement("Birds", new Time(19700101), null),
            new Announcement("More Birds", new Time(19700101), null),
            new Announcement("Snakes", new Time(19700101), null),
            new Announcement("More Snakes", new Time(19700101), null),
            new Announcement("Raccoons", new Time(19700101), null),
            new Announcement("More Raccoons", new Time(19700101), null),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        announcements.clear();
        for(int i = 0; i < nextAnnouncements.length; i ++) {
            announcements.add(nextAnnouncements[i]);
        }

        LinearLayout filter_Layout = (LinearLayout) findViewById(R.id.filterLayout);

        View v2 = inf.inflate(R.layout.filter_view, null);
        filter_Layout.addView(v2);

        filter_Layout.setVisibility(View.INVISIBLE);

        final ImageButton next = (ImageButton) findViewById(R.id.nextBT);
        final ImageButton prev = (ImageButton) findViewById(R.id.prevBT);


        next.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        announcementsIndex = (announcementsIndex+10)%10;
                        //Toast.makeText(MainActivity.this, "hjju", Toast.LENGTH_SHORT).show();
                        TextView[] textviews = { //this is temporary make this adaptable for leancloud
                                (TextView) findViewById(R.id.content1),
                                (TextView) findViewById(R.id.content2),
                                (TextView) findViewById(R.id.content3),
                                (TextView) findViewById(R.id.content4),
                                (TextView) findViewById(R.id.content5),
                                (TextView) findViewById(R.id.content6),
                                (TextView) findViewById(R.id.content7),
                                (TextView) findViewById(R.id.content8)
                        };

                        //those are the two sample announcements for now (more can be added later)

                        String current = (String)textviews[0].getText(); //this is used to make sure the button actually changes the thing

                        Announcement[] currentAnnouncements = new Announcement[textviews.length];
                        for(int i = 0; i < textviews.length; i ++) {
                                currentAnnouncements[i] = new Announcement((String)textviews[i].getText(), null, null);
                                textviews[i].setText(announcements.get((announcementsIndex+i)%announcements.size()).getAnnouncement());
                        }; //change this to get the announcement from the database instead of from a set string

                        announcementsIndex+=8; //8 is the number on one page at the moment
                        //replaces the announcements in like a cycle
                        nextAnnouncements = currentAnnouncements;
                        String afterChange = (String)textviews[0].getText(); //compares the changes and if they are the same it changes again otherwise nothing happens

                        for(int i = 0; i < currentAnnouncements.length; i ++) {
                            currentAnnouncements[i] = new Announcement((String)textviews[i].getText(), null, null);
                        }

                        if(current.equals(afterChange)) {
                            next.callOnClick();
                        }

                        Toast.makeText(MainActivity.this, nextAnnouncements[0].getAnnouncement(), Toast.LENGTH_SHORT).show();
                        //prev.callOnClick();
                    }
                }
        );
        prev.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        announcementsIndex-=8; //8 is the number on one page at the moment
                        announcementsIndex = (announcementsIndex+10)%10;
                        //Toast.makeText(MainActivity.this, "hjju", Toast.LENGTH_SHORT).show();
                        TextView[] textviews = { //this is temporary make this adaptable for the database
                                (TextView) findViewById(R.id.content1),
                                (TextView) findViewById(R.id.content2),
                                (TextView) findViewById(R.id.content3),
                                (TextView) findViewById(R.id.content4),
                                (TextView) findViewById(R.id.content5),
                                (TextView) findViewById(R.id.content6),
                                (TextView) findViewById(R.id.content7),
                                (TextView) findViewById(R.id.content8)
                        };

                        //those are the two sample announcements for now (more can be added later)

                        String current = (String)textviews[0].getText(); //this is used to make sure the button actually changes the thing

                        Announcement[] currentAnnouncements = new Announcement[textviews.length];
                        for(int i = 0; i < textviews.length; i ++) {
                            currentAnnouncements[i] = new Announcement((String)textviews[i].getText(), null, null);
                            textviews[i].setText(announcements.get((announcementsIndex+i)%announcements.size()).getAnnouncement());
                        }; //change this to get the announcement from the database instead of from a set string

                        //replaces the announcements in like a cycle
                        nextAnnouncements = currentAnnouncements;
                        String afterChange = (String)textviews[0].getText(); //compares the changes and if they are the same it changes again otherwise nothing happens

                        for(int i = 0; i < currentAnnouncements.length; i ++) {
                            currentAnnouncements[i] = new Announcement((String)textviews[i].getText(), null, null);
                        }

                        if(current.equals(afterChange)) {
                            prev.callOnClick();
                        }

                        Toast.makeText(MainActivity.this, nextAnnouncements[0].getAnnouncement(), Toast.LENGTH_SHORT).show();
                        //prev.callOnClick();
                    }
                }
        );
        toolbar.setOnMenuItemClickListener(this);

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

    public void showAnnouncementsPage(LinearLayout l /*provide a layout to inflate the announcements page into*/) {
        LayoutInflater inf = LayoutInflater.from(this);

        //setContentView(R.layout.view_announcements);
        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().show();
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.linearOne);
        LayoutInflater inf2 = LayoutInflater.from(this);
        View v = inf2.inflate(R.layout.announcements_list, null);
        mainLayout.addView(v);

        View v = inf.inflate(R.layout.view_announcements, null);
        l.addView(v);
    }

    public void showMainPage() /*schedule page*/ {
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
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
                showAnnouncementsPage();
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

    public void doSomething(){ Log.d("something","something");}

    /**
    *below: from the announcements thing
    **/

    public boolean onMenuItemClick(MenuItem m) {
        switch (m.getItemId()) {

            case R.id.filter:
//                Toast.makeText(this, "filterV", Toast.LENGTH_SHORT).show();
                LinearLayout filterLayout = (LinearLayout) findViewById(R.id.filterLayout);

              //  Toast.makeText(this, "filterV " + filterLayout.getWidth(), Toast.LENGTH_SHORT).show();

                if(!shown)
                {
                    filterLayout.setVisibility(View.VISIBLE);
                   // Toast.makeText(this, "shown", Toast.LENGTH_SHORT).show();
                    shown = !shown;
                }

                else
                {
                    filterLayout.setVisibility(View.INVISIBLE);
                 //   Toast.makeText(this, "hide", Toast.LENGTH_SHORT).show();
                    shown = !shown;
                }

                ImageButton up = (ImageButton) findViewById(R.id.up);
                ImageButton down = (ImageButton) findViewById(R.id.down);
                Button apply = (Button) findViewById(R.id.applyButton);


                up.setOnClickListener(
                        new ImageButton.OnClickListener() {
                            public void onClick(View v) {
                                TextView text = (TextView) findViewById(R.id.dayInt);
                                String dayText = text.getText().toString();
                                try {
                                    int daytextInt = Integer.parseInt(dayText);
                                    if(daytextInt <= 5) daytextInt++;
                                    else daytextInt = 1;
                                    text.setText("" + daytextInt);
                                } catch (NumberFormatException e) {
                                    System.out.println(e); //outputs to the console (useful when debugging or something)
                                }
                                //Toast.makeText(MainActivity.this, dayText, Toast.LENGTH_SHORT).show();
                            }
                        }
                );

                down.setOnClickListener(
                        new ImageButton.OnClickListener() {
                            public void onClick(View v) {
                                TextView text = (TextView) findViewById(R.id.dayInt);
                                String dayText = text.getText().toString();
                                try {
                                    int daytextInt = Integer.parseInt(dayText);
                                    if(daytextInt >= 2) daytextInt--;
                                    else daytextInt = 6;
                                    text.setText("" + daytextInt);
                                } catch (NumberFormatException e) {
                                    System.out.println(e); //outputs to the console (useful when debugging or something)
                                }
                                //Toast.makeText(MainActivity.this, dayText, Toast.LENGTH_SHORT).show();
                            }
                        }
                );
                apply.setOnClickListener(
                        new Button.OnClickListener() {
                            public void onClick(View v) {

                                //write code here to apply the filter

                                LinearLayout filterLayout = (LinearLayout) findViewById(R.id.filterLayout);
                                filterLayout.setVisibility(View.INVISIBLE);
                                shown = !shown;
                            }
                        }
                );





            default:
                break;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);

        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        }
        return super.onCreateOptionsMenu(menu);
    }
}
