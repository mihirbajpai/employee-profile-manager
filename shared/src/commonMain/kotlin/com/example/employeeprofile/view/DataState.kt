package com.example.employeeprofile.view

/**
 * Loading / Success / Failure for something being fetched. The list screen doesn't need this —
 * an empty list is a perfectly good answer there — but a detail screen does: "still loading"
 * and "no such employee" are different things and have to look different.
 */
sealed interface DataState<out T> {
    class Loading<T> : DataState<T>
    data class Success<out T>(val value: T) : DataState<T>
    data class Failure(val error: Throwable) : DataState<Nothing>
}
