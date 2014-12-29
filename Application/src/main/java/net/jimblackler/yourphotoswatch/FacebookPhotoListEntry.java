package net.jimblackler.yourphotoswatch;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class FacebookPhotoListEntry extends PhotoListEntry {
  String id;
  Uri imageUri;
  public FacebookPhotoListEntry(int position, JSONObject entry) {
    super(position);
    try {
      JSONArray images = entry.getJSONArray("images");
      JSONObject bestImage = null;
      int target = 320;
      for (int j = 0; j < images.length(); j++) {
        JSONObject image = images.getJSONObject(j);
        if (bestImage == null) {
          bestImage = image;
          continue;
        }
        int bestImageScore = Math.min(bestImage.getInt("width"), bestImage.getInt("height"));
        int thisScore = Math.min(image.getInt("width"), image.getInt("height"));
        if (bestImageScore < target) {
          if (thisScore > bestImageScore)
            bestImage = image;
          continue;
        }
        if (thisScore < target)
          continue;
        if (thisScore < bestImageScore)
          bestImage = image;
      }
      imageUri = Uri.parse(bestImage.getString("source"));
      id = entry.getString("id");
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Bitmap getBitmap(ContentResolver contentResolver) {
    try {
      URL url = new URL(imageUri.toString());
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoInput(true);
      connection.connect();
      InputStream input = connection.getInputStream();
      return BitmapFactory.decodeStream(input);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String getId() {
    return "facebook_" + id;
  }
}
