package readTest;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static readTest.Test.WarmUpDataFormat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by dell on 2017/12/3.
 */
public class MmapSize {
  private static final int testFileNum = 10;
  public static void main(String[] args) throws IOException {

    StorageType type = StorageType.valueOf(args[0]);
    String folder = (type == StorageType.ONDISK)?ReadFactory.onDiskFolder:ReadFactory.ramFolder;

    int size = (int)TestUtil.convertHumantoSize("1");
    for (int i = 0; i < testFileNum; i++) {
      size =  size * 2;
      double time = mmapSpecSize(folder + String.format(WarmUpDataFormat, i), size);
      System.out.println("size: " + size + " time: " + time);
    }
  }

  public static double mmapSpecSize(String filename, int size) throws IOException {
    System.out.println("filename: " + filename);
    long beginTime = System.nanoTime();
    RandomAccessFile dataFile =
        new RandomAccessFile(filename, "r");
    FileChannel dataChannel = dataFile.getChannel();
    //实现里面不能超过Integer.MAX_VALUE
    ByteBuffer buffer = dataChannel.map(READ_ONLY, 0,size);
    long endTime = System.nanoTime();
    return (endTime - beginTime)/1e6;
  }
}
