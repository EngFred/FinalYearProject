package com.engineerfred.finalyearproject.data.local

import android.content.SharedPreferences
import com.engineerfred.finalyearproject.domain.model.LiteModel
import javax.inject.Inject

class PrefsStore @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        private const val ON_BOARD_COMPLETED_KEY = "OnboardingCompleted"
        private const val SELECTED_MODEL_KEY = "SelectedModel"
        private const val USER_NAME_KEY = "Username"
    }

    fun setOnboardingCompleted(completed: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(ON_BOARD_COMPLETED_KEY, completed)
            apply()
        }
    }

    fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean(ON_BOARD_COMPLETED_KEY, false)
    }

    fun setSelectedModel(model: LiteModel) {
        with(sharedPreferences.edit()) {
            putString(SELECTED_MODEL_KEY, model.name)
            apply()
        }
    }

    fun getSelectedModel(): LiteModel? {
        val modelName = sharedPreferences.getString(SELECTED_MODEL_KEY, null)
        return modelName?.let { LiteModel.valueOf(it) }
    }

    fun setUsername(name: String) {
        with(sharedPreferences.edit()) {
            putString(USER_NAME_KEY, name)
            apply()
        }
    }

    fun getUsername(): String? {
        val username = sharedPreferences.getString(USER_NAME_KEY, null)
        return username
    }

}