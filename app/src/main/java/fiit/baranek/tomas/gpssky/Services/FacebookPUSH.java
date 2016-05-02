package fiit.baranek.tomas.gpssky.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONObject;

import fiit.baranek.tomas.gpssky.MainActivity;

public class FacebookPUSH extends Service {
    public FacebookPUSH() {
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

    public void push(String message, String EventID, Context context){
        AccessToken token = AccessToken.getCurrentAccessToken();
        String path = "/"+ EventID +"/" + "feed";
        Bundle parametre = new Bundle();
        parametre.putString("message", message);
        //parametre.putString("description", "topic share");
        final String[] result = new String[1];
        //parametre.putByteArray("picture", fotecka);


        GraphRequest request = new GraphRequest(token, path, parametre, HttpMethod.POST, new GraphRequest.Callback() {

            @Override
            public void onCompleted(GraphResponse response) {
                JSONObject obj = response.getJSONObject();
                if (obj != null) {
                    result[0] ="id : " + obj.optString("id");
                    System.out.println("Všetko je OKAY");
                } else {
                   result[0] =  "errof : " + response.getError().getErrorMessage();
                    System.out.println("Všetko nie je OKAY:" + response.getError().getErrorMessage());
                }
            }
        });


        request.executeAsync();
        //Toast.makeText(context,result[0], Toast.LENGTH_LONG).show();
    }
}
