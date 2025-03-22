package com.engineerfred.finalyearproject.ui.screen.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engineerfred.finalyearproject.data.local.Detector
import com.engineerfred.finalyearproject.data.local.PrefsStore
import com.engineerfred.finalyearproject.domain.model.LiteModel
import com.engineerfred.finalyearproject.utils.PdfUtils.generateMedicalReport
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
class HomeViewModel @Inject constructor(
    prefsStore: PrefsStore
): ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        prefsStore.getUsername()?.let { username ->
            _uiState.update {
                it.copy(
                    username = username
                )
            }
        }
    }

    private var detectorCache: MutableMap<LiteModel, Detector?> = mutableMapOf()


    fun onEvent(event: HomeUiEvents) {
        when(event) {
            is HomeUiEvents.DetectClicked -> {
                detect(event.model, event.context)
            }

            is HomeUiEvents.SelectedImage -> {
                updateImageUriAndBitmap(event.context, event.imageUri)
            }

            is HomeUiEvents.GeneratedMedicalReport -> {
                if ( _uiState.value.boundingBoxes.isNotEmpty() && _uiState.value.imageBitmap != null && _uiState.value.username.isNullOrEmpty().not() ) {
                    viewModelScope.launch {
                        _uiState.update {
                            it.copy(
                                savingDocument = true
                            )
                        }
                        generateMedicalReport(event.context, _uiState.value.username!!, _uiState.value.boundingBoxes, _uiState.value.imageBitmap!!)
                        _uiState.update {
                            it.copy(
                                savingDocument = false
                            )
                        }
                    }
                }
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

    private fun detect(model: LiteModel, context: Context) {
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
            val detectorInstance = detectorCache[model] ?: createDetector(model, context)
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

    private suspend fun createDetector(model: LiteModel, context: Context): Detector {
        return withContext(Dispatchers.IO) {
            val modelPath = when (model) {
                LiteModel.MODEL_1 -> "model1.tflite"
                LiteModel.MODEL_2 -> "model2.tflite"
                LiteModel.MODEL_3 -> "model3.tflite"
            }
            modelPath.let { Detector(context, it)
                .also { detectorCache[model] = it } }
        }
    }

    override fun onCleared() {
        super.onCleared()
        detectorCache.values.forEach { it?.closeInterpreter() }
        detectorCache.clear()
    }
}