package nativelib.interception

import com.sun.jna.Structure

@Structure.FieldOrder("code", "state", "flags", "rolling", "x", "y", "information")
class InterceptionStroke(
    @JvmField var code: Short = 0,
    @JvmField var state: Short = 0,
    @JvmField var information: Short = 0,
    @JvmField var rolling: Short = 0,
    @JvmField var x: Int = 0,
    @JvmField var y: Int = 0,
    @JvmField var flags: Short = 0,
    var isInjected: Boolean = false
) : Structure()