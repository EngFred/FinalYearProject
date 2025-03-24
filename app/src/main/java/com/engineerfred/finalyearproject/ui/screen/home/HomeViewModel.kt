package com.engineerfred.finalyearproject.ui.screen.home

import android.content.Context
import android.net.Uri
import android.util.Log
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
    private val prefsStore: PrefsStore
): ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        getUsername()
        getModel()
    }

    private var detectorCache: MutableMap<LiteModel, Detector?> = mutableMapOf()


    fun onEvent(event: HomeUiEvents) {
        when(event) {
            is HomeUiEvents.ModelUpdated -> {
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

    private fun detect(model: LiteModel?, context: Context) {
        _uiState.update {
            it.copy(
                feedbackMessage = null
            )
        }

        if (_uiState.value.imageBitmap == null) {
            _uiState.update {
                it.copy(feedbackMessage = "No image selected!")
            }
            return
        }

        viewModelScope.launch {

            _uiState.update { it.copy(isDetecting = true) }

            saveModel(model)

            // Select a model (either the provided one or a random one)
            val selectedModel = _uiState.value.usedModel ?: LiteModel.entries.toTypedArray().random()

            val detectorInstance = detectorCache[selectedModel] ?: createDetector(selectedModel, context)

            val boundingBoxes = detectorInstance.detect(_uiState.value.imageBitmap!!)

            _uiState.update {
                it.copy(
                    usedModel = selectedModel,
                    boundingBoxes = boundingBoxes,
                    feedbackMessage = if (boundingBoxes.isEmpty())
                        "No fractures detected! If you're not satisfied, try another model."
                    else null,
                    isDetecting = false
                )
            }
        }
    }


    private suspend fun createDetector(model: LiteModel, context: Context): Detector {
        return withContext(Dispatchers.IO) {
            val modelPath = when (model) {
                LiteModel.FAST -> "best_fast.tflite"
                LiteModel.BALANCED -> "best_balanced.tflite"
                LiteModel.PRECISION -> "best_precision.tflite"
                LiteModel.EXTENDED -> "best_extended.tflite"
            }
            modelPath.let { Detector(context, it)
                .also { detectorCache[model] = it } }
        }
    }

    private fun saveModel(model: LiteModel?) {
        if (model != null) {
            if ( model != _uiState.value.usedModel ) {
                prefsStore.setSelectedModel(model)
                _uiState.update {
                    it.copy(usedModel = model)
                }
            }
        }
    }

    private fun getModel() {
        prefsStore.getSelectedModel()?.let { model ->
            Log.wtf("LITE_MODEL", "Model from preferences -> ${model.name}")
            _uiState.update {
                it.copy(
                    usedModel = model
                )
            }
        } ?: Log.wtf("LITE_MODEL", "No model found in preferences")
    }

    private fun getUsername() {
        prefsStore.getUsername()?.let { username ->
            _uiState.update {
                it.copy(
                    username = username
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        detectorCache.values.forEach { it?.closeInterpreter() }
        detectorCache.clear()
    }
}