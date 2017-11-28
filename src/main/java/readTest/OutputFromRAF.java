package readTest;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by dell on 2017/11/22.
 */
public class OutputFromRAF {
  private byte [] resultPacket;
  //packet最多几kb
  public OutputFromRAF(String humanReadableSize) {
    int size = (int)TestUtil.convertHumantoSize(humanReadableSize);
    resultPacket = new byte[size];
  }

  protected void outputRAFReadTime(RandomAccessFile raf)
      throws IOException {
    int times = (int)(raf.length() / resultPacket.length);
    int left = (int)(raf.length() % resultPacket.length);
    for (int i = 0; i < times; i++) {
      raf.read(resultPacket, 0, resultPacket.length);
    }
    raf.read(resultPacket, 0, left);
  }
}
