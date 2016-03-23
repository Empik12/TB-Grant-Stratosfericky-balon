package fiit.baranek.tomas.gpssky;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class SMSsettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smssettings);

        setTitle("SMS notification settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    /*
        @Override
        protected void onDestroy(){
            super.onDestroy();
            Intent Myintent=new Intent(this, MainActivity.class).putExtra("SMSInformationSet", true);
            startActivity(Myintent);
        }

    */


    public void SetSmsSettings(){

    }

    @Override
    public  void finish() {
        Intent intent = new Intent(this, MainActivity.class);
        String PhoneNumber = "+421918573335";
        intent.putExtra("PhoneNumber",PhoneNumber);
        startActivity(intent);
        super.finish();

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
