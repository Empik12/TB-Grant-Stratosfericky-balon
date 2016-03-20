package fiit.baranek.tomas.gpssky;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SharingSettingsActivity extends AppCompatActivity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing_settings);

        setTitle("Sharing settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setPublishPermissions(Arrays.asList("publish_actions, publish_pages, manage_pages,user_about_me,user_posts"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                AccessToken token = loginResult.getAccessToken().getCurrentAccessToken();
                Bundle params = new Bundle();
                params.putString("message", "This is a test message");
/* make the API call */
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/795930057178952/feed",
                        params,
                        HttpMethod.POST,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {

                                FacebookRequestError error = response.getError();
                                if (error != null) {
                                    System.out.println("CHyba:" + error.toString());

                                }
                                else
                                {
                                    System.out.println("Alest okey");
                                }
                            }
                        }
                ).executeAsync();
            }

            @Override
            public void onCancel() {
                //Log.d("Cenceled");
                System.out.println("Zrusene");
                System.out.println("Tu som 4");
            }

            @Override
            public void onError(FacebookException error) {
                System.out.println("Tu som 5");
                Log.d("Error", error.toString());
            }
        });



    }

/*
    @Override
    protected void onDestroy(){
        super.onDestroy();
        Intent Myintent=new Intent(this, MainActivity.class).putExtra("SharingInformationSet", true);
        startActivity(Myintent);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


}
