package net.jimblackler.yourphotoswatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

  private static final String TAG = MainActivity.class.getName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


    setContentView(R.layout.main_activity);
    findViewById(R.id.select_from_phone).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(MainActivity.this, PhoneSelectActivity.class));
      }
    });

    findViewById(R.id.select_from_picasa).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(MainActivity.this, PicasaSelectActivity.class));
      }
    });

    findViewById(R.id.select_from_facebook).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(MainActivity.this, FacebookSelectActivity.class));
      }
    });


    if (!getIntent().getAction().endsWith(".CONFIG")) {
      findViewById(R.id.launch_config).setVisibility(View.VISIBLE);
      findViewById(R.id.launch_config).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          startActivity(getPackageManager().getLaunchIntentForPackage("com.google.android.wearable.app"));
        }
      });
    }

    findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(MainActivity.this, AboutActivity.class));
      }
    });



  }

}
