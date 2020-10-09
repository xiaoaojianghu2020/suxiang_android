package com.sx.enjoy.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class NewOrderBean(
    @SerializedName("orderNo")val id:String,
    @SerializedName("payPrice")val payPrice:String
): Serializable