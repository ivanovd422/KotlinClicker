package nativelib

import com.sun.jna.Pointer

object Kernel32 : DirectNativeLib("kernel32") {

    const val HIGH_PRIORITY_CLASS = 0x00000080

    @JvmStatic
    external fun SetPriorityClass(hProcess: Pointer, dwPriorityClass: Int): Boolean

    @JvmStatic
    external fun GetCurrentProcess(): Pointer

}