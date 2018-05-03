package hackthis.everythingthis;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.avos.avoscloud.AVOSCloud;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

public class CustomReceiver extends BroadcastReceiver {
    public ArrayList<String> defaultSubscribes;
    public ArrayList<String> subscribed;
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;

    public void onReceive(Context context, Intent intent){
        try {
            Log.d("pushannon","received with action "+intent.getAction());
            if(intent.getAction().equals("spartapp.notification")) {
                preferences = context.getSharedPreferences(context.getResources().getString(R.string.preferences_key), Context.MODE_PRIVATE);
                editor = preferences.edit();

                JSONObject json = new JSONObject(intent.getExtras().getString("com.avos.avoscloud.Data"));
                final String message = json.getString("content");

                Log.d("pushannon","message = "+message);

                int notificationNum = preferences.getInt(context.getResources().getString(R.string.notification_num_key),0);

                try {
                    defaultSubscribes = new ArrayList<>();
                    defaultSubscribes.add("Student Council");
                    defaultSubscribes.add("{Hack,THIS}");

                    Log.d("pushannon","trying to find club "+json.getString("clubname").toLowerCase());
                    HashSet<String> clubSet = new HashSet<>(preferences.getStringSet(context.getResources().getString(R.string.subscribed_channels_key),
                            new HashSet<>(defaultSubscribes)));
                    subscribed = new ArrayList<>(clubSet);
                    Log.d("pushannon","subscribed channels:\n"+subscribed.toString());
                    boolean found = false;
                    for(String i : subscribed){
                        if(i.toLowerCase().equals(json.getString("clubname").toLowerCase())){
                            Log.d("pushannon","    "+i.toLowerCase());
                            found = true;
                            break;
                        }
                    }
                    if(found){

                    }
                    else{
                        return;
                    }
                }
                catch(Exception e){
                    Log.d("pushannon","-error:",e);
                    return;
                }

                if(notificationNum<Integer.MAX_VALUE) {
                    editor.putInt(context.getResources().getString(R.string.notification_num_key), notificationNum + 1);
                    editor.commit();
                }
                else{
                    editor.putInt(context.getResources().getString(R.string.notification_num_key), 0);
                    editor.commit();
                }


                Intent resultIntent = new Intent(AVOSCloud.applicationContext, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(AVOSCloud.applicationContext, 0, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(AVOSCloud.applicationContext)
                        .setSmallIcon(R.drawable.app_mini)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.app_medium))
                        .setContentTitle(
                                AVOSCloud.applicationContext.getResources().getString(R.string.app_name))
                        .setContentText(message)
                        .setContentIntent(pendingIntent)
                        .setTicker(message);
                mBuilder.setAutoCancel(true);

                NotificationManager mNotifyMgr =
                        (NotificationManager) AVOSCloud.applicationContext
                                .getSystemService(
                                        Context.NOTIFICATION_SERVICE);
                mNotifyMgr.notify(notificationNum, mBuilder.build());
                context.sendBroadcast(new Intent("updateAnnouncements"));
            }

        }
        catch (Exception e){
            Log.d("pushannon","onReceive method throws error:\n"+e);
        }
    }
}
