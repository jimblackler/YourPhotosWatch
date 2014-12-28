package net.jimblackler.yourphotoswatch;


import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;

public class PhotoListEntryOnPhone extends PhotoListEntry {
  private final long imageId;

  public PhotoListEntryOnPhone(Cursor cursor, int position) {
    super(position);
    imageId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
  }

  @Override
  public Bitmap getBitmap(ContentResolver contentResolver) {
    return MediaStore.Images.Thumbnails.getThumbnail(contentResolver,
        imageId, MediaStore.Images.Thumbnails.MINI_KIND, null);
  }

  @Override
  public String getId() {
    return "phone_" + imageId;
  }
}
