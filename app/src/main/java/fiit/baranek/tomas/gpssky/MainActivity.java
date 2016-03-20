package fiit.baranek.tomas.gpssky;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(getApplicationContext());
    }
    public void BasicSettings(View v){
        Intent intent = new Intent(this, BasicSettingsActivity.class);
        startActivity(intent);
    }

    public void SMSSettings(View v){
        Intent intent = new Intent(this, SMSsettingsActivity.class);
        startActivity(intent);
    }

    public void SharingSettings(View v){
        onPause();
        Intent intent = new Intent(this, SharingSettingsActivity.class);
        startActivity(intent);

    }


    String StatusSetting="";

    public void Start(View v){


        new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
               System.out.println("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                System.out.println("done!");
            }
        }.start();




        /*if(SharingInformationSet==false) StatusSetting="Sharing";
        if(SMSInformationSet==false && StatusSetting.equals(""))   StatusSetting="SMS";
            else if (SMSInformationSet==false)  StatusSetting=StatusSetting+", sms";

        if(BasicInformationSet==false && StatusSetting.equals(""))   StatusSetting="Basic";
        else if (SMSInformationSet==false)  StatusSetting=StatusSetting +", basic";

        if(StatusSetting.equals("")) Toast.makeText(MainActivity.this, "Letíme do vesmíru", Toast.LENGTH_SHORT).show();
        else Toast.makeText(MainActivity.this, StatusSetting + " information not set", Toast.LENGTH_SHORT).show();
        */
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




}
