package net.jimblackler.yourphotoswatch;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;

public class PhotoListEntry {
  private final long imageId;
  private final int position;
  private boolean enabled;

  public PhotoListEntry(Cursor cursor, int position) {
    this.position = position;
    imageId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
  }

  public Bitmap getBitmap(ContentResolver contentResolver) {
    return MediaStore.Images.Thumbnails.getThumbnail(contentResolver,
        imageId, MediaStore.Images.Thumbnails.MINI_KIND, null);
  }

  public long getImageId() {
    return imageId;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean value) {
    enabled = value;
  }

  public int getPosition() {
    return position;
  }
}
