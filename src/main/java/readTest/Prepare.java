package readTest;

import alluxio.exception.AlluxioException;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by dell on 2017/11/23.
 */
public class Prepare {
  public static void main(String[] args) throws IOException, AlluxioException, URISyntaxException {
    String fileSize = args[0];
    GenFile.genFileOfSpecifiedSize(ReadFactory.onDiskFolder + "test" + fileSize, fileSize, "local");
  }
}
