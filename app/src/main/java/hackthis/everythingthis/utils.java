package hackthis.everythingthis;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.net.InetAddress;

public class utils {
    public static boolean testInternetConnection(Context context){
       /* ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();*/
       Log.d("WIFI", "testing connection");
       try {
            InetAddress ipAddr = InetAddress.getByName("https://cn.bing.com/");
            Log.d("WIFI", "got IP address");
            // /You can replace it with your name
            return !ipAddr.equals("");
        } catch (Exception e) {
           Log.d("WIFI", "exception caught");
            return false;
        }
    }
}
