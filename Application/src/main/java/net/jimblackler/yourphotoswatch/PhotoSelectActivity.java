package net.jimblackler.yourphotoswatch;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import net.jimblackler.yourphotoswatch.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PhotoSelectActivity extends Activity implements
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    DataApi.DataListener {

  private GoogleApiClient googleApiClient;
  private PhotoRecyclerAdapter recyclerAdapter;
  private Map<Long, PhotoListEntry> photosById;

  @Override
  public void onConnected(Bundle bundle) {
    Wearable.DataApi.addListener(googleApiClient, this);

    Wearable.DataApi.getDataItems(googleApiClient).setResultCallback(new ResultCallback<DataItemBuffer>() {
      @Override
      public void onResult(DataItemBuffer dataItems) {
        for (DataItem dataItem : dataItems) {
          String[] parts = dataItem.getUri().getPath().split("/");
          switch (parts[1]) {
            case "image":
              PhotoListEntry photoListEntry = photosById.get(Long.parseLong(parts[2]));
              photoListEntry.setEnabled(true);
              recyclerAdapter.notifyItemChanged(photoListEntry.getPosition());
              break;
            default:
              break;
          }
        }
      }
    });
  }

  @Override
  public void onConnectionSuspended(int i) {
    Wearable.DataApi.removeListener(googleApiClient, this);
  }

  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
    Wearable.DataApi.removeListener(googleApiClient, this);
  }

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
    photosById = new HashMap<>();
    int position = 0;
    while (cursor.moveToNext()) {
      PhotoListEntry photoListEntry = new PhotoListEntry(cursor, position);
      entries.add(photoListEntry);
      photosById.put(photoListEntry.getImageId(), photoListEntry);
      position++;
    }

    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);

    recyclerView.setHasFixedSize(true);
    recyclerAdapter =
        new PhotoRecyclerAdapter(entries, new PhotoRecyclerAdapter.PhotoListEntryObserver() {
      @Override
      public void modified(final PhotoListEntry listEntry) {
        new AsyncTask<Void, Void, Void>(){
          @Override
          protected Void doInBackground(Void... params) {
            if (googleApiClient.isConnected()) {

              String path = "/image/" + Long.toString(listEntry.getImageId());

              if (listEntry.isEnabled()) {
                PutDataMapRequest dataMap = PutDataMapRequest.create(path);

                Bitmap bitmap = listEntry.getBitmap(getContentResolver());

                try {
                  AutoCropper autoCropper = new AutoCropper(PhotoSelectActivity.this);
                  bitmap = autoCropper.crop(bitmap);
                } catch (IOException e) {
                  e.printStackTrace();
                }

                dataMap.getDataMap().putAsset("photo", toAsset(bitmap));
                dataMap.getDataMap().putLong("time", new Date().getTime());
                PutDataRequest request = dataMap.asPutDataRequest();
                Wearable.DataApi.putDataItem(googleApiClient, request);
              } else {
                Wearable.DataApi.deleteDataItems(googleApiClient, Uri.parse("wear:" + path));
              }
            }
            return null;
          }
        }.execute();

      }
    });

    recyclerView.setAdapter(recyclerAdapter);
    LinearLayoutManager layout = new GridLayoutManager(this, 3);
    recyclerView.setLayoutManager(layout);
    recyclerView.setItemAnimator(new DefaultItemAnimator());

    googleApiClient = new GoogleApiClient.Builder(this)
        .addApi(Wearable.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
  }

  @Override
  protected void onStart() {
    super.onStart();
    googleApiClient.connect();
  }

  @Override
  protected void onStop() {
    Wearable.DataApi.removeListener(googleApiClient, this);
    super.onStop();

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_select_pictures, menu);
    int sortOrder = getIntent().getIntExtra("sort", R.id.oldest_first);
    menu.findItem(sortOrder).setChecked(true);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    switch(id) {
      case R.id.newest_first:
      case R.id.oldest_first: {
        Intent intent = new Intent(this, PhotoSelectActivity.class);
        intent.putExtra("sort", id);
        finish();
        startActivity(intent);
        overridePendingTransition(0, 0);
        break;
      }
    }

    return super.onOptionsItemSelected(item);
  }

  private static Asset toAsset(Bitmap bitmap) {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
    return Asset.createFromBytes(byteStream.toByteArray());
  }

  @Override
  public void onDataChanged(DataEventBuffer dataEvents) {

  }
}
