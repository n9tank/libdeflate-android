#ifndef COMPRESSOR_H
#define COMPRESSOR_H

#include "jni_util.h"

jlong JNICALL Java_me_steinborn_libdeflate_LibdeflateCompressor_allocate(JNIEnv *env, jclass klass, jint level);
void JNICALL Java_me_steinborn_libdeflate_LibdeflateCompressor_free(JNIEnv *env, jclass klass, jlong ctx);
jlong performCompression(jlong ctx, jbyte *inBytes, jint inPos, jint inSize, jbyte *outBytes, jint outPos, jint outSize, jint type);
jlong JNICALL Java_me_steinborn_libdeflate_LibdeflateCompressor_compressBothHeap( JNIEnv *env, jclass klass, jlong ctx, jbyteArray in, jint inPos, jint inSize, jbyteArray out, jint outPos, jint outSize, jint type);
jlong JNICALL Java_me_steinborn_libdeflate_LibdeflateCompressor_compressBothDirect( JNIEnv *env, jclass klass, jlong ctx, jobject in, jint inPos, jint inSize, jobject out, jint outPos, jint outSize, jint type);
jlong JNICALL Java_me_steinborn_libdeflate_LibdeflateCompressor_compressOnlySourceDirect( JNIEnv *env, jclass klass, jlong ctx, jobject in, jint inPos, jint inSize, jbyteArray out, jint outPos, jint outSize, jint type);
jlong JNICALL Java_me_steinborn_libdeflate_LibdeflateCompressor_compressOnlyDestinationDirect( JNIEnv *env, jclass klass, jlong ctx, jbyteArray in, jint inPos, jint inSize, jobject out, jint outPos, jint outSize, jint type);
//jlong JNICALL Java_me_steinborn_libdeflate_LibdeflateCompressor_getCompressBound( JNIEnv *env, jclass klass, jlong ctx, jlong length, jint type);

#endif
