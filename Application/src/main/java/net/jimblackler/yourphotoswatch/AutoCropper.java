package net.jimblackler.yourphotoswatch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.helper.opencv_objdetect;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.bytedeco.javacpp.opencv_core.CvRect;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvGetSeqElem;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_highgui.cvLoadImage;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_SCALE_IMAGE;
import static org.bytedeco.javacpp.opencv_objdetect.cvHaarDetectObjects;

public class AutoCropper {

  CvMemStorage storage;
  CvHaarClassifierCascade classifier;

  public AutoCropper(Context context) throws IOException {
    storage = CvMemStorage.create();  // Allocate the memory storage.
    // Preload the opencv_objdetect module to work around a known bug.
    Loader.load(opencv_objdetect.class);

    InputStream in = context.getAssets().open("haarcascade_frontalface_alt.xml");
    final File file = File.createTempFile("classifier", ".xml");
    streamToFile(in, file);
    in.close();
    String path = file.getPath();
    Pointer p = cvLoad(path);
    file.delete();
    classifier = new CvHaarClassifierCascade(p);
  }

  public static void streamToFile(InputStream in, File file) throws IOException {
    FileOutputStream out = new FileOutputStream(file);
    byte[] buffer = new byte[16384];
    int bytesRead;
    while ((bytesRead = in.read(buffer)) != -1) {
      out.write(buffer, 0, bytesRead);
    }
    out.close();
  }

  public Bitmap crop(Bitmap bitmap) {
    cvClearMemStorage(storage);
    boolean debugMode = false;
    try {
      File tempFile = File.createTempFile("output", ".png");
      FileOutputStream out = new FileOutputStream(tempFile);
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
      IplImage iplImage = cvLoadImage(tempFile.getAbsolutePath());
      tempFile.delete();

      CvSeq faces =
          cvHaarDetectObjects(iplImage, classifier, storage, 1.025, 3, CV_HAAR_SCALE_IMAGE);

      if (debugMode) {
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
      }

      float minX = Float.MAX_VALUE;
      float maxX = Float.MIN_VALUE;

      float minY = Float.MAX_VALUE;
      float maxY = Float.MIN_VALUE;

      for (int i = 0; i < faces.total(); i++) {
        CvRect rect = new CvRect(cvGetSeqElem(faces, i));
        if (debugMode) {
          Paint paint = new Paint();
          paint.setStyle(Paint.Style.STROKE);
          paint.setStrokeWidth(3);
          paint.setColor(Color.RED);
          Canvas canvas = new Canvas(bitmap);
          canvas.drawRect(rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + rect.height(), paint);
        }
        float extraX = rect.width() * 0.33f;
        float extraY = rect.height() * 0.33f;
        minX = Math.max(Math.min(rect.x() - extraX, minX), 0);
        maxX = Math.min(Math.max(rect.x() + rect.width() + extraX, maxX), bitmap.getWidth());
        minY = Math.max(Math.min(rect.y() - extraY, minY), 0);
        maxY = Math.min(Math.max(rect.y() + rect.height() + extraY, maxY), bitmap.getHeight());
      }

      float leftX;
      float rightX;
      float topY;
      float bottomY;
      if (bitmap.getHeight() > bitmap.getWidth()) {
        // Tall image.
        float centerY = bitmap.getHeight() / 2;
        topY = centerY - bitmap.getWidth() / 2;
        bottomY = centerY + bitmap.getWidth() / 2;
        leftX = 0;
        rightX = bitmap.getWidth();
        if (faces.total() > 0) {
          if (maxY - minY > bitmap.getWidth()) {
            centerY = (minY + maxY) / 2;
            topY = centerY - bitmap.getWidth() / 2;
            bottomY = centerY + bitmap.getWidth() / 2;
          } else if (minY < topY) {
            topY = minY;
            bottomY = topY + bitmap.getWidth();
          } else if (maxY > bottomY) {
            bottomY = maxY;
            topY = bottomY - bitmap.getWidth();
          }
        }
      } else {
        // Wide image.
        float centerX = bitmap.getWidth() / 2;
        leftX = centerX - bitmap.getHeight() / 2;
        rightX = centerX + bitmap.getHeight() / 2;
        topY = 0;
        bottomY = bitmap.getHeight();
        if (faces.total() > 0) {
          if (maxX - minX > bitmap.getHeight()) {
            centerX = (minX + maxX) / 2;
            leftX = centerX - bitmap.getHeight() / 2;
            rightX = centerX + bitmap.getHeight() / 2;
          } else if (minX < leftX) {
            leftX = minX;
            rightX = leftX + bitmap.getHeight();
          } else if (maxX > rightX) {
            rightX = maxX;
            leftX = rightX - bitmap.getHeight();
          }
        }
      }

      if (debugMode) {
        {
          Paint paint = new Paint();
          paint.setStyle(Paint.Style.STROKE);
          paint.setStrokeWidth(4);
          paint.setColor(Color.GREEN);
          Canvas canvas = new Canvas(bitmap);
          canvas.drawRect(leftX, topY, rightX, bottomY, paint);
        }

        {
          Paint paint = new Paint();
          paint.setStyle(Paint.Style.STROKE);
          paint.setStrokeWidth(2);
          paint.setColor(Color.BLUE);
          Canvas canvas = new Canvas(bitmap);
          canvas.drawRect(minX, minY, maxX, maxY, paint);
        }
      } else {
        bitmap = Bitmap.createBitmap(bitmap, (int) leftX, (int) topY,
            (int) (rightX - leftX), (int) (bottomY - topY));
      }

      return bitmap;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return bitmap;
  }
}