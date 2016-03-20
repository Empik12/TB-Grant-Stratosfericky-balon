package fiit.baranek.tomas.gpssky;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class BasicSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_settings);
        setTitle("Basic settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    /*
        @Override
        protected void onDestroy(){
            super.onDestroy();
            Intent Myintent=new Intent(this, MainActivity.class).putExtra("BasicInformationSet", true);
            startActivity(Myintent);
        }
    */
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
