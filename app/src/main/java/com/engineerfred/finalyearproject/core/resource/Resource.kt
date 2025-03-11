package com.engineerfred.finalyearproject.core.resource

sealed class Resource<out T> {
    data class Success<T>(val data: T): Resource<T>()
    data class Error(val errMsg: String) : Resource<Nothing>()
}