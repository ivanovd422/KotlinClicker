import com.sun.jna.platform.WindowUtils
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.awt.Color
import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage
import java.lang.Exception


fun main() {
    val opencvpath = System.getProperty("user.dir") + "\\lib\\"
    System.load(opencvpath + Core.NATIVE_LIBRARY_NAME + ".dll")

    val rectangle = detektWindow(WINDOW_NAME)

    mainLoop@ while (true) {
        var isCorrectTargetSelected = ifTargetSelectedAndHasHp(rectangle)

        while (isCorrectTargetSelected) {
            HardwareController.attack()
            isCorrectTargetSelected = ifTargetSelectedAndHasHp(rectangle)

            if (isCorrectTargetSelected.not()) {
                HardwareController.pressNextTarget()
                continue@mainLoop
            }
        }

        if (detektTargetAndClick(rectangle)) {
            continue
        } else {
            HardwareController.pressNextTarget()
        }

        moveAroundToRight(rectangle)
    }
}

private fun detektWindow(windowName: String): Rectangle {
    val user32 = MyUser32.instance
    val rect = Rectangle(0, 0, 0, 0)
    var windowTitle = ""

    val windows = WindowUtils.getAllWindows(true)
    windows.forEach {
        if (it.title.contains(windowName)) {
            rect.setRect(it.locAndSize)
            windowTitle = it.title
        }
    }

    val tst: WinDef.HWND = user32.FindWindow(null, windowTitle)
    user32.ShowWindow(tst, User32.SW_SHOW)
    user32.SetForegroundWindow(tst)

    return rect
}

private fun ifTargetSelectedAndHasHp(rectangle: Rectangle): Boolean {
    val target = findTargetByDropDownMenu(rectangle) ?: return false

    val hp = checkHpBar(target)
    return hp > 0
}

private fun findTargetByDropDownMenu(rectangle: Rectangle): Mat? {
    Thread.sleep(100L)
    val capture: BufferedImage = Robot().createScreenCapture(rectangle)
    val screen: Mat = img2Mat(capture)

    Imgproc.cvtColor(screen, screen, Imgproc.COLOR_BGR2GRAY)

    val targetBar: Mat = Imgcodecs.imread("./src/main/resources/$DROP_DOWN_ICON_NAME.png", Imgcodecs.IMREAD_COLOR)
    Imgproc.cvtColor(targetBar, targetBar, Imgproc.COLOR_BGR2GRAY)

    Imgproc.matchTemplate(screen, targetBar, screen, Imgproc.TM_CCOEFF_NORMED)
    val point = Core.minMaxLoc(screen)

    if (point.maxVal < 0.95) {
        return null
    }

    val subImage: BufferedImage
    try {
        subImage = capture.getSubimage(point.maxLoc.x.toInt(), point.maxLoc.y.toInt(), 170, 35)
    } catch (e: Exception) {
        return null
    }

    return img2Mat(subImage)
}

private fun checkHpBar(hpBarMat: Mat): Int {
    val lower = Scalar(0.0, 150.0, 90.0)
    val upper = Scalar(10.0, 255.0, 255.0)

    val subImageMat: Mat = hpBarMat.clone()
    Imgproc.cvtColor(subImageMat, subImageMat, Imgproc.COLOR_BGR2HSV)
    Core.inRange(subImageMat, lower, upper, subImageMat)
    val remainingContours: MutableList<MatOfPoint> = mutableListOf()
    Imgproc.findContours(subImageMat, remainingContours, subImageMat, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

    val remainingLeftX = remainingContours.firstOrNull()?.toList()?.minBy { it.x }?.x ?: 0.0
    val remainingRightX = remainingContours.firstOrNull()?.toList()?.maxBy { it.x }?.x ?: 0.0

    val totalHpBarWidth = subImageMat.width()
    val remainingHpBarWidth = remainingRightX - remainingLeftX
    val percentHpRemaining = remainingHpBarWidth * 100 / totalHpBarWidth

    if (percentHpRemaining < 1) {
        return 0
    }

    return percentHpRemaining.toInt()
}

private fun detektTargetAndClick(rectangle: Rectangle): Boolean {
    val points = findPossibleTargets(rectangle)

    points.forEach {
        val (xPosition, yPosition) = getCenterPosition(it)
        val point = getCurrentMousePosition()

        moveMouseByBresenhamLine(
            point.x,
            point.y,
            xPosition,
            yPosition + 50,
        )

        if (isMouseSelectingAMob(rectangle)) {
            HardwareController.simpleClick()
            HardwareController.simpleClick()
            Thread.sleep(300)
            val targetMat = findTargetByDropDownMenu(rectangle)

            if (targetMat != null) {
                val hp = checkHpBar(targetMat)
                if (hp > 0) {
                    return true
                }
            }
        }
    }

    return false
}

private fun findPossibleTargets(rectangle: Rectangle): List<MatOfPoint> {
    val capture: BufferedImage = Robot().createScreenCapture(rectangle)
    fillBlackExcess(capture, rectangle)

    val source: Mat = img2Mat(capture)

    Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2GRAY)
    Imgproc.threshold(source, source, 252.0, 255.0, Imgproc.THRESH_BINARY)
    val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(10.0, 1.0))
    Imgproc.morphologyEx(source, source, Imgproc.MORPH_CLOSE, kernel)
    Imgproc.erode(source, source, kernel)
    Imgproc.dilate(source, source, kernel)

    val points: MutableList<MatOfPoint> = mutableListOf()
    Imgproc.findContours(source, points, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

    return points
        .sortedBy { it.toList().maxBy { it.y }.y }
        .filter {
        val maxX = it.toList().maxBy { it.x }.x
        val minX = it.toList().minBy { it.x }.x
        val width = (maxX - minX)

        val maxY = it.toList().maxBy { it.y }.y
        val minY = it.toList().minBy { it.y }.y

        val height = (maxY - minY)

        width > 30 && width < 200 && height < 30
    }
}

private fun fillBlackExcess(capture: BufferedImage, rectangle: Rectangle) {
    val offset = 150
    val chatHeight = 350
    val chatWidth = 350
    val panelsHeight = 100
    val windowNameHeight = 35
    val radarWidth = 200
    val radarHeight = 200

    capture.createGraphics()
        .apply {
            color = Color.BLACK
            //fill character
            fillRect(
                rectangle.width / 2 - 50,
                rectangle.height / 2 - offset,
                100,
                100
            )

            //fill chat
            fillRect(
                0,
                rectangle.height - chatHeight,
                chatWidth,
                chatHeight
            )

            //fill panels
            fillRect(
                chatWidth,
                rectangle.height - panelsHeight,
                rectangle.width - chatWidth,
                panelsHeight
            )

            //fill window name
            fillRect(
                0,
                0,
                rectangle.width,
                windowNameHeight
            )

            //fill radar
            fillRect(
                rectangle.width - radarWidth,
                0,
                rectangle.width,
                radarHeight
            )

            dispose()
        }
}

fun getCenterPosition(point: MatOfPoint): Pair<Int, Int> {
    val list = point.toList()
    val x = list.map { it.x }.average().toInt()
    val y = list.map { it.y }.average().toInt()
    return Pair(x, y)
}

private fun isMouseSelectingAMob(rectangle: Rectangle): Boolean {
    Thread.sleep(100L)
    val minMatchThreshold = 0.8
    val capture: BufferedImage = Robot().createScreenCapture(rectangle)

    val thresholdScreen: Mat = img2Mat(capture)
    Imgproc.cvtColor(thresholdScreen, thresholdScreen, Imgproc.COLOR_BGR2GRAY)

    val template: Mat = Imgcodecs.imread("./src/main/resources/$TARGET_TEMPLATE_NAME.png")
    Imgproc.cvtColor(template, template, Imgproc.COLOR_BGR2GRAY)

    Imgproc.matchTemplate(thresholdScreen, template, thresholdScreen, Imgproc.TM_CCOEFF_NORMED)
    val value = Core.minMaxLoc(thresholdScreen).maxVal

    return value > minMatchThreshold
}

fun moveAroundToRight(rectangle: Rectangle) {
    println("turn around")
    var point = getCurrentMousePosition()
    val centerX = rectangle.width / 2
    val centerY = rectangle.height / 2

    moveMouseByBresenhamLine(
        point.x,
        point.y,
        centerX,
        centerY
    )
    point = getCurrentMousePosition()

    HardwareController.rightClickDown()
    Thread.sleep(50L)

    moveMouseByBresenhamLine(
        point.x,
        point.y,
        point.x + MOUSE_MOVE_AROUND_VALUE,
        point.y,
    )
    Thread.sleep(50L)
    HardwareController.rightClickUp()
}

