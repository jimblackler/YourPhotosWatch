package net.jimblackler.yourphotoswatch;

import android.util.JsonReader;

import java.io.IOException;

public class ReaderUtil {
  static class ReaderException extends Exception {
    ReaderException(String str) {
      super(str);
    }

    public ReaderException(IOException e) {
      super(e);
    }
  }
  static String getText(JsonReader reader) throws IOException, ReaderException {
    String text = null;
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      if (name.equals("$t"))
        text = reader.nextString();
      else
        reader.skipValue();
    }
    reader.endObject();
    return text;
  }
}
