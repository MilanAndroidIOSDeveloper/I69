package com.i69app.db

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/**
 * Created by Waheed on 04,November,2019
 */

/**
 * A generic class that can provide a resource backed by both the sqlite database and the network.
 *
 *  You can read more about it in the [Architecture
 * Guide](https://developer.android.com/arch).
 *
 * @param <ResultType>
 * @param <RequestType>
 */
abstract class NetworkAndDBBoundResource<ResultType, RequestType> @MainThread
constructor(private val appExecutors: AppExecutors) {
    /**
     * The final result LiveData
     */
    private val result = MediatorLiveData<DBResource<ResultType?>>()

    init {
        // Send loading state to UI
        result.value = DBResource.loading()

        val dbSource = this.loadFromDb()

        result.addSource(dbSource) { data ->

            result.removeSource(dbSource) // Once done data loading remove source

            when {
                shouldFetch(data) -> fetchFromNetwork(dbSource)
                else -> {
                    result.addSource(dbSource) { newData ->
                        setValue(DBResource.success(newData,result.value?.retrofitAPICode ?: 0))
                    }
                }
            }
        }
    }

    /**
     * Fetch the data from network and persist into DB and then
     * send it back to UI.
     */
    private fun fetchFromNetwork(dbSource: LiveData<ResultType>) {
        val apiResponse = createCall()
        // we re-attach dbSource as a new source, it will dispatch its latest value quickly
        result.addSource(dbSource) {
            result.setValue(DBResource.loading())
        }

        result.addSource(apiResponse) { response ->
            result.removeSource(dbSource)
            result.removeSource(apiResponse)

            response?.apply {
                when {
                    status.isSuccessful() -> {
                        appExecutors.diskIO().execute {

                            processResponse(this)?.let { requestType ->
                                saveCallResult(requestType)
                            }
                            appExecutors.mainThread().execute {
                                // we specially request a new live data,
                                // otherwise we will get immediately last cached value,
                                // which may not be updated with latest results received from network.
                                result.addSource(loadFromDb()) { newData ->
                                    setValue(
                                        DBResource.success(
                                            newData,
                                            result.value?.retrofitAPICode ?: 0
                                        )
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        result.addSource(dbSource) {
                            result.setValue(
                                DBResource.error(
                                    errorMessage,
                                    result.value?.retrofitAPICode ?: 0
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    @MainThread
    private fun setValue(newValue: DBResource<ResultType?>) {
        if (result.value != newValue) result.value = newValue
    }


    fun asLiveData(): LiveData<DBResource<ResultType?>> = result

    @WorkerThread
    private fun processResponse(response: DBResource<RequestType>): RequestType? = response.data

    @WorkerThread
    protected abstract fun saveCallResult(item: RequestType)

    @MainThread
    protected abstract fun shouldFetch(data: ResultType?): Boolean

    @MainThread
    protected abstract fun loadFromDb(): LiveData<ResultType>

    @MainThread
    protected abstract fun createCall(): LiveData<DBResource<RequestType>>
}