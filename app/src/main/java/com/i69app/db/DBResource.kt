package com.i69app.db

import com.i69app.utils.Status


/**
 * Created by Waheed on 04,November,2019
 */

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */
data class DBResource<ResultType>(
    var status: Status,
    var data: ResultType? = null,
    var retrofitAPICode: Int = 0,
    var errorMessage: String? = null
) {

    companion object {
        /**
         * Creates [DBResource] object with `SUCCESS` status and [data].
         * Returning object of Resource(Status.SUCCESS, data, null)
         * last value is null so passing it optionally
         *
         */
        fun <ResultType> success(data: ResultType, retrofitAPICode: Int): DBResource<ResultType> =
            DBResource(Status.SUCCESS, data, retrofitAPICode = retrofitAPICode)

        /**
         * Creates [DBResource] object with `LOADING` status to notify
         * the UI to showing loading.
         * Returning object of Resource(Status.SUCCESS, null, null)
         * last two values are null so passing them optionally
         */
        fun <ResultType> loading(): DBResource<ResultType> = DBResource(Status.LOADING)

        /**
         * Creates [DBResource] object with `ERROR` status and [message].
         * Returning object of Resource(Status.ERROR, errorMessage = message)
         */
        fun <ResultType> error(message: String?, retrofitAPICode: Int): DBResource<ResultType> =
            DBResource(Status.ERROR, errorMessage = message, retrofitAPICode = retrofitAPICode)

    }
}