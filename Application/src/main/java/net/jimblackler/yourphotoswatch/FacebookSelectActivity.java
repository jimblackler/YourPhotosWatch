package net.jimblackler.yourphotoswatch;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FacebookSelectActivity extends BasePhotoSelectActivity {

  private UiLifecycleHelper uiHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.photo_select_facebook_activity);
    setEntries(new ArrayList<PhotoListEntry>());
    uiHelper = new UiLifecycleHelper(this, new Session.StatusCallback() {
      @Override
      public void call(Session session,
                       SessionState state, Exception exception) {
        if (!session.isOpened())
          return;
        getPhotos(session);
      }
    });
    uiHelper.onCreate(savedInstanceState);
    LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
    loginButton.setReadPermissions(Arrays.asList("user_photos"));
    loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
      @Override
      public void onUserInfoFetched(GraphUser user) {
        if (user == null) {
          // User logged out.
          setEntries(new ArrayList<PhotoListEntry>());
        }
      }
    });
    Session session = Session.getActiveSession();
    if (session != null) {
      getPhotos(session);
    }
  }

  private void getPhotos(Session session) {
    Bundle params = new Bundle();
    params.putInt("limit", 500);
    new Request(session, "/me/photos/uploaded", params, HttpMethod.GET,
        new Request.Callback() {
          public void onCompleted(Response response) {
            FacebookRequestError error = response.getError();
            if (error != null) {
              System.out.println(error.toString());
              return;
            }
            AsyncTask<JSONObject, Void, List<PhotoListEntry>> asyncTask =
                new AsyncTask<JSONObject, Void, List<PhotoListEntry>>() {
                  @Override
                  protected List<PhotoListEntry> doInBackground(JSONObject... params) {
                    try {
                      List<PhotoListEntry> entries = new ArrayList<>();
                      int position = 0;
                      JSONObject obj = params[0];
                      JSONArray array = obj.getJSONArray("data");
                      for (int i = 0; i < array.length(); i++) {
                        JSONObject entry = array.getJSONObject(i);
                        entries.add(new FacebookPhotoListEntry(position, entry));
                        position++;
                      }
                      return entries;
                    } catch (JSONException e) {
                      throw new RuntimeException(e);
                    }
                  }

                  @Override
                  protected void onPostExecute(List<PhotoListEntry> entries) {
                    setEntries(entries);
                  }
                };

            asyncTask.execute(response.getGraphObject().getInnerJSONObject());

          }
        }
    ).executeAsync();
  }

  @Override
  public void onResume() {
    super.onResume();
    uiHelper.onResume();
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    uiHelper.onSaveInstanceState(outState);
  }

  @Override
  public void onPause() {
    super.onPause();
    uiHelper.onPause();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    uiHelper.onDestroy();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    uiHelper.onActivityResult(requestCode, resultCode, data);
  }
}
