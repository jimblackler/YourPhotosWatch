package net.jimblackler.yourphotoswatch;


import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

abstract public class InternetPhotoListEntry extends PhotoListEntry {
  Uri imageUri;

  public InternetPhotoListEntry(int position) {
    super(position);
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
}
