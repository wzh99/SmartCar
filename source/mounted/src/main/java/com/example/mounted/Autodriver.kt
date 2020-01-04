package com.example.mounted

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.concurrent.ArrayBlockingQueue
import kotlin.math.max

class Autodriver(private val activity: MainActivity,
                 private var listener: (String) -> Unit) {

    var enabled = false

    private val queue = ArrayBlockingQueue<Mat>(1)

    private val width = 640
    private val height = 360
    private val beginWidthRatio = 0.1f
    private val endWidthRatio = 0.9f
    private val beginHeightRatio = 0.65f
    private val endHeightRatio = 0.95f

    private val binThresh = 100.0
    private val routeThresh = 0.9 // a route is found if below this threshold

    private val delay = 250L

    private val processor = Thread {
        while (true) {
            if (!enabled) {
                Thread.sleep(delay)
                continue
            }

            // Resize image to reduce memory usage
            val gray = queue.take()
            if (gray.empty()) continue
            val resized = Mat(height, width, CvType.CV_8UC1)
            Imgproc.resize(gray, resized, Size(width.toDouble(), height.toDouble()))

            // Equalize histogram
            Imgproc.equalizeHist(resized, resized)

            // Crop out region of interest
            val beginRow = (beginHeightRatio * height).toInt()
            val endRow = (endHeightRatio * height).toInt()
            val cropHeight = endRow - beginRow
            val beginCol = (beginWidthRatio * width).toInt()
            val endCol = (endWidthRatio * width).toInt()
            val cropWidth = endCol - beginCol
            val cropped = resized.submat(beginRow, endRow, beginCol, endCol)

            // Threshold cropped image
            val binary = Mat(cropHeight, cropWidth, CvType.CV_8UC1)
            Imgproc.threshold(cropped, binary, binThresh, 255.0, Imgproc.THRESH_BINARY)

            // Compute non-path area ratio in three sub-regions
            val subWidth = cropWidth / 3
            val ratios = Array(3) { 0.0 }
            for (i in ratios.indices) {
                val region = Mat(0, subWidth, CvType.CV_8UC1)
                binary.submat(
                    0, cropHeight, subWidth * i, max(subWidth * (i + 1), cropWidth)
                ).copyTo(region)
                ratios[i] = Core.mean(region).`val`[0] / 255
            }

            // Display cropped image
            val show = Mat.zeros(height, width, CvType.CV_8UC1)
            binary.copyTo(show.submat(beginRow, endRow, beginCol, endCol))
            val bmp = Bitmap.createBitmap(show.width(), show.height(), Bitmap.Config.RGB_565)
            Utils.matToBitmap(show, bmp)
            activity.runOnUiThread {
                activity.procView.setImageBitmap(bmp)
            }

            // Decide where to go
            val minVal = ratios.min()!!
            if (minVal > routeThresh) {// no route is found
                listener("L") // turn around to find route
                activity.runOnUiThread {
                    activity.ratioText.text = String.format("%.2f %.2f %.2f",
                        ratios[0], ratios[1], ratios[2])
                }
            } else { // route is found
                var minIdx = 1
                for (i in ratios.indices)
                    if (ratios[i] == minVal) minIdx = i
                val instr = arrayOf("L", "A", "R")
                listener(instr[minIdx])

                var ratioText = ""
                for (i in ratios.indices) {
                    var dirText = String.format("%.2f", ratios[i])
                    if (i == minIdx)
                        dirText = "*$dirText*"
                    ratioText += "$dirText "
                }
                activity.runOnUiThread { activity.ratioText.text = ratioText }
            }

            Thread.sleep(delay)
        }
    }

    init { processor.start() }

    fun update(gray: Mat) { if (enabled) queue.offer(gray) }
}