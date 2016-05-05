package fiit.baranek.tomas.gpssky.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;

import fiit.baranek.tomas.gpssky.R;
import fiit.baranek.tomas.gpssky.Settings.SharingSettings;

public class SharingSettingsActivity extends AppCompatActivity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private EditText EditTextEventID;
    private CheckBox CheckBoxAltitude;
    private CheckBox CheckBoxPhoto;
    private CheckBox CheckBoxBatteryStatus;
    private CheckBox CheckBoxDataNetwork;
    private SharingSettings settings = new SharingSettings();
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing_settings);

        EditTextEventID = (EditText) findViewById(R.id.editTextEventId);
        CheckBoxAltitude = (CheckBox) findViewById(R.id.checkBoxAltitude);
        CheckBoxPhoto = (CheckBox) findViewById(R.id.checkBoxPhoto);
        CheckBoxBatteryStatus = (CheckBox) findViewById(R.id.checkBoxBatteryStatus);
        CheckBoxDataNetwork = (CheckBox) findViewById(R.id.checkBoxDataNetwork);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutSharing);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setPublishPermissions(Arrays.asList("publish_actions, publish_pages, manage_pages,user_about_me,user_posts"));


        if(getIntent().getStringExtra("event_id")!= null && !getIntent().getStringExtra("event_id").equals(""))
            settings.setEventID(getIntent().getStringExtra("event_id"));
        settings.setAltitude(getIntent().getBooleanExtra("altitude",false));
        settings.setPhoto(getIntent().getBooleanExtra("photo",false));
        settings.setBatteryStatus(getIntent().getBooleanExtra("battery_status",false));
        settings.setDataNetwork(getIntent().getBooleanExtra("data_network",false));


        setTitle("Sharing settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        callbackManager = CallbackManager.Factory.create();

        if(!settings.getEventID().equals(""))
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
            }

            @Override
            public void onCancel() {
                //Log.d("Cenceled");
                System.out.println("Zrusene");
                System.out.println("Tu som 4");
            }

            @Override
            public void onError(FacebookException error) {
                if(error.toString().equals("CONNECTION_FAILURE: CONNECTION_FAILURE")) {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Please turn on access.", Snackbar.LENGTH_LONG);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.RED);

                    snackbar.show();
                }
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

        if(isOnline()) {
            if (isLoggedIn()) {
                if (EditTextEventID.getText().toString().length() > 5) {
                    settings.setEventID(EditTextEventID.getText().toString());

                    if (CheckBoxAltitude.isChecked()) {
                        settings.setAltitude(true);
                    } else {
                        settings.setAltitude(false);
                    }

                    if (CheckBoxPhoto.isChecked()) {
                        settings.setPhoto(true);
                    } else {
                        settings.setPhoto(false);
                    }

                    if (CheckBoxBatteryStatus.isChecked()) {
                        settings.setBatteryStatus(true);
                    } else {
                        settings.setBatteryStatus(false);
                    }

                    if (CheckBoxDataNetwork.isChecked()) {
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
                } else {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Please enter true EVENT ID", Snackbar.LENGTH_LONG);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.RED);

                    snackbar.show();
                }
            } else {
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Please login to Facebook.", Snackbar.LENGTH_LONG);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.RED);

                snackbar.show();
            }
        }else {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Please turn on access.", Snackbar.LENGTH_LONG);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.RED);

            snackbar.show();
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

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        else {
            return false;
        }
    }


}
