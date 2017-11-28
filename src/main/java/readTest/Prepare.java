package readTest;

import java.io.IOException;

/**
 * Created by dell on 2017/11/23.
 */
public class Prepare {
  public static void main(String[] args) throws IOException {
    String fileSize = args[0];
    GenFile.genFileOfSpecifiedSize(ReadFactory.onDiskFolder + "test" + fileSize, fileSize);
  }
}
