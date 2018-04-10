package hackthis.everythingthis;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.LogUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by Alan Tao on 2018/3/20.
 */

public class ScheduleBlock extends LinearLayout {

    public int scheduleHeight, scheduleWidth;
    Context context;
    //Data
    private Calendar browsingTime;
    private Date currentTime;
    private int selectedDate;
    private HashMap<String, Integer> themeColorTable;
    private HashMap<Integer, Subject[]> subjectTable;
    private int screenWidth, screenHeight;
    private boolean leftArrowEnabled, rightArrowEnabled;

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

    public boolean isSet;

    TextView month;

    //ending time of periods
    int p1, p2, p3, p4;

    //month names
    private final String monthNames[] = {"January", "February", "March","April","May","June","July","August","September","October","November","December"};
    //maximum month dates
    private final int maxDate365[] = {31,28,31,30,31,30,31,31,30,31,30,31};
    private final int maxDate366[] = {31,29,31,30,31,30,31,31,30,31,30,31};

    //preferences
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;


    //Dimensions
    private int DateViewCommonWidth;

    public ScheduleBlock(Context context, int height, int width, SharedPreferences PREFERENCES, SharedPreferences.Editor EDITOR){
        super(context);

        this.context = context;

        editor = EDITOR;
        preferences = PREFERENCES;

        //params
        screenHeight = height;
        screenWidth = width;
        scheduleWidth = screenWidth;
        scheduleHeight = (int)(0.85 * screenHeight);
        leftArrowEnabled = true;
        rightArrowEnabled = true;


        LinearLayout.LayoutParams scheduleParams = new LayoutParams(scheduleWidth, scheduleHeight);
        this.setLayoutParams(scheduleParams);
        this.setOrientation(VERTICAL);

        //setPeriodTimes base on day of week
        currentTime=getTime();
        browsingTime = Calendar.getInstance();
        browsingTime.setTime(currentTime);
        setPeriodTimes();
        selectedDate = currentTime.getDate();

        //initialize header
        header = new LinearLayout(context);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ((int)(0.13*screenHeight)));
        header.setLayoutParams(headerParams);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER);

        this.addView(header);

        //initialize fetch button
        fetch = new ImageView(context);
        fetch.setImageResource(R.drawable.fetch_enabled);
        LinearLayout.LayoutParams fetchParams = new LinearLayout.LayoutParams(((int)(0.15*screenWidth)), ViewGroup.LayoutParams.WRAP_CONTENT);
        fetch.setPadding((int)(0.04*screenWidth),0,(int)(0.04*screenWidth),0);
        fetch.setLayoutParams(fetchParams);

        fetch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: insert fetching stuff here
            }
        });

        header.addView(fetch);

        //initialize month textview
        month = new TextView(context);

        header.addView( month );

        LinearLayout.LayoutParams monthParams = new LayoutParams((int)(0.70*screenWidth), ((int)(0.13*screenHeight)));
        month.setPadding(0,0,0,0);
        month.setLayoutParams(monthParams);
        month.setGravity(Gravity.CENTER);
        month.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        month.setTextColor(getResources().getColor(R.color.purple));

        month.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int)(month.getLayoutParams().height * 0.4));


        //initialize arrows
        leftArrow = new ImageView(context);
        rightArrow = new ImageView(context);
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams((int)(0.075*screenWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
        leftArrow.setLayoutParams(arrowParams);
        rightArrow.setLayoutParams(arrowParams);
        leftArrow.setPadding(10+(int)(0.01*screenWidth),0,5+(int)(0.01*screenWidth),0);
        rightArrow.setPadding(5+(int)(0.01*screenWidth),0,10+(int)(0.01*screenWidth),0);
        leftArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(leftArrowEnabled){
                    browsingTime.set(Calendar.MONTH, browsingTime.get(Calendar.MONTH)-1);
                }
                updatePage();
            }
        });

        rightArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rightArrowEnabled){
                    browsingTime.set(Calendar.MONTH, browsingTime.get(Calendar.MONTH)+1);
                }
                updatePage();
            }
        });

        header.addView(leftArrow);
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
        bodyBlock = new LinearLayout(context);
        LinearLayout.LayoutParams bodyBlockParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ((int)(0.62*screenHeight)));
        //bodyBlockParams.setMargins(0, (int)(0.03*screenHeight),0,0);
        bodyBlock.setLayoutParams(bodyBlockParams);
        bodyBlock.setOrientation(VERTICAL);
        bodyBlock.setBackgroundColor(getResources().getColor(R.color.shaded_background));
        this.addView(bodyBlock);

        //download file from database
        //To be written

        //input data
        //readFile();       //pre-written method for reading downloaded schedule
        readDemoFile();     //for demo purposes

        //initializing date selection bar
        //This will trigger a chain of events

        //see OnSelection()
        updatePage();
    }

    public void updatePage(){

        Log.i("calendar",browsingTime.get(Calendar.YEAR)+" "+browsingTime.get(Calendar.MONTH)+" "+
                browsingTime.get(Calendar.DATE)+" "+browsingTime.get(Calendar.DAY_OF_WEEK));

        //if current month is after july, then it must be the starting year of a new school year, therefore available months are july to december
        int minMonth, maxMonth;
        leftArrowEnabled = true;
        rightArrowEnabled = true;

        if(browsingTime.get(Calendar.MONTH) >= 6){
            minMonth = 6;
            maxMonth = 11;

            if(browsingTime.get(Calendar.MONTH) > maxMonth){
                browsingTime.set(Calendar.YEAR, (browsingTime.get(Calendar.YEAR)+1));
                browsingTime.set(Calendar.MONTH, Calendar.JANUARY);
            }
            else if(browsingTime.get(Calendar.MONTH)<=minMonth){
                browsingTime.set(Calendar.MONTH, Calendar.JULY);
                leftArrowEnabled = false;
            }
        }
        else{
            //second year in a school year, available months 1 ~ 6
            minMonth = 0;
            maxMonth = 5;

            if(browsingTime.get(Calendar.MONTH) >= maxMonth){
                browsingTime.set(Calendar.MONTH, Calendar.JUNE);
                rightArrowEnabled= false;
            }
            else if(browsingTime.get(Calendar.MONTH)<minMonth){
                browsingTime.set(Calendar.YEAR, browsingTime.get(Calendar.YEAR-1));
                browsingTime.set(Calendar.MONTH, Calendar.DECEMBER);
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

        if(is365()){
            if(selectedDate > maxDate365[browsingTime.get(Calendar.MONTH)]){
                selectedDate = maxDate365[browsingTime.get(Calendar.MONTH)];
            }
        }
        else{
            if(selectedDate > maxDate366[browsingTime.get(Calendar.MONTH)]){
                selectedDate = maxDate366[browsingTime.get(Calendar.MONTH)];
            }
        }

        initializeDateBar();

    }

    public void onLayout(boolean changed, int l, int t, int r, int b){
        super.onLayout(changed, l, t, r, b);
        if(isSet) {
            Log.d("calendar","onLayout function toggles" + selectedDate);
            dateList.get(selectedDate - 1).toggle();
            isSet = false;
        }
    }

    public void readDemoFile(){
        subjectTable = new HashMap<>(0);
        Subject chinese = new Subject("Chinese", "Yongkuan Zhang", "N404");
        Subject studyHall = new Subject("Study Hall", "", "");
        Subject calc = new Subject("AP Calculus BC","Qin Jing","N306");
        Subject lang = new Subject("AP Lang", "James Cusack", "N408");
        Subject history = new Subject("US History", "Andrew Dickerson", "N405");
        Subject foda = new Subject("Foundations of Digital Art", "Angelito Balboa", "N 401");
        Subject french = new Subject("French III", "Lisbeth Stammerjohann", "Library");
        Subject physics = new Subject("AP Physics II", "Xiaobin Xu", "N315");
        subjectTable.put(1, new Subject[]{chinese, studyHall, calc, lang});
        subjectTable.put(2, new Subject[]{history, foda, calc, physics});
        subjectTable.put(3, new Subject[]{chinese, physics, lang, french});
        subjectTable.put(4, new Subject[]{calc, lang, history, physics});
        subjectTable.put(5, new Subject[]{chinese, foda, lang, french});
        subjectTable.put(6, new Subject[]{history, physics, calc, studyHall});
        //colors
        themeColorTable = new HashMap<>(0);
        themeColorTable.put("AP Physics II", this.getResources().getColor(R.color.orange));
        themeColorTable.put("AP Calculus BC", this.getResources().getColor(R.color.algebra));
        themeColorTable.put("US History", this.getResources().getColor(R.color.blue));
        themeColorTable.put("AP Lang", this.getResources().getColor(R.color.english));
    }

    public void readFile() throws IOException {
        /*
        * Assuming that input streams from the database are in the following format:
        * (integer date) (String class1),(String class2),(String class3)...
        * for example
        * 23,AP physics,Mr.X,R311,History,Mr.Someone,Mars,English,Somebody,Room101,Advanced_Acting,Mr.Allen,BlackBox Theatre
        * Note that spaces are allowed
        * */
        subjectTable = new HashMap<>(0);
        FileInputStream in = null;

        context.openFileInput(FILENAME);

        BufferedReader reader = new BufferedReader( new InputStreamReader( in, "UTF-8" ) );
        String line;
        while( (line = reader.readLine()) != null ){
            StringTokenizer tokenizer = new StringTokenizer( line );
            int date = Integer.parseInt( tokenizer.nextToken() );
            Subject[] subjects = new Subject[tokenizer.countTokens()/3];
            for( int i = 0; i < tokenizer.countTokens()/3; i ++ ) {
                Subject subject = new Subject(
                        tokenizer.nextToken(),
                        tokenizer.nextToken(),
                        tokenizer.nextToken());
                subjects[i] = subject;
            }
            subjectTable.put( date, subjects );
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
            if(thisYear % 400 == 0)
                return true;
            else if(thisYear % 100 == 0)
                return false;
            return true;
        }
        return false;
    }

    public void onSelection(){
        //clears the existing contents in the body
        bodyBlock.removeAllViews();

        //updates the colors and text to match the selected date
        Subject[] subjects = subjectTable.get(selectedDate);
        currentTime = getTime();
        int hm = currentTime.getHours()*100+currentTime.getMinutes();

        Date tmr = new Date();
        tmr.setTime(currentTime.getTime()+24L*60L*60L*1000L);

        //if the calendar does not yet exist
        if( subjects == null ) {
            bodyBlock.setGravity(Gravity.CENTER);
            ImageView restImage = new ImageView(context);

            GregorianCalendar temp = new GregorianCalendar(browsingTime.get(Calendar.YEAR), browsingTime.get(Calendar.MONTH), browsingTime.get(Calendar.DATE));
            int fuckCalendarsWhyTheFuckDoesntWeekDayChangeAutomatically = temp.get(GregorianCalendar.DAY_OF_WEEK);

            TextView errorMessage = new TextView(context);

            errorMessage.setTextSize((float)(screenWidth > screenHeight ? screenWidth/50.0 : screenHeight/50.0));
            errorMessage.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);

            if(fuckCalendarsWhyTheFuckDoesntWeekDayChangeAutomatically == GregorianCalendar.SUNDAY
                    || fuckCalendarsWhyTheFuckDoesntWeekDayChangeAutomatically == GregorianCalendar.SATURDAY) {
                restImage.setImageResource(R.drawable.weekend);
                errorMessage.setText("ENJOY YOUR WEEKEND");
            }
            else {
                errorMessage.setText("ENJOY YOUR DAY OFF");
                restImage.setImageResource(R.drawable.vacation);
            }
            restImage.setLayoutParams(new LinearLayout.LayoutParams((int)(0.6*screenWidth),(int)(0.6*screenWidth)));
            bodyBlock.addView(restImage);


            bodyBlock.addView(errorMessage);
        }
        else {
            bodyBlock.setGravity(Gravity.TOP);
            courseBlock[] blocks = new courseBlock[4];
            boolean[] isMain = new boolean[4];
            if (selectedDate == currentTime.getDate() && hm < p4) {
                if (hm < p1) {
                    Log.i("Demo", "period one1 is next");
                    isMain[0] = true;
                } else if (hm < p2) {
                    Log.i("Demo", "period 2 is next");
                    isMain[1] = true;
                } else if (hm < p3) {
                    isMain[2] = true;
                } else{
                    isMain[3] = true;
                }
            } else if (selectedDate == tmr.getDate() && tmr.getMonth() == currentTime.getMonth()) {
                Log.i("Demo", "tomorrow's period 1 is next");
                isMain[0] = true;

            } else {
                Log.i("Demo", "no next periods in today");
            }

            for(int i = 0; i < 4; i ++){
                blocks[i] = new courseBlock(context, isMain[i], subjects[i], i);
                bodyBlock.addView(blocks[i]);
            }

        }
    }

    public int getColor( String subjectName ){
        if( themeColorTable.containsKey(subjectName) )
            return themeColorTable.get(subjectName);
        else
            return Color.BLACK;
    }

    public void addDateView( int date ){
        DateView dateView = new DateView(context, date, dateSpinner);
        dateList.add( dateView );
        dateScroll.addView( dateView );
    }

    public void addInterval(){
        dateScroll.addView( new DateSeperator( context, DateViewCommonWidth ) );
    }

    //Simple immutable struct for class name & teacher & room
    public class Subject{

        private final String name;
        private final String teacher;
        private final String room;

        public Subject( String name, String teacher, String room ){
            this.name = name;
            this.teacher = teacher;
            this.room = room;
        }

        public String name(){return name;}
        public String teacher(){return teacher;}
        public String room(){return room;}
    }

    //Encapsulated class for date selection buttons
    public class DateView extends FrameLayout implements View.OnClickListener {

        private final TextView textView;
        private final Drawable background;
        private boolean selected;

        public DateView(Context context, int date, HorizontalScrollView parent ){

            super(context);
            this.selected = false;
            this.setForegroundGravity(Gravity.CENTER);
            this.setLayoutParams(new LayoutParams(DateViewCommonWidth, DateViewCommonWidth));
            this.setScaleX(1.5f);
            this.setScaleY(1.5f);
            this.setId(date);
            textView = new TextView(context);
            textView.setText(String.valueOf(date));
            textView.setTextColor(context.getResources().getColor(R.color.purple));
            textView.setGravity(Gravity.CENTER);
            textView.setTypeface(null, Typeface.BOLD);
            this.addView(textView);

            background = context.getResources().getDrawable(R.drawable.date_picker);
            background.setColorFilter(context.getResources().getColor(R.color.purple), PorterDuff.Mode.SCREEN );
            this.setBackgroundResource(R.drawable.date_picker_off);
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
                this.setBackgroundResource(R.drawable.date_picker);
                textView.setTextColor(Color.WHITE);
                scroll();
                onSelection();
            }else {
                this.setBackgroundResource(R.drawable.date_picker_off);
                textView.setTextColor(getContext().getResources().getColor(R.color.purple));
            }
        }

        public void onClick(View view){
            dateList.get(selectedDate - 1).toggle();
            toggle();
        }

    }

    public void scroll(){
        if(selectedDate<=3) {
            Log.i("calendar","rolled to beginning");
            //the first elements doesnt need scrolling
            dateSpinner.smoothScrollTo(0,0);
        }
        else {
            //the rest scrolls to the middle
            Log.i("calendar",selectedDate+" rolled");
            dateSpinner.smoothScrollTo((selectedDate - 3) * 2 * DateViewCommonWidth, 0);
        }
    }

    //Fills the intervals
    public class DateSeperator extends FrameLayout{

        public DateSeperator(Context context, int width){
            super(context);
            this.setLayoutParams(new LayoutParams(width, DateViewCommonWidth));
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

            course = new TextView(context);
            extra = new TextView(context);
            bar = new ImageView(context);
            background = new ImageView(context);
            description = new FrameLayout(context);

            isMain = b;
            course.setText(Course.name());
            extra.setText(Course.teacher()+" "+Course.room());
            this.setId(blockNumber);
            this.setOrientation(LinearLayout.HORIZONTAL);
            this.setGravity(Gravity.CENTER_HORIZONTAL);

            if(isMain){
                //set the dimensions of the frame
                LinearLayout.LayoutParams lay = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(0.42*(int)(0.6*screenHeight)));
                lay.setMargins(4, 10, 4, 4);
                this.setLayoutParams(lay);


                //set the bar image
                bar.setLayoutParams(new FrameLayout.LayoutParams(25, ViewGroup.LayoutParams.MATCH_PARENT));
                bar.setScaleType(ImageView.ScaleType.FIT_XY);
                bar.setImageResource(R.drawable.rounded_edge_short);
                bar.setColorFilter(getColor(Course.name()));


                //set the background image
                background.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
                background.setScaleType(ImageView.ScaleType.FIT_XY);
                background.setImageResource(R.drawable.physics_background);



                //set textviews
                FrameLayout.LayoutParams margin = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                margin.setMargins(2, 2, 2, 2);
                course.setLayoutParams(margin);
                course.setTextColor(Color.WHITE);
                course.setGravity(Gravity.TOP);
                if(course.getText().length()>12)
                    course.setTextSize(35.0f);
                else
                    course.setTextSize(40.0f);

                extra.setLayoutParams(margin);
                extra.setGravity(Gravity.BOTTOM);
                extra.setTextColor(Color.WHITE);
                if(extra.getText().length()>20)
                    extra.setTextSize(22.0f);
                else
                    extra.setTextSize(26.0f);
                //set inner frame
                description.setLayoutParams(new LinearLayout.LayoutParams((int)(0.8*screenWidth), LayoutParams.MATCH_PARENT));

                //add components: linear{bar, frame{name, extra, background}}
                this.addView(bar);
                this.addView(description);
                description.addView(background);
                description.addView(course);
                description.addView(extra);
            }
            else{
                //set the dimensions of the frame
                LinearLayout.LayoutParams lay = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(0.175*(int)(0.6*screenHeight)));
                lay.setMargins(4, 10, 4, 4);
                this.setLayoutParams(lay);

                //set the bar image
                bar.setLayoutParams(new LayoutParams(25,ViewGroup.LayoutParams.MATCH_PARENT));
                bar.setScaleType(ImageView.ScaleType.FIT_XY);
                bar.setImageResource(R.drawable.rounded_edge_short);
                bar.setColorFilter(getColor(Course.name()));


                //set the background image
                background.setScaleType(ImageView.ScaleType.FIT_XY);
                background.setImageResource(R.drawable.rounded_edge_short_flipped);
                background.setColorFilter(Color.WHITE);
                background.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));


                //set textviews
                FrameLayout.LayoutParams margin = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                margin.setMargins(2, 2, 2, 2);
                course.setLayoutParams(margin);
                course.setGravity(Gravity.CENTER_VERTICAL);
                course.setTextColor(getColor(Course.name()));
                if(course.getText().length()>15)
                    course.setTextSize(20.0f);
                else
                    course.setTextSize(26.0f);

                //set inner frame
                LinearLayout.LayoutParams LAY = new LinearLayout.LayoutParams((int)(0.8*screenWidth), ViewGroup.LayoutParams.MATCH_PARENT);
                description.setLayoutParams(LAY);

                //add components: linear{bar, frame{name, extra, background}}
                this.addView(bar);
                this.addView(description);
                description.addView(background);
                description.addView(course);
            }
        }

        public courseBlock(Context context, Subject Course, int blockNumber){
            super(context);

            course = new TextView(context);
            extra = new TextView(context);
            bar = new ImageView(context);
            background = new ImageView(context);
            description = new FrameLayout(context);

            course.setText(Course.name());
            extra.setText(Course.teacher()+" "+Course.room());
            this.setId(blockNumber);
            this.setOrientation(LinearLayout.HORIZONTAL);
            this.setGravity(Gravity.CENTER_HORIZONTAL);

            //set the dimensions of the frame
            LinearLayout.LayoutParams lay = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)(0.21*(int)(0.6*screenHeight)));
            lay.setMargins(4, 10, 4, 4);
            this.setLayoutParams(lay);

            //set the bar image
            bar.setLayoutParams(new LayoutParams(25,ViewGroup.LayoutParams.MATCH_PARENT));
            bar.setScaleType(ImageView.ScaleType.FIT_XY);
            bar.setImageResource(R.drawable.rounded_edge_short);
            bar.setColorFilter(getColor(Course.name()));


            //set the background image
            background.setScaleType(ImageView.ScaleType.FIT_XY);
            background.setImageResource(R.drawable.rounded_edge_short_flipped);
            background.setColorFilter(Color.WHITE);
            background.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));


            //set textviews
            FrameLayout.LayoutParams margin = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            margin.setMargins(5, 5, 5, 5);
            course.setLayoutParams(margin);
            course.setGravity(Gravity.CENTER_VERTICAL);
            course.setTextColor(getColor(Course.name()));
            if(course.getText().length()>15)
                course.setTextSize(20.0f);
            else
                course.setTextSize(26.0f);

            //set inner frame
            LinearLayout.LayoutParams LAY = new LinearLayout.LayoutParams((int)(0.8*screenWidth), ViewGroup.LayoutParams.MATCH_PARENT);
            description.setLayoutParams(LAY);

            //add components: linear{bar, frame{name, extra, background}}
            this.addView(bar);
            this.addView(description);
            description.addView(background);
            description.addView(course);
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

    public void setPeriodTimes(){
        Log.i("Demo","period times set");
        if(currentTime.getDay()==Calendar.WEDNESDAY){
            p1=855;
            p2=940;
            p3=1210;
            p4=1340;
        }
        else {
            p1 = 935;
            p2 = 1115;
            p3 = 1350;
            p4 = 1515;
        }
    }
}
