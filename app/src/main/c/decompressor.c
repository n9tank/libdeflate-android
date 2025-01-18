/*
 * Copyright 2024 Andrew Steinborn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include "common.h"
#include <jni.h>
#include "libdeflate/libdeflate.h"
JNIEXPORT jlong JNICALL
Java_me_steinborn_libdeflate_LibdeflateDecompressor_allocate(JNIEnv *env,
															 jclass klass)
{
	struct libdeflate_decompressor *decompressor =
		libdeflate_alloc_decompressor();
	return (jlong)decompressor;
}

JNIEXPORT void JNICALL
Java_me_steinborn_libdeflate_LibdeflateDecompressor_free(JNIEnv *env,
														 jclass klass,
														 jlong ctx)
{
	libdeflate_free_decompressor((struct libdeflate_decompressor *)ctx);
}

jlong performDecompression(jlong ctx,
						   jbyte *inStart,
						   jint inSize, /* Input buffer */
						   jbyte *outStart,
						   jint outSize, /* Output buffer */
						   jint type)
{
	struct libdeflate_decompressor *decompressor =
		(struct libdeflate_decompressor *)ctx;
	size_t actualOutBytes = 0;
	size_t actualInBytes = 0;
	enum libdeflate_result result;
	switch (type)
	{
	case COMPRESSION_TYPE_DEFLATE:
		result = libdeflate_deflate_decompress_ex(
			decompressor, inStart, inSize, outStart, outSize,
			&actualInBytes, &actualOutBytes);
		break;
	case COMPRESSION_TYPE_ZLIB:
		result = libdeflate_zlib_decompress_ex(
			decompressor, inStart, inSize, outStart, outSize,
			&actualInBytes, &actualOutBytes);
		break;
	default:
		//case COMPRESSION_TYPE_GZIP:
		result = libdeflate_gzip_decompress_ex(
			decompressor, inStart, inSize, outStart, outSize,
			&actualInBytes, &actualOutBytes);
		break;
	}
	switch (result)
	{
	case LIBDEFLATE_BAD_DATA:
		return -1;
	case LIBDEFLATE_INSUFFICIENT_SPACE:
		actualOutBytes |= 0x80000000;
		break;
	default:
		break;
	}
	return (jlong)(((uint64_t)actualInBytes << 32) | actualOutBytes);
}

JNIEXPORT jlong JNICALL
Java_me_steinborn_libdeflate_LibdeflateDecompressor_decompressBothHeap(
	JNIEnv *env, jclass klass, jlong ctx, jbyteArray in, jint inPos, jint inSize,
	jbyteArray out, jint outPos, jint outSize, jint type)
{
	jbyte *inBytes = (*env)->GetPrimitiveArrayCritical(env, in, 0);
	if (inBytes == NULL)
	{
		return -2;
	}
	jbyte *outBytes = (*env)->GetPrimitiveArrayCritical(env, out, 0);
	if (outBytes == NULL)
	{
		(*env)->ReleasePrimitiveArrayCritical(env, in, inBytes, JNI_ABORT);
		return -2;
	}
	jlong result = performDecompression(ctx, inBytes + inPos, inSize, outBytes + outPos, outSize, type);
	(*env)->ReleasePrimitiveArrayCritical(env, in, inBytes, JNI_ABORT);
	(*env)->ReleasePrimitiveArrayCritical(env, out, outBytes, (result & 0x7FFFFFFF) == 0 ? JNI_ABORT : 0);
	return result;
}

JNIEXPORT jlong JNICALL
Java_me_steinborn_libdeflate_LibdeflateDecompressor_decompressBothDirect(
	JNIEnv *env, jclass klass, jlong ctx, jobject in, jint inPos, jint inSize, jobject out,
	jint outPos, jint outSize, jint type)
{
	jbyte *inBytes = (*env)->GetDirectBufferAddress(env, in);
	jbyte *outBytes = (*env)->GetDirectBufferAddress(env, out);
	return performDecompression(ctx, inBytes + inPos, inSize, outBytes + outPos, outSize, type);
}

JNIEXPORT jlong JNICALL
Java_me_steinborn_libdeflate_LibdeflateDecompressor_decompressOnlySourceDirect(
	JNIEnv *env, jclass klass, jlong ctx, jobject in, jint inPos, jint inSize,
	jbyteArray out, jint outPos, jint outSize, jint type)
{
	jbyte *inBytes = (*env)->GetDirectBufferAddress(env, in);
	jbyte *outBytes = (*env)->GetPrimitiveArrayCritical(env, out, 0);
	if (outBytes == NULL)
	{
		return -2;
	}
	jlong result = performDecompression(ctx, inBytes + inPos, inSize, outBytes + outPos, outSize, type);
	(*env)->ReleasePrimitiveArrayCritical(env, out, outBytes, (result & 0x7FFFFFFF) == 0 ? JNI_ABORT : 0);
	return result;
}

JNIEXPORT jlong JNICALL
Java_me_steinborn_libdeflate_LibdeflateDecompressor_decompressOnlyDestinationDirect(
	JNIEnv *env, jclass klass, jlong ctx, jbyteArray in, jint inPos, jint inSize,
	jobject out, jint outPos, jint outSize, jint type)
{
	jbyte *outBytes = (*env)->GetDirectBufferAddress(env, out);
	jbyte *inBytes = (*env)->GetPrimitiveArrayCritical(env, in, 0);
	if (outBytes == NULL)
	{
		return -2;
	}
	jlong result = performDecompression(ctx, inBytes + inPos, inSize, outBytes + outPos, outSize, type);
	(*env)->ReleasePrimitiveArrayCritical(env, in, inBytes, JNI_ABORT);
	return result;
}
