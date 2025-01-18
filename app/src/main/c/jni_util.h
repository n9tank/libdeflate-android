#ifndef JNIUTIL_H
#define JNIUTIL_H

#include <jni.h>

#define throwException(env, type, msg)                 \
	{                                                  \
		jclass err_cls = (*env)->FindClass(env, type); \
		if (err_cls)                                   \
		{                                              \
			(*env)->ThrowNew(env, err_cls, msg);       \
		}                                              \
	}
#endif
