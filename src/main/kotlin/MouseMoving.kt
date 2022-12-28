
fun moveMouseByBresenhamLine(xStart: Int, yStart: Int, xEnd: Int, yEnd: Int) {
    var dx: Int
    var dy: Int
    val pdx: Int
    val pdy: Int
    val es: Int
    val el: Int
    dx = xEnd - xStart
    dy = yEnd - yStart
    
    val incx: Int = sign(dx)

    val incy: Int = sign(dy)
    if (dx < 0) dx = -dx
    if (dy < 0) dy = -dy

    if (dx > dy) {
        pdx = incx
        pdy = 0
        es = dy
        el = dx
    } else {
        pdx = 0
        pdy = incy
        es = dx
        el = dy
    }

    var x: Int = xStart
    var y: Int = yStart
    var err: Int = el / 2


    var lastX = xStart
    var lastY = yStart
    var xMove = 0
    var yMove = 0
    var shouldWaitCounter = 0

    for (t in 0 until el) {
        err -= es
        if (err < 0) {
            err += el
            x += incx
            y += incy
        } else {
            x += pdx
            y += pdy
        }

        xMove = x - lastX
        yMove = y - lastY

        HardwareController.moveMouse(xMove, yMove)
        lastX = x
        lastY = y

        if (shouldWaitCounter == 4) {
            Thread.sleep(1)
            shouldWaitCounter = 0
        } else {
            shouldWaitCounter++
        }
    }
}

private fun sign(x: Int): Int {
    return if (x > 0) 1 else if (x < 0) -1 else 0
}