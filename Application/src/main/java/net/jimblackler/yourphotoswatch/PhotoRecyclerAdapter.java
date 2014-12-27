package net.jimblackler.yourphotoswatch;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import net.jimblackler.yourphotoswatch.R;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PhotoRecyclerAdapter extends RecyclerView.Adapter<PhotoRecyclerAdapter.ViewHolder> {

  private final Bitmap blankBitmap;
  private final Map<Long, Bitmap> bitmapCache;
  private final PhotoListEntryObserver observer;
  private List<PhotoListEntry> items;
  private ContentResolver contentResolver;

  public PhotoRecyclerAdapter(List<PhotoListEntry> items, PhotoListEntryObserver observer) {
    this.items = items;
    this.observer = observer;
    blankBitmap = Bitmap.createBitmap(512, 384, Bitmap.Config.ARGB_8888);
    final int maximumSize = 100;
    bitmapCache = new LinkedHashMap<Long, Bitmap>(maximumSize, 0.75f, true) {
      protected boolean removeEldestEntry(Map.Entry<Long, Bitmap> eldest) {
        return size() > maximumSize;
      }
    };
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    contentResolver = parent.getContext().getContentResolver();
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_list_entry, parent, false);
    return new ViewHolder(view, bitmapCache, blankBitmap, observer);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    final PhotoListEntry item = items.get(position);
    holder.update(contentResolver, item);
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  public interface PhotoListEntryObserver {
    void modified(PhotoListEntry listEntry);
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    private final Map<Long, Bitmap> bitmapCache;
    private final PhotoListEntryObserver observer;
    private final CheckBox checkBox;
    private final AnimatorSet selectionAnimation;
    private final CompoundButton.OnCheckedChangeListener changeListener;
    private ImageView imageView;
    private Bitmap blankBitmap;
    private PhotoListEntry listEntry;
    private AsyncTask<PhotoListEntry, Void, Bitmap> fetcher;

    public ViewHolder(final View view, Map<Long, Bitmap> bitmapCache, Bitmap blankBitmap,
                      final PhotoListEntryObserver observer) {
      super(view);
      imageView = (ImageView) view.findViewById(R.id.imageView);

      this.bitmapCache = bitmapCache;
      this.blankBitmap = blankBitmap;
      this.observer = observer;
      checkBox = (CheckBox) view.findViewById(R.id.checkBox);
      selectionAnimation =
          (AnimatorSet) AnimatorInflater.loadAnimator(view.getContext(), R.animator.select_photo);
      selectionAnimation.setTarget(view);
      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          checkBox.toggle();
        }
      });
      changeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          selectionAnimation.start();
          listEntry.setEnabled(checkBox.isChecked());
          update(view.getContext().getContentResolver(), listEntry);
          observer.modified(listEntry);
        }
      };

    }

    public void update(final ContentResolver contentResolver, final PhotoListEntry listEntry) {
      checkBox.setOnCheckedChangeListener(null);
      this.listEntry = listEntry;
      imageView.setImageBitmap(blankBitmap);
      if (fetcher != null)
        fetcher.cancel(false);

      checkBox.setOnCheckedChangeListener(null);
      checkBox.setChecked(listEntry.isEnabled());

      Bitmap bitmap = bitmapCache.get(listEntry.getImageId());
      if (bitmap == null) {
        fetcher = new AsyncTask<PhotoListEntry, Void, Bitmap>() {
          @Override
          protected Bitmap doInBackground(PhotoListEntry... params) {
            Bitmap bitmap = params[0].getBitmap(contentResolver);
            bitmapCache.put(listEntry.getImageId(), bitmap);
            return bitmap;
          }

          @Override
          protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
          }
        };
        fetcher.execute(listEntry);
      } else {
        imageView.setImageBitmap(bitmap);
      }
      checkBox.setOnCheckedChangeListener(changeListener);
    }

  }
}