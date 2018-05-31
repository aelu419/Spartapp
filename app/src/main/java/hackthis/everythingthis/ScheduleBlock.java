package hackthis.everythingthis;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.graphics.Matrix;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.search.Resources;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static hackthis.everythingthis.utils.testInternetConnection;

public class ScheduleBlock extends LinearLayout {
    Context context;
    //Data
    private Calendar browsingTime;
    private Date currentTime;
    private int selectedDate;
    private HashMap<String, Subject[]> subjectTable;
    private int screenWidth, screenHeight;
    private boolean leftArrowEnabled, rightArrowEnabled;
    public boolean isLoggedIn;
    private String PSname, PSpass;

    //IO
    private final String FILENAME = "schedule";

    //UI
    private LinearLayout header;
    private ArrayList<DateView> dateList;
    private HorizontalScrollView dateSpinner;
    private LinearLayout dateScroll;
    private LinearLayout bodyBlock;
    private ImageView fetch;
    private ImageView leftArrow, rightArrow;
    private ScrollView bodyBlockHolder;

    public boolean isSet;

    TextView month;

    //ending time of periods
    public int[] periodTimes;

    //month names
    private final String monthNames[] = {"January", "February", "March","April","May","June","July","August","September","October","November","December"};
    //maximum month dates
    private final int maxDate365[] = {31,28,31,30,31,30,31,31,30,31,30,31};
    private final int maxDate366[] = {31,29,31,30,31,30,31,31,30,31,30,31};

    //preferences
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;

    private int LoginTimes;


    //Dimensions
    private int DateViewCommonWidth;

    public ScheduleBlock(Context context, int height, int width, SharedPreferences PREFERENCES, SharedPreferences.Editor EDITOR){
        super(context);

        this.context = context;

        editor = EDITOR;
        preferences = PREFERENCES;
        isLoggedIn = false;
        LoginTimes = 0;

        //params
        screenHeight = height;
        screenWidth = width;
        leftArrowEnabled = true;
        rightArrowEnabled = true;

        LinearLayout.LayoutParams scheduleParams = new LayoutParams(screenWidth, screenHeight);
        this.setLayoutParams(scheduleParams);
        this.setOrientation(VERTICAL);

        //setPeriodTimes base on day of week
        currentTime=getTime();
        browsingTime = Calendar.getInstance();
        browsingTime.setTime(currentTime);
        setPeriodTimes(new GregorianCalendar(browsingTime.get(Calendar.YEAR), browsingTime.get(Calendar.MONTH), browsingTime.get(Calendar.DATE)));
        selectedDate = currentTime.getDate();

        //initialize header
        //{fetch button---left arrow---month---right arrow}
        header = new LinearLayout(context);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ((int)(0.15*screenHeight)));
        header.setLayoutParams(headerParams);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER);

        this.addView(header);

        //initialize fetch button
        fetch = new ImageView(context);
        fetch.setImageResource(R.drawable.fetch_enabled);
        LinearLayout.LayoutParams fetchParams = new LinearLayout.LayoutParams(((int)(0.075*screenWidth)), ViewGroup.LayoutParams.WRAP_CONTENT);
        fetch.setPadding(5,0,5,0);
        fetch.setLayoutParams(fetchParams);
        fetch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isLoggedIn) {
                    isLoggedIn = false;
                    LoginScreen ls = new LoginScreen(false, true);
                    bodyBlock.removeAllViews();
                    bodyBlock.addView(ls);
                }
            }
        });

        header.addView(fetch);

        //initialize month textview
        month = new TextView(context);

        header.addView( month );

        //initialize arrows
        leftArrow = new ImageView(context);
        rightArrow = new ImageView(context);
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams((int)(0.075*screenWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
        leftArrow.setLayoutParams(arrowParams);
        leftArrow.setPadding(10,15,10,15);
        rightArrow.setLayoutParams(arrowParams);
        rightArrow.setPadding(10,15,10,15);


        leftArrow.setImageResource(R.drawable.arrow_left_disabled);
        rightArrow.setImageResource(R.drawable.arrow_right_disabled);
        leftArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(leftArrowEnabled) {
                    updatePage(browsingTime.get(Calendar.MONTH)-1);
                }
            }
        });

        rightArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rightArrowEnabled){
                    updatePage(browsingTime.get(Calendar.MONTH)+1);
                }
            }
        });

        header.addView(leftArrow);

        LinearLayout.LayoutParams monthParams = new LayoutParams((int)(0.7*screenWidth), ((int)(0.15*screenHeight)));
        month.setPadding(0,0,0,0);
        month.setLayoutParams(monthParams);
        month.setGravity(Gravity.CENTER);
        month.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        month.setTextColor(getResources().getColor(R.color.purple));

        month.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)(month.getLayoutParams().height * 0.4));

        header.addView(rightArrow);

        //initialize dateSpinner
        dateSpinner = new HorizontalScrollView(context);
        dateSpinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ((int)(0.09*screenHeight))));
        dateSpinner.setPadding(5,5,5,5);
        dateSpinner.setSmoothScrollingEnabled(true);
        dateSpinner.setVerticalScrollBarEnabled(false);
        dateSpinner.setHorizontalScrollBarEnabled(false);
        this.addView(dateSpinner);

        //initialize dateScroll
        dateScroll = new LinearLayout(context);
        dateScroll.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dateScroll.setOrientation(HORIZONTAL);
        dateSpinner.addView(dateScroll);

        //initialize bodyBlock

        bodyBlockHolder = new ScrollView(context);

        bodyBlockHolder.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)(0.76*screenHeight)));
        bodyBlockHolder.setBackgroundColor(getResources().getColor(R.color.shaded_background));
        bodyBlockHolder.setHorizontalScrollBarEnabled(false);
        bodyBlockHolder.setVerticalScrollBarEnabled(true);

        bodyBlock = new LinearLayout(context);
        bodyBlock.setOrientation(VERTICAL);
        bodyBlock.setGravity(Gravity.CENTER_HORIZONTAL);
        bodyBlock.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        bodyBlockHolder.addView(bodyBlock);

        this.addView(bodyBlockHolder);

        //readDemoFile();

        PSname = preferences.getString(getResources().getString(R.string.ps_name_key),null);
        PSpass = preferences.getString(getResources().getString(R.string.ps_pass_key),null);

        login(false);

        updatePage(browsingTime.get(Calendar.MONTH));

    }

    public void login(boolean calledFromFetch){
        try {
            subjectTable = getSchedule();
            isLoggedIn = true;
        }
        catch(Exception e) {
            LoginScreen ls = new LoginScreen(calledFromFetch, false);
            bodyBlock.removeAllViews();
            bodyBlock.addView(ls);
        }

        Log.d("Demo","login finished function");
    }

    public void updatePage(int newMonth){

        if(!isLoggedIn){
            fetch.setImageResource(R.drawable.fetch_enabled);
            return;
        }

        Log.i("updatePage","updatePage: from "+browsingTime.get(Calendar.YEAR)+" "+browsingTime.get(Calendar.MONTH)+" "+
                browsingTime.get(Calendar.DATE)+" "+browsingTime.get(Calendar.DAY_OF_WEEK)+"\t to "+newMonth);

        //if current month is after july, then it must be the starting year of a new school year, therefore available months are july to december
        int minMonth, maxMonth;
        leftArrowEnabled = true;
        rightArrowEnabled = true;

        if(browsingTime.get(Calendar.MONTH) >= Calendar.JULY){
            minMonth = Calendar.JULY;
            maxMonth = Calendar.DECEMBER;

            if(newMonth > maxMonth){
                if(browsingTime.get(Calendar.DATE)>(is365()?maxDate365[Calendar.JANUARY]:maxDate366[Calendar.JANUARY])) {
                    //Log.d("updatePage","has more day");
                    browsingTime.set(Calendar.DATE, (is365() ? maxDate365[Calendar.JANUARY] : maxDate366[Calendar.JANUARY]));
                }
                browsingTime.set(Calendar.YEAR, (browsingTime.get(Calendar.YEAR)+1));
                browsingTime.set(Calendar.MONTH, Calendar.JANUARY);
            }
            else if(newMonth <= minMonth){
                if(browsingTime.get(Calendar.DATE)>(is365()?maxDate365[Calendar.JULY]:maxDate366[Calendar.JULY])) {
                    //Log.d("updatePage","has more day");
                    browsingTime.set(Calendar.DATE, (is365() ? maxDate365[Calendar.JULY] : maxDate366[Calendar.JULY]));
                }
                browsingTime.set(Calendar.MONTH, Calendar.JULY);
                leftArrowEnabled = false;
            }
            else{
                if(browsingTime.get(Calendar.DATE)>(is365()?maxDate365[newMonth]:maxDate366[newMonth]))
                    browsingTime.set(Calendar.DATE,(is365()?maxDate365[newMonth]:maxDate366[newMonth]));
                browsingTime.set(Calendar.MONTH, newMonth);
            }
        }
        else{
            //second year in a school year, available months 1 ~ 6
            minMonth = Calendar.JANUARY;
            maxMonth = Calendar.JUNE;

            //Log.d("updatePage","currently on "+browsingTime.get(Calendar.MONTH)+" "+browsingTime.get(Calendar.DATE)+", next month has"+(is365()?maxDate365[newMonth]:maxDate366[newMonth]));

            if(newMonth >= maxMonth){
                if(browsingTime.get(Calendar.DATE)>(is365()?maxDate365[Calendar.JUNE]:maxDate366[Calendar.JUNE])) {
                    //Log.d("updatePage","has more day");
                    browsingTime.set(Calendar.DATE, (is365() ? maxDate365[Calendar.JUNE] : maxDate366[Calendar.JUNE]));
                }
                browsingTime.set(Calendar.MONTH, Calendar.JUNE);
                rightArrowEnabled= false;
            }
            else if(newMonth < minMonth){
                if(browsingTime.get(Calendar.DATE)>(is365()?maxDate365[Calendar.DECEMBER]:maxDate366[Calendar.DECEMBER])) {
                    //Log.d("updatePage","has more day");
                    browsingTime.set(Calendar.DATE, (is365() ? maxDate365[Calendar.DECEMBER] : maxDate366[Calendar.DECEMBER]));
                }
                browsingTime.set(Calendar.YEAR, browsingTime.get(Calendar.YEAR)-1);
                browsingTime.set(Calendar.MONTH, Calendar.DECEMBER);
            }
            else{
                if(browsingTime.get(Calendar.DATE)>(is365()?maxDate365[newMonth]:maxDate366[newMonth])) {
                    //Log.d("updatePage","has more day");
                    browsingTime.set(Calendar.DATE, (is365() ? maxDate365[newMonth] : maxDate366[newMonth]));
                }
                browsingTime.set(Calendar.MONTH, newMonth);
            }
        }

        if(leftArrowEnabled)
            leftArrow.setImageResource(R.drawable.arrow_left_enabled);
        else
            leftArrow.setImageResource(R.drawable.arrow_left_disabled);

        if(rightArrowEnabled)
            rightArrow.setImageResource(R.drawable.arrow_right_enabled);
        else
            rightArrow.setImageResource(R.drawable.arrow_right_disabled);

        /*if(is365()){
            if(selectedDate > maxDate365[browsingTime.get(Calendar.MONTH)]){
                selectedDate = maxDate365[browsingTime.get(Calendar.MONTH)];
            }
        }
        else{
            if(selectedDate > maxDate366[browsingTime.get(Calendar.MONTH)]){
                selectedDate = maxDate366[browsingTime.get(Calendar.MONTH)];
            }
        }*/

        initializeDateBar();

    }

    public void onLayout(boolean changed, int l, int t, int r, int b){
        super.onLayout(changed, l, t, r, b);
        //GregorianCalendar gc = new GregorianCalendar();
       // gc.setTime(currentTime);
        if(isSet) {
            Log.d("calendar","onLayout function toggles" + browsingTime.get(Calendar.DATE));
            dateList.get(browsingTime.get(Calendar.DATE) - 1).toggle();
            isSet = false;
        }
    }

    public void initializeDateBar(){
        Log.i("Demo","Start Initializing date bar");
        if(dateSpinner.getLayoutParams().height - 10 > screenWidth/11) {
            DateViewCommonWidth = screenWidth / 11;
        }
        else{
            DateViewCommonWidth = dateSpinner.getLayoutParams().height - 10;
        }

        month.setText(monthNames[browsingTime.get(Calendar.MONTH)]);

        dateList = new ArrayList<DateView>(0);
        dateSpinner.setSmoothScrollingEnabled( true );
        dateSpinner.setHorizontalScrollBarEnabled( false );
        dateScroll.removeAllViews();
        if(is365()) {
            for (int i = 1; i <= maxDate365[browsingTime.get(Calendar.MONTH)]; i++) {
                addInterval();
                addDateView(i);
            }
        }
        else{
            for (int i = 1; i <= maxDate366[browsingTime.get(Calendar.MONTH)]; i++) {
                addInterval();
                addDateView(i);
            }
        }
        addInterval();

        Log.i("Demo","Finished initializing date bar");
        isSet = true;
    }

    private boolean is365(){
        int thisYear = browsingTime.get(Calendar.YEAR);
        if(thisYear % 4 == 0){
            if(thisYear % 400 == 0) {
                Log.i("year",thisYear+"returns false");
                return false;
            }
            else{
                Log.i("year",thisYear+"returns"+(thisYear%100==0));
                return (thisYear%100==0);
            }
        }
        Log.i("year",thisYear+"returns true");
        return true;
    }

    public Subject[] getFromTable(Calendar cal){
        int month = cal.get(Calendar.MONTH)+1;
        int yr =  cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String m = month<10?"0"+Integer.toString(month):Integer.toString(month);
        String d = day<10?"0"+Integer.toString(day):Integer.toString(day);

        //updates the colors and text to match the selected date
        Log.d("DATEPARSE", Integer.toString(yr)+"-"+m+"-"+d);
        Subject[] subjects = subjectTable.get(Integer.toString(yr)+"-"+m+"-"+d);

        return subjects;
    }

    public GregorianCalendar getTomorrow(){
        Date time = currentTime;
        GregorianCalendar cal;
        do{
            time.setTime(time.getTime()+24L*60L*60L*1000L);
            cal = new GregorianCalendar();
            cal.setTime(time);
            if(getFromTable(cal)!=null && getFromTable(cal).length!=0){
                Log.d("Demo","found tomorrow:"+cal.get(Calendar.YEAR)+ cal.get(Calendar.MONTH)+
                        cal.get(Calendar.DATE));
                return cal;
            }
        }while(cal.get(Calendar.MONTH)<Calendar.JULY || cal.get(Calendar.YEAR)<currentTime.getYear());
        return null;
    }

    public void onSelection(){

        if(!isLoggedIn){
            return;
        }

        Log.d("Demo","starting onSelection");

        currentTime = getTime();

        GregorianCalendar todayTemp = new GregorianCalendar();
        todayTemp.setTime(currentTime);
        Subject[] currentTimeSubjects = getFromTable(todayTemp);
        boolean todayIsSchoolDay = currentTimeSubjects!=null && currentTimeSubjects.length!=0;
        int hm = currentTime.getHours()*100+currentTime.getMinutes();
        Log.d("Demo", "today is "+todayIsSchoolDay+" a school day");

        Subject[] subjects = getFromTable(browsingTime);

        ArrayList<Subject> subjectsTrimmed = new ArrayList<>(8);
        ArrayList<Integer> periodsTrimmed = new ArrayList<>(8);
        if(subjects!=null) {
            Log.d("Demo","got classes");
            for (int i = 0; i < subjects.length; i++) {
                if(subjects[i]==null){

                }
                else{
                    if(i!=subjects.length-1) {
                        if (subjects[i].equals(subjects[i + 1])) {
                            subjectsTrimmed.add(subjects[i]);
                            periodsTrimmed.add(i);
                            i++;
                        } else {
                            subjectsTrimmed.add(subjects[i]);
                            periodsTrimmed.add(i);
                        }
                    }
                    else{
                        subjectsTrimmed.add(subjects[i]);
                        periodsTrimmed.add(i);
                    }
                }
            }
        }

        Log.d("Demo",subjectsTrimmed.toString()+" "+periodsTrimmed.toString());

        GregorianCalendar tmrTemp = getTomorrow();

        GregorianCalendar temp = new GregorianCalendar(browsingTime.get(Calendar.YEAR), browsingTime.get(Calendar.MONTH), browsingTime.get(Calendar.DATE));
        int fuckCalendarsWhyTheFuckDoesntWeekDayChangeAutomatically = temp.get(GregorianCalendar.DAY_OF_WEEK);
        setPeriodTimes(new GregorianCalendar(browsingTime.get(Calendar.YEAR), browsingTime.get(Calendar.MONTH),
                browsingTime.get(Calendar.DATE)));

        Log.d("Demo","today:"+browsingTime.get(Calendar.YEAR)+ " "+browsingTime.get(Calendar.MONTH)+" "+
                browsingTime.get(Calendar.DATE));

        //if the calendar does not yet exist
        if( subjectsTrimmed.isEmpty() || subjects == null) {
                bodyBlock.removeAllViews();
                ImageView restImage = new ImageView(context);
                restImage.setPadding(20,40,20,0);

                TextView errorMessage = new TextView(context);

                errorMessage.setTextSize((float)(screenWidth > screenHeight ? screenWidth/50.0 : screenHeight/50.0));
                errorMessage.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
                errorMessage.setTextColor(getResources().getColor(R.color.black));
                if(fuckCalendarsWhyTheFuckDoesntWeekDayChangeAutomatically == GregorianCalendar.SUNDAY
                        || fuckCalendarsWhyTheFuckDoesntWeekDayChangeAutomatically == GregorianCalendar.SATURDAY) {
                    restImage.setImageResource(R.drawable.weekend);
                    errorMessage.setText("ENJOY YOUR WEEKEND");
                }
                else {
                    errorMessage.setText("ENJOY YOUR DAY OFF");
                    restImage.setImageResource(R.drawable.vacation);
                }
                LinearLayout.LayoutParams restParams = new LinearLayout.LayoutParams((int)(0.7*screenWidth),(int)(0.7*screenWidth));
                restParams.setMargins(0, 140, 0, 0);
                restImage.setLayoutParams(restParams);
                bodyBlock.addView(restImage);
                bodyBlock.addView(errorMessage);
            Log.d("Demo","empty day");
        }
        else {
                //initialize blocks in terms of time
                bodyBlock.removeAllViews();
                courseBlock[] blocks = new courseBlock[subjectsTrimmed.size()];
                boolean[] isMain = new boolean[subjectsTrimmed.size()];
                if (todayTemp.get(GregorianCalendar.MONTH) == temp.get(GregorianCalendar.MONTH)
                        && todayTemp.get(GregorianCalendar.DATE) == temp.get(GregorianCalendar.DATE)) {
                    Log.d("Demo","today is selected");
                    boolean foundFirst = false;
                    for(int i = 0; i < subjectsTrimmed.size(); i++){
                        if(periodTimes[periodsTrimmed.get(i)]>hm && !foundFirst){
                            foundFirst = true;
                            isMain[i] = true;
                            Log.d("Demo",hm+"less than"+periodTimes[periodsTrimmed.get(i)]);
                        }
                        else{
                            isMain[i] = false;
                        }
                    }
                } else if(tmrTemp != null){
                    if (tmrTemp.get(GregorianCalendar.MONTH) == temp.get(GregorianCalendar.MONTH)
                            && tmrTemp.get(GregorianCalendar.DATE) == temp.get(GregorianCalendar.DATE)) {
                        Log.i("Demo", "tomorrow is selected");
                        if (todayIsSchoolDay) {
                            if (hm > periodTimes[periodsTrimmed.get(periodsTrimmed.size()-1)]) {
                                isMain[0] = true;
                            }
                        } else {
                            isMain[0] = true;
                        }
                    } else {
                        Log.i("Demo", "no next periods in today");
                    }
                }
                else {
                    Log.i("Demo", "no next periods in today");
                }

                for (int i = 0; i < subjectsTrimmed.size(); i++) {
                    blocks[i] = new courseBlock(context, isMain[i], subjectsTrimmed.get(i), i);
                    bodyBlock.addView(blocks[i]);
                }

            Log.d("Demo","school day");

        }
        Log.d("Demo","ending onSelection");
    }

    public void addDateView( int date ){
        DateView dateView = new DateView(context, date, dateSpinner);
        dateList.add( dateView );
        dateScroll.addView( dateView );
    }

    public void addInterval(){
        dateScroll.addView( new DateSeperator( context, DateViewCommonWidth ) );
    }


    //Encapsulated class for date selection buttons
    public class DateView extends FrameLayout implements View.OnClickListener {

        private final TextView textView;
        private ImageView image;
        private boolean selected;

        public DateView(Context context, int date, HorizontalScrollView parent ){

            super(context);
            this.selected = false;
            this.setForegroundGravity(Gravity.CENTER);
            this.setLayoutParams(new LinearLayout.LayoutParams(DateViewCommonWidth, DateViewCommonWidth));
            this.setScaleX(1.5f);
            this.setScaleY(1.5f);
            this.setId(date);

            image = new ImageView(context);
            image.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setAdjustViewBounds(true);
            image.setImageResource(R.drawable.date_picker_off);
            this.addView(image);

            textView = new TextView(context);
            textView.setText(String.valueOf(date));
            textView.setTextColor(context.getResources().getColor(R.color.purple));
            textView.setGravity(Gravity.CENTER);
            textView.setTypeface(null, Typeface.BOLD);
            this.addView(textView);

            this.setOnClickListener( this );
        }

        /*public void toggle(){
            selected = !selected;
            if(selected) {
                this.setBackgroundResource(R.drawable.date_picker);
                textView.setTextColor(Color.WHITE);
            }else {
                this.setBackgroundResource(R.drawable.date_picker_off);
                textView.setTextColor(getContext().getResources().getColor(R.color.purple));
            }

            onSelection();
        }*/

        public void toggle(){
            selected = !selected;
            Log.d("calendar",textView.getText().toString()+"toggled");
            if(selected) {
                selectedDate = Integer.parseInt(textView.getText().toString());
                browsingTime.set(Calendar.DATE, Integer.parseInt(textView.getText().toString()));
                image.setImageResource(R.drawable.date_picker);
                textView.setTextColor(Color.WHITE);
                scroll();
                if(isLoggedIn)
                    onSelection();
            }else {
                image.setImageResource(R.drawable.date_picker_off);
                textView.setTextColor(getContext().getResources().getColor(R.color.purple));
            }
        }

        public void onClick(View view){
            dateList.get(browsingTime.get(Calendar.DATE) - 1).toggle();
            toggle();
        }

    }

    public void scroll(){
        if(browsingTime.get(Calendar.DATE)<=3) {
            Log.i("calendar","rolled to beginning");
            //the first elements doesnt need scrolling
            dateSpinner.smoothScrollTo(0,0);
        }
        else {
            //the rest scrolls to the middle
            Log.i("calendar",browsingTime.get(Calendar.DATE)+" rolled");
            dateSpinner.smoothScrollTo((browsingTime.get(Calendar.DATE) - 3) * 2 * DateViewCommonWidth, 0);
        }
    }

    //Fills the intervals
    public class DateSeperator extends FrameLayout{

        public DateSeperator(Context context, int width){
            super(context);
            this.setLayoutParams(new LinearLayout.LayoutParams(width, DateViewCommonWidth));
        }

    }



    public class courseBlock extends LinearLayout{
        public boolean isMain;

        TextView course;
        TextView extra;
        ImageView bar;
        ImageView background;
        FrameLayout description;

        //generates course block from parameters, see onSelect and onProgressUpdate
        public courseBlock(Context context, boolean b, Subject Course, int blockNumber){
            super(context);

            this.setId(blockNumber);
            this.setOrientation(LinearLayout.HORIZONTAL);
            this.setGravity(Gravity.CENTER_HORIZONTAL);

            bar = new ImageView(context);
            description = new FrameLayout(context);

            isMain = b;
            //add components: linear{bar, frame{course, extra, background}}
            course = new TextView(context);
            extra = new TextView(context);
            background = new ImageView(context);

            course.setText(Course.name());
            course.setTypeface(null, Typeface.BOLD);
            extra.setText(Course.room()+"\n"+Course.teacher());

            String type = Course.type();

            //long TIME = new Date().getTime();
            //Log.i("Demo","initialization started"+(new Date().getTime()-TIME));
            Log.d("Demo", "initializing course: "+course.getText()+" (type: "+type+")");

            if(isMain){
                //set the dimensions of the frame
                LinearLayout.LayoutParams lay = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(0.47*(int)(0.76 *screenHeight)));
                lay.setMargins(4, 10, 4, 10);
                this.setLayoutParams(lay);
                //this.setBackground(getResources().getDrawable(R.drawable.button_background));

                //set inner frame
                description.setLayoutParams(new LinearLayout.LayoutParams((int)(0.9*screenWidth), ViewGroup.LayoutParams.MATCH_PARENT));

                /*Bitmap bitmapOrg = BitmapFactory.decodeResource(context.getResources(), Course.imageInt);

                int width = bitmapOrg.getWidth();
                int height = bitmapOrg.getHeight();
                Log.d("CROP", Integer.toString(width));
                int newWidth = 1050;
                int newHeight = 613;

                // calculate the scale - in this case = 0.4f
                float scaleWidth = ((float) newWidth) / width;
                float scaleHeight = ((float) newHeight) / height;

                // createa matrix for the manipulation
                Matrix matrix = new Matrix();
                // resize the bit map
                matrix.postScale(scaleWidth, scaleHeight);

                // recreate the new Bitmap
                Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 150, 50,
                        newWidth, newHeight, matrix, true);

                // make a Drawable from Bitmap to allow to set the BitMap
                // to the ImageView, ImageButton or what ever
                BitmapDrawable bmd = new BitmapDrawable(resizedBitmap);
                */

               //set the background image
                background.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
                background.setScaleType(ImageView.ScaleType.FIT_XY);
                background.setImageResource(Course.imageInt);
                background.setBackgroundColor(getResources().getColor(R.color.white));

                //set textviews
                FrameLayout.LayoutParams margin = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                margin.setMargins(30, 2, 2, 2);
                course.setLayoutParams(margin);
                course.setTextColor(Color.BLACK);
                course.setGravity(Gravity.TOP);
                if(course.getText().length()>15)
                    course.setTextSize(40.0f);
                else
                    course.setTextSize(45.0f);

                extra.setLayoutParams(margin);
                extra.setGravity(Gravity.BOTTOM);
                extra.setTextColor(Color.BLACK);
                if(extra.getText().length()>20)
                    extra.setTextSize(20.0f);
                else
                    extra.setTextSize(24.0f);

                //add components: linear{bar, frame{name, extra, background}}
                description.addView(background);
                description.addView(course);
                description.addView(extra);

                this.addView(description);
            }
            else{
                //set the dimensions of the frame
                LinearLayout.LayoutParams lay = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(0.14*(int)(0.76*screenHeight)));
                lay.setMargins(4, 15, 4, 15);
                this.setLayoutParams(lay);

                //set inner frame
                LinearLayout.LayoutParams LAY = new LinearLayout.LayoutParams((int)(0.9*screenWidth), ViewGroup.LayoutParams.MATCH_PARENT);
                description.setLayoutParams(LAY);

                //set the bar image
                bar.setLayoutParams(new LinearLayout.LayoutParams(25,ViewGroup.LayoutParams.MATCH_PARENT));
                bar.setScaleType(ImageView.ScaleType.FIT_XY);
                bar.setImageResource(R.drawable.rounded_edge_short);
                bar.setColorFilter(getResources().getColor(Course.colorInt));


                //set the background image
                background.setScaleType(ImageView.ScaleType.FIT_XY);
                background.setImageResource(R.drawable.rounded_edge_short_flipped);
                background.setColorFilter(Color.WHITE);
                background.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));


                //set textviews
                FrameLayout.LayoutParams margin = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                margin.setMargins(5, 5, 5, 5);
                course.setLayoutParams(margin);
                course.setGravity(Gravity.CENTER_VERTICAL);
                course.setTextColor(getResources().getColor(Course.colorInt));
                if(course.getText().length()>18)
                    course.setTextSize(20.0f);
                else
                    course.setTextSize(26.0f);
                description.addView(background);
                description.addView(course);


                this.addView(bar);

                this.addView(description);
            }

            //Log.i("Demo","intialization for "+Course.name()+" finished at "+(new Date().getTime()-TIME));

        }
    }



    //tries to get time from internet, if fails then set time as system time
    public static Date getTime() {
        /*todo: currently time is set to local time, it is recommended to switch to internet time*/
        return new Date();
    }

    //login screen ctrlf
    public class LoginScreen extends LinearLayout{
        public EditText nameText, passwordText;
        public Button button1;
        public LinearLayout buttonBox;
        public TextView hint;
        public ImageView returnButton;
        public LinearLayout iconBox;

        //mode: log in from fetch button = true
        public LoginScreen(boolean calledFromFetch, boolean returnable){
            super(context);
            this.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, (int)(0.76*screenHeight)));
            this.setPadding((int)(0.075*screenWidth),(int)(0.03*screenHeight),(int)(0.075*screenWidth),10);
            this.setOrientation(VERTICAL);
            this.setGravity(Gravity.RIGHT); //looks nice
            this.setBackgroundColor(getResources().getColor(R.color.powerschool));

            if(returnable){
               returnButton = new ImageView(context);
               returnButton.setLayoutParams(new LinearLayout.LayoutParams((int)(0.1*screenWidth), (int)(0.05*screenHeight)));
               returnButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
               returnButton.setAdjustViewBounds(true);
               returnButton.setImageResource(R.drawable.return_button);
               returnButton.setOnClickListener(new OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       isLoggedIn = true;
                       onSelection();
                   }
               });
               this.addView(returnButton);
            }


            iconBox = new LinearLayout(context);
            iconBox.setLayoutParams(new LinearLayout.LayoutParams((int)(0.85*screenWidth), ViewGroup.LayoutParams.WRAP_CONTENT));
            iconBox.setGravity(Gravity.CENTER_HORIZONTAL);

            ImageView powerschoolIcon = new ImageView(context);
            powerschoolIcon.setLayoutParams(new LinearLayout.LayoutParams((int)(0.25*screenWidth), ViewGroup.LayoutParams.WRAP_CONTENT));
            powerschoolIcon.setImageResource(R.drawable.powerschool);
            powerschoolIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            powerschoolIcon.setAdjustViewBounds(true);

            iconBox.addView(powerschoolIcon);
            this.addView(iconBox);

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams((int)(0.85*screenWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
            textParams.setMargins(0,10,0,10);

            hint = new TextView(context);
            if(calledFromFetch){
                hint.setText("Your last login has failed, please try again!");
            }
            else{
                hint.setText("");
            }
            hint.setTextColor(getResources().getColor(R.color.white));
            hint.setLayoutParams(textParams);
            this.addView(hint);

            nameText = new EditText(context);
            nameText.setHint("   ID");
            nameText.setLayoutParams(textParams);
            nameText.setTextColor(getResources().getColor(R.color.white));
            nameText.setBackgroundColor(getResources().getColor(R.color.powerschool_shaded));
            nameText.setPadding(8,8,8,8);
            this.addView(nameText);

            passwordText = new EditText(context);
            passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordText.setTextColor(getResources().getColor(R.color.white));
            passwordText.setHint(" password");
            passwordText.setLayoutParams(textParams);
            passwordText.setBackgroundColor(getResources().getColor(R.color.powerschool_shaded));
            passwordText.setPadding(8,8,8,8);
            this.addView(passwordText);

            buttonBox = new LinearLayout(context);
            LayoutParams lparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lparams.setMargins(0, 30, 0,0);
            buttonBox.setLayoutParams(lparams);
            buttonBox.setGravity(Gravity.START);
            buttonBox.setOrientation(HORIZONTAL);
            this.addView(buttonBox);

            LinearLayout.LayoutParams buttonParam = new LinearLayout.LayoutParams((int)(0.2*screenWidth), (int)(0.1*screenWidth));
            buttonParam.setMargins(0,5,20,0);

            button1 = new Button(context);
            button1.setBackground(getResources().getDrawable(R.drawable.button_background));
            button1.setLayoutParams(buttonParam);
            buttonBox.addView(button1);
            button1.setTextColor(getResources().getColor(R.color.purple));
            button1.setTextSize(12.0f);
            button1.setText("log in");

            button1.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    LoginTimes = 0;
                    hint.setText("downloading schedule, please wait....\nThe application will restart it self once done downloading, please do not re-open the app manually.");
                    PSname = nameText.getText().toString();
                    PSpass = passwordText.getText().toString();
                    Log.d("Demo","logging in with information: name("+PSname+") pass("+PSpass+")");
                    editor.putString(getResources().getString(R.string.ps_name_key),
                            nameText.getText().toString());
                    editor.putString(getResources().getString(R.string.ps_pass_key),
                            passwordText.getText().toString());
                    editor.apply();
                    iconMode();
                    try{
                        openWebView(PSname, PSpass);}
                    catch(Exception e){
                        login(true);
                        Log.d("DEV","KMS");
                    }
                }
            });
        }

        private void iconMode(){
            this.removeAllViews();
            this.addView(iconBox);
            this.addView(hint);
        }

    }

    public void setPeriodTimes(GregorianCalendar date){
        periodTimes = new int[8];
        if(date.get(GregorianCalendar.DAY_OF_WEEK)==Calendar.WEDNESDAY){
            periodTimes = new int[]{815, 835, 855, 920, 940, 1120, 1210, 1320};
        }
        else {
            periodTimes = new int[]{815, 855, 935, 1035, 1115, 1205, 1350, 1435};
        }
    }

    public HashMap<String, Subject[]> getSchedule() throws Exception{
        HashMap<String, Subject[]> schedule = new HashMap<>(6);

        HashMap<String, Integer> dateDay = getDateDayPairs();

        HashMap<Integer, Subject[]> weeklySchedule = readWeeklySchedule();

        for(Map.Entry<String, Integer> keyValuePair : dateDay.entrySet()){
            String date = keyValuePair.getKey();
            Integer day = keyValuePair.getValue();
            Log.d("Demo",date+" is day "+day);
            if(day != -1)
                schedule.put(date, weeklySchedule.get(day));
            else
                schedule.put(date, null);
        }

        return schedule;
    }

    public HashMap<String, Integer> getDateDayPairs()throws AVException, ParseException {
        HashMap<String, Integer> dateDay;

        AVQuery query = new AVQuery("UpdateCalendar");
        List<AVObject> qList = query.find();
        Boolean update = qList.get(0).getBoolean("pendingUpdate");
        String startOfYear = qList.get(0).getString("yearStart");
        if(update){
            dateDay = fetchDateDayPairs(startOfYear);
            writeDateDayPairs(dateDay);
        }
        else{
            try{
                dateDay = readDateDayPairs();
                Log.d("WKD", "read from file");
            }catch(IOException e){
                dateDay = fetchDateDayPairs(startOfYear);
                writeDateDayPairs(dateDay);
            }
        }
        return dateDay;
    }

    public void writeDateDayPairs(HashMap<String, Integer> dateDay){
        //File file = new File("DateDayPairs.dat");
        //Log.d("WKD", new Boolean(file.canWrite()).toString());
        try {
            FileOutputStream f = context.openFileOutput("date_day.dat", context.MODE_PRIVATE);
            ObjectOutputStream s = new ObjectOutputStream(f);
            s.writeObject(dateDay);
            s.close();
            Log.d("WKD", "output success");
        }
        catch(FileNotFoundException e){Log.d("WKD", "witchcraft");}
        catch(IOException e){Log.d("WKD", "unknown powers");}
    }

    public HashMap<String, Integer> readDateDayPairs() throws IOException{
        try {
            FileInputStream f = context.openFileInput("date_day.dat");
            ObjectInputStream s = new ObjectInputStream(f);
            HashMap<String, Integer> dateDay = (HashMap<String, Integer>)s.readObject();
            s.close();
            Log.d("WKD", "input success");
            return dateDay;
        }
        catch(ClassNotFoundException e){Log.d("WKD", "classnotfound");}
        return null;
    }

    public HashMap<String, Integer> fetchDateDayPairs(String startOfYear) throws ParseException, AVException {
        HashMap<String, Integer> dateDay = new HashMap<>(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(startOfYear));
        List<AVObject> schoolDays = getWkDayList();
        for(AVObject schoolDay : schoolDays){
            String time = sdf.format(c.getTime());
            Integer day = schoolDay.getInt("dayInCycle");
            dateDay.put(time, day);
            c.add(Calendar.DATE, 1);
            Log.d("WKD_", time  + " " + day);
        }
        return dateDay;
    }

    public List<AVObject> getWkDayList()throws AVException {
      /*  AVQuery calendar = new AVQuery("DayCycle");
        calendar.limit(1000);
        List<AVObject> schoolDays = calendar.find();
        schoolDays = QSDateHelper(schoolDays);
        Log.d("WKD_", new Integer(schoolDays.size()).toString());
        return schoolDays;
*/
        AVQuery calendar = new AVQuery("Calendar");
        List<AVObject> schoolDays = calendar.find();
        List<AVObject> dayCycle = new ArrayList<AVObject>(0);

        schoolDays = QSDateHelper(schoolDays);
        int schoolDayCount = -1;
        int days = -1;
        for(AVObject schoolDay : schoolDays){
            for(Boolean isDay : (List<Boolean>)schoolDay.getList("weeklyCalendar")) {
                AVObject schoolday = new AVObject("Void");
                schoolday.put("daysSince", days);
                if (isDay){
                    schoolday.put("dayInCycle", schoolDayCount % 6 + 1);
                    schoolDayCount ++;
                }
                else {
                    schoolday.put("dayInCycle", -1);
                }
                days++;
                //schoolday.saveInBackground();
                dayCycle.add(schoolday);
                //Log.d("WKD", new Boolean(isDay).toString() + " " + new Integer(schoolDayCount%6+1));
            }
        }
        return dayCycle;
    }

    public List<AVObject> QSDateHelper(List<AVObject> arr){
        QuickSortDate(arr, 0, arr.size()-1);
        for(AVObject i : arr)
            Log.d("SORT","sorted " +i.getInt("daysSince"));
        return arr;
    }

    public void QuickSortDate(List<AVObject> arr, int low, int high){
        for(int k = low; k <= high; k++){
        }
        if(arr==null || high-low <1 || high<=low){
            return;
        }
        AVObject midValue = arr.get(low);
        int i = low, j = high;
        while(true){
            while(i<j && arr.get(j).getInt("daysSince")>=midValue.getInt("daysSince")){
                j--;
            }
            while(i<j && arr.get(i).getInt("daysSince")<=midValue.getInt("daysSince")){
                i++;
            }
            if(i<j){
                AVObject temp = arr.get(i);
                arr.set(i, arr.get(j));
                arr.set(j, temp);
            }
            else{
                AVObject temp = arr.get(low);
                arr.set(low, arr.get(i));
                arr.set(i, temp);
                break;
            }
        }
        QuickSortDate(arr, low, i-1);
        QuickSortDate(arr, i+1, high);
    }

    class AVObjectComparator implements Comparator<AVObject> {
        @Override
        public int compare(AVObject obj1, AVObject obj2){
            return obj1.getInt("daysSince") - obj2.getInt("daysSince");
        }

    }

    public void openWebView(String account, String password) throws InterruptedException{

        //if not connected, then re-login directly
        if(!testInternetConnection(context)){
            login(true);
            return;
        }

        final String account_ = account;
        final String password_ = password;
        final WebView webView = new WebView(context.getApplicationContext());

        webView.getSettings().setJavaScriptEnabled(true);


        webView.loadUrl("https://power.this.edu.cn/public/home.html");
        Log.d("HTML", "loaded url");

        webView.setWebViewClient(new WebViewClient() {

            public void onReceivedError(WebView view, int errorCode, String description, String failingURL){
                super.onReceivedError(view, errorCode, description, failingURL);
                Log.d("WebViewStuff",errorCode+description+failingURL);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
               // super.onPageFinished(view, url);
                LoginTimes++;
                if(LoginTimes>5){
                    login(true);
                    webView.destroy();
                }
                Log.d("LoginCount", Integer.toString(LoginTimes));
                Log.d("HTML", url + " onpagefinished()");
                webView.evaluateJavascript("document.getElementById('fieldAccount').value='"+account_+"'", null);
                webView.evaluateJavascript("document.getElementById('fieldPassword').value='"+password_+"'", null);
                webView.evaluateJavascript("document.getElementById('btn-enter').click();",
                        new ValueCallback<String>() {
                            public void onReceiveValue(String html_) {
                                webView.evaluateJavascript(
                                        "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                                        new ValueCallback<String>() {
                                            @Override
                                            public void onReceiveValue(String html_) {
                                                try{
                                                    String html = StringEscapeUtils.unescapeJava(html_);
                                                    Log.d("HTML", html);
                                                    writeWeeklySchedule(html);
                                                }
                                                catch(Exception e){
                                                    Log.d("HTML", "escape failed");
                                                }
                                            }
                                        });
                            }
                        });

            }
        });

    }

    public void writeWeeklySchedule(String html) throws Exception{
        HashMap<Integer, Subject[]> schedule = fetchSchedule(html);
        Log.d("HTML_OUT", "called");
        if(schedule.get(1)==null){
            Log.d("HTML_OUT", "schedule.get(1) returned null" );
            if(LoginTimes>=5){
                login(true);
            }
            return;
        }

        FileOutputStream f = context.openFileOutput("week_schedule.dat", context.MODE_PRIVATE);
        Log.d("HTML_OUT", "output found");
        PrintWriter out = new PrintWriter(f);
        for(int i = 1; i < 7; i ++) {
            Subject[] day = schedule.get(i);
            for (Subject subject : day) {
                out.write(subject.name() + "?" + subject.teacher() + "?" + subject.room() + "?");
                Log.d("HTML_OUT", subject.name() + "?" + subject.teacher() + "?" + subject.room() + "?");
            }
            out.write("\n");
        }
        out.close();
        Log.d("HTML_OUT", "output success");
        triggerRebirth(context.getApplicationContext());
    }

    public HashMap<Integer, Subject[]> readWeeklySchedule() throws Exception{
        Log.d("HTML_IN", "called");
        FileInputStream f = context.openFileInput("week_schedule.dat");
        Log.d("HTML_IN", "found");
        BufferedReader in = new BufferedReader(new InputStreamReader(f));
        Log.d("HTML_IN", "buffer on");
        HashMap<Integer, Subject[]> schedule = new HashMap<>(0);
        String line;
        int dayInCycle = 1;
        while((line = in.readLine())!=null){
            StringTokenizer tizer = new StringTokenizer(line, "?");
            Subject[] daySchedule = new Subject[8];
            for(int period = 0; period < 8; period ++){
                String name = tizer.nextToken();
                String teacher = tizer.nextToken();
                String room = tizer.nextToken();
                Subject subject = new Subject(name, teacher, room, context);
                Log.d("HTML_IN",subject.name() + "," + subject.teacher() + "," + subject.room() + ",");
                daySchedule[period] = subject;
            }
            schedule.put(dayInCycle, daySchedule);
            dayInCycle ++;
        }
        in.close();
        return schedule;
    }

    public HashMap<Integer, Subject[]> fetchSchedule(String html) throws IOException, InterruptedException {

        String pageSource = html;

        HashMap<Integer, Subject[]> schedule = new HashMap<Integer, Subject[]>(0);

        int inx = 0;
        String afterLastCcid = pageSource;

        Log.d("HTML", "parsing...\n");
        Log.d("HTML", new Integer(pageSource.indexOf("ccid")).toString());
        while( (inx = afterLastCcid.indexOf("ccid")) != -1 ) {

            afterLastCcid = afterLastCcid.substring(inx+4);

            //extract period info
            int inxOfTd = afterLastCcid.indexOf("<td>");
            int inxOfEndTd = afterLastCcid.indexOf("</td>");
            String periodSeq = afterLastCcid.substring(inxOfTd+4, inxOfEndTd);

            //extract class info
            int inxStart = afterLastCcid.indexOf("<td");
            for(int i = 0; i < 15; i ++)
                inxStart += afterLastCcid.substring(inxStart+1).indexOf("<td") + 1;
            int inxOfQuote = afterLastCcid.substring(inxStart).indexOf(";")+inxStart;
            String className = afterLastCcid.substring(inxStart + 17, inxOfQuote-5);
            Log.d("BUH", className);
            int InxOfAnd;
            if((InxOfAnd = className.indexOf("&")) != -1)
                className = className.substring(0, InxOfAnd) + className.substring(InxOfAnd+6);

            //extract teacher info
            int inxOfDetails = afterLastCcid.indexOf("Details about");
            int inxOfClsBtn = afterLastCcid.indexOf("class=\"button mini");
            String teacherName = afterLastCcid.substring(inxOfDetails + 14, inxOfClsBtn - 2);

            //extract room info
            int inxOfRm = afterLastCcid.indexOf("Rm:");
            String roomNum = afterLastCcid.substring(inxOfRm + 3, inxOfRm + 8);

            //System.out.println(periodSeq + " " + className + " " + teacherName + " " + roomNum);

            //parse period
            //Log.d("HTML", "still parsing1");

            String periodInfo = periodSeq;
            while(true) {
                //Log.d("HTML", "still parsing2");
                String days = periodInfo.substring(periodInfo.indexOf("(")+1, periodInfo.indexOf(")"));
                Log.w("HTML", days);
                for(int i = 0; i * 2 < days.length(); i ++) {
                    int dayNum = days.charAt(i*2) - 48;
                    Subject period = new Subject(className, teacherName, roomNum, context);

                    int pN, pC, pNe, pCe;
                    try {
                        pN = Integer.parseInt(periodInfo.substring(0, 1));
                        pC = periodInfo.substring(1, 2).equals("A") ? 0 : 1;
                        pNe = Integer.parseInt(periodInfo.substring(3, 4));
                        pCe = periodInfo.substring(4, 5).equals("A") ? 0 : 1;
                    }
                    catch(NumberFormatException e) {
                        break;
                    }

                    if(!schedule.containsKey(new Integer(dayNum))){
                        schedule.put(new Integer(dayNum), new Subject[8]);
                    }
                    schedule.get(new Integer(dayNum))[(pN-1)*2 + pC] = period;
                    schedule.get(new Integer(dayNum))[(pNe-1)*2 + pCe] = period;
                }

                int endInx = periodInfo.indexOf(")");
                //Log.w("HTML", new Integer(endInx).toString());
                try {
                    periodInfo = periodInfo.substring(endInx + 2);
                }
                catch(StringIndexOutOfBoundsException e) {
                    break;
                }
            }

        }
        Log.d("HTML", "done parsing");
        return schedule;
    }

    public static void triggerRebirth(Context context) {
        Intent mStartActivity = new Intent(context, MainActivity.class);
        Log.d("EASTER", "how long will this go on...");
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }
}
