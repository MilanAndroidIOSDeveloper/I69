package com.i69app.data.remote.requests

data class PurchaseRequest(
    val id: String = "",
    val coins: Int = 0,
    val money: Double = 0.0,
    val method: String = ""
)