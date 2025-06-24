#include "native-keys-storer.h"
#include <jni.h>
#include <string>


enum string_code {
    E_INVALID_KEY,
    E_INDICATIVE_API_KEY,
    E_MATOMO_URL,
    E_MATOMO_API_KEY,
};

string_code hashit(std::string const &inString) {
    if (inString == "INDICATIVE_API_KEY") return E_INDICATIVE_API_KEY;
    if (inString == "MATOMO_URL") return E_MATOMO_URL;
    if (inString == "MATOMO_API_KEY") return E_MATOMO_API_KEY;
    return E_INVALID_KEY;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_appcoins_sdk_billing_helpers_PrivateKeysNativeHelper_getApiKey(JNIEnv *env,
                                                                        jobject instance,
                                                                        jstring buildType,
                                                                        jstring key) {
    string_code apiKeyCode = hashit(env->GetStringUTFChars(key, nullptr));
    std::string stringToBeReturned;

    if (strcmp(env->GetStringUTFChars(buildType, nullptr), "release") == 0) {
        switch (apiKeyCode) {
            case E_INDICATIVE_API_KEY:
                stringToBeReturned = INDICATIVE_API_KEY;
                break;
            case E_MATOMO_URL:
                stringToBeReturned = MATOMO_URL;
                break;
            case E_MATOMO_API_KEY:
                stringToBeReturned = MATOMO_API_KEY;
                break;
            case E_MATOMO_URL:
                stringToBeReturned = MATOMO_URL;
                break;
            default:
                stringToBeReturned = "";
        }
    } else if (strcmp(env->GetStringUTFChars(buildType, nullptr), "debug") == 0) {
        switch (apiKeyCode) {
            case E_INDICATIVE_API_KEY:
                stringToBeReturned = INDICATIVE_API_KEY_DEV;
                break;
            case E_MATOMO_URL:
                stringToBeReturned = MATOMO_URL_DEV;
                break;
            case E_MATOMO_API_KEY:
                stringToBeReturned = MATOMO_API_KEY_DEV;
                break;
            case E_MATOMO_URL:
                stringToBeReturned = MATOMO_URL;
                break;
            default:
                stringToBeReturned = "";
        }
    } else {
        stringToBeReturned = "";
    }
    return env->NewStringUTF(stringToBeReturned.c_str());
}
