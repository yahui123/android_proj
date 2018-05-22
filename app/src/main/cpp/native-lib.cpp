#include <jni.h>
#include <string>
#include <signal.h>
#include "security.hpp"
using namespace std;


#ifdef __cplusplus
extern "C" {
#endif




#ifdef __cplusplus
}

#endif

extern "C"
JNIEXPORT jstring JNICALL
Java_com_tang_trade_module_register_generate_GenerateWordsModel_getWord(JNIEnv *env,
                                                                         jobject instance,
                                                                         jint type) {

    const char *data = security::client::random_character(9, " ", type).c_str();

        jclass Class_string; jmethodID mid_String, mid_getBytes; jbyteArray bytes; jbyte* log_utf8; jstring codetype, jstr;
        Class_string = env->FindClass( "java/lang/String");
        //获取class //先将gbk字符串转为java里的string格式
        mid_String = env->GetMethodID( Class_string, "<init>", "([BLjava/lang/String;)V");
        int len=strlen(data)+1;
        //需要加1，把字符串的结束符也包含进来
        bytes = env->NewByteArray( len);
        env->SetByteArrayRegion( bytes, 0, len, (jbyte*) data);
        codetype = env->NewStringUTF( "utf-8");
        jstr = (jstring)env->NewObject( Class_string, mid_String, bytes, codetype);
        env->DeleteLocalRef( bytes); //再将string变utf-8字符串。
        mid_getBytes = env->GetMethodID( Class_string, "getBytes", "(Ljava/lang/String;)[B");
        codetype = env->NewStringUTF( "utf-8");
        bytes = (jbyteArray)env->CallObjectMethod( jstr, mid_getBytes, codetype);
        log_utf8 = env->GetByteArrayElements( bytes, JNI_FALSE);
        return env->NewStringUTF((char *)log_utf8);
}


