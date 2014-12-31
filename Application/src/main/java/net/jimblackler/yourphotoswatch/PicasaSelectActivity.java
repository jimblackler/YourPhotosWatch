package net.jimblackler.yourphotoswatch;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class PicasaSelectActivity extends BasePhotoSelectActivity {
  static final int REQUEST_CODE_PICK_ACCOUNT = 1;
  static final int REQUEST_CODE_REQUEST_AUTHORIZATION = 2;

  @Override
  protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

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
          throw new RuntimeException(e);
        }
        return null;
      }

    }.execute();

  }

  protected void fetchPhotos() {
    String feed = "https://picasaweb.google.com/data/feed/base/user/101378164668882205053/albumid/1000000482205053?v=2";
    findViewById(R.id.progress).setVisibility(View.VISIBLE);
    new AsyncTask<String, Void, List<? extends PhotoListEntry>>() {
      @Override
      protected List<? extends PhotoListEntry> doInBackground(String... params) {
        try {

          List<PhotoListEntry> entriesOut = new ArrayList<>();
          HttpURLConnection connection = (HttpURLConnection) new URL(params[0]).openConnection();
          String token = PreferenceManager.getDefaultSharedPreferences(PicasaSelectActivity.this).
              getString("token", "");
          connection.setRequestProperty("Authorization", "Bearer " + token);
          connection.connect();
          int code = connection.getResponseCode();
          if (code == HttpURLConnection.HTTP_UNAUTHORIZED ||
              code == HttpURLConnection.HTTP_FORBIDDEN) {
            // Token cleared and forgotten.
            SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(PicasaSelectActivity.this);
            Editor editor = settings.edit();
            editor.remove("token");
            editor.commit();
            GoogleAuthUtil.clearToken(PicasaSelectActivity.this, token);
            authenticateFromEmailAndFetch();
            return null;
          }
          InputStream inputStream = connection.getInputStream();

          XPathFactory factory = XPathFactory.newInstance();
          XPath xPath = factory.newXPath();
          xPath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
              switch (prefix) {
                case "ns":
                  return "http://www.w3.org/2005/Atom";
                case "gphoto":
                  return "http://schemas.google.com/photos/2007";
                case "media":
                  return "http://search.yahoo.com/mrss/";
                case "openSearch":
                  return "http://a9.com/-/spec/opensearchrss/1.0/";
                default:
                  return null;
              }
            }

            @Override
            public String getPrefix(String namespaceURI) {
              return null;
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
              return null;
            }
          });
          NodeList entries = (NodeList) xPath.evaluate("/ns:feed/ns:entry",
              new InputSource(new InputStreamReader(inputStream)), XPathConstants.NODESET);
          for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            entriesOut.add(new PicasaPhotoListEntry(entry, xPath, i));
          }
          return entriesOut;

        } catch (XPathExpressionException | IOException | GoogleAuthException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      protected void onPostExecute(List<? extends PhotoListEntry> entries) {
        if (entries != null)
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

}
