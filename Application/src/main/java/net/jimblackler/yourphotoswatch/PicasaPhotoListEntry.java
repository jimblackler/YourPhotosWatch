package net.jimblackler.yourphotoswatch;

import android.net.Uri;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

class PicasaPhotoListEntry extends InternetPhotoListEntry {
  private final String id;
  private final Date publishDate;
  private int width;
  private int height;

  public PicasaPhotoListEntry(Element entry, XPath xPath, int position) throws XPathExpressionException {
    super(position);

    String uriString = xPath.evaluate("./media:group/media:content/@url", entry);
    imageUri = Uri.parse(uriString);
    id = xPath.evaluate("./media:group/media:title/text()", entry);
    width = Integer.parseInt(xPath.evaluate("./media:group/media:content/@width", entry));
    height = Integer.parseInt(xPath.evaluate("./media:group/media:content/@height", entry));

    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    String createdTimeString = xPath.evaluate("./ns:published/text()", entry);
    try {
      publishDate = format.parse(createdTimeString);
    } catch (ParseException e) {
      throw new RuntimeException(e);
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

}
