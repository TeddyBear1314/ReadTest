package readTest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by dell on 2017/11/22.
 */
public class RAFRead extends OutputFromRAF implements Read{
  private String filename;
  private RandomAccessFile raf;
  public RAFRead(String filename,String humanReadableSize) {
    super(humanReadableSize);
    this.filename = filename;
  }

  public RandomAccessFile getRAFFromFolder()
      throws FileNotFoundException {
      return new RandomAccessFile(filename, "rw");
    }

  @Override
  public void read1sttime() throws IOException{
    raf = getRAFFromFolder();
   outputRAFReadTime(raf);
  }

  @Override
  public void readafter1sttime() throws IOException {
   raf.seek(0);
   outputRAFReadTime(raf);
  }
}
