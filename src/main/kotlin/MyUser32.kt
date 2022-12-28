import com.sun.jna.Native
import com.sun.jna.platform.win32.User32
import com.sun.jna.win32.W32APIOptions

interface MyUser32 : User32 {
    companion object {
        val instance = Native.load(
            "user32",
            MyUser32::class.java, W32APIOptions.DEFAULT_OPTIONS
        ) as MyUser32
    }
}