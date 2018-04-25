package hackthis.everythingthis;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class ScheduleBlock extends LinearLayout {
    Context context;
    //Data
    private Calendar browsingTime;
    private Date currentTime;
    private int selectedDate;
    private HashMap<String, Integer> themeColorTable;
    private HashMap<Integer, Subject[]> subjectTable;
    private ArrayList<String> subjectTypes;
    private ArrayList<String[]> allSubjects;
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


    //Dimensions
    private int DateViewCommonWidth;

    public ScheduleBlock(Context context, int height, int width, SharedPreferences PREFERENCES, SharedPreferences.Editor EDITOR){
        super(context);

        this.context = context;

        editor = EDITOR;
        preferences = PREFERENCES;

        subjectTypes = new ArrayList<>(30);
        allSubjects = new ArrayList<>(30);
        subjectTypes.add("art");
        allSubjects.add(new String[]{"ceramics", "easternart6", "easternart7", "easternart8", "easternarti", "easternartii", "foundationsofart", "foundationsofdigitalart", "introtosculpture", "paintingi", "paintingii", "paintingiii", "theartportfolio", "westernart6", "westernart7", "westernart8"});
        subjectTypes.add("beginningguitar");
        allSubjects.add(new String[]{"beginningguitar"});
        subjectTypes.add("biology");
        allSubjects.add(new String[]{"biology"});
        subjectTypes.add("chinese");
        allSubjects.add(new String[]{"apchinese", "chinese6a", "chinese6b", "chinese7a", "chinese7b", "chinese8a", "chinese8b", "chinese9a", "chinese9b", "chinese10a", "chinese10b", "chinese11a", "chinese11b", "chinese12a", "chinese12b"});
        subjectTypes.add("choir");
        allSubjects.add(new String[]{"choir"});
        subjectTypes.add("computer");
        allSubjects.add(new String[]{"apcomputersciencea", "desktoppublishing", "digitalphotography", "digitalvideo", "grade6computerfoundations", "grade7computerfoundations", "grade8computerfoundations", "introtocomputerscience", "mobileappdesign", "roboticsi", "roboticsii", "webdesign"});
        subjectTypes.add("filmstudies");
        allSubjects.add(new String[]{"filmstudyi", "filmstudyii"});
        subjectTypes.add("french");
        allSubjects.add(new String[]{"frenchlangi", "frenchlangii", "frenchlangiii", "frenchlangiv"});
        subjectTypes.add("highschoolenrichment");
        allSubjects.add(new String[]{"highschoolenrichment"});
        subjectTypes.add("linguistics");
        allSubjects.add(new String[]{"linguistics"});
        subjectTypes.add("math");
        allSubjects.add(new String[]{"advalgebraii", "advgeometry", "algebrai", "algebraii", "algebraiii", "algebraii/trignometry", "apcalculusab", "apcalculusbc","apstatistics", "appliedmath","calculus","geometry", "introtolinearalgebra", "math6", "math7", "precalculus"});
        subjectTypes.add("music");
        allSubjects.add(new String[]{"advband8", "band", "beginninginstrumentalmusic", "instrumentalmusicband6", "instrumentalmusicband7", "vocalmusic6", "vocalmusic7"});
        subjectTypes.add("spanish");
        allSubjects.add(new String[]{"spanishi", "spanishii", "spanishiii", "spanishiv", "bilingualtranslation"});
        subjectTypes.add("steam");
        allSubjects.add(new String[]{"steam", "steami", "steamii", "steamiii", "steamday"});
        subjectTypes.add("chemistry");
        allSubjects.add(new String[]{"apchemistry", "chemistry"});
        subjectTypes.add("economics");
        allSubjects.add(new String[]{"apmacroeconomics", "economics"});
        subjectTypes.add("english");
        allSubjects.add(new String[]{"langarts6", "langarts7", "langarts8", "englih9", "english10", "englih11", "englih12", "apenglishlanguage&composition", "apenglishliteraturecomposition"});
        subjectTypes.add("history");
        allSubjects.add(new String[]{"ancientworldhistory7", "apushistory", "apworldhistory", "arthistorymethods", "chinesehistory6", "chinesehistory7", "chinesehistory8", "chinesehistoryi", "chinesehistoryii", "Geography 6","medievalworldhistory8", "modernworldhistory", "ushistory",});
        subjectTypes.add("socialstudy");
        allSubjects.add(new String[]{"currentaffairs", "digitalethnography", "foundationsofmodernchina", "humanities", "philosophy"});
        subjectTypes.add("physics");
        allSubjects.add(new String[]{"apphysicsi", "apphysicsii", "earthandspacescience", "earthscience6", "lifescience7", "physicalscience8"});
        subjectTypes.add("piano");
        allSubjects.add(new String[]{"pianoi", "pianoii"});
        subjectTypes.add("studyhall");
        allSubjects.add(new String[]{"studyhall"});
        subjectTypes.add("theater");
        allSubjects.add(new String[]{"advacting", "classicalacting", "theater6", "theater7", "theater8"});
        subjectTypes.add("genderstudies");
        allSubjects.add(new String[]{"genderstudiesi"});
        subjectTypes.add("els");
        allSubjects.add(new String[]{"langsupport6", "langsupport7", "langsupport8", "langsupport9", "langsupport10"});
        subjectTypes.add("health");
        allSubjects.add(new String[]{"health7", "health8", "health9", "health10"});
        subjectTypes.add("fitness");
        allSubjects.add(new String[]{"advfitness"});
        subjectTypes.add("sport");
        allSubjects.add(new String[]{"outdooreducation", "pe6", "pe7", "pe8", "pe9", "sportsmanagement", "strengthtraining", "ultimatesports"});


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
        header = new LinearLayout(context);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ((int)(0.15*screenHeight)));
        header.setLayoutParams(headerParams);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER);

        this.addView(header);

        //initialize fetch button
        fetch = new ImageView(context);
        fetch.setImageResource(R.drawable.fetch);
        LinearLayout.LayoutParams fetchParams = new LinearLayout.LayoutParams(((int)(0.15*screenWidth)), ViewGroup.LayoutParams.WRAP_CONTENT);
        fetch.setPadding((int)(0.04*screenWidth),0,(int)(0.04*screenWidth),0);
        fetch.setLayoutParams(fetchParams);

        fetch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isLoggedIn) return;
                //TODO: insert fetching stuff here
            }
        });

        header.addView(fetch);

        //initialize month textview
        month = new TextView(context);

        header.addView( month );

        LinearLayout.LayoutParams monthParams = new LayoutParams((int)(0.70*screenWidth), ((int)(0.15*screenHeight)));
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
        //download file from database
        //To be written

        //input data
        //readFile();       //pre-written method for reading downloaded schedule
        readDemoFile();     //for demo purposes

        //initializing date selection bar
        //        //This will trigger a chain of events
        //
        //        //see OnSelection()
        //read stored powerschool settings

        //todo:initialize webview before here

        PSname = preferences.getString(getResources().getString(R.string.ps_name_key),null);
        PSpass = preferences.getString(getResources().getString(R.string.ps_pass_key),null);
        login();

        updatePage();

        initializeDateBar();
    }

    public void login(){
        if(testPSLogin()){
            isLoggedIn = true;
            editor.putString(getResources().getString(R.string.ps_name_key), PSname);
            editor.putString(getResources().getString(R.string.ps_pass_key), PSpass);
            editor.commit();
            fetch.setImageResource(R.drawable.fetch_enabled);
        }
        else{
            isLoggedIn = false;
            LoginScreen ls = new LoginScreen();
            bodyBlock.addView(ls);
        }
    }

    public boolean testPSLogin(){
        if((PSname==null || PSname.equals("")) || (PSpass==null || PSpass.equals(""))) {
            Log.d("Demo","logging in under mode"+false);
            return false;
        }
        else{
            //todo: replace this part to testing powerschool login with actual inputs
            Log.d("Demo","logging in under mode"+true);
            return true;
        }
    }

    public void updatePage(){

        if(!isLoggedIn){
            return;
        }

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

        if(!isLoggedIn){
            return;
        }

        //clears the existing contents in the body
        bodyBlock.removeAllViews();

        Log.d("Demo","starting onSelection");

        //updates the colors and text to match the selected date
        Subject[] subjects = subjectTable.get(selectedDate);
        //TODO: change this to get from browsingTime
        Log.d("Demo","getting subjects from "+selectedDate);

        ArrayList<Subject> subjectsTrimmed = new ArrayList<>(8);
        ArrayList<Integer> periodsTrimmed = new ArrayList<>(8);
        if(subjects!=null) {
            Log.d("Demo","got classes");
            for (int i = 0; i < subjects.length; i++) {
                if(subjects[i]==null){

                }
                else{
                    if(i!=subjects.length-1) {
                        if (subjects[i] == subjects[i + 1]) {

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

        Log.d("Demo",subjectsTrimmed.toString()+"\n"+periodsTrimmed.toString());

        currentTime = getTime();
        GregorianCalendar todayTemp = new GregorianCalendar();
        todayTemp.setTime(currentTime);
        int hm = currentTime.getHours()*100+currentTime.getMinutes();


        Date tmr = new Date();
        tmr.setTime(currentTime.getTime()+24L*60L*60L*1000L);
        GregorianCalendar tmrTemp = new GregorianCalendar();
        tmrTemp.setTime(tmr);
        Log.d("Demo","tmr:"+tmrTemp.get(Calendar.YEAR)+ tmrTemp.get(Calendar.MONTH)+
                tmrTemp.get(Calendar.DATE));

        GregorianCalendar temp = new GregorianCalendar(browsingTime.get(Calendar.YEAR), browsingTime.get(Calendar.MONTH), browsingTime.get(Calendar.DATE));
        int fuckCalendarsWhyTheFuckDoesntWeekDayChangeAutomatically = temp.get(GregorianCalendar.DAY_OF_WEEK);
        setPeriodTimes(new GregorianCalendar(browsingTime.get(Calendar.YEAR), browsingTime.get(Calendar.MONTH),
                browsingTime.get(Calendar.DATE)));

        Log.d("Demo","today:"+browsingTime.get(Calendar.YEAR)+ browsingTime.get(Calendar.MONTH)+
                browsingTime.get(Calendar.DATE));


        //if the calendar does not yet exist
        if( subjectsTrimmed.isEmpty() || subjects == null) {
            Log.d("Demo","empty day");
            ImageView restImage = new ImageView(context);
            restImage.setPadding(20,40,20,0);

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

            restImage.setLayoutParams(new LinearLayout.LayoutParams((int)(0.7*screenWidth),(int)(0.7*screenWidth)));
            bodyBlock.addView(restImage);
            bodyBlock.addView(errorMessage);
        }
        else {
            Log.d("Demo","school day");
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
                    }
                    else{
                        isMain[i] = false;
                    }
                }
            } else if (tmrTemp.get(GregorianCalendar.MONTH) == temp.get(GregorianCalendar.MONTH)
                    && tmrTemp.get(GregorianCalendar.DATE) == temp.get(GregorianCalendar.DATE)) {
                Log.i("Demo", "tomorrow is selected");
                if(hm > periodTimes[7]) {
                    isMain[0] = true;
                }

            } else {
                Log.i("Demo", "no next periods in today");
            }



            for(int i = 0; i < subjectsTrimmed.size(); i ++){
                blocks[i] = new courseBlock(context, isMain[i], subjectsTrimmed.get(i), i);
                bodyBlock.addView(blocks[i]);
            }
        }
        Log.d("Demo","ending onSelection");
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

        public Subject( String name, String teacher, String room){
            this.name = name;
            this.teacher = teacher;
            this.room = room;
        }

        public String name(){return name;}
        public String teacher(){return teacher;}
        public String room(){return room;}

        public boolean equals(Object obj){
            Subject temp = (Subject)obj;
            if(temp.name().equals(this.name)){
                return true;
            }
            return false;
        }

        public String toString(){return name;}
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
            this.setLayoutParams(new LinearLayout.LayoutParams(DateViewCommonWidth, DateViewCommonWidth));
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
                if(isLoggedIn)
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


            Log.d("Demo", "initializing subject "+blockNumber+" "+b);

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
            extra.setText(Course.teacher());

            if(isMain){
                //set the dimensions of the frame
                LinearLayout.LayoutParams lay = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(0.42*(int)(0.76 *screenHeight)));
                lay.setMargins(4, 10, 4, 4);
                this.setLayoutParams(lay);
                //this.setBackground(getResources().getDrawable(R.drawable.button_background));

                //set the bar image
                bar.setLayoutParams(new LinearLayout.LayoutParams(25,ViewGroup.LayoutParams.MATCH_PARENT));
                bar.setScaleType(ImageView.ScaleType.FIT_XY);
                bar.setImageResource(R.drawable.rounded_edge_short);
                bar.setColorFilter(Color.WHITE);

                //set inner frame
                description.setLayoutParams(new LinearLayout.LayoutParams((int)(0.8*screenWidth), ViewGroup.LayoutParams.MATCH_PARENT));

                //set the background image
                background.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
                background.setScaleType(ImageView.ScaleType.FIT_XY);
                background.setImageResource(findDrawableWithString(Course.name));
                background.setBackgroundColor(getResources().getColor(R.color.white));

                description.setBackground(getResources().getDrawable(R.drawable.button_background));

                //set textviews
                FrameLayout.LayoutParams margin = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                margin.setMargins(2, 2, 2, 2);
                course.setLayoutParams(margin);
                course.setTextColor(Color.BLACK);
                course.setGravity(Gravity.TOP);
                if(course.getText().length()>12)
                    course.setTextSize(40.0f);
                else
                    course.setTextSize(45.0f);

                extra.setLayoutParams(margin);
                extra.setGravity(Gravity.BOTTOM);
                extra.setTextColor(Color.BLACK);
                if(extra.getText().length()>20)
                    extra.setTextSize(24.0f);
                else
                    extra.setTextSize(30.0f);

                //add components: linear{bar, frame{name, extra, background}}
                description.addView(background);
                description.addView(course);
                description.addView(extra);

                this.addView(bar);
                this.addView(description);
            }
            else{
                //set the dimensions of the frame
                LinearLayout.LayoutParams lay = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(0.175*(int)(0.76*screenHeight)));
                lay.setMargins(4, 10, 4, 4);
                this.setLayoutParams(lay);

                //set inner frame
                LinearLayout.LayoutParams LAY = new LinearLayout.LayoutParams((int)(0.8*screenWidth), ViewGroup.LayoutParams.MATCH_PARENT);
                description.setLayoutParams(LAY);

                //set the bar image
                bar.setLayoutParams(new LinearLayout.LayoutParams(25,ViewGroup.LayoutParams.MATCH_PARENT));
                bar.setScaleType(ImageView.ScaleType.FIT_XY);
                bar.setImageResource(R.drawable.rounded_edge_short);
                bar.setColorFilter(getColor(Course.name()));


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
                course.setTextColor(getColor(Course.name()));
                if(course.getText().length()>15)
                    course.setTextSize(20.0f);
                else
                    course.setTextSize(26.0f);
                description.addView(background);
                description.addView(course);


                this.addView(bar);

                this.addView(description);
            }

        }
    }

    //tries to get time from internet, if fails then set time as system time
    public static Date getTime() {
        Date date = new Date();

        if(testInternetConnection()) {
            String URL1 = "http://www.baidu.com";
            String URL2 = "http://www.ntsc.ac.cn";
            String URL3 = "http://www.beijing-time.org";
            Log.i("Demo", "current time source :");
            try {
                if (getWebDate(URL1) != null) {
                    Log.i("Demo", URL1);
                    date = getWebDate(URL1);
                } else if (getWebDate(URL2) != null) {
                    Log.i("Demo", URL2);
                    date = getWebDate(URL2);
                } else if (getWebDate(URL3) != null) {
                    Log.i("Demo", URL3);
                    date = getWebDate(URL3);
                } else {
                    Log.i("Demo", "System");
                }
            } catch (Exception e) {

            }
        }
        return date;
    }

    public class LoginScreen extends LinearLayout{
        public EditText nameText, passwordText;
        public TextView draftHint;
        public Button button1;
        public LinearLayout buttonBox;
        public CheckBox remember;
        public LoginScreen(){
            super(context);
            this.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, (int)(0.76*screenHeight)));
            this.setPadding((int)(0.075*screenWidth),(int)(0.03*screenHeight),(int)(0.075*screenWidth),10);
            this.setOrientation(VERTICAL);
            this.setGravity(Gravity.LEFT);
            this.setBackgroundColor(getResources().getColor(R.color.powerschool));

            LinearLayout iconBox = new LinearLayout(context);
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

            draftHint = new TextView(context);
            draftHint.setLayoutParams(textParams);
            draftHint.setTextColor(getResources().getColor(R.color.white));
            this.addView(draftHint);

            nameText = new EditText(context);
            nameText.setHint("ID");
            nameText.setLayoutParams(textParams);
            nameText.setTextColor(getResources().getColor(R.color.white));
            nameText.setBackgroundColor(getResources().getColor(R.color.powerschool_shaded));
            nameText.setPadding(8,8,8,8);
            this.addView(nameText);

            passwordText = new EditText(context);
            passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordText.setTextColor(getResources().getColor(R.color.white));
            passwordText.setHint("Password");
            passwordText.setLayoutParams(textParams);
            passwordText.setBackgroundColor(getResources().getColor(R.color.powerschool_shaded));
            passwordText.setPadding(8,8,8,8);
            this.addView(passwordText);

            LinearLayout rememberBox = new LinearLayout(context);
            rememberBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            remember = new CheckBox(context);
            remember.setChecked(false);
            remember.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            rememberBox.addView(remember);

            TextView rememberText = new TextView(context);
            rememberText.setText("remember my login informations");
            rememberText.setPadding(0,0,0,10);
            rememberBox.addView(rememberText);

            this.addView(rememberBox);

            buttonBox = new LinearLayout(context);
            buttonBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
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
            button1.setText("log in");

            button1.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    PSname = nameText.getText().toString();
                    PSpass = passwordText.getText().toString();
                    Log.d("Demo","logging in with informations: name("+PSname+") pass("+PSpass+")");
                    if(testPSLogin()){
                        if(remember.isChecked()){
                            editor.putString(getResources().getString(R.string.ps_name_key),
                                    nameText.getText().toString());
                            editor.putString(getResources().getString(R.string.ps_pass_key),
                                    passwordText.getText().toString());
                            editor.commit();
                        }
                        else{
                            editor.putString(getResources().getString(R.string.ps_name_key),
                                    null);
                            editor.putString(getResources().getString(R.string.ps_pass_key),
                                    null);
                            editor.commit();
                        }
                        fetch.setImageResource(R.drawable.fetch_enabled);
                        isLoggedIn = true;
                        updatePage();
                        bodyBlock.removeAllViews();
                        onSelection();

                    }
                    else{
                        draftHint.setText("Wrong ID or password, please re-enter!");
                        nameText.setText("");
                        passwordText.setText("");
                        remember.setChecked(false);
                    }
                }
            });
        }

    }

    public static boolean testInternetConnection(){
        try
        {
            Log.d("announcement","try to connect");
            URL url = new URL("http://www.baidu.com");
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(500);
            connection.connect();
        }catch (Exception e){
            Log.d("announcement","failed to connect");
            return false;
        }

        return true;
    }

    //get time from internet with given url
    private static Date getWebDate(String url) {
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
            connecter.setConnectTimeout(500);
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

    public void setPeriodTimes(GregorianCalendar date){
        periodTimes = new int[8];
        if(date.get(GregorianCalendar.DAY_OF_WEEK)==Calendar.WEDNESDAY){
            periodTimes = new int[]{835, 855, 920, 940, 1120, 1210, 1320, 1340};
        }
        else {
            periodTimes = new int[]{855, 935, 1035, 1115, 1205, 1350, 1435, 1515};
        }
    }

    public int findDrawableWithString(String source){
        source = source.toLowerCase();
        source = trimName(source);
        Log.d("coursefind",source);
        int id = context.getResources().getIdentifier("course_"+binSearchSubjectType(source), "drawable", context.getPackageName());
        return id;
    }

    public static String trimName(String str){
        ArrayList<Character> temp = new ArrayList<>(20);
        char[] charStr = str.toCharArray();
        for(int i = 0; i < charStr.length; i++){
            int ascii = (int)charStr[i];
            if((ascii <= 90 && ascii >=65) || (ascii <= 122 && ascii >= 97) || (ascii <= 57 && ascii >= 48)){
                temp.add(charStr[i]);
            }
        }
        char[] charTemp = new char[temp.size()];
        for(int i = 0; i < temp.size(); i++){
            charTemp[i] = temp.get(i);
        }
        return new String(charTemp);
    }

    public String binSearchSubjectType(String name){
        for(int i = 0; i < allSubjects.size(); i++){
            int low = 0, mid, high = allSubjects.get(i).length-1;

            Log.d("coursefind","current course length "+allSubjects.get(i).length);

            while(low<=high){
                mid = (low + high)/2;
                if(subjectTypes.get(i).equals("history")){
                    Log.d("coursefind","current mid on "+allSubjects.get(i)[mid]);
                }
                if(allSubjects.get(i)[mid].equals(name)){
                    Log.d("coursefind","returned with type"+subjectTypes.get(i));
                    return subjectTypes.get(i);
                }
                else if(name.compareTo(allSubjects.get(i)[mid])<0){
                    high = mid-1;
                }
                else{
                    low = mid + 1;
                }
            }
        }
        Log.d("coursefind","returned with type none");
        return "none";
    }
}
