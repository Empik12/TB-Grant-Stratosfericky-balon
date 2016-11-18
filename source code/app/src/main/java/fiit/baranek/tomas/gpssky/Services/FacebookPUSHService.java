package fiit.baranek.tomas.gpssky.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONObject;


/**
 * Created by Tomáš Baránek
 * This is Service for sharing a text message
 *
 */
public class FacebookPUSHService extends Service {
    public FacebookPUSHService() {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void push(Boolean Mapshare, String GoogleMapsURL,String message, String EventID, Context context){

        AccessToken token = AccessToken.getCurrentAccessToken();
        String path = "/"+ EventID +"/" + "feed";
        Bundle parametre = new Bundle();
        parametre.putString("message", message);
        if(Mapshare)
            parametre.putString("link",GoogleMapsURL);
        final String[] result = new String[1];


        GraphRequest request = new GraphRequest(token, path, parametre, HttpMethod.POST, new GraphRequest.Callback() {

            @Override
            public void onCompleted(GraphResponse response) {
            }
        });


        request.executeAsync();
    }
}
