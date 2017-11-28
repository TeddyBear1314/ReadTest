package readTest;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by dell on 2017/11/23.
 */
public class GenFile {
  private static Random rand = new Random();
  private static final String SAMPLE_STRING = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890\n";

  //generate a fixed-length string with random contents.
  private static String genRandomValue(int num) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < num; i++) {
      sb.append(SAMPLE_STRING.charAt(rand.nextInt(SAMPLE_STRING.length())));
    }
    return sb.toString();
  }


  public static void genFileOfSpecifiedSize(String filename, String humanReadableSize)
      throws IOException {
    long size = TestUtil.convertHumantoSize(humanReadableSize);
    FileOutputStream fout = new FileOutputStream(filename);
    BufferedOutputStream out = new BufferedOutputStream(fout);
    byte[] outBytes = new byte[0];
    while (size > 0) {
     outBytes = genRandomValue(100).getBytes();
     if (size > outBytes.length) {
       out.write(outBytes);
       size -= outBytes.length;
     } else {
       break;
     }
    }
    //此时size已经很小了
    out.write(outBytes, 0, (int)size);
    out.flush();
    out.close();
  }

}
