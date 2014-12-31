package net.jimblackler.yourphotoswatch;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;

public class PhonePhotoListEntry extends PhotoListEntry {
  private final long imageId;
  private int width;
  private int height;

  public PhonePhotoListEntry(Cursor cursor, int position) {
    super(position);
    imageId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));

    // Guess at the width and height (actually this is not particularly reliable).
    if (cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.WIDTH)) >
      cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT))) {
      width = 512;
      height = 384;
    } else {
      width = 384;
      height = 512;
    }
  }

  @Override
  public Bitmap getBitmap(ContentResolver contentResolver) {
    Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(contentResolver,
        imageId, MediaStore.Images.Thumbnails.MINI_KIND, null);
    width = bitmap.getWidth();
    height = bitmap.getWidth();
    return bitmap;
  }

  @Override
  public String getId() {
    return "phone_" + imageId;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

}
