package com.ick.kalambury.net.api

import com.ick.kalambury.net.api.dto.TableConfigDto
import com.ick.kalambury.net.api.dto.TableIdDto
import com.ick.kalambury.net.api.dto.TablesDto
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.adapter.rxjava3.Result
import retrofit2.http.*

interface RestApiInterface {

    @POST("/v1/game/create/{uuid}/{nickname}")
    fun createTable(
        @Path("uuid") uuid: String,
        @Path("nickname") nickname: String,
        @Body config: TableConfigDto,
    ): Single<Result<TableIdDto>>

    @GET("/v2/game/tables")
    fun getTables(): Single<Result<TablesDto>>

    @GET("/v1/words/{setId}")
    fun getWordsSet(@Path("setId") setId: String): Single<Result<ResponseBody>>

    @Multipart
    @POST("v1/log/submit")
    fun submitLogs(
        @Part file: MultipartBody.Part,
        @Part("file") body: RequestBody,
    ): Completable

}