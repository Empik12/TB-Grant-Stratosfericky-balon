package fiit.baranek.tomas.gpssky;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import fiit.baranek.tomas.gpssky.Settings.BasicSettings;

public class BasicSettingsActivity extends AppCompatActivity {

    private BasicSettings setting = new BasicSettings();
    private EditText EditTextSave;
    private EditText EditTextInftervalOfSending;
    private RadioButton RadioButtonSave;
    private RadioButton RadioButtonDiscard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_settings);

        EditTextSave = (EditText) findViewById(R.id.editTextSave);
        EditTextInftervalOfSending = (EditText) findViewById(R.id.editTextIntervalOfSending);
        RadioButtonSave = (RadioButton) findViewById(R.id.radioButtonSave);
        RadioButtonDiscard = (RadioButton) findViewById(R.id.radioButtonDiscard);

        setting.setIntervalOfSending(getIntent().getIntExtra("interval_of_sending", 5));
        setting.setSave(getIntent().getBooleanExtra("save", false));
        setting.setFileName(getIntent().getStringExtra("file_name"));

        EditTextInftervalOfSending.setText(String.valueOf(setting.getIntervalOfSending()));
        if(setting.getSave()){
            RadioButtonSave.setChecked(true);
        } else {
            RadioButtonDiscard.setChecked(true);
        }
        EditTextSave.setText(setting.getFileName());

        setTitle("Basic settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    public void Confirm(View v) {

        if(RadioButtonSave.isChecked()){
            setting.setSave(true);
        } else {
            setting.setSave(false);
        }

        setting.setFileName(EditTextSave.getText().toString());
        setting.setIntervalOfSending(Integer.parseInt(EditTextInftervalOfSending.getText().toString()));

        Intent intent = new Intent();
        intent.putExtra("file_name", setting.getFileName());
        intent.putExtra("interval_of_sending", setting.getIntervalOfSending());
        intent.putExtra("save", setting.getSave());
        setResult(RESULT_OK, intent);
        finish();
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
}
