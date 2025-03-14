package com.engineerfred.finalyearproject.di

import android.content.Context
import com.engineerfred.finalyearproject.data.remote.ApiConstants
import com.engineerfred.finalyearproject.data.remote.ApiService
import com.engineerfred.finalyearproject.data.repo.AppRepositoryImpl
import com.engineerfred.finalyearproject.data.local.OfflineDetector
import com.engineerfred.finalyearproject.domain.repo.AppRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun providesOkhttp() : OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .connectTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun providesRetrofitInstance(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(ApiConstants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    @Provides
    @Singleton
    fun providesApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun providesAppRepository(
        apiService: ApiService,
        detector: OfflineDetector
    ): AppRepository = AppRepositoryImpl(apiService, detector)

    @Provides
    @Singleton
    fun providesOfflineDetector(
        @ApplicationContext
        context: Context
    ): OfflineDetector = OfflineDetector(context)

}