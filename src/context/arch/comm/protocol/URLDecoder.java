package context.arch.comm.protocol;

import java.io.ByteArrayOutputStream;

/** 
 * This class decodes application/x-www-form-urlencoded MIME types 
 * doing the inverse of java.net.URLEncoder.
 *
 * @see java.net.URLEncoder
 */
public class URLDecoder {

  /**
   * Empty constructor for URLDecoder
   */
  private URLDecoder() {
  }

  /**
  * Translates String from x-www-form-urlEncoded format into text 
  *
  * @param s String to be translated
  * @return the translated String
  */
  public static String decode(String s) {

    ByteArrayOutputStream out = new ByteArrayOutputStream(s.length());
    for(int i=0; i<s.length(); i++) {
      int c = (int)s.charAt(i);
      if (c == '+') {
        out.write(' ');;
      }
      else if (c == '%') {
        int c1 = Character.digit(s.charAt(++i), 16);
        int c2 = Character.digit(s.charAt(++i), 16);
        out.write((char)(c1*16+c2));
      }
      else {
        out.write(c);
      }
    }
    return out.toString();
  }
}
