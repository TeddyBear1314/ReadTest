package readTest;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 先假定只有一个文件
 */
public class MmapRead  extends OutputFromBuffer implements Read{
  private String filename;
  private ByteBuffer buffer;
  public MmapRead(String filename, String humanReadableSize) {
    super(humanReadableSize);
    this.filename = filename;
  }

  public ByteBuffer mmapWholeFile() throws IOException {
      RandomAccessFile dataFile =
          new RandomAccessFile(filename, "r");
      FileChannel dataChannel = dataFile.getChannel();
      //实现里面不能超过Integer.MAX_VALUE
     return dataChannel.map(READ_ONLY, 0, dataFile.length());
  }

  @Override
  public void read1sttime() throws IOException {
    buffer = mmapWholeFile();
    outputBufferReadTime(buffer);
  }

  @Override
  public void readafter1sttime() {
    buffer.flip();
    outputBufferReadTime(buffer);
  }
}
