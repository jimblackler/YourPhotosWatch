package net.jimblackler.yourphotoswatch;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class PhoneSelectActivity extends BasePhotoSelectActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.photo_select_activity);

    ContentResolver contentResolver = getContentResolver();

    Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    int sortOrder = getIntent().getIntExtra("sort", R.id.oldest_first);
    String order = MediaStore.Images.Media.DATE_TAKEN + " " +
        (sortOrder == R.id.oldest_first ? "ASC" : "DESC");

    Cursor cursor = contentResolver.query(uri, null, null, null, order);

    List<PhotoListEntry> entries = new ArrayList<>();

    int position = 0;
    while (cursor.moveToNext()) {
      PhotoListEntry photoListEntry = new PhonePhotoListEntry(cursor, position);
      entries.add(photoListEntry);
      position++;
    }
    cursor.close();
    setEntries(entries);
  }

}
