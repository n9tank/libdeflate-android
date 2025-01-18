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
#include "jni_util.h"
#include "common.h"
#include "libdeflate/libdeflate.h"

JNIEXPORT jlong JNICALL
Java_me_steinborn_libdeflate_LibdeflateCompressor_allocate(JNIEnv *env,
														   jclass klass,
														   jint level)
{
	struct libdeflate_compressor *compressor = libdeflate_alloc_compressor(level);
	return (jlong)compressor;
}

JNIEXPORT void JNICALL
Java_me_steinborn_libdeflate_LibdeflateCompressor_free(JNIEnv *env,
													   jclass klass,
													   jlong ctx)
{
	libdeflate_free_compressor((struct libdeflate_compressor *)ctx);
}

jint performCompression(jlong ctx, void *inStart, jint inSize,
						void *outStart, jint outSize,
						jint type)
{
	struct libdeflate_compressor *compressor =
		(struct libdeflate_compressor *)ctx;
	size_t result;
	switch (type)
	{
	case COMPRESSION_TYPE_DEFLATE:
		result = libdeflate_deflate_compress(compressor, inStart, inSize, outStart,
											 outSize);
		break;
	case COMPRESSION_TYPE_ZLIB:
		result = libdeflate_zlib_compress(compressor, inStart, inSize, outStart,
										  outSize);
		break;
	default:
		//case COMPRESSION_TYPE_GZIP:
		result = libdeflate_gzip_compress(compressor, inStart, inSize, outStart,
										  outSize);
		break;
	}
	return (jint)result;
}

JNIEXPORT jint JNICALL
Java_me_steinborn_libdeflate_LibdeflateCompressor_compressBothHeap(
	JNIEnv *env, jclass klass, jlong ctx, jbyteArray in, jint inPos,
	jint inSize, jbyteArray out, jint outPos, jint outSize, jint type)
{
	jbyte *inBytes = (*env)->GetPrimitiveArrayCritical(env, in, 0);
	if (inBytes == NULL)
	{
		return -1;
	}
	jbyte *outBytes = (*env)->GetPrimitiveArrayCritical(env, out, 0);
	if (outBytes == NULL)
	{
		(*env)->ReleasePrimitiveArrayCritical(env, in, inBytes, JNI_ABORT);
		return -1;
	}
	jint result = performCompression(ctx, inBytes + inPos, inSize, outBytes + outPos, outSize, type);
	(*env)->ReleasePrimitiveArrayCritical(env, in, inBytes, JNI_ABORT);
	(*env)->ReleasePrimitiveArrayCritical(env, out, outBytes, 0);
	return result;
}

JNIEXPORT jint JNICALL
Java_me_steinborn_libdeflate_LibdeflateCompressor_compressBothDirect(
	JNIEnv *env, jclass klass, jlong ctx, jobject in, jint inPos, jint inSize,
	jobject out, jint outPos, jint outSize, jint type)
{
	jbyte *inBytes = (*env)->GetDirectBufferAddress(env, in);
	jbyte *outBytes = (*env)->GetDirectBufferAddress(env, out);
	return performCompression(ctx, inBytes + inPos, inSize, outBytes + outPos,
							  outSize, type);
}

JNIEXPORT jint JNICALL
Java_me_steinborn_libdeflate_LibdeflateCompressor_compressOnlySourceDirect(
	JNIEnv *env, jclass klass, jlong ctx, jobject in, jint inPos, jint inSize,
	jbyteArray out, jint outPos, jint outSize, jint type)
{
	jbyte *inBytes = (*env)->GetDirectBufferAddress(env, in);
	jbyte *outBytes = (*env)->GetPrimitiveArrayCritical(env, out, 0);
	if (outBytes == NULL)
	{
		return -1;
	}

	jint result = performCompression(ctx, inBytes + inPos, inSize, outBytes + outPos,
									 outSize, type);
	(*env)->ReleasePrimitiveArrayCritical(env, out, outBytes, 0);
	return result;
}

JNIEXPORT jint JNICALL
Java_me_steinborn_libdeflate_LibdeflateCompressor_compressOnlyDestinationDirect(
	JNIEnv *env, jclass klass, jlong ctx, jbyteArray in, jint inPos,
	jint inSize, jobject out, jint outPos, jint outSize, jint type)
{
	jbyte *outBytes = (*env)->GetDirectBufferAddress(env, out);
	jbyte *inBytes = (*env)->GetPrimitiveArrayCritical(env, in, 0);
	if (outBytes == NULL)
	{
		return -1;
	}

	jint result = performCompression(ctx, inBytes + inPos, inSize, outBytes + outPos,
									 outSize, type);
	(*env)->ReleasePrimitiveArrayCritical(env, in, inBytes, JNI_ABORT);
	return result;
}