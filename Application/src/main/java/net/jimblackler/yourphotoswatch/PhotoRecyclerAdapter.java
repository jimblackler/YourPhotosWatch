package net.jimblackler.yourphotoswatch;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhotoRecyclerAdapter extends RecyclerView.Adapter<PhotoRecyclerAdapter.ViewHolder> {

  private final Map<String, Bitmap> bitmapCache;
  private final PhotoListEntryObserver observer;
  private final Set<String> enabled;
  private List<? extends PhotoListEntry> items;
  private ContentResolver contentResolver;

  public PhotoRecyclerAdapter(List<? extends PhotoListEntry> items, Set<String> enabled,
                              PhotoListEntryObserver observer) {
    this.items = items;
    this.observer = observer;
    this.enabled = enabled;

    final int maximumSize = 80;
    bitmapCache = new LinkedHashMap<String, Bitmap>(maximumSize, 0.75f, true) {
      protected boolean removeEldestEntry(Map.Entry<String, Bitmap> eldest) {
        return size() > maximumSize;
      }
    };
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    contentResolver = parent.getContext().getContentResolver();
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_list_entry, parent, false);
    return new ViewHolder(view, bitmapCache, enabled, observer);
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
    private final Map<String, Bitmap> bitmapCache;
    private final PhotoListEntryObserver observer;
    private final CheckBox checkBox;
    private final AnimatorSet selectionAnimation;
    private final CompoundButton.OnCheckedChangeListener changeListener;
    private final Set<String> enabled;
    private ImageView imageView;
    private PhotoListEntry listEntry;
    private AsyncTask<PhotoListEntry, Void, Bitmap> fetcher;

    public ViewHolder(final View view, Map<String, Bitmap> bitmapCache,
                      final Set<String> enabled,
                      final PhotoListEntryObserver observer) {
      super(view);
      imageView = (ImageView) view.findViewById(R.id.imageView);

      this.bitmapCache = bitmapCache;
      this.observer = observer;
      this.enabled = enabled;
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
          if (isChecked)
            enabled.add(listEntry.getId());
          else
            enabled.remove(listEntry.getId());
          update(view.getContext().getContentResolver(), listEntry);
          observer.modified(listEntry);
        }
      };

    }

    public void update(final ContentResolver contentResolver, final PhotoListEntry listEntry) {
      checkBox.setOnCheckedChangeListener(null);
      this.listEntry = listEntry;
      try {
        imageView.setImageBitmap(Bitmap.createBitmap(listEntry.getWidth(),
            listEntry.getHeight(), Bitmap.Config.ARGB_8888));
      } catch (OutOfMemoryError e) {
        bitmapCache.clear();
        imageView.setImageBitmap(null);
      }
      if (fetcher != null)
        fetcher.cancel(false);

      checkBox.setOnCheckedChangeListener(null);
      checkBox.setChecked(enabled.contains(listEntry.getId()));

      Bitmap bitmap = bitmapCache.get(listEntry.getId());
      if (bitmap == null) {
        fetcher = new AsyncTask<PhotoListEntry, Void, Bitmap>() {
          @Override
          protected Bitmap doInBackground(PhotoListEntry... params) {
            try {
              Bitmap bitmap = params[0].getBitmap(contentResolver);
              bitmapCache.put(listEntry.getId(), bitmap);
              return bitmap;
            } catch (OutOfMemoryError e) {
              bitmapCache.clear();
              return null;
            }
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