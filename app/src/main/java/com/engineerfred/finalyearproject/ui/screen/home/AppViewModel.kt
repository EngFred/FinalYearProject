package com.engineerfred.finalyearproject.ui.screen.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.finalyearproject.data.local.OfflineDetector
import com.engineerfred.finalyearproject.domain.model.Detector
import com.engineerfred.finalyearproject.utils.toBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
): ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()

    private var detectorCache: MutableMap<Detector, OfflineDetector?> = mutableMapOf()


    fun onEvent(event: AppUiEvents) {
        when(event) {
            is AppUiEvents.DetectClicked -> {
                detect(event.detector, event.context)
            }

            is AppUiEvents.SelectedImage -> {
                updateImageUriAndBitmap(event.context, event.imageUri)
            }
        }
    }

    private fun updateImageUriAndBitmap(context: Context, imageUri: Uri) {
        _uiState.update {
            it.copy(
                feedbackMessage = null,
                imageUri = imageUri,
                imageBitmap = imageUri.toBitmap(context.contentResolver),
                boundingBoxes = emptyList(),
            )
        }
    }

    private fun detect(detector: Detector, context: Context) {
        _uiState.update {
            it.copy(
                feedbackMessage = null
            )
        }
        if ( _uiState.value.imageBitmap == null ) {
            _uiState.update {
                it.copy(
                    feedbackMessage = "No image selected!"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isDetecting = true)
            }
            val detectorInstance = detectorCache[detector] ?: createDetector(detector, context)
            val boundingBoxes = detectorInstance.detect(_uiState.value.imageBitmap!!)
            _uiState.update {
                it.copy(
                    boundingBoxes = boundingBoxes,
                    feedbackMessage = if ( boundingBoxes.isEmpty() ) "No fractures detected! If your not contented with the results, try again with another model." else null,
                )
            }
            _uiState.update {
                it.copy(isDetecting = false)
            }
        }
    }

    private suspend fun createDetector(detector: Detector, context: Context): OfflineDetector {
        return withContext(Dispatchers.IO) {
            val modelPath = when (detector) {
                Detector.DETECTOR_1 -> "model1.tflite"
                Detector.DETECTOR_2 -> "model2.tflite"
                Detector.DETECTOR_3 -> "model3.tflite"
            }
            modelPath.let { OfflineDetector(context, it).also { detectorCache[detector] = it } }
        }
    }

    override fun onCleared() {
        super.onCleared()
        detectorCache.values.forEach { it?.closeInterpreter() }
        detectorCache.clear()
    }
}