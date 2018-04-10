package hackthis.everythingthis;

import android.app.SearchManager;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.support.v7.widget.Toolbar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.search.SearchActivity;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class AnnouncementBlock extends LinearLayout{
    //status vars
    Context context;
    boolean shown;

    public ArrayList<Announcement> announcements = new ArrayList<Announcement>();

    public Announcement[] nextAnnouncements = {
            //format: content, new Time(time), club (you can use null)
            new Announcement("Dogs", "a dog event", new Date(), new Club("dog club", "DOG")),
            new Announcement("More Dogs", "more dog events", new Date(), new Club("more dog club","MOREDOG")),
            new Announcement("Cats", "a cat event", new Date(), new Club("cat club", "CAT")),
            new Announcement("Dogs", "a dog event", new Date(), new Club("dog club", "DOG")),
            new Announcement("More Dogs", "more dog events", new Date(), new Club("more dog club","MOREDOG")),
            new Announcement("Cats", "a cat event", new Date(), new Club("cat club", "CAT")),
            new Announcement("Dogs", "a dog event", new Date(), new Club("dog club", "DOG")),
            new Announcement("More Dogs", "more dog events", new Date(), new Club("more dog club","MOREDOG")),
            new Announcement("Cats", "a cat event", new Date(), new Club("cat club", "CAT"))
    };

    public ArrayList<Club> subscribedChannels = new ArrayList<>(30);

    public ArrayList <String> filterTraits = new ArrayList<>(10);
    public String searchKey="";

    public SharedPreferences.Editor edit;

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



    public AnnouncementBlock(Context viewContext, LayoutParams body, SharedPreferences.Editor editor){
        super(viewContext);
        context = viewContext;

        edit = editor;

        bodyParams = body;
        bodyHeight = body.height;
        bodyWidth = body.width;

        contentLayout = new LinearLayout.LayoutParams((int)(0.9 * bodyWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
        contentLayout.setMargins(0,8,0,8);

        optionCellLayout = new LinearLayout.LayoutParams((int)(0.75 * bodyWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
        optionCellLayout.setMargins(0,8,0,8);

        STANDARD_TEXT_SIZE = (float)(0.02*bodyHeight);

        this.setLayoutParams(bodyParams);
        this.setOrientation(VERTICAL);
        this.setGravity(Gravity.TOP);

        //TODO: delete this after pull method is complete
        announcements.clear();
        for(int i = 0; i < nextAnnouncements.length; i ++) {
            announcements.add(nextAnnouncements[i]);
        }
        subscribedChannels.clear();
        subscribedChannels.add(new Club("dog club", "DOG"));
        subscribedChannels.add(new Club("more dog club","MOREDOG"));
        subscribedChannels.add(new Club("cat club", "CAT"));


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

        ArrayList<Announcement> filteredAnnouncement = new ArrayList<>(50);

        for(int i = 0; i < announcements.size(); i++){
            boolean isFiltered = false;
            for(int j = 0; j < filterTraits.size(); j++){
                if(announcements.get(i).getClub().getName().toLowerCase().equals(filterTraits.get(j).toLowerCase())) {
                    isFiltered = true;
                    break;
                }
            }
            if(!isFiltered) {
                if((searchKey!="" && hasRelatedContent(announcements.get(i))) || searchKey=="")
                    filteredAnnouncement.add(announcements.get(i));
            }
        }

        Contents[] contents= new Contents[filteredAnnouncement.size()];
        for(int i = 0; i < filteredAnnouncement.size(); i++) {
            contents[i] = new Contents(filteredAnnouncement.get(i));
        }

        sortByDates(contents);

        for(int i = 0; i < contents.length; i ++){
            list.addView(contents[i]);
        }
    }

    public boolean hasRelatedContent (Announcement a){
        if(a.getClub().getName().toString().toLowerCase().contains(searchKey.toLowerCase()))
            return true;
        if(a.getAnnouncement().toLowerCase().contains(searchKey.toLowerCase()))
            return true;
        if(a.title.toLowerCase().contains(searchKey.toLowerCase()))
            return true;
        return false;
    }

    public void sortByDates(Contents[] contents){
        QuickSortAnnouncement(contents);
    }

    public Contents[] QuickSortAnnouncement(Contents[] arr){
        if(arr == null || arr.length<=2)
            return arr;
        int len = 0;
        Contents midValue = arr[0];
        for(int i = 1; i < arr.length; i++){

			if((arr[i].content).compareTo(midValue.content)<=0)
				len++;
        }
        Contents[] tempLeft = new Contents[len];
        Contents[] tempRight = new Contents[arr.length - 1 - len];
        int m1 = 0, m2 = 0;
        for(int i = 1; i < arr.length; i++){
			if((arr[i].content).compareTo(midValue.content)<=0)
				tempLeft[m1++] = arr[i];
			else{
				tempRight[m2++] = arr[i];
			}
        }

        tempLeft = QuickSortAnnouncement(tempLeft);
        tempRight = QuickSortAnnouncement(tempRight);

        for(int i = 0; i < arr.length; i++){
            if(i < len)
                arr[i] = tempLeft[i];
            else if(i>len)
                arr[i] = tempRight[i - len - 1];
            else
                arr[i] = midValue;
        }

        return arr;
    }


    public void pullFromDatabase(){
        //TODO: write pull method here

        //TODO: update announcements into announcements<Announcement>
        //TODO: update clubs into subscribedChannels<Club>

    }

    public class Contents extends LinearLayout{

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
            editText.setHint("Search");
            editText.setHintTextColor(getResources().getColor(R.color.purple));
            editText.setBackgroundColor(getResources().getColor(R.color.shaded_background));
            editText.setLayoutParams(generateLinearParams(0.65, 0.08));
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
                    searchKey = editText.getText().toString();
                    loadAnnouncements();
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
                    //TODO: call search method
                    searchKey = editText.getText().toString();
                    loadAnnouncements();
                }
            });

            this.addView(searchButton);

            filterButton = new ImageView(context);
            filterButton.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int)(0.08*bodyHeight)));
            filterButton.setImageResource(R.drawable.filter_icon);
            filterButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    bodyLayout.removeAllViews();

                    filterShown = !filterShown;

                    if(filterShown){
                        filter = new AnnouncementFilterLayout();
                        bodyLayout.addView(filter);
                    }
                    else{
                        bodyLayout.addView(list);
                        loadAnnouncements();
                    }


                    //TODO: Call filter init method
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

            LinearLayout.LayoutParams introTextParams = new LinearLayout.LayoutParams(bodyWidth-20, ViewGroup.LayoutParams.WRAP_CONTENT);
            introTextParams.setMargins(0,10,0,10);

            introText_1 = new TextView(context);
            introText_2 = new TextView(context);

            introText_1.setLayoutParams(introTextParams);
            introText_1.setTextSize(STANDARD_TEXT_SIZE);
            introText_1.setTextColor(AnnouncementBlock.this.getResources().getColor(R.color.black));
            introText_1.setGravity(Gravity.LEFT);
            introText_1.setText("Select which of your subscribed channels to mute:");
            this.addView(introText_1);

            cells = new ArrayList<>(30);

            for(int i = 0; i < subscribedChannels.size(); i++){
                String temp = subscribedChannels.get(i).getName().toString();
                boolean isSelected = false;

                for(int j = 0; j < filterTraits.size(); j++){
                    if(filterTraits.get(j).equals(temp)){
                        isSelected = true;
                        break;
                    }
                }

                cells.add(new OptionCell(subscribedChannels.get(i), isSelected));

                this.addView(cells.get(i));
            }

            pullFromDatabase();

            //TODO: complete introText_2 and time limit part, also implement time Period into loadAnnouncements()
            //TODO: idea: class TimeLimit (Date time, boolean larger) larger: true : filter ones larger than time
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
                this.setGravity(Gravity.LEFT);

                nameBar = new TextView(context);

                nameBar.setText(contentClub.getName());
                nameBar.setLayoutParams(new LinearLayout.LayoutParams((int)(0.6*bodyWidth), ViewGroup.LayoutParams.WRAP_CONTENT));
                nameBar.setPadding(8,8,8,8);
                nameBar.setBackgroundColor(getResources().getColor(R.color.white));
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
                                for(int i = 0; i < filterTraits.size(); i++){
                                    if (filterTraits.get(i).equals(contentClub.getName())) {
                                        filterTraits.remove(i);
                                        i--;
                                    }
                                }
                            }
                            else{
                                filterTraits.add(contentClub.getName());
                            }
                            isSelected = b;

                        Log.d("announcement", filterTraits.toString());
                    }
                });
                this.addView(box);
            }
        }

    }

    public LinearLayout.LayoutParams generateLinearParams (double width, double height){
        return new LinearLayout.LayoutParams((int)(width * bodyWidth), (int)(height * bodyHeight));
    }

}
