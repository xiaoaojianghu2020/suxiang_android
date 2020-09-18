package com.sx.enjoy.net

import com.likai.lib.net.HttpResult
import com.sx.enjoy.bean.UserBean
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    //注册发送验证码
    @GET("api-user/user/code")
    fun sendCode(@QueryMap map:Map<String,String>):Observable<HttpResult<String>>

    //注册
    @POST("app/users/register")
    fun register(@Body body : RequestBody):Observable<HttpResult<String>>

    //登录
    @FormUrlEncoded
    @POST("api-user/user/login")
    fun login(@Body body : RequestBody):Observable<HttpResult<UserBean>>

}
