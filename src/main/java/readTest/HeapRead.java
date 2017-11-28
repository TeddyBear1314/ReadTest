package readTest;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by dell on 2017/11/22.
 */
public class HeapRead extends OutputFromBuffer implements Read{
  private final int size;
  private ByteBuffer buffer;
  public HeapRead(String humanReadableSize, int size) {
    super(humanReadableSize);
    this.size = size;
  }

  public ByteBuffer allocateBuffersFromHeap() {
      return ByteBuffer.allocate(size);
  }

  @Override
  public void read1sttime() throws IOException {
   buffer = allocateBuffersFromHeap();
   outputBufferReadTime(buffer);
  }

  @Override
  public void readafter1sttime() throws IOException {
    buffer.flip();
    outputBufferReadTime(buffer);
  }
}
