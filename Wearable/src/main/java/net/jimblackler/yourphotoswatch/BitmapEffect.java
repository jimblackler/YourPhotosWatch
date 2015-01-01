package net.jimblackler.yourphotoswatch;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;

public class BitmapEffect {
  public static Bitmap createShadow(Bitmap in) {
    Bitmap out = Bitmap.createBitmap(in.getWidth(), in.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(out);
    Paint blurPaint = new Paint();
    blurPaint.setMaskFilter(new BlurMaskFilter(6, BlurMaskFilter.Blur.NORMAL));
    int[] offsetXY = new int[2];
    Bitmap alpha = in.extractAlpha(blurPaint, offsetXY);
    Paint alphaPaint = new Paint();
    alphaPaint.setColor(Color.BLACK);
    canvas.drawBitmap(alpha, offsetXY[0] + 2, offsetXY[1] + 3, alphaPaint);
    alpha.recycle();
    return out;
  }
}
