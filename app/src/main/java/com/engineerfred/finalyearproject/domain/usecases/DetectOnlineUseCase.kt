package com.engineerfred.finalyearproject.domain.usecases

import com.engineerfred.finalyearproject.domain.repo.AppRepository
import okhttp3.MultipartBody
import javax.inject.Inject

class DetectOnlineUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(file: MultipartBody.Part) = repository.detectOnline(file)
}