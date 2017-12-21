import alluxio.AlluxioURI;
import alluxio.client.file.FileInStream;
import alluxio.client.file.FileSystem;
import alluxio.exception.AlluxioException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import readTest.GenFile;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by dell on 2017/12/20.
 */
public class CacheReadTest {
  private static final String SCHEME = "alluxio";
  private static final int PACKET_SIZE = 64 * 1024;
  private static byte [] packet1 = new byte[PACKET_SIZE];
  private static byte [] packet2 = new byte[PACKET_SIZE];
  private static int num = 1024 * 1024/64;
  public static void main(String[] args) throws IOException, AlluxioException, URISyntaxException {
    //GenFile.genFileOfSpecifiedSize(args[0], args[1], args[2]);
    for (int i = 0; i < 7; i++) {
      readFromFile("/warm" + i, "1", args[1]);
    }
    readFromFile(args[0],"1", args[1]);
   readFromFile(args[0], "2", args[1]);
  /*   readFromFile(args[0], 2, args[1]);
    readFromFile(args[0], 2, args[1]);
    readFromFile(args[0], 2, args[1]);
    readFromFile(args[0], 2, args[1]);*/
  }

  protected static void readFromFile(String filename, String index, String scheme)
      throws IOException, AlluxioException, URISyntaxException {
     InputStream is = Factory.create(filename, scheme);
    //FileOutputStream out = new FileOutputStream("./res" + index + ".txt");
     long beginTime = System.nanoTime();
    for (int i = 0; i < num; i++) {
      is.read(packet1);
     // out.write(packet1);
    }
    long endTime = System.nanoTime();
    System.out.println("file read time:" + (endTime - beginTime)/1e6 + " ms");
    beginTime = System.nanoTime();
    is.close();
    endTime = System.nanoTime();
    System.out.println("close time:" +(endTime - beginTime)/1e6 + " ms");
    //out.close();
  }

  private static class Factory {
    public static InputStream create(String filename, String scheme)
        throws IOException, AlluxioException, URISyntaxException {
       if ("alluxio".equals(scheme)) {
         FileSystem fs = FileSystem.Factory.get();
         return fs.openFile(new AlluxioURI(filename));
       } else if ("hdfs".equals(scheme)) {
         Configuration conf = new Configuration();
         org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.get(new URI("hdfs://slave018/"),conf);
         return fs.open(new Path(filename));
       }else {
         return new FileInputStream(filename);
       }
    }
  }
}
