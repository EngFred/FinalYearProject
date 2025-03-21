package com.engineerfred.finalyearproject.di

import android.content.Context
import android.content.SharedPreferences
import com.engineerfred.finalyearproject.data.local.PrefsStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesSharedPreferences(
        @ApplicationContext
        context: Context
    ) : SharedPreferences {
        return context.getSharedPreferences("FracDetectPrefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providesPreferencesStore(
        sharedPreferences: SharedPreferences
    ) : PrefsStore {
        return PrefsStore(sharedPreferences)
    }
}