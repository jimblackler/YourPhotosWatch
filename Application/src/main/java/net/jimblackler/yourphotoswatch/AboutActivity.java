package net.jimblackler.yourphotoswatch;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.WebView;

import java.io.IOException;
import java.io.InputStream;

public class AboutActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.about_activity);

    WebView webView = (WebView) findViewById(R.id.webView1);

    try {
      InputStream summary = getAssets().open("about.html");
      String text = convertStreamToString(summary);
      PackageManager manager = getPackageManager();
      PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
      text = text.replace("$app", getResources().getText(
          getResources().getIdentifier("app_name", "string", getPackageName())));
      text = text.replace("$ver", info.versionName);
      webView.loadData(text, "text/html", null);
    } catch (IOException | PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
  }

  static String convertStreamToString(InputStream is) {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }


}
