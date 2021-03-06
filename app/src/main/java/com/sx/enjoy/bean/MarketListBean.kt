package com.sx.enjoy.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class MarketListBean(
    @SerializedName("id")val id:String,
    @SerializedName("type")val type:Int,
    @SerializedName("createTime")val createTime:String,
    @SerializedName("userId")val userId:String,
    @SerializedName("userName")val userName:String,
    @SerializedName("orderNo")val orderNo:String,
    @SerializedName("richOrderNo")val richOrderNo:String,
    @SerializedName("userImg")val userImg:String,
    @SerializedName("userPhone")val userPhone:String,
    @SerializedName("amount")val amount:String,
    @SerializedName("richNum")val richNum:String,
    @SerializedName("amountSum")val amountSum:String,
    @SerializedName("transaction")val transaction:String,
    @SerializedName("aliNumber")val aliNumber:String,
    @SerializedName("aliQrcode")val aliQrcode:String,
    @SerializedName("wxQrcode")val wxQrcode:String,
    @SerializedName("isAliPay")val isAliPay:Int,
    @SerializedName("isWxPay")val isWxPay:Int
): Serializable