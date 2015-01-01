package net.jimblackler.yourphotoswatch;

import android.net.Uri;
import android.util.JsonReader;

import net.jimblackler.yourphotoswatch.ReaderUtil.ReaderException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class PicasaPhotoListEntry extends InternetPhotoListEntry {

  private int height;
  private String id;
  private Date publishDate;
  private int width;
private boolean valid;
  public PicasaPhotoListEntry(JsonReader reader, int position) throws ReaderException {
    super(position);

    valid = true;
    try {

      reader.beginObject();
      while (reader.hasNext()) {
        String name = reader.nextName();
        switch (name) {
          case "published":
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            String createdTimeString = ReaderUtil.getText(reader);
            try {
              publishDate = format.parse(createdTimeString);
            } catch (ParseException e) {
              // Ignored by design.
            }
            break;
          case "media$group":
            reader.beginObject();
            while (reader.hasNext()) {
              name = reader.nextName();
              switch (name) {
                case "media$title":
                  id = ReaderUtil.getText(reader);
                  break;
                case "media$content":
                  int target = 320;
                  reader.beginArray();
                  while (reader.hasNext()) {
                    Uri uri = null;
                    int width = 0;
                    int height = 0;
                    String medium = "";
                    reader.beginObject();
                    while (reader.hasNext()) {
                      name = reader.nextName();
                      switch (name) {
                        case "url":
                          String uriString = reader.nextString();
                          uri = Uri.parse(uriString);
                          break;
                        case "width":
                          width = Integer.parseInt(reader.nextString());
                          break;
                        case "height":
                          height = Integer.parseInt(reader.nextString());
                          break;
                        case "medium":
                          if(reader.nextString().equals("video")) {
                            valid = false;
                          }
                          break;
                        default:
                          reader.skipValue();
                      }
                    }
                    reader.endObject();

                    int smallest = Math.min(width, height);
                    int best = Math.min(this.width, this.height);
                    if (this.imageUri == null) {
                      this.imageUri = uri;
                      this.width = width;
                      this.height = height;
                    } else if (smallest < target) {
                      if (smallest > best) {
                        this.imageUri = uri;
                        this.width = width;
                        this.height = height;
                      }
                    } else {
                      if (smallest < best) {
                        this.imageUri = uri;
                        this.width = width;
                        this.height = height;
                      }
                    }

                  }
                  reader.endArray();
                  break;
                default:
                  reader.skipValue();
              }

            }
            reader.endObject();
            break;
          default:
            reader.skipValue();
        }
      }
      reader.endObject();
    } catch (IOException e) {
      throw new ReaderException(e);
    }

  }

  @Override
  public String getId() {
    return "picasa_" + id;
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

  public boolean isValid() {
    return valid;
  }
}
