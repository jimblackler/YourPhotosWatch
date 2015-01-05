package net.jimblackler.yourphotoswatch;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.JsonReader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.jimblackler.yourphotoswatch.ReaderUtil.ReaderException;

public class PicasaSelectActivity extends BasePhotoSelectActivity {
  static final int REQUEST_CODE_PICK_ACCOUNT = 1;
  static final int REQUEST_CODE_REQUEST_AUTHORIZATION = 2;

  @Override
  protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

    if (resultCode == RESULT_CANCELED)
      finish();

    if (resultCode != RESULT_OK)
      return;

    switch (requestCode) {
      case REQUEST_CODE_PICK_ACCOUNT:
      case REQUEST_CODE_REQUEST_AUTHORIZATION:
        String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

        SharedPreferences settings =
            PreferenceManager.getDefaultSharedPreferences(PicasaSelectActivity.this);
        Editor editor = settings.edit();
        editor.putString("email", email);
        editor.commit();

        authenticateFromEmailAndFetch();
        break;
    }
  }

  private void authenticateFromEmailAndFetch() {
    final SharedPreferences settings =
        PreferenceManager.getDefaultSharedPreferences(PicasaSelectActivity.this);

    final String email = settings.getString("email", "");
    if (email.equals("")) {
      String[] accountTypes = new String[]{"com.google"};
      Intent intent = AccountPicker.newChooseAccountIntent(null, null,
          accountTypes, false, null, null, null, null);
      startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
      return;
    }
    if (PreferenceManager.getDefaultSharedPreferences(this).contains("token")) {
      fetchPhotos();
      return;
    }

    new AsyncTask<Void, Void, String>() {
      @Override
      protected String doInBackground(Void... params) {
        try {
          Editor editor;
          String scope = "oauth2:https://picasaweb.google.com/data/";
          String token = GoogleAuthUtil.getToken(PicasaSelectActivity.this, email, scope);

          editor = settings.edit();
          editor.putString("token", token);
          editor.commit();

          fetchPhotos();
        } catch (UserRecoverableAuthException e) {
          startActivityForResult(e.getIntent(), REQUEST_CODE_REQUEST_AUTHORIZATION);
        } catch (GoogleAuthException | IOException e) {
          e.printStackTrace();
          finish();
        }
        return null;
      }

    }.execute();

  }

  protected void fetchPhotos() {
    final SharedPreferences settings =
        PreferenceManager.getDefaultSharedPreferences(PicasaSelectActivity.this);
    final String email = settings.getString("email", "");
    String feed = "https://picasaweb.google.com/data/feed/api/user/" + email + "?kind=photo&max-results=5000&alt=json&fields=entry(published,title,media:group(media:title,media:content))";

    runOnUiThread(new Runnable(){
      @Override
      public void run() {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
      }
    });

    new AsyncTask<String, Void, List<? extends PhotoListEntry>>() {
      @Override
      protected List<? extends PhotoListEntry> doInBackground(String... params) {
        try {
          HttpURLConnection connection = (HttpURLConnection) new URL(params[0]).openConnection();
          String token = PreferenceManager.getDefaultSharedPreferences(PicasaSelectActivity.this).
              getString("token", "");
          connection.setRequestProperty("Authorization", "Bearer " + token);
          connection.connect();
          int code = connection.getResponseCode();
          if (code == HttpURLConnection.HTTP_UNAUTHORIZED ||
              code == HttpURLConnection.HTTP_FORBIDDEN) {
            // Token cleared and forgotten.
            Editor editor = settings.edit();
            editor.remove("token");
            editor.commit();
            GoogleAuthUtil.clearToken(PicasaSelectActivity.this, token);
            authenticateFromEmailAndFetch();
            return null;
          }

          final List<PicasaPhotoListEntry> entries = new ArrayList<>();
          final InputStream inputStream = connection.getInputStream();
          JsonReader reader = new JsonReader(new InputStreamReader(inputStream));


            reader.beginObject();
            while (reader.hasNext()) {
              String name = reader.nextName();
              switch (name) {
                case "feed":
                  reader.beginObject();
                  while (reader.hasNext()) {
                    name = reader.nextName();
                    switch (name) {
                      case "entry":
                        reader.beginArray();
                        while (reader.hasNext()) {
                          PicasaPhotoListEntry entry =
                              new PicasaPhotoListEntry(reader, entries.size());
                          if (entry.isValid())
                            entries.add(entry);
                        }
                        reader.endArray();
                        break;
                      default:
                        reader.skipValue();
                    }
                  }
                  reader.endObject();
                  break;
                default:
                reader.skipValue();

              }
            }
            reader.endObject();

          final int sortOrder = getIntent().getIntExtra("sort", R.id.oldest_first);

          Collections.sort(entries, new Comparator<PicasaPhotoListEntry>() {
            @Override
            public int compare(PicasaPhotoListEntry lhs, PicasaPhotoListEntry rhs) {
              if (sortOrder == R.id.oldest_first)
                return lhs.getPublishDate().compareTo(rhs.getPublishDate());
              else
                return rhs.getPublishDate().compareTo(lhs.getPublishDate());
            }
          });

          return entries;

        } catch (IOException | GoogleAuthException | ReaderException e) {
          e.printStackTrace();
          return null;
        }
      }

      @Override
      protected void onPostExecute(List<? extends PhotoListEntry> entries) {

        setEntries(entries);
      }
    }.execute(feed);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.photo_select_activity);
    setEntries(new ArrayList<PhotoListEntry>());
    authenticateFromEmailAndFetch();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (!super.onCreateOptionsMenu(menu))
      return false;
    menu.add(R.string.picasa_logout).setOnMenuItemClickListener(
        new MenuItem.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        new AsyncTask<Void, Void, Void>(){
          @Override
          protected Void doInBackground(Void... params) {
            SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(PicasaSelectActivity.this);
            try {
              Editor editor = settings.edit();
              if (settings.contains("token")) {
                editor.remove("token");
                GoogleAuthUtil.clearToken(PicasaSelectActivity.this, settings.getString("token", ""));
              }
              if (settings.contains("email")) {
                editor.remove("email");
              }
              editor.commit();
            } catch (IOException | GoogleAuthException e) {
              e.printStackTrace();
            }
            finish();
            return null;
          }
        }.execute();

        return true;
      }
    });

    return true;

  }
}
