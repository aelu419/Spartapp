package hackthis.everythingthis;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;

import org.w3c.dom.Text;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PostBlock extends LinearLayout {
    Context context;
    LinearLayout.LayoutParams bodyParams;
    int bodyWidth, bodyHeight;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    //UI components
    //this <- LinearLayout
    LinearLayout Title;
        ImageView LogOut;
    ScrollView Contents;
        LinearLayout Body;
    //

    public final int LOGIN = 0, DRAFT = 1, SEND = 2, SAVE = 3, ERROR = 4;

    public float STANDARD_TEXT_SIZE;
    public boolean internetConnected;
    public boolean hasDraft;
    public int pageMode;
    public Club club;

    public PostBlock(Context CONTEXT, LinearLayout.LayoutParams BODYPARAMS, SharedPreferences PREFERENCES,
                     SharedPreferences.Editor EDITOR){
        super(CONTEXT);
        context = CONTEXT;
        bodyParams = BODYPARAMS;
        bodyHeight = bodyParams.height;
        bodyWidth = bodyParams.width;
        preferences = PREFERENCES;
        editor = EDITOR;

        this.setLayoutParams(bodyParams);
        this.setOrientation(VERTICAL);
        this.setGravity(Gravity.START);

        STANDARD_TEXT_SIZE = (float)(0.02*bodyHeight);

        Title = new LinearLayout(context);
        LinearLayout.LayoutParams tableParams = new LinearLayout.LayoutParams(bodyWidth, (int)(0.15*bodyHeight));
        Title.setLayoutParams(tableParams);
        Title.setOrientation(HORIZONTAL);
        Title.setPadding(30,10,10,10);
        Title.setGravity(Gravity.START);
        Title.setBackgroundColor(getResources().getColor(R.color.purple));
        this.addView(Title);

        TextView titleText = new TextView(context);
        titleText.setLayoutParams(new LinearLayout.LayoutParams((int)(0.85*bodyWidth-10), (int)(0.15*bodyHeight)));
        titleText.setTextSize(STANDARD_TEXT_SIZE + 4.0f);
        titleText.setText("Post Announcement");
        titleText.setGravity(Gravity.CENTER_VERTICAL);
        titleText.setTextColor(getResources().getColor(R.color.white));
        Title.addView(titleText);

        LogOut = new ImageView(context);
        LogOut.setPadding(4,4,4,4);
        LogOut.setImageResource(R.drawable.logout_button);
        LogOut.setLayoutParams(new LinearLayout.LayoutParams((int)(0.15*bodyWidth-10), (int)(0.15*bodyHeight)));
        LogOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });
        Title.addView(LogOut);

        Contents = new ScrollView(context);
        Contents.setLayoutParams(new LinearLayout.LayoutParams(bodyWidth, (int)(0.85*bodyHeight)));
        Contents.setVerticalScrollBarEnabled(true);
        Contents.setHorizontalScrollBarEnabled(false);
        Contents.setBackgroundColor(getResources().getColor(R.color.background));
        this.addView(Contents);

        internetConnected = testInternetConnection();

        hasDraft = preferences.getBoolean(getResources().getString(R.string.has_draft_key),false);
        editor.putBoolean(getResources().getString(R.string.has_draft_key), hasDraft);
        editor.commit();

        login(hasDraft);
    }

    //loginType: does not have draft stored (false), have draft stored (true)
    public void login(Boolean loginType){

        Contents.removeAllViews();

        String thisName = preferences.getString(getResources().getString(R.string.login_name_key), null);
        String thisPass = preferences.getString(getResources().getString(R.string.login_password_key), null);

        if(testLoginValidity(thisName, thisPass)){
            startDraft();
            return;
        }

        pageMode = LOGIN;
        LoginPage lp = new LoginPage();
        Contents.addView(lp);
    }

    public void logOut(){
        //only triggers on draft page, error page and send page

        editor.putString(getResources().getString(R.string.login_password_key),"");
        editor.putString(getResources().getString(R.string.login_name_key),"");
        editor.commit();

        //TODO: call saveDraft page
        login(preferences.getBoolean(getResources().getString(R.string.has_draft_key), false));
    }

    //postType: see loginType
    public void startDraft(){

        pageMode = DRAFT;

        editor.putBoolean(getResources().getString(R.string.has_draft_key), false);
        editor.commit();

        Contents.removeAllViews();

        EditPage ep = new EditPage();
        Contents.addView(ep);

        //TODO: initialize post screen
    }

    public boolean testInternetConnection(){
        try
        {
            Log.d("announcement","try to connect");
            URL url = new URL("http://www.baidu.com");
            URLConnection connection = url.openConnection();
            connection.connect();
        }catch (Exception e){
            Log.d("announcement","failed to connect");
            return false;
        }

        return true;
    }

    public boolean testLoginValidity(String name, String passWord){

        if(name == null || passWord == null) return false;

        AVQuery clubQuery = new AVQuery("Clubs");
        clubQuery.whereMatches("name", name);
        String pw = "";
        try {
            List<AVObject> club = clubQuery.find();
            if(club == null) return false;
            pw = club.get(0).getString("key");
        } catch (AVException e){
            reportInternetError();
            return false;
        }

        if(!passWord.equals(pw))
            return false;

        Log.d("announcement","success on loging into "+name+" "+passWord);

        club = new Club(name, passWord);
        return true;
    }

    public class EditPage extends LinearLayout{
        EditText editTitle, editBody;
        TextView subTitle;
        ImageButton submit, save, clear;
        String titleText, bodyText;
        LinearLayout footBar;
        Date date;

        public EditPage(){
            super(context);

            date = getTime();

            this.setLayoutParams(new LinearLayout.LayoutParams(bodyWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            this.setPadding((int)(0.075*bodyWidth),10,(int)(0.075*bodyWidth),10);
            this.setOrientation(VERTICAL);
            this.setGravity(Gravity.LEFT);
            this.setBackgroundColor(getResources().getColor(R.color.background));

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams((int)(0.85*bodyWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
            textParams.setMargins(0,10,0,10);

            editTitle = new EditText(context);
            editTitle.setHint("Announcement Title");
            editTitle.setLayoutParams(textParams);
            editTitle.setTextSize(STANDARD_TEXT_SIZE);
            editTitle.setTextColor(getResources().getColor(R.color.black));
            editTitle.setBackgroundColor(getResources().getColor(R.color.white));
            editTitle.setPadding(8,8,8,8);
            this.addView(editTitle);

            subTitle = new TextView(context);
            subTitle.setLayoutParams(textParams);
            subTitle.setTextColor(getResources().getColor(R.color.colorPrimary));
            subTitle.setTextSize(STANDARD_TEXT_SIZE);
            subTitle.setText(club.getName() + new SimpleDateFormat("MMMM - dd").format(date));
            this.addView(subTitle);

            editBody = new EditText(context);
            editBody.setHint("Announcement Contents");
            editBody.setLayoutParams(textParams);
            editBody.setTextSize(STANDARD_TEXT_SIZE);
            editBody.setTextColor(getResources().getColor(R.color.black));
            editBody.setBackgroundColor(getResources().getColor(R.color.white));
            editBody.setPadding(8,8,8,8);
            this.addView(editBody);

            footBar = new LinearLayout(context);
            footBar.setLayoutParams(new LinearLayout.LayoutParams((int)(0.9*bodyWidth), ViewGroup.LayoutParams.WRAP_CONTENT));
            footBar.setOrientation(HORIZONTAL);
            footBar.setGravity(Gravity.CENTER_HORIZONTAL);
            footBar.setPadding(0,10,0,10);
            this.addView(footBar);

            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int)(0.12*bodyHeight));
            buttonParams.setMargins(10,0,10,0);

            submit = new ImageButton(context);
            submit.setLayoutParams(buttonParams);
            submit.setBackground(getResources().getDrawable(R.drawable.button_background));
            submit.setImageResource(R.drawable.upload_button);
            submit.setPadding(10,10,10,10);
            submit.setScaleType(ImageView.ScaleType.FIT_CENTER);
            submit.setAdjustViewBounds(true);
            submit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            footBar.addView(submit);

            save = new ImageButton(context);
            save.setLayoutParams(buttonParams);
            save.setBackground(getResources().getDrawable(R.drawable.button_background));
            save.setImageResource(R.drawable.save_button);
            save.setPadding(10,10,10,10);
            save.setScaleType(ImageView.ScaleType.FIT_CENTER);
            save.setAdjustViewBounds(true);
            save.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            footBar.addView(save);

            clear = new ImageButton(context);
            clear.setLayoutParams(buttonParams);
            clear.setBackground(getResources().getDrawable(R.drawable.button_background));
            clear.setImageResource(R.drawable.clear_button);
            clear.setPadding(10,10,10,10);
            clear.setScaleType(ImageView.ScaleType.FIT_CENTER);
            clear.setAdjustViewBounds(true);
            clear.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            footBar.addView(clear);

        }
    }

    public class LoginPage extends LinearLayout{
        public EditText nameText, passwordText;
        public TextView draftHint;
        public Button button1, button2;
        public CheckBox remember;
        public LoginPage(){
            super(context);
            //TODO: finish according to design
            this.setLayoutParams(new LinearLayout.LayoutParams(bodyWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            this.setPadding((int)(0.075*bodyWidth),10,(int)(0.075*bodyWidth),10);
            this.setOrientation(VERTICAL);
            this.setGravity(Gravity.LEFT);
            this.setBackgroundColor(getResources().getColor(R.color.background));

            Log.d("announcement","log in under mode "+
                    preferences.getBoolean(getResources().getString(R.string.has_draft_key), false));

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams((int)(0.85*bodyWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
            textParams.setMargins(0,10,0,10);

            draftHint = new TextView(context);
            draftHint.setTextSize(STANDARD_TEXT_SIZE);
            draftHint.setLayoutParams(textParams);
            draftHint.setTextColor(getResources().getColor(R.color.red));

            if(preferences.getBoolean(getResources().getString(R.string.has_draft_key), false)){

                draftHint.setText("you have a saved draft, please log in and choose whether to continue on it or not");
            }
            this.addView(draftHint);

            nameText = new EditText(context);
            nameText.setHint("Club Name");
            nameText.setLayoutParams(textParams);
            nameText.setTextSize(STANDARD_TEXT_SIZE);
            nameText.setTextColor(getResources().getColor(R.color.black));
            nameText.setBackgroundColor(getResources().getColor(R.color.white));
            nameText.setPadding(8,8,8,8);
            this.addView(nameText);

            passwordText = new EditText(context);
            passwordText.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            passwordText.setTextColor(getResources().getColor(R.color.black));
            passwordText.setHint("Club Key");
            passwordText.setTextSize(STANDARD_TEXT_SIZE);
            passwordText.setLayoutParams(textParams);
            passwordText.setBackgroundColor(getResources().getColor(R.color.white));
            passwordText.setPadding(8,8,8,8);
            this.addView(passwordText);

            remember = new CheckBox(context);
            remember.setChecked(false);
            remember.setBackgroundColor(getResources().getColor(R.color.white));
            remember.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            this.addView(remember);

            TextView rememberText = new TextView(context);
            rememberText.setText("remember my login informations");
            rememberText.setTextSize(STANDARD_TEXT_SIZE);
            rememberText.setPadding(0,0,0,10);
            this.addView(rememberText);

            button1 = new Button(context);
            button1.setBackground(getResources().getDrawable(R.drawable.button_background));
            button1.setLayoutParams(new LinearLayout.LayoutParams((int)(0.2*bodyWidth), (int)(0.1*bodyWidth)));
            this.addView(button1);

            button2 = new Button(context);
            button2.setBackground(getResources().getDrawable(R.drawable.button_background));
            LinearLayout.LayoutParams jiba = new LinearLayout.LayoutParams((int)(0.2*bodyWidth), (int)(0.1*bodyWidth));
            jiba.setMargins(0,5,0,0);
            button2.setLayoutParams(jiba);


            button1.setTextSize(STANDARD_TEXT_SIZE+2.0f);
            button1.setTextColor(getResources().getColor(R.color.purple));
            button2.setTextSize(STANDARD_TEXT_SIZE+2.0f);
            button2.setTextColor(getResources().getColor(R.color.purple));

            if(hasDraft){
                button1.setText("continue");
                this.addView(button2);
                button2.setText("abandon");
            }
            else{
                button1.setText("log in");
            }

            button1.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(hasDraft){
                        if(testLoginValidity(nameText.getText().toString(),passwordText.getText().toString()))
                        {
                            if (remember.isChecked()) {
                                editor.putString(getResources().getString(R.string.login_name_key),
                                        nameText.getText().toString());
                                editor.putString(getResources().getString(R.string.login_password_key),
                                        passwordText.getText().toString());
                                editor.commit();
                            }
                            startDraft();
                        }
                        else{
                            draftHint.setText("Wrong club name or club key, please re-enter!");
                            nameText.setText("");
                            passwordText.setText("");
                            remember.setChecked(false);
                        }
                    }
                    else{
                        if(testLoginValidity(nameText.getText().toString(),passwordText.getText().toString()))
                        {
                            if (remember.isChecked()) {
                                editor.putString(getResources().getString(R.string.login_name_key),
                                        nameText.getText().toString());
                                editor.putString(getResources().getString(R.string.login_password_key),
                                        passwordText.getText().toString());
                                editor.commit();
                            }
                            startDraft();
                        }
                        else{
                            draftHint.setText("Wrong club name or club key, please re-enter!");
                            nameText.setText("");
                            passwordText.setText("");
                            remember.setChecked(false);
                        }
                    }
                }
            });

            button2.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(testLoginValidity(nameText.getText().toString(),passwordText.getText().toString()))
                    {
                        if (remember.isChecked()) {
                            editor.putString(getResources().getString(R.string.login_name_key),
                                    nameText.getText().toString());
                            editor.putString(getResources().getString(R.string.login_password_key),
                                    passwordText.getText().toString());
                            editor.commit();
                        }

                        editor.putString(getResources().getString(R.string.draft_title_key),null);
                        editor.putString(getResources().getString(R.string.draft_body_key),null);
                        editor.commit();

                        hasDraft = false;

                        startDraft();
                    }
                    else{
                        draftHint.setText("Wrong club name or club key, please re-enter!");
                        nameText.setText("");
                        passwordText.setText("");
                        remember.setChecked(false);
                    }
                }
            });
        }
    }

    public void reportInternetError(){
        //TODO: do it
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
            this.setBackground(getResources().getDrawable(R.drawable.error_background));
            this.setPadding(12,12,12,12);

            message.setLayoutParams(new LinearLayout.LayoutParams((int)(0.6 * bodyWidth), ViewGroup.LayoutParams.WRAP_CONTENT));
            message.setGravity(Gravity.CENTER_HORIZONTAL);
            message.setTextColor(getResources().getColor(R.color.black));
            message.setTextSize(STANDARD_TEXT_SIZE);
            message.setText("Please connect the internet and press this to refresh");
            this.addView(message);

            icon.setLayoutParams(new LinearLayout.LayoutParams((int)(0.6 * bodyWidth), (int)(0.6 * bodyWidth)));
            icon.setPadding(20,20,20,20);
            icon.setImageResource(R.drawable.wifi_disconnected);
            this.addView(icon);

            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: insert page refreshing method here
                }
            });
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
}
