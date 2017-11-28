package readTest;

import java.nio.ByteBuffer;

/**
 * Created by dell on 2017/11/22.
 */
public class OutputFromBuffer {
  private byte [] resultPacket;
  //我们这里的packet最多几KB
  public OutputFromBuffer(String humanReadableSize) {
    int size = (int)TestUtil.convertHumantoSize(humanReadableSize);
    resultPacket = new byte[size];
  }

  /**
   * write all the remaining content of the ByteBuffer into the resultPacket.
   * @param dataBuffer
   */
  protected void outputBufferReadTime(ByteBuffer dataBuffer) {
    int times = dataBuffer.remaining() / resultPacket.length;
    int left = dataBuffer.remaining() % resultPacket.length;
    for (int i = 0; i < times; i++) {
      dataBuffer.get(resultPacket, 0, resultPacket.length);
    }
    dataBuffer.get(resultPacket, 0, left);
  }
}
