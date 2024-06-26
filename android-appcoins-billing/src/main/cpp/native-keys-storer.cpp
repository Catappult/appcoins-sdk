#include "native-keys-storer.h"
#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_appcoins_sdk_billing_helpers_PrivateKeysNativeHelper_getIndicativeApiKey(JNIEnv *env, jobject instance) {
// Convert the C++ string to a Java string and return it
    return env->NewStringUTF(std::string(INDICATIVE_API_KEY).c_str());
}