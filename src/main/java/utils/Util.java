package utils;

import readTest.TestUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by dell on 2017/11/27.
 */
public class Util {

  private static final int MB = 1024 * 1024;

  public static void main(String[] args) throws IOException {
    String filename = args[0];
    String filename2 = args[1];
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
    String line = null;
    BufferedWriter bw = new BufferedWriter(new FileWriter(filename2));
    while ((line = br.readLine()) != null) {
      String [] splits = line.split(",");
      Input input = new Input(splits[0], Double.parseDouble(splits[1]), Double.parseDouble(splits[2]), Double.parseDouble(splits[3]), Double.parseDouble(splits[4]));
      Output output = convertMsToThroughput(input);
     bw.write(output.toString());
    }
    br.close();
    bw.close();
  }

  public static Output convertMsToThroughput(Input input) {
    return new Output(convertBperMstoMBperS(input.getSize()/input.getTime1()), convertBperMstoMBperS(input.getSize()/input.getTime2()), convertBperMstoMBperS(input.getSize()/input.getTime3()), convertBperMstoMBperS(input.getSize()/input.getTime4()));
  }

  private static double convertBperMstoMBperS(double input) {
    return input/MB * 1000 * 250;//* 250是个trick，因为源文件的size是读了250次的
  }

  private static class Input {
    private final String size;
    private final double time1;
    private final double time2;
    private final double time3;
    private final double time4;

    public Input(String size, double time1, double time2,double time3, double time4) {
      this.size = size;
      this.time1 = time1;
      this.time2 = time2;
      this.time3 = time3;
      this.time4 = time4;
    }

    public int getSize() {
      return (int)TestUtil.convertHumantoSize(size);
    }

    public double getTime1() {
      return time1;
    }

    public double getTime2() {
      return time2;
    }

    public double getTime3() {
      return time3;
    }

    public double getTime4() {
      return time4;
    }
  }

  private static class Output {
    private final double throughput1;
    private final double throughput2;
    private final double throughput3;
    private final double throughput4;
    public Output(double throughput1, double throughput2, double throughput3, double throughput4) {
      this.throughput1 = throughput1;
      this.throughput2 = throughput2;
      this.throughput3 = throughput3;
      this.throughput4 = throughput4;
    }

    @Override public String toString() {
      return throughput1 + "," + throughput2 + "," + throughput3 + "," + throughput4 + '\n';
    }
  }
}
