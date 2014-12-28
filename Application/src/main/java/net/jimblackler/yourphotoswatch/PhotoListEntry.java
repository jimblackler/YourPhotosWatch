package net.jimblackler.yourphotoswatch;

import android.content.ContentResolver;
import android.graphics.Bitmap;

public abstract class PhotoListEntry {

  private boolean enabled;
  private final int position;

  public PhotoListEntry(int position) {
    this.position = position;

  }

  public abstract Bitmap getBitmap(ContentResolver contentResolver);

  public abstract String getId();

  public int getPosition() {
    return position;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean value) {
    enabled = value;
  }
}
