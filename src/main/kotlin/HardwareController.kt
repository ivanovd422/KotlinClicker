import com.sun.jna.Callback
import nativelib.Kernel32
import nativelib.interception.Interception
import nativelib.interception.InterceptionFilter
import nativelib.interception.InterceptionFilter.INTERCEPTION_FILTER_KEYBOARD_ESC
import nativelib.interception.InterceptionMouseFlag
import nativelib.interception.InterceptionStroke
import kotlin.system.exitProcess

object HardwareController : Thread() {

    private val keyboardCallback = object : Callback {
        fun callback(device: Int) {
            Interception.interception_is_keyboard(device)
        }
    }
    private val context = Interception.interception_create_context()
    private val emptyStroke = InterceptionStroke()

    private val stroke = InterceptionStroke(
        0, 0,
        (InterceptionMouseFlag.INTERCEPTION_MOUSE_MOVE_RELATIVE or
                InterceptionMouseFlag.INTERCEPTION_MOUSE_CUSTOM).toShort(),
        0, 0, 0, 0, true
    )

    init {
        Kernel32.SetPriorityClass(Kernel32.GetCurrentProcess(), Kernel32.HIGH_PRIORITY_CLASS)

        Interception.interception_set_filter(
            context,
            keyboardCallback,
            INTERCEPTION_FILTER_KEYBOARD_ESC.toShort()
        )

        start()
    }

    override fun run() {
        var device: Int
        while (Interception.interception_receive(
                context,
                Interception.interception_wait(context).also { device = it },
                emptyStroke,
                1
            ) > 0
        ) {

            val strokeCode = emptyStroke.code
            val keyboardEscShort = INTERCEPTION_FILTER_KEYBOARD_ESC.toShort()

//            println("device - ${device}")
//            println("received code - ${emptyStroke.code}")

            if (device == KEYBOARD_DEVICE_ID && strokeCode == keyboardEscShort) {
                println("finish program")
                exitProcess(0)
            }

            if (!emptyStroke.isInjected) {
                Interception.interception_send(context, device, emptyStroke, 1)
            }
        }
        Interception.interception_destroy_context(context)
    }

    fun moveMouse(x: Int, y: Int) {
        stroke.x = x
        stroke.y = y
        Interception.interception_send(context, MOUSE_DEVICE_ID, stroke, 1)
    }

    fun rightClickDown() {
        stroke.x = 0
        stroke.y = 0
        stroke.code = InterceptionFilter.INTERCEPTION_MOUSE_RIGHT_BUTTON_DOWN.toShort()
        Interception.interception_send(context, MOUSE_DEVICE_ID, stroke, 1)
    }

    fun rightClickUp() {
        stroke.x = 0
        stroke.y = 0
        stroke.code = InterceptionFilter.INTERCEPTION_MOUSE_RIGHT_BUTTON_UP.toShort()
        Interception.interception_send(context, MOUSE_DEVICE_ID, stroke, 1)
    }

    fun leftClickUp() {
        stroke.x = 0
        stroke.y = 0
        stroke.code = InterceptionFilter.INTERCEPTION_MOUSE_LEFT_BUTTON_UP.toShort()
        Interception.interception_send(context, MOUSE_DEVICE_ID, stroke, 1)
    }

    fun leftClickDown() {
        stroke.x = 0
        stroke.y = 0
        stroke.code = InterceptionFilter.INTERCEPTION_MOUSE_LEFT_BUTTON_DOWN.toShort()
        Interception.interception_send(context, MOUSE_DEVICE_ID, stroke, 1)
    }

    fun simpleClick() {
        println("simpleClick")
        stroke.x = 0
        stroke.y = 0
        stroke.code = InterceptionFilter.INTERCEPTION_MOUSE_LEFT_BUTTON_DOWN.toShort()
        Interception.interception_send(context, MOUSE_DEVICE_ID, stroke, 1)
        sleep(100)
        stroke.code = InterceptionFilter.INTERCEPTION_MOUSE_LEFT_BUTTON_UP.toShort()
        Interception.interception_send(context, MOUSE_DEVICE_ID, stroke, 1)
    }

    fun pressNextTarget() {
        println("pressNextTarget")
        typeKeyboard(InterceptionFilter.INTERCEPTION_FILTER_KEYBOARD_2)
        sleep(100)
    }

    fun attack() {
        println("attack")
        typeKeyboard(InterceptionFilter.INTERCEPTION_FILTER_KEYBOARD_1)
        sleep(100)
    }

    private fun typeKeyboard(commandCode: Int) {
        stroke.x = 0
        stroke.y = 0
        stroke.code = commandCode.toShort()
        Interception.interception_send(context, KEYBOARD_DEVICE_ID, stroke, 1)
    }
}