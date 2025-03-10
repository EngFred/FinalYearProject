package com.engineerfred.finalyearproject.ui.vm

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.finalyearproject.data.repo.Repository
import com.engineerfred.finalyearproject.resource.Resource
import com.engineerfred.finalyearproject.utils.prepareFilePart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.engineerfred.finalyearproject.model.BoundingBox
import com.engineerfred.finalyearproject.model.Detector
import com.engineerfred.finalyearproject.utils.DetectionMode
import com.engineerfred.finalyearproject.utils.toBitmap
import kotlinx.coroutines.withContext
import java.net.InetAddress

class MyViewModel(
    private val context: Context
): ViewModel() {
    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri = _imageUri.asStateFlow()

    private val _errMsg = MutableStateFlow<String?>(null)
    val errMsg = _errMsg.asStateFlow()

    private val _isDetecting = MutableStateFlow(false)
    val isDetecting = _isDetecting.asStateFlow()

    private val _ipAddress = MutableStateFlow<String?>(null)
    val ipAddress = _ipAddress.asStateFlow()

    private val _detectionMode = MutableStateFlow<DetectionMode?>(null)
    val detectionMode = _detectionMode.asStateFlow()

    private val _imageBitmap = MutableStateFlow<Bitmap?>(null)
    val imageBitmap = _imageBitmap.asStateFlow()

    private val _boundingBoxes = MutableStateFlow<List<BoundingBox>>(emptyList())
    val boundingBoxes = _boundingBoxes.asStateFlow()

    private val _showCamera = MutableStateFlow(false)
    val showCamera = _showCamera.asStateFlow()

    private var repo: Repository? = null
    private var detector: Detector? = null

    init {

        detector = Detector(context, object : Detector.DetectorListener {
            override fun onEmptyDetect() {
                _boundingBoxes.value = emptyList()
            }

            override fun onDetect(boxes: List<BoundingBox>) {
                _boundingBoxes.value = boxes
            }
        })
    }

    fun setIpAddress(targetIpAddress: String) {
        _ipAddress.value = targetIpAddress
    }


    fun updateCameraVisibility(visible: Boolean) {
        _showCamera.value = visible
    }

    fun updateImageUriAndBitmap(imageUri: Uri) {
        _imageUri.value = imageUri
        _boundingBoxes.value = emptyList()
        _detectionMode.value = null
        _imageBitmap.value = imageUri.toBitmap(context.contentResolver)
    }

    fun predictLocally(bitmap: Bitmap) {
        _errMsg.value = null
        _detectionMode.value = DetectionMode.Local
        detector?.detect(bitmap)
        _showCamera.value = false
    }

    fun predictOnline(context: Context) {
        _detectionMode.value = DetectionMode.Remote

        if ( _imageUri.value == null ) {
            _errMsg.value = "No image selected!"
            return
        }

        val file = prepareFilePart(context, _imageUri.value!!)

        if (_ipAddress.value == null) {
            _errMsg.value = "Please set the API IP Address first!"
            return
        }

        if (!isValidIpAddress(_ipAddress.value!!)) {
            _errMsg.value = "Invalid IP address format"
            return
        }

        viewModelScope.launch( Dispatchers.IO ) {
            _isDetecting.value = true
            _errMsg.value = null
            val baseUrl = "http://${_ipAddress.value}:5000"
            Log.d("MyViewModel", "predictOnline: baseUrl = $baseUrl")
            repo = Repository(baseUrl)
            val res = repo!!.predict(file)
            when (res) {
                is Resource.Error -> {
                    _isDetecting.value = false
                    _errMsg.value = res.errMsg
                }

                is Resource.Success -> {
                    _isDetecting.value = false
                    res.data?.let {
                        _imageUri.value = it.toUri()
                    }
                }
            }
        }
    }

    private fun isValidIpAddress(ip: String): Boolean {
        val ipRegex = Regex(
            "^((25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$"
        )
        return ip.matches(ipRegex)
    }
}