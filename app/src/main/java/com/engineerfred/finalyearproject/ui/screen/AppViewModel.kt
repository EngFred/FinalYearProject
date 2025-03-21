package com.engineerfred.finalyearproject.ui.screen

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.finalyearproject.domain.usecases.DetectOfflineUseCase
import com.engineerfred.finalyearproject.domain.usecases.DetectOnlineUseCase
import com.engineerfred.finalyearproject.core.resource.Resource
import com.engineerfred.finalyearproject.domain.model.DetectionMode
import com.engineerfred.finalyearproject.domain.model.Detector
import com.engineerfred.finalyearproject.utils.prepareFilePart
import com.engineerfred.finalyearproject.utils.toBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val detectOnlineUseCase: DetectOnlineUseCase,
    private val detectOfflineUseCase: DetectOfflineUseCase,
): ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: AppUiEvents) {
        when(event) {
            is AppUiEvents.DetectedLocally -> {
                detectOffline(event.detector)
            }
            is AppUiEvents.DetectedRemotely -> {
                detectOnline(event.context)
            }

            is AppUiEvents.SelectedImage -> {
                updateImageUriAndBitmap(event.context, event.imageUri)
            }

            is AppUiEvents.ToggledCameraVisibility -> {
                updateCameraVisibility(event.visible)
            }
        }
    }

    private fun updateCameraVisibility(visible: Boolean) {
        _uiState.update {
            it.copy(
                showCamera = visible
            )
        }
    }

    private fun updateImageUriAndBitmap(context: Context, imageUri: Uri) {
        _uiState.update {
            it.copy(
                feedbackMessage = null,
                imageUri = imageUri,
                imageBitmap = imageUri.toBitmap(context.contentResolver),
                boundingBoxes = emptyList(),
                detectionMode = null,
                showCamera = false
            )
        }
    }

    private fun detectOffline(detector: Detector) {

        if ( _uiState.value.imageBitmap == null ) {
            _uiState.update {
                it.copy(
                    feedbackMessage = "No image selected!"
                )
            }
            return
        }

        viewModelScope.launch {
            val boundingBoxes = detectOfflineUseCase(detector, _uiState.value.imageBitmap!!)
            _uiState.update {
                it.copy(
                    boundingBoxes = boundingBoxes,
                    detectionMode = DetectionMode.Local,
                    showCamera = false,
                    feedbackMessage = if ( boundingBoxes.isEmpty() ) "No fracture detected!" else null,
                )
            }
        }
    }

    private fun detectOnline(context: Context) {
        _uiState.update {
            it.copy(
                detectionMode = DetectionMode.Remote
            )
        }

        if ( _uiState.value.imageUri == null ) {
            _uiState.update {
                it.copy(
                    feedbackMessage = "No image selected!"
                )
            }
            return
        }

        val file = prepareFilePart(context, _uiState.value.imageUri!!)

        viewModelScope.launch( Dispatchers.IO ) {
            _uiState.update {
                it.copy(
                    isDetecting = true,
                    feedbackMessage = null
                )
            }
            val res = detectOnlineUseCase.invoke(file)
            when (res) {
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isDetecting = false,
                            feedbackMessage = res.errMsg
                        )
                    }
                }

                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isDetecting = false,
                            feedbackMessage = "Detection Completed!",
                            imageUri = if ( res.data != null ) res.data.toUri() else it.imageUri
                        )
                    }
                }
            }
        }
    }
}