package nativelib.interception

import com.sun.jna.Callback
import com.sun.jna.Pointer
import nativelib.DirectNativeLib

object Interception : DirectNativeLib("interception") {

    @JvmStatic
    external fun interception_is_keyboard(device: Int): Int

    @JvmStatic
    external fun interception_is_mouse(device: Int): Int

    @JvmStatic
    external fun interception_create_context(): Pointer

    @JvmStatic
    external fun interception_set_filter(context: Pointer, predicate: Callback, filter: Short)

    @JvmStatic
    external fun interception_receive(context: Pointer, device: Int, stroke: InterceptionStroke, nstroke: Int): Int

    @JvmStatic
    external fun interception_wait(context: Pointer): Int

    @JvmStatic
    external fun interception_send(context: Pointer, device: Int, stroke: InterceptionStroke, nstroke: Int): Int

    @JvmStatic
    external fun interception_destroy_context(context: Pointer)

}