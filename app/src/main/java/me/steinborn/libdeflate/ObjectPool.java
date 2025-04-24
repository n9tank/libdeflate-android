package me.steinborn.libdeflate;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.*;

public class ObjectPool {
 public static final ConcurrentLinkedQueue<LibdeflateCompressor>[] deflatePool;
 static{
  ConcurrentLinkedQueue<LibdeflateCompressor> pool[]= new ConcurrentLinkedQueue[12];
  for(int i=0;i<pool.length;i++)
   pool[i]=new ConcurrentLinkedQueue();
  deflatePool=pool;
 }
 public static final LongAdder deflateNum=new LongAdder();
 public static final ConcurrentLinkedQueue<LibdeflateDecompressor> inflatePool=new ConcurrentLinkedQueue();
 public static final LongAdder inflateNum=new LongAdder();
 public static LibdeflateCompressor allocDeflate(int lvl, int mode) {
  int in=lvl - 1;
  ConcurrentLinkedQueue<LibdeflateCompressor>[] deflatePool=ObjectPool.deflatePool;
  ConcurrentLinkedQueue<LibdeflateCompressor> list = deflatePool[in];
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
 public static void addInflateCount(){
  inflateNum.increment();
 }
 public static void addDeflateCount(){
  deflateNum.increment();
 }
 public static void deflateGc() {
  LongAdder deflateNum=ObjectPool.deflateNum;
  deflateNum.decrement();
  if (deflateNum.sum() > 0)return;
  for (ConcurrentLinkedQueue<LibdeflateCompressor> list:deflatePool) {
   if (list != null)
	GcList(list);
  }
 }
 public static void inflateGc() {
  LongAdder inflateNum=ObjectPool.inflateNum;
  inflateNum.decrement();
  if (inflateNum.sum() <= 0)
   GcList(inflatePool);
 }
 public static void GcList(ConcurrentLinkedQueue list) {
  Object obj;
  try{
   while ((obj = (list.poll())) != null)
	((AutoCloseable)obj).close();
  }catch (Exception e) {}
 }
}
