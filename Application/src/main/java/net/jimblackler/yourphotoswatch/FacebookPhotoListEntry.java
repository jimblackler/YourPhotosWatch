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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FacebookPhotoListEntry extends InternetPhotoListEntry {
  private final Date publishDate;
  private final String id;
  private final int width;
  private final int height;

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
      DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

      String createdTimeString = entry.getString("created_time");
      publishDate = format.parse(createdTimeString);
      width = bestImage.getInt("width");
      height = bestImage.getInt("height");

    } catch (JSONException | ParseException e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  public String getId() {
    return "facebook_" + id;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  public Date getPublishDate() {
    return publishDate;
  }
}
