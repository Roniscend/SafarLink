package com.example.safarlink.domain.model

data class RideOption(
    val id: String,
    val providerName: String,
    val packageName: String,
    val deepLinkUri: String,
    val price: Int? = null,
    val eta: String = ""
)