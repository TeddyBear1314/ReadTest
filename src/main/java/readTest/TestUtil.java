package readTest;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by dell on 2017/11/22.
 */
public class TestUtil {
  public static String convertSizeToHuman(long size) {
    float res = Float.parseFloat(String.format("%.2f", (double)size / (1024 * 1024 * 1024)));
    if (res > 1) return res + "GB";
    res = Float.parseFloat(String.format("%.2f",(double)size / (1024 * 1024)));
    if (res > 1) return res + "MB";
    res = Float.parseFloat(String.format("%.2f",(double)size / 1024));
    if (res > 1) return res + "KB";
    return size + "B";
  }

  //目前不知道怎么判断非法输入
  public static long convertHumantoSize(String size) {
    int i = 0;
    int num = 0;
    while (i < size.length()) {
      if (Character.isDigit(size.charAt(i))) {
        num = num * 10 + Character.getNumericValue(size.charAt(i));
      } else {
        break;
      }
      i++;
    }
    String unit = size.substring(i, size.length());
    if (unit.equalsIgnoreCase("k") || unit.equalsIgnoreCase("kb"))
      return num * 1024;
    else if (unit.equalsIgnoreCase("m") || unit.equalsIgnoreCase("mb"))
      return num * 1024 * 1024;
    else if (unit.equalsIgnoreCase("g") || unit.equalsIgnoreCase("gb"))
      return (long)num * 1024 * 1024 * 1024;
    return num;
  }

  public static void main(String[] args) {
    long size = convertHumantoSize("16B");
    System.out.println(size);
  }

  public static int getCurrentProcessId()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException,
      NoSuchFieldException {
    java.lang.management.RuntimeMXBean runtime =
        java.lang.management.ManagementFactory.getRuntimeMXBean();
    java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
    jvm.setAccessible(true);
    sun.management.VMManagement mgmt =
        (sun.management.VMManagement) jvm.get(runtime);
    java.lang.reflect.Method pid_method =
        mgmt.getClass().getDeclaredMethod("getProcessId");
    pid_method.setAccessible(true);

    int pid = (Integer) pid_method.invoke(mgmt);
    return pid;
  }
}
