package readTest;

import alluxio.AlluxioURI;
import alluxio.client.file.FileSystem;
import alluxio.exception.AlluxioException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

/**
 * Created by dell on 2017/11/23.
 */
public class GenFile {
  private static Random rand = new Random();
  private static final String SAMPLE_STRING = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890";

  //generate a fixed-length string with random contents.
  private static String genRandomValue(int num) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < num; i++) {
      sb.append(SAMPLE_STRING.charAt(rand.nextInt(SAMPLE_STRING.length())));
    }
    return sb.toString();
  }


  public static void genFileOfSpecifiedSize(String filename, String humanReadableSize,String scheme)
      throws IOException, AlluxioException, URISyntaxException {
    long size = TestUtil.convertHumantoSize(humanReadableSize);
    OutputStream fout = Factory.create(scheme, filename);
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



  private static class Factory {
    public static OutputStream create(String scheme, String filename)
        throws IOException, AlluxioException, URISyntaxException {
      if ("alluxio".equals(scheme)) {
        FileSystem fs = FileSystem.Factory.get();
        return fs.createFile(new AlluxioURI(filename));
      } else if ("hdfs".equals(scheme)) {
        Configuration conf = new Configuration();
        org.apache.hadoop.fs.FileSystem fs = org.apache.hadoop.fs.FileSystem.get(new URI("hdfs://slave018/"), conf);
        return fs.create(new Path(filename));
      }else {
        return new FileOutputStream(filename);
      }
    }
  }

}
