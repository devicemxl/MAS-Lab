package utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Base64 {
  public static String encode(byte[] raw) {
    StringBuffer encoded = new StringBuffer();
    for (int i = 0; i < raw.length; i += 3)
      encoded.append(encodeBlock(raw, i)); 
    return encoded.toString();
  }
  
  protected static char[] encodeBlock(byte[] raw, int offset) {
    int block = 0;
    int slack = raw.length - offset - 1;
    int end = (slack >= 2) ? 2 : slack;
    for (int i = 0; i <= end; i++) {
      byte b = raw[offset + i];
      int neuter = (b < 0) ? (b + 256) : b;
      block += neuter << 8 * (2 - i);
    } 
    char[] base64 = new char[4];
    for (int j = 0; j < 4; j++) {
      int sixbit = block >>> 6 * (3 - j) & 0x3F;
      base64[j] = getChar(sixbit);
    } 
    if (slack < 1)
      base64[2] = '='; 
    if (slack < 2)
      base64[3] = '='; 
    return base64;
  }
  
  protected static char getChar(int sixbit) {
    if (sixbit >= 0 && sixbit <= 25)
      return (char)(65 + sixbit); 
    if (sixbit >= 26 && sixbit <= 51)
      return (char)(97 + sixbit - 26); 
    if (sixbit >= 52 && sixbit <= 61)
      return (char)(48 + sixbit - 52); 
    if (sixbit == 62)
      return '+'; 
    if (sixbit == 63)
      return '/'; 
    return '?';
  }
  
  public static byte[] decode(String base64) {
    int pad = 0;
    for (int i = base64.length() - 1; base64.charAt(i) == '='; i--)
      pad++; 
    int length = base64.length() * 6 / 8 - pad;
    byte[] raw = new byte[length];
    int rawindex = 0;
    for (int j = 0; j < base64.length(); j += 4) {
      int block = (getValue(base64.charAt(j)) << 18) + (getValue(base64.charAt(j + 1)) << 12) + (getValue(base64.charAt(j + 2)) << 6) + getValue(base64.charAt(j + 3));
      for (int k = 0; k < 3 && rawindex + k < raw.length; k++)
        raw[rawindex + k] = (byte)(block >> 8 * (2 - k) & 0xFF); 
      rawindex += 3;
    } 
    return raw;
  }
  
  protected static int getValue(char c) {
    if (c >= 'A' && c <= 'Z')
      return c - 65; 
    if (c >= 'a' && c <= 'z')
      return c - 97 + 26; 
    if (c >= '0' && c <= '9')
      return c - 48 + 52; 
    if (c == '+')
      return 62; 
    if (c == '/')
      return 63; 
    if (c == '=')
      return 0; 
    return -1;
  }
  
  public static Object stringToObject(String s) {
    byte[] snbytes = null;
    snbytes = decode(s);
    ByteArrayInputStream byteStream = new ByteArrayInputStream(snbytes);
    try {
      ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
      return is.readObject();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public static String objectToString(Object o) {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream(32000);
    try {
      ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
      os.flush();
      os.writeObject(o);
      os.flush();
    } catch (IOException e) {
      e.printStackTrace();
    } 
    byte[] objectBuf = byteStream.toByteArray();
    String mdstring = encode(objectBuf);
    return mdstring;
  }
}
