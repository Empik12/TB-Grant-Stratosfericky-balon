package fiit.baranek.tomas.gpssky;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import fiit.baranek.tomas.gpssky.Settings.BasicSettings;

public class BasicSettingsActivity extends AppCompatActivity {

    private BasicSettings setting = new BasicSettings();
    private EditText EditTextSave;
    private EditText EditTextInftervalOfSending;
    private RadioButton RadioButtonSave;
    private RadioButton RadioButtonDiscard;
    private RadioGroup RadioGroup;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_settings);

        EditTextSave = (EditText) findViewById(R.id.editTextSave);
        EditTextInftervalOfSending = (EditText) findViewById(R.id.editTextIntervalOfSending);
        RadioButtonSave = (RadioButton) findViewById(R.id.radioButtonSave);
        RadioButtonDiscard = (RadioButton) findViewById(R.id.radioButtonDiscard);
        RadioGroup = (RadioGroup) findViewById(R.id.RadioGroup);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        RadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radioButtonSave)
                    EditTextSave.setEnabled(true);
                else {
                    EditTextSave.setText("");
                    EditTextSave.setEnabled(false);
                }
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                System.out.println("Button id: " + String.valueOf(checkedId));

            }
        });


        setting.setIntervalOfSending(getIntent().getIntExtra("interval_of_sending", 5));
        setting.setSave(getIntent().getBooleanExtra("save", false));
        setting.setFileName(getIntent().getStringExtra("file_name"));

        EditTextInftervalOfSending.setText(String.valueOf(setting.getIntervalOfSending()));
        if(setting.getSave()){
            RadioButtonSave.setChecked(true);
            EditTextSave.setEnabled(true);
        } else {
            RadioButtonDiscard.setChecked(true);
            EditTextSave.setEnabled(false);
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

        if(setting.getIntervalOfSending() >= 10 ) {
            if(setting.getSave() && setting.getFileName().equals("")){
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Please enter file name", Snackbar.LENGTH_LONG);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.RED);

                snackbar.show();
            }else {
                final File DirectoryForData = new File(getApplicationContext().getExternalFilesDirs(null)[1], setting.getFileName());
                if(DirectoryForData.exists() && setting.getSave()) {
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Warning")
                            .setMessage("This file already exists. Do you want to save to this file?")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.putExtra("file_name", setting.getFileName());
                                    intent.putExtra("interval_of_sending", setting.getIntervalOfSending());
                                    intent.putExtra("save", setting.getSave());
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else {
                    DirectoryForData.mkdirs();
                    Intent intent = new Intent();
                    if(setting.getSave())
                        intent.putExtra("file_name", setting.getFileName());
                    else
                        intent.putExtra("file_name", "");
                    intent.putExtra("interval_of_sending", setting.getIntervalOfSending());
                    intent.putExtra("save", setting.getSave());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        } else {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Bad interval of sending", Snackbar.LENGTH_LONG);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.RED);

            snackbar.show();
        }
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