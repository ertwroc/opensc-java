/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jnix.h>
/* Header for class org_opensc_pkcs11_PKCS11Provider */

#ifndef _Included_org_opensc_pkcs11_PKCS11Provider
#define _Included_org_opensc_pkcs11_PKCS11Provider
#ifdef __cplusplus
extern "C" {
#endif
#undef org_opensc_pkcs11_PKCS11Provider_serialVersionUID
#define org_opensc_pkcs11_PKCS11Provider_serialVersionUID 1421746759512286392LL
#undef org_opensc_pkcs11_PKCS11Provider_KEYS
#define org_opensc_pkcs11_PKCS11Provider_KEYS 0L
#undef org_opensc_pkcs11_PKCS11Provider_VALUES
#define org_opensc_pkcs11_PKCS11Provider_VALUES 1L
#undef org_opensc_pkcs11_PKCS11Provider_ENTRIES
#define org_opensc_pkcs11_PKCS11Provider_ENTRIES 2L
#undef org_opensc_pkcs11_PKCS11Provider_serialVersionUID
#define org_opensc_pkcs11_PKCS11Provider_serialVersionUID 4112578634029874840LL
#undef org_opensc_pkcs11_PKCS11Provider_serialVersionUID
#define org_opensc_pkcs11_PKCS11Provider_serialVersionUID -4298000515446427739LL
#undef org_opensc_pkcs11_PKCS11Provider_serialVersionUID
#define org_opensc_pkcs11_PKCS11Provider_serialVersionUID -2568219416560640437LL
#undef org_opensc_pkcs11_PKCS11Provider_version
#define org_opensc_pkcs11_PKCS11Provider_version 0.2
#undef org_opensc_pkcs11_PKCS11Provider_patchlevel
#define org_opensc_pkcs11_PKCS11Provider_patchlevel 0.0
/*
 * Class:     org_opensc_pkcs11_PKCS11Provider
 * Method:    loadNativePKCS11Module
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_PKCS11Provider_loadNativePKCS11Module)
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_opensc_pkcs11_PKCS11Provider
 * Method:    unloadPKCS11Module
 * Signature: (J)V
 */
JNIEXPORT void JNICALL JNIX_FUNC_NAME(Java_org_opensc_pkcs11_PKCS11Provider_unloadPKCS11Module)
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
