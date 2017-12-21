package readTest;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 先假定只有一个文件
 */
public class MmapRead  extends OutputFromBuffer implements Read{
  private String filename;
  private ByteBuffer buffer;
  private RandomAccessFile dataFile;
  private FileChannel dataChannel;
  public MmapRead(String filename, String humanReadableSize) {
    super(humanReadableSize);
    this.filename = filename;
  }

  public ByteBuffer mmapWholeFile() throws IOException {
      dataFile =
          new RandomAccessFile(filename, "r");
      dataChannel = dataFile.getChannel();
      //实现里面不能超过Integer.MAX_VALUE
     MappedByteBuffer res = dataChannel.map(READ_ONLY, 0, dataFile.length());
    System.out.println("is mappedByteBuffer loaded:" + res.isLoaded());
    return res;
  }

  @Override
  public void read1sttime() throws IOException {
    buffer = mmapWholeFile();
    outputBufferReadTime(buffer);
  }

  @Override
  public void readafter1sttime() {
    buffer.rewind();
    outputBufferReadTime(buffer);
  }

  public void close() throws IOException {
    dataChannel.close();
    dataFile.close();
  }

  public ByteBuffer getBuffer() {
    return buffer;
  }
}
