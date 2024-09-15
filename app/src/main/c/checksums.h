#ifndef CHECKSUMS_H
#define CHECKSUMS_H

#include "jni_util.h"

jint JNICALL Java_me_steinborn_libdeflate_LibdeflateCRC32_crc32Heap(JNIEnv *env, jclass klass, jlong crc32, jbyteArray array, jint off, jint len);
jint JNICALL Java_me_steinborn_libdeflate_LibdeflateCRC32_crc32Direct( JNIEnv *env, jclass klass, jlong crc32, jobject buf, jint off, jint len);
/*jint JNICALL Java_me_steinborn_libdeflate_LibdeflateAdler32_adler32Heap(JNIEnv *env, jclass klass, jlong adler32, jbyteArray array, jint off, jint len);
jint JNICALL Java_me_steinborn_libdeflate_LibdeflateAdler32_adler32Direct( JNIEnv *env, jclass klass, jlong adler32, jobject buf, jint off, jint len);*/

#endif
