import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by dell on 2017/11/13.
 */
public class Main {
  public static void main(String[] args) throws IOException {
    InputStreamReader isr = new InputStreamReader(System.in);
    BufferedReader br =  new BufferedReader(isr);
    String line =null;
    line = br.readLine();
    System.out.println(line);

    br.close();
    BufferedReader br2 = new BufferedReader(isr);
    while ((line = br2.readLine()) != null) {
      System.out.println(line);
    }
  }
}
