package me.steinborn.libdeflate;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ObjectPool {
 public static ConcurrentLinkedQueue<LibdeflateCompressor>[] deflatePool=new ConcurrentLinkedQueue[12];
 public static ConcurrentLinkedQueue<LibdeflateDecompressor> inflatePool=new ConcurrentLinkedQueue();
 public static LibdeflateCompressor allocDeflate(int lvl, int mode) {
  int in=lvl - 1;
  ConcurrentLinkedQueue<LibdeflateCompressor>[] deflatePool=ObjectPool.deflatePool;
  ConcurrentLinkedQueue<LibdeflateCompressor> list=deflatePool[in];
  if (list == null) {
   //很少需要不同的压缩等级此处可以不考虑优化
   synchronized (deflatePool) {
    list = deflatePool[in];
    if (list == null)
     deflatePool[in] = list = new ConcurrentLinkedQueue();
   }
  }
  LibdeflateCompressor obj= list.poll();
  if (obj != null)
   obj.mode = mode;
  else obj = new LibdeflateCompressor(lvl, mode);
  return obj;
 }
 public static void free(LibdeflateCompressor ctx) {
  deflatePool[ctx.lvl - 1].add(ctx);
 }
 public static LibdeflateDecompressor allocInfalte(int mode) {
  LibdeflateDecompressor obj= inflatePool.poll();
  if (obj != null)
   obj.mode = mode;
  else obj = new LibdeflateDecompressor(mode);
  return obj;
 }
 public static void free(LibdeflateDecompressor ctx) {
  inflatePool.add(ctx);
 }
 public static void deflateGc() {
  try {
   for (ConcurrentLinkedQueue obj:deflatePool)
    GcList(obj);
  } catch (Exception e) {}
 }
 public static void inflateGc() {
  try {
   GcList(inflatePool);
  } catch (Exception e) {}
 }
 public static void GcList(ConcurrentLinkedQueue list) throws Exception {
  if (list != null) {
   Object obj;
   while ((obj = list.poll()) != null)
    ((AutoCloseable)obj).close();
  }
 }
}
