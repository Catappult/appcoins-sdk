#include "native-keys-storer.h"
#include <jni.h>
#include <string>


enum string_code {
    E_INVALID_KEY,
    E_ADYEN_API_KEY,
    E_INDICATIVE_API_KEY,
    E_RAKAM_API_KEY,
};

string_code hashit(std::string const &inString) {
    if (inString == "ADYEN_API_KEY") return E_ADYEN_API_KEY;
    if (inString == "INDICATIVE_API_KEY") return E_INDICATIVE_API_KEY;
    if (inString == "RAKAM_API_KEY") return E_RAKAM_API_KEY;
    return E_INVALID_KEY;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_appcoins_sdk_billing_helpers_PrivateKeysNativeHelper_getApiKey(JNIEnv *env,
                                                                        jobject instance,
                                                                        jstring buildType,
                                                                        jstring key) {
    string_code apiKeyCode = hashit(env->GetStringUTFChars(key, 0));
    std::string stringToBeReturned;

    if (strcmp(env->GetStringUTFChars(buildType, 0), "release") == 0) {
        switch (apiKeyCode) {
            case E_ADYEN_API_KEY:
                stringToBeReturned = ADYEN_API_KEY;
                break;
            case E_RAKAM_API_KEY:
                stringToBeReturned = RAKAM_API_KEY;
                break;
            case E_INDICATIVE_API_KEY:
                stringToBeReturned = INDICATIVE_API_KEY;
                break;
            default:
                stringToBeReturned = "";
        }
    } else if (strcmp(env->GetStringUTFChars(buildType, 0), "debug") == 0) {
        switch (apiKeyCode) {
            case E_ADYEN_API_KEY:
                stringToBeReturned = ADYEN_API_KEY_DEV;
                break;
            case E_RAKAM_API_KEY:
                stringToBeReturned = RAKAM_API_KEY_DEV;
                break;
            case E_INDICATIVE_API_KEY:
                stringToBeReturned = INDICATIVE_API_KEY_DEV;
                break;
            default:
                stringToBeReturned = "";
        }
    } else {
        stringToBeReturned = "";
    }
    return env->NewStringUTF(stringToBeReturned.c_str());
}