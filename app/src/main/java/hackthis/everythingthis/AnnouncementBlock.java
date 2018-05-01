package hackthis.everythingthis;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;

import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AnnouncementBlock extends LinearLayout{
    //status vars
    Context context;

    boolean internetConnected = true;

    public ArrayList<Announcement> announcements = new ArrayList<>();

    public ArrayList<String> defaultSubscribes = new ArrayList<>(10);

    public ArrayList<Club> clubs = new ArrayList<>(50);

    public ArrayList <String> subscribed;
    public String searchKey="";
    public String clubSearchKey= "";

    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;

    //UI vars
    LinearLayout.LayoutParams bodyParams;
    public float STANDARD_TEXT_SIZE;
    public int bodyWidth, bodyHeight;
    public boolean filterShown = false;

    //main frame this (linear layout)

    //toolbar layout
    SearchBar searchBar;

    //body
    ScrollView bodyLayout;
        //inside body:
        AnnouncementListLayout list;
            //inside list
            LinearLayout.LayoutParams contentLayout;
        AnnouncementFilterLayout filter;
            //inside filter
            LinearLayout.LayoutParams optionCellLayout;
    //leanCloud
    AVQuery<AVObject> clubQuery;
    AVQuery<AVObject> announcementQuery;


    public AnnouncementBlock(Context viewContext, LayoutParams body, SharedPreferences PREFERENCES, SharedPreferences.Editor EDITOR){
        super(viewContext);
        context = viewContext;

        defaultSubscribes.add("Student Council");

        editor = EDITOR;
        preferences = PREFERENCES;

        bodyParams = body;
        bodyHeight = body.height;
        bodyWidth = body.width;

        contentLayout = new LinearLayout.LayoutParams((int)(0.9 * bodyWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLayout.setMargins(0,8,0,8);

        optionCellLayout = new LinearLayout.LayoutParams((int)(0.75 * bodyWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
        optionCellLayout.setMargins(0,8,0,8);

        if(bodyWidth <= 1080){
            STANDARD_TEXT_SIZE = 12.0f;
        }
        else{
            STANDARD_TEXT_SIZE = 14.0f;
        }

        this.setLayoutParams(bodyParams);
        this.setOrientation(VERTICAL);
        this.setGravity(Gravity.TOP);

        subscribed = new ArrayList<>(preferences.getStringSet(getResources().getString(R.string.subscribed_channels_key),
                new HashSet<>(defaultSubscribes)));

        editor.putStringSet(getResources().getString(R.string.subscribed_channels_key),
                new HashSet<>(subscribed));

        editor.commit();

        searchBar = new SearchBar();
        this.addView(searchBar);


        bodyLayout = new ScrollView(context);
        bodyLayout.setLayoutParams(generateLinearParams(1, 0.87));
        bodyLayout.setBackgroundColor(getResources().getColor(R.color.background));
        bodyLayout.setHorizontalScrollBarEnabled(false);
        bodyLayout.setVerticalScrollBarEnabled(true);

        list = new AnnouncementListLayout();
        bodyLayout.addView(list);

        this.addView(bodyLayout);


        loadAnnouncements();
    }

    public void loadAnnouncements(){

        /*if(filterShown)
            return;*/

        pullFromDatabase();

        list.removeAllViews();

        if(!internetConnected){
            ErrorButton eb = new ErrorButton();
            list.addView(eb);
            return;
        }

        ArrayList<Announcement> filteredAnnouncement = new ArrayList<>(50);

        for(int i = 0; i < announcements.size(); i++){
            for(int j = 0; j < subscribed.size(); j++){
                if(announcements.get(i).getClub().getName().toLowerCase().equals(subscribed.get(j).toLowerCase())){
                    if((searchKey!=null && hasRelatedContent(announcements.get(i))) || searchKey==null)
                        filteredAnnouncement.add(announcements.get(i));
                    break;
                }
            }
        }

        Contents[] contents= new Contents[filteredAnnouncement.size()];
        for(int i = 0; i < filteredAnnouncement.size(); i++) {
            contents[i] = new Contents(filteredAnnouncement.get(i));
        }

        contents = QSContentHelper(contents);

        for(Contents i : contents){
            list.addView(i);
        }
    }

    public boolean hasRelatedContent (Announcement a){
        if(a.getClub().getName().toLowerCase().contains(searchKey.toLowerCase()))
            return true;
        if(a.getAnnouncement().toLowerCase().contains(searchKey.toLowerCase()))
            return true;
        return a.title.toLowerCase().contains(searchKey.toLowerCase());
    }

    public Contents[] QSContentHelper(Contents[] arr){
        QuickSortContents(arr, 0, arr.length-1);
        return arr;
    }

    public void QuickSortContents(Contents[] arr, int low, int high){
        for(int k = low; k <= high; k++){
        }
        if(arr==null || high-low <1 || high<=low){
            return;
        }
        Contents midValue = arr[low];
        int i = low, j = high;
        while(true){
            while(i<j && arr[j].content.getPostTime().getTime()
                    <=midValue.content.getPostTime().getTime()){
                j--;
            }
            while(i<j && arr[i].content.getPostTime().getTime()
                    >=midValue.content.getPostTime().getTime()){
                i++;
            }
            if(i<j){
                Contents temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
            else{
                Contents temp = arr[low];
                arr[low] = arr[i];
                arr[i] = temp;
                break;
            }
        }
        QuickSortContents(arr, low, i-1);
        QuickSortContents(arr, i+1, high);
    }


    public void pullFromDatabase(){
        //check if there is internet connection

        try
        {
            Log.d("announcement","try to connect");
            URL url = new URL("http://www.baidu.com");

            URLConnection connection = url.openConnection();
            connection.connect();
            internetConnected = true;


        }catch (Exception e){

            Log.d("announcement","failed to connect");
            internetConnected = false;
            return;

        }



        try {
            announcements.clear();
            for(String i : subscribed) {
                announcementQuery = new AVQuery<>("Announcements");
                announcementQuery.whereContains("clubName",i);
                List<AVObject> annList = announcementQuery.find();
                for(AVObject j : annList){
                    announcements.add(new Announcement(j.getString("announcementTitle"),
                            j.getString("announcementTitle"), j.getDate("updatedAt"),
                            new Club(j.getString("clubName"), j.getString("key"))));
                }
            }
        }
        catch (AVException e){
            //TODO: add handler?
        }



        try {
            clubQuery = new AVQuery<>("Clubs");
            clubQuery.setLimit(1000);
            List<AVObject> clubsList = clubQuery.find();
            clubs.clear();
            for(AVObject i : clubsList){
                //TODO: add the key part
                clubs.add(new Club(i.getString("name"), i.getString("key")));
            }
        }
        catch(AVException e){
            //TODO: any handler?
        }

    }

    public class Contents extends LinearLayout implements Comparable{

        public TextView titleText, subTitle, bodyText;
        public Announcement content;

        public Contents(Announcement a){
            super(context);
            content = a;

            this.setLayoutParams(contentLayout);
            this.setPadding(8,8,8,8);
            this.setBackgroundColor(getResources().getColor(android.R.color.white));
            this.setOrientation(VERTICAL);
            this.setGravity(Gravity.CENTER_HORIZONTAL);

            titleText = new TextView(context);
            this.addView(titleText);
            subTitle = new TextView(context);
            this.addView(subTitle);
            bodyText = new TextView(context);
            this.addView(bodyText);

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams((int)(0.9*bodyWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
            textParams.setMargins(0,4,0,4);

            titleText.setText(a.title);
            subTitle.setText(a.getClub().getName() + " - " + new SimpleDateFormat("MMMMdd").format(a.getPostTime()));
            bodyText.setText(a.getAnnouncement());

            titleText.setTextSize(STANDARD_TEXT_SIZE + 5.0f);
            //Typeface titleFace = Typeface.create(Typeface.SERIF, Typeface.BOLD);
            //titleText.setTypeface(titleFace);
            titleText.setLayoutParams(textParams);
            titleText.setTextColor(getResources().getColor(R.color.black));
            titleText.setGravity(Gravity.CENTER_HORIZONTAL);

            subTitle.setTextSize(STANDARD_TEXT_SIZE);
            subTitle.setTextColor(getResources().getColor(R.color.purple));
            subTitle.setLayoutParams(textParams);
            subTitle.setGravity(Gravity.CENTER_HORIZONTAL);
            bodyText.setTextSize(STANDARD_TEXT_SIZE);
            bodyText.setTextColor(getResources().getColor(R.color.black));
            bodyText.setLayoutParams(textParams);
            bodyText.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        public int compareTo(Object obj){
            long temp = ((Contents)obj).content.getPostTime().getTime();
            long thisTemp = this.content.getPostTime().getTime();

            if(thisTemp > temp){
                return 1;
            }
            else if (thisTemp == temp){
                return 0;
            }
            else{
                return -1;
            }
        }
    }

    public class AnnouncementListLayout extends LinearLayout{
        public AnnouncementListLayout(){
            super(context);
            this.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            this.setGravity(Gravity.CENTER_HORIZONTAL);
            this.setOrientation(VERTICAL);
            this.setPadding((int)(0.05*bodyWidth),8,(int)(0.05*bodyWidth),8);
        }
    }

    public class SearchBar extends LinearLayout{

        //inside toolbar:
        ImageView searchButton;
        ImageView filterButton;
        ImageView refreshButton;
        EditText editText;

        public SearchBar(){
            super(context);
            LinearLayout.LayoutParams searchBarParams = generateLinearParams(1, 0.13);
            this.setLayoutParams(searchBarParams);
            this.setPadding(0, (int)(0.025*bodyHeight), 0,  (int)(0.025*bodyHeight));
            this.setGravity(Gravity.CENTER_HORIZONTAL);
            this.setOrientation(HORIZONTAL);
            //this.setBackgroundColor(getResources().getColor(R.color.background)); color too dark

            editText = new EditText(context);
            editText.setHint("Search Announcement");
            editText.setHintTextColor(getResources().getColor(R.color.purple));
            editText.setBackgroundColor(getResources().getColor(R.color.shaded_background));
            editText.setLayoutParams(generateLinearParams(0.5, 0.08));
            editText.setPadding(8,8,8,8);
            editText.setTextColor(getResources().getColor(R.color.black));
            TextWatcher renewKey = new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    if(!filterShown){
                        searchKey = editText.getText().toString();
                        loadAnnouncements();
                    }
                    else{
                        clubSearchKey = editText.getText().toString();
                        filter.loadCells();
                    }

                }
            };
            editText.addTextChangedListener(renewKey);

            this.addView(editText);

            searchButton = new ImageView(context);
            searchButton.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int)(0.08*bodyHeight)));
            searchButton.setImageResource(R.drawable.search_icon);
            searchButton.setBackgroundColor(getResources().getColor(R.color.shaded_background));
            searchButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!filterShown) {
                        searchKey = editText.getText().toString();
                        loadAnnouncements();
                    }
                    else{
                        clubSearchKey = editText.getText().toString();
                        filter.loadCells();
                    }
                }
            });

            this.addView(searchButton);

            refreshButton = new ImageView(context);
            refreshButton.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int)(0.08*bodyHeight)));
            refreshButton.setImageResource(R.drawable.fetch_enabled);
            refreshButton.setPadding(0,20,0,20);
            refreshButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!filterShown){
                        loadAnnouncements();
                    }
                }
            });

            this.addView(refreshButton);

            filterButton = new ImageView(context);
            filterButton.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int)(0.08*bodyHeight)));
            filterButton.setImageResource(R.drawable.filter_icon);
            filterButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    bodyLayout.removeAllViews();

                    editText.setText("");

                    filterShown = !filterShown;

                    if(filterShown){
                        filter = new AnnouncementFilterLayout();
                        bodyLayout.addView(filter);
                        editText.setHint("Search Club");
                    }
                    else{
                        bodyLayout.addView(list);
                        loadAnnouncements();
                        editText.setHint("Search Announcement");
                    }
                }
            });

            this.addView(filterButton);
        }
    }

    public class AnnouncementFilterLayout extends LinearLayout{

        TextView introText_1, introText_2;
        ArrayList<OptionCell> cells;

        public AnnouncementFilterLayout(){
            super(context);
            this.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            this.setPadding((int)(0.05*bodyWidth),10,(int)(0.05*bodyWidth),10);
            this.setOrientation(VERTICAL);
            this.setGravity(Gravity.TOP);
            //this.setBackgroundColor(getResources().getColor(R.color.white));

            LinearLayout.LayoutParams introTextParams = new LinearLayout.LayoutParams((int)(0.9*bodyWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
            introTextParams.setMargins(0,10,0,10);

            introText_1 = new TextView(context);
            introText_2 = new TextView(context);

            introText_1.setLayoutParams(introTextParams);
            introText_1.setTextSize(STANDARD_TEXT_SIZE);
            introText_1.setTextColor(AnnouncementBlock.this.getResources().getColor(R.color.black));
            introText_1.setGravity(Gravity.START);
            introText_1.setText("Select which of these school organizations to subscribe:");
            this.addView(introText_1);

            cells = new ArrayList<>(30);

            this.loadCells();

            pullFromDatabase();
        }

        public void loadCells(){

            if(!internetConnected){
                this.removeAllViews();
                ErrorButton eb = new ErrorButton();
                this.addView(eb);
                return;
            }

            for(OptionCell i : cells)
                this.removeView(i);

            cells.clear();

            for(int i = 0; i < clubs.size(); i++){
                String temp = clubs.get(i).getName();
                boolean isSelected = false;

                for(int j = 0; j < subscribed.size(); j++){
                    if(subscribed.get(j).equals(temp)){
                        isSelected = true;
                        break;
                    }
                }
                if((clubSearchKey!=null && clubs.get(i).getName().toLowerCase().contains(clubSearchKey.toLowerCase()))
                        || clubSearchKey==null){
                    if(isSelected) cells.add(new OptionCell(clubs.get(i), true));
                }
            }

            for(int i = 0; i < clubs.size(); i++){
                String temp = clubs.get(i).getName();
                boolean isSelected = false;

                for(int j = 0; j < subscribed.size(); j++){
                    if(subscribed.get(j).equals(temp)){
                        isSelected = true;
                        break;
                    }
                }

                if((clubSearchKey!=null && clubs.get(i).getName().toLowerCase().contains(clubSearchKey.toLowerCase()))
                        || clubSearchKey==null){
                    if(!isSelected) cells.add(new OptionCell(clubs.get(i), false));
                }
            }

            for(OptionCell i : cells)
                this.addView(i);
        }

        public class OptionCell extends LinearLayout{
            public TextView nameBar;
            public CheckBox box;

            public Club contentClub;
            public boolean isSelected;

            public OptionCell(Club club, boolean selected){
                super(context);
                contentClub = club;
                isSelected = selected;

                this.setLayoutParams(optionCellLayout);
                this.setOrientation(HORIZONTAL);
                this.setGravity(Gravity.START);

                nameBar = new TextView(context);

                nameBar.setText(contentClub.getName());
                nameBar.setLayoutParams(new LinearLayout.LayoutParams((int)(0.6*bodyWidth), ViewGroup.LayoutParams.WRAP_CONTENT));
                nameBar.setPadding(8,8,8,8);
                nameBar.setBackgroundColor(isSelected ? getResources().getColor(R.color.purple) : getResources().getColor(R.color.white));
                nameBar.setTextColor(isSelected ? getResources().getColor(R.color.white) : getResources().getColor(R.color.black));
                nameBar.setTextSize(STANDARD_TEXT_SIZE);
                this.addView(nameBar);

                box = new CheckBox(context);
                box.setChecked(isSelected);
                box.setLayoutParams(new LinearLayout.LayoutParams((int)(0.15*bodyWidth), ViewGroup.LayoutParams.MATCH_PARENT));
                box.setBackgroundColor(getResources().getColor(R.color.shaded_background));
                box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if(!b){
                                for(int i = 0; i < subscribed.size(); i++){
                                    if (subscribed.get(i).equals(contentClub.getName())) {
                                        subscribed.remove(i);
                                        i--;
                                    }
                                }

                                nameBar.setBackgroundColor(getResources().getColor(R.color.white));
                                nameBar.setTextColor(getResources().getColor(R.color.black));
                            }
                            else{
                                subscribed.add(contentClub.getName());

                                nameBar.setBackgroundColor(getResources().getColor(R.color.purple));
                                nameBar.setTextColor(getResources().getColor(R.color.white));
                            }

                            editor.putStringSet(getResources().getString(R.string.subscribed_channels_key),
                                    new HashSet<>(subscribed));
                            editor.commit();

                            isSelected = b;

                        Log.d("announcement", subscribed.toString());
                    }
                });
                this.addView(box);
            }
        }

    }

    public class ErrorButton extends LinearLayout{

        TextView message = new TextView(context);
        ImageView icon = new ImageView(context);

        public ErrorButton(){
            super(context);
            this.setOrientation(VERTICAL);
            LinearLayout.LayoutParams errorParams = new LinearLayout.LayoutParams((int)(0.9 * bodyWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
            errorParams.setMargins(0,50,0,50);
            this.setLayoutParams(errorParams);
            this.setGravity(Gravity.CENTER_HORIZONTAL);
            this.setPadding(12,12,12,12);

            message.setLayoutParams(new LinearLayout.LayoutParams((int)(0.6 * bodyWidth), ViewGroup.LayoutParams.WRAP_CONTENT));
            message.setGravity(Gravity.CENTER_HORIZONTAL);
            message.setTextColor(getResources().getColor(R.color.black));
            message.setTextSize(STANDARD_TEXT_SIZE);
            message.setText("Please connect to the internet");
            this.addView(message);

            icon.setLayoutParams(new LinearLayout.LayoutParams((int)(0.6 * bodyWidth), (int)(0.6 * bodyWidth)));
            icon.setPadding(20,20,20,20);
            icon.setImageResource(R.drawable.wifi_disconnected);
            this.addView(icon);

            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    pullFromDatabase();
                    if(filterShown){
                        filter.loadCells();
                    }
                    else{
                        loadAnnouncements();
                    }
                }
            });
        }
    }

    public LinearLayout.LayoutParams generateLinearParams (double width, double height){
        return new LinearLayout.LayoutParams((int)(width * bodyWidth), (int)(height * bodyHeight));
    }

}
