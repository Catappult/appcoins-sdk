package com.appcoins.sdk.core.device

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.GLES20
import com.appcoins.sdk.core.logger.Logger.logWarning

class OpenGLHelper {

    fun getDeviceSupportedExtensions(): List<String>? {
        try {
            val display: EGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (display == EGL14.EGL_NO_DISPLAY) {
                logWarning("No EGL display found")
                return null
            }

            val version = IntArray(2)
            if (!EGL14.eglInitialize(display, version, 0, version, 1)) {
                logWarning("EGL initialization failed")
                return null
            }

            val configAttribs = intArrayOf(EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, EGL14.EGL_NONE)
            val configs: Array<EGLConfig?> = arrayOfNulls(1)
            val numConfigs = IntArray(1)

            if (!EGL14.eglChooseConfig(
                    display,
                    configAttribs,
                    0,
                    configs,
                    0,
                    configs.size,
                    numConfigs,
                    0
                ) || numConfigs[0] == 0 || configs[0] == null
            ) {
                logWarning("No compatible EGL configuration found")
                EGL14.eglTerminate(display)
                return null
            }

            val contextAttribs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
            val context: EGLContext =
                EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, contextAttribs, 0)

            if (context == EGL14.EGL_NO_CONTEXT) {
                logWarning("EGL context creation failed")
                EGL14.eglTerminate(display)
                return null
            }

            val surfaceAttribs = intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE)
            val surface = EGL14.eglCreatePbufferSurface(display, configs[0], surfaceAttribs, 0)

            if (surface == null || surface == EGL14.EGL_NO_SURFACE) {
                logWarning("EGL surface creation failed")
                EGL14.eglDestroyContext(display, context)
                EGL14.eglTerminate(display)
                return null
            }

            if (!EGL14.eglMakeCurrent(display, surface, surface, context)) {
                logWarning("Failed to make EGL context current")
                EGL14.eglDestroySurface(display, surface)
                EGL14.eglDestroyContext(display, context)
                EGL14.eglTerminate(display)
                return null
            }

            val extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS)

            EGL14.eglDestroySurface(display, surface)
            EGL14.eglDestroyContext(display, context)
            EGL14.eglTerminate(display)

            return extensions?.split(" ")?.toList()
        } catch (ex: Exception) {
            logWarning("Exception: ${ex.message}")
        }

        return null
    }
}
