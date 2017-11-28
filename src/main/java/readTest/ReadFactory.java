package readTest;

/**
 * Created by dell on 2017/11/22.
 */
public class ReadFactory {
  static final String ramFolder = "/mnt/ramdisk/";
  static final String onDiskFolder = "/home/experiment/huangzhi/zerocopy/ondisk/";
  /*
  总共分为以下几种情况：
  1.ONDISK IO
  2.ONDISK MMAP
  3.RAMFS IO
  4.RAMFS MMAP
  5.HEAP
   */
  public static Read create(String filename, StorageType storageType, ReadMethod readMethod, String packetSize) {
    if (storageType == StorageType.ONDISK && readMethod == ReadMethod.IO) {
      return new RAFRead(onDiskFolder + filename, packetSize);
    }
    if (storageType == StorageType.ONDISK && readMethod == ReadMethod.MMAP) {
      return new MmapRead(onDiskFolder + filename, packetSize);
    }
    if (storageType == StorageType.RAMFS && readMethod == ReadMethod.IO) {
      return new RAFRead(ramFolder + filename, packetSize);
    }

    if (storageType == StorageType.RAMFS && readMethod == ReadMethod.MMAP) {
      return new MmapRead(ramFolder + filename, packetSize);
    }

    if (storageType == StorageType.HEAP) {
      //这里trick一下，此时文件名这个命令行参数就是大小
      int size = (int)TestUtil.convertHumantoSize(filename);
      return new HeapRead(packetSize, size);
    }
     throw new RuntimeException("ReadFactory create cannot go here!");
  }
}
