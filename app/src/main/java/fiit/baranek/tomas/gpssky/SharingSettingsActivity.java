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
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.facebook.internal.BoltsMeasurementEventListener;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import fiit.baranek.tomas.gpssky.Settings.SharingSettings;

public class SharingSettingsActivity extends AppCompatActivity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    EditText EditTextEventID;
    CheckBox CheckBoxAltitude;
    CheckBox CheckBoxPhoto;
    CheckBox CheckBoxBatteryStatus;
    CheckBox CheckBoxDataNetwork;
    SharingSettings settings = new SharingSettings();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing_settings);

        EditTextEventID = (EditText) findViewById(R.id.editTextEventID);
        CheckBoxAltitude = (CheckBox) findViewById(R.id.checkBoxAltitude);
        CheckBoxPhoto = (CheckBox) findViewById(R.id.checkBoxPhoto);
        CheckBoxBatteryStatus = (CheckBox) findViewById(R.id.checkBoxBatteryStatus);
        CheckBoxDataNetwork = (CheckBox) findViewById(R.id.checkBoxDataNetwork);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setPublishPermissions(Arrays.asList("publish_actions, publish_pages, manage_pages,user_about_me,user_posts"));


        settings.setEventID(getIntent().getStringExtra("event_id"));
        settings.setAltitude(getIntent().getBooleanExtra("altitude",false));
        settings.setPhoto(getIntent().getBooleanExtra("photo",false));
        settings.setBatteryStatus(getIntent().getBooleanExtra("battery_status",false));
        settings.setDataNetwork(getIntent().getBooleanExtra("data_network",false));


        setTitle("Sharing settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        callbackManager = CallbackManager.Factory.create();

        EditTextEventID.setText(settings.getEventID());
        if(settings.getAltitude()){
            CheckBoxAltitude.setChecked(true);
        } else {
            CheckBoxAltitude.setChecked(false);
        }
        if(settings.getPhoto()){
            CheckBoxPhoto.setChecked(true);
        } else {
            CheckBoxPhoto.setChecked(false);
        }
        if(settings.getBatteryStatus()){
            CheckBoxBatteryStatus.setChecked(true);
        } else {
            CheckBoxBatteryStatus.setChecked(false);
        }
        if(settings.getDataNetwork()){
            CheckBoxDataNetwork.setChecked(true);
        } else {
            CheckBoxDataNetwork.setChecked(false);
        }



        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = AccessToken.getCurrentAccessToken();
                System.out.println(token.toString());

                 token = loginResult.getAccessToken().getCurrentAccessToken();
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

    public void Confirm(View v) {

        settings.setEventID(EditTextEventID.getText().toString());

        if(CheckBoxAltitude.isChecked()){
            settings.setAltitude(true);
        } else {
            settings.setAltitude(false);
        }

        if(CheckBoxPhoto.isChecked()){
            settings.setPhoto(true);
        } else {
            settings.setPhoto(false);
        }

        if(CheckBoxBatteryStatus.isChecked()){
            settings.setBatteryStatus(true);
        } else {
            settings.setBatteryStatus(false);
        }

        if(CheckBoxDataNetwork.isChecked()){
            settings.setDataNetwork(true);
        } else {
            settings.setDataNetwork(false);
        }

        Intent intent = new Intent();
        intent.putExtra("event_id", settings.getEventID());
        intent.putExtra("altitude", settings.getAltitude());
        intent.putExtra("battery_status", settings.getBatteryStatus());
        intent.putExtra("data_network", settings.getDataNetwork());
        intent.putExtra("photo", settings.getPhoto());
        setResult(RESULT_OK, intent);
        finish();
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
