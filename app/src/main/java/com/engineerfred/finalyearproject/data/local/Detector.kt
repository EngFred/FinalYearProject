package com.engineerfred.finalyearproject.data.local

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import androidx.core.graphics.scale
import com.engineerfred.finalyearproject.domain.model.BoundingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.PriorityQueue
import javax.inject.Inject

class Detector @Inject constructor(
    private val context: Context,
    private val modelPath: String
) {

    init {
        setup()
    }

    private var interpreter: Interpreter? = null
    private var labels = listOf(
        "elbow positive",
        "fingers positive",
        "forearm fracture",
        "humerus fracture",
        "humerus",
        "shoulder fracture",
        "wrist positive",
    )

    private var tensorWidth = 0
    private var tensorHeight = 0
    private var numChannel = 0
    private var numElements = 0

    private val imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
        .add(CastOp(INPUT_IMAGE_TYPE))
        .build()

    private fun setup() {
        try {
            val model = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options()
            options.numThreads = Runtime.getRuntime().availableProcessors()
            interpreter = Interpreter(model, options)

            val inputShape = interpreter?.getInputTensor(0)?.shape() ?: return
            val outputShape = interpreter?.getOutputTensor(0)?.shape() ?: return

            tensorWidth = inputShape[1]
            tensorHeight = inputShape[2]
            numChannel = outputShape[1]
            numElements = outputShape[2]

            Log.wtf(TAG, "Model loaded successfully!\n_________________________\nInput shape: $inputShape\nOutput shape: $outputShape\nTensor width: $tensorWidth\nTensor height: $tensorHeight\nNum channel: $numChannel\nNum Elements: $numElements")

        } catch (ex: Exception) {
            Log.e(TAG, "Error loading model: ${ex.message}")
        }
    }

    suspend fun detect(frame: Bitmap) : List<BoundingBox> {
        return try {
            withContext(Dispatchers.Default) {  // Use Default for CPU/GPU-bound tasks
                if (interpreter == null || tensorWidth == 0 || tensorHeight == 0 || numChannel == 0 || numElements == 0) {
                    return@withContext emptyList()
                }

                val resizedBitmap = frame.scale(tensorWidth, tensorHeight, false)

                val tensorImage = TensorImage(DataType.FLOAT32)
                tensorImage.load(resizedBitmap)
                val processedImage = imageProcessor.process(tensorImage)
                val imageBuffer = processedImage.buffer

                val output = TensorBuffer.createFixedSize(intArrayOf(1, numChannel, numElements),
                    OUTPUT_IMAGE_TYPE
                )

                interpreter?.run(imageBuffer, output.buffer) ?: return@withContext emptyList()

                return@withContext bestBox(output.floatArray) ?: emptyList()
            }
        }catch (ex: Exception) {
            Log.wtf(TAG, "Error detecting: $ex")
            return emptyList()
        }
    }

    private fun bestBox(array: FloatArray) : List<BoundingBox>? {
        val boundingBoxes = mutableListOf<BoundingBox>()
        for (c in 0 until numElements) {
            var maxConf = -1.0f
            var maxIdx = -1
            var j = 4
            var arrayIdx = c + numElements * j
            while (j < numChannel){
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx]
                    maxIdx = j - 4
                }
                j++
                arrayIdx += numElements
            }
            if (maxConf > CONFIDENCE_THRESHOLD) {
                var clsName: String
                if (maxIdx >= 0 && maxIdx < labels.size) {
                    clsName = labels[maxIdx]
                } else {
                    continue // Skip this box if classification index is invalid
                }
                val cx = array[c] // 0
                val cy = array[c + numElements] // 1
                val w = array[c + numElements * 2]
                val h = array[c + numElements * 3]
                val x1 = cx - (w/2F)
                val y1 = cy - (h/2F)
                val x2 = cx + (w/2F)
                val y2 = cy + (h/2F)
                if (x1 < 0F || x1 > 1F) continue
                if (y1 < 0F || y1 > 1F) continue
                if (x2 < 0F || x2 > 1F) continue
                if (y2 < 0F || y2 > 1F) continue

                boundingBoxes.add(
                    BoundingBox(
                        x1 = x1, y1 = y1, x2 = x2, y2 = y2,
                        cx = cx, cy = cy, w = w, h = h,
                        cnf = maxConf, cls = maxIdx, clsName
                    )
                )
            }
        }
        if (boundingBoxes.isEmpty()) return null
        return applyNMS(boundingBoxes)
    }

    private fun applyNMS(boxes: List<BoundingBox>) : MutableList<BoundingBox> {
        val sortedBoxes = PriorityQueue(compareByDescending<BoundingBox> { it.cnf }).apply { addAll(boxes) }
        val selectedBoxes = mutableListOf<BoundingBox>()

        while (sortedBoxes.isNotEmpty()) {
            val best = sortedBoxes.poll() ?: continue
            selectedBoxes.add(best)

            val filtered = sortedBoxes.filter { calculateIoU(best, it) < IOU_THRESHOLD }
            sortedBoxes.clear()
            sortedBoxes.addAll(filtered)
        }

        return selectedBoxes
    }

    private fun calculateIoU(box1: BoundingBox, box2: BoundingBox): Float {
        val x1 = maxOf(box1.x1, box2.x1)
        val y1 = maxOf(box1.y1, box2.y1)
        val x2 = minOf(box1.x2, box2.x2)
        val y2 = minOf(box1.y2, box2.y2)
        val intersectionArea = maxOf(0F, x2 - x1) * maxOf(0F, y2 - y1)
        val box1Area = box1.w * box1.h
        val box2Area = box2.w * box2.h
        return intersectionArea / (box1Area + box2Area - intersectionArea)
    }

    companion object {
        private const val TAG = "FracDetector2"
        private const val INPUT_MEAN = 0f
        private const val INPUT_STANDARD_DEVIATION = 255f
        private val INPUT_IMAGE_TYPE = DataType.FLOAT32
        private val OUTPUT_IMAGE_TYPE = DataType.FLOAT32
        private const val CONFIDENCE_THRESHOLD = 0.15F //2
        private const val IOU_THRESHOLD = 0.15F //3
    }

    fun closeInterpreter() {
        interpreter?.close()
        interpreter = null
    }
}