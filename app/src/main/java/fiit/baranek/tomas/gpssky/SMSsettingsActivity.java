package fiit.baranek.tomas.gpssky;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import fiit.baranek.tomas.gpssky.Settings.SMSSettings;

public class SMSsettingsActivity extends AppCompatActivity {

    private EditText EditTextPhoneNumer;
    private CheckBox CheckBoxAltitude;
    private CheckBox CheckBoxBatteryStatus;
    private CheckBox CheckBoxDataNetwork;
    private SMSSettings settings = new SMSSettings();
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smssettings);

        setTitle("SMS notification settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EditTextPhoneNumer = (EditText) findViewById(R.id.editTextPhone);
        CheckBoxAltitude = (CheckBox) findViewById(R.id.checkBoxAltitude);
        CheckBoxBatteryStatus = (CheckBox) findViewById(R.id.checkBoxBatteryStatus);
        CheckBoxDataNetwork = (CheckBox) findViewById(R.id.checkBoxDataNetwork);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutSMS);



        settings.setPhoneNumber(getIntent().getStringExtra("phone_number"));
        settings.setAltitude(getIntent().getBooleanExtra("altitude", false));
        settings.setBatteryStatus(getIntent().getBooleanExtra("battery_status", false));
        settings.setDataNetwork(getIntent().getBooleanExtra("data_network",false));

        EditTextPhoneNumer.setText(settings.getPhoneNumber());
        if(settings.getAltitude()){
            CheckBoxAltitude.setChecked(true);
        } else {
            CheckBoxAltitude.setChecked(false);
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



    }

    public void Confirm(View v) {
        if(!EditTextPhoneNumer.getText().toString().equals("")) {
            settings.setPhoneNumber(EditTextPhoneNumer.getText().toString());

            if (CheckBoxAltitude.isChecked()) {
                settings.setAltitude(true);
            } else {
                settings.setAltitude(false);
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
            intent.putExtra("phone_number", settings.getPhoneNumber());
            intent.putExtra("altitude", settings.getAltitude());
            intent.putExtra("battery_status", settings.getBatteryStatus());
            intent.putExtra("data_network", settings.getDataNetwork());
            setResult(RESULT_OK, intent);
            finish();
        }else {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Please enter phone number", Snackbar.LENGTH_LONG);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.RED);

            snackbar.show();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                System.out.println("Vraciam sa");
                finish();
            }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
