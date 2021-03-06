package com.ick.kalambury.net.api

import com.ick.kalambury.net.api.dto.TableConfigDto
import com.ick.kalambury.net.api.dto.TableIdDto
import com.ick.kalambury.net.api.dto.TablesDto
import com.ick.kalambury.net.api.exceptions.*
import com.ick.kalambury.util.SchedulerProvider
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import retrofit2.adapter.rxjava3.Result as RetrofitResult

class RestApiManager(
    client: OkHttpClient,
    baseUrl: String,
    private val schedulers: SchedulerProvider,
) {

    private val api: RestApiInterface = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()
        .create(RestApiInterface::class.java)

    fun getTables(): Single<Result<TablesDto>> {
        return api.getTables()
            .subscribeOn(schedulers.io())
            .map(this::handleResult)
    }

    fun createTable(
        uuid: String,
        nickname: String,
        config: TableConfigDto
    ): Single<Result<TableIdDto>> {
        return api.createTable(uuid, nickname, config)
            .subscribeOn(schedulers.io())
            .map(this::handleResult)
    }

    fun getWordsSet(setId: String): Single<Result<ByteArray>> {
        return api.getWordsSet(setId)
            .subscribeOn(schedulers.io())
            .map {
            if (it.isError) return@map Result.failure(getException(it.error()!!))

            var body: ResponseBody? = null
            try {
                it.response()!!.let { response ->
                    if (response.isSuccessful) {
                        body = response.body()

                        if (body == null) return@map Result.failure(NetworkFailureException("No response body!"))

                        val out = ByteArrayOutputStream()
                        body!!.byteStream().copyTo(out)

                        return@map Result.success(out.toByteArray())
                    } else {
                        return@map Result.failure(
                            NonSuccessfulResponseException(
                                response.code(),
                                response.message()
                            )
                        )
                    }
                }
            } catch (e: IOException) {
                return@map Result.failure(NetworkFailureException(e))
            } finally {
                body?.close()
            }
        }
    }

    fun logSubmit(file: MultipartBody.Part, body: RequestBody): Completable {
        return api.submitLogs(file, body)
            .subscribeOn(schedulers.io())
            .onErrorResumeNext { Completable.error { getException(it) } }
    }

    private fun <T> handleResult(retrofitResult: RetrofitResult<T>): Result<T> {
        return if (retrofitResult.isError) {
            Result.failure(getException(retrofitResult.error()!!))
        } else {
            Result.success(retrofitResult.response()!!.body()!!)
        }
    }

    private fun getException(throwable: Throwable): Throwable {
        return when (throwable) {
            is IOException -> NetworkFailureException(throwable)
            is HttpException -> convertHttpException(throwable)
            else -> throwable
        }
    }

    private fun convertHttpException(exception: HttpException): Throwable {
        return when (exception.code()) {
            400 -> BadRequestException(exception.message())
            401 -> AuthorizationFailedException(exception.message())
            404 -> NotFoundException(exception.message())
            409 -> ConflictException(exception.message())
            429 -> TooManyRequestsException(exception.message())
            else -> NonSuccessfulResponseException(exception.code(), exception.message())
        }
    }

}