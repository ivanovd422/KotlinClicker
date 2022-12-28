import org.opencv.core.CvType
import org.opencv.core.Mat
import java.awt.MouseInfo
import java.awt.Point
import java.awt.image.BufferedImage

fun img2Mat(bufferImage: BufferedImage): Mat {
    val out: Mat
    val data: ByteArray
    var r: Int
    var g: Int
    var b: Int
    if (bufferImage.type == BufferedImage.TYPE_INT_RGB) {
        out = Mat(bufferImage.height, bufferImage.width, CvType.CV_8UC3)
        data = ByteArray(bufferImage.width * bufferImage.height * out.elemSize().toInt())
        val dataBuff = bufferImage.getRGB(0, 0, bufferImage.width, bufferImage.height, null, 0, bufferImage.width)
        for (i in dataBuff.indices) {
            data[i * 3] = (dataBuff[i] shr 0 and 0xFF).toByte()
            data[i * 3 + 1] = (dataBuff[i] shr 8 and 0xFF).toByte()
            data[i * 3 + 2] = (dataBuff[i] shr 16 and 0xFF).toByte()
        }
    } else {
        out = Mat(bufferImage.height, bufferImage.width, CvType.CV_8UC1)
        data = ByteArray(bufferImage.width * bufferImage.height * out.elemSize().toInt())
        val dataBuff = bufferImage.getRGB(0, 0, bufferImage.width, bufferImage.height, null, 0, bufferImage.width)
        for (i in dataBuff.indices) {
            r = (dataBuff[i] shr 0 and 0xFF).toByte().toInt()
            g = (dataBuff[i] shr 8 and 0xFF).toByte().toInt()
            b = (dataBuff[i] shr 16 and 0xFF).toByte().toInt()
            data[i] = (0.21 * r + 0.71 * g + 0.07 * b).toInt().toByte()
        }
    }
    out.put(0, 0, data)
    return out
}

fun getCurrentMousePosition(): Point {
    return MouseInfo.getPointerInfo().location
}