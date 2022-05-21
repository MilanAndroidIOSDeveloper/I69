package com.i69app.data.config

import com.i69app.BuildConfig


object Constants {
    /*//const val BASE_URL = "https://api.i69app.com/"
    const val BASE_URL = "https://admin.chatadmin-mod.click/admin/"*/
    const val URL_TERMS_AND_CONDITION = "https://sites.google.com/view/i69-tos/"
    const val URL_PRIVACY_POLICY = "https://sites.google.com/view/i69-privacy-policy"
    const val ADMIN_EMAIL = "isixtynine.adm@gmail.com"
    const val SEARCH_RANDOM_LIMIT = 30
    const val SEARCH_POPULAR_LIMIT = 30

    const val INTEREST_MUSIC = 0
    const val INTEREST_MOVIE = 1
    const val INTEREST_TV_SHOW = 2
    const val INTEREST_SPORT_TEAM = 3
    const val EXTRA_INTEREST_TYPE = "EXTRA_INTEREST_TYPE"
    const val EXTRA_INTEREST_VALUE = "EXTRA_INTEREST_VALUE"
    const val EXTRA_IMG_SLIDER = "EXTRA_IMG_SLIDER"
    const val SLIDER_POSITION = "SLIDER_POSITION"


    /// Twitter
    var TWITTER_SIGN_IN_URL = "${BuildConfig.BASE_URL}auth/twitter/redirect/"
    var TWITTER_CALLBACK_URL = "${BuildConfig.BASE_URL}signin-twitter"


    /// IN APP BILLING
    const val IN_APP_FIRST_TYPE = "1_type"
    const val IN_APP_SECOND_TYPE = "2_type"
    const val IN_APP_THIRD_TYPE = "3_type"
    const val IN_APP_FOURTH_TYPE = "4_type"
    const val IN_APP_FIFTH_TYPE = "5_type"
    const val IN_APP_SIXTH_TYPE = "6_type"

    // One Signal
    const val ONESIGNAL_APP_ID = "0be9bbe8-af94-4498-8c96-36cc96604f41"

    // QuickBlox
//    const val QUICK_BLOX_APPLICATION_ID = "92345"
//    const val QUICK_BLOX_AUTH_KEY = "FUpscj3ysk8FVqS"
//    const val QUICK_BLOX_AUTH_SECRET = "7SQ6OgFY97S8u27"
//    const val QUICK_BLOX_ACCOUNT_KEY = "mmAJDN7meP3iFHT2Fsz4"
    const val QUICK_BLOX_APPLICATION_ID = "93464"
    const val QUICK_BLOX_AUTH_KEY = "hqVhTOTtk2xbYU8"
    const val QUICK_BLOX_AUTH_SECRET = "3ER3Lxpqac2KyuZ"
    const val QUICK_BLOX_ACCOUNT_KEY = "XnFLqBiyFmsGFW554E_6"

    const val CHAT_PORT = 5223
    const val SOCKET_TIMEOUT = 300
    const val KEEP_ALIVE: Boolean = true
    const val USE_TLS: Boolean = true
    const val AUTO_JOIN: Boolean = false
    const val AUTO_MARK_DELIVERED: Boolean = true
    const val RECONNECTION_ALLOWED: Boolean = true
    const val ALLOW_LISTEN_NETWORK: Boolean = true


    /// Google In-App Purchase
    val IN_APP_SKUS = arrayOf(
        com.i69app.data.config.Constants.IN_APP_FIRST_TYPE,
        com.i69app.data.config.Constants.IN_APP_SECOND_TYPE,
        com.i69app.data.config.Constants.IN_APP_THIRD_TYPE,
        com.i69app.data.config.Constants.IN_APP_FOURTH_TYPE,
        com.i69app.data.config.Constants.IN_APP_FIFTH_TYPE,
        com.i69app.data.config.Constants.IN_APP_SIXTH_TYPE
    )

    val IN_APP_SKUS_COINS = arrayOf(
        100,
        500,
        1150,
        250, /// Arrangement Wrong
        2550,
        5600
    )

    const val BASE_64_ENCODED_PUBLIC_KEY =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgEuKZHjXCRHqSvfWDn4WUve5DGE9847jCdG/foBN/WvAhOaAQpun6Row7TKnIO0M77mfQksoprYljGjDwngXf3kSoNFhzU+kHv1syfB7dzV7WcIR3phHyY8wZVjDZjrfYzbhfUjA91vfZSQl78eEzF5TGWArkoKp7jNUP0vKOL1aGCfdn6ypsdh39S+sQI8XGRyiWSkSwkcoPeOqJRkgCLbKKgjT1fF4Cc0as0Rj5/03durHfbeN76KsYqd74ZhWJwt3UuEX7NQNLwmwDdMv3leNMQddxAobyumpiEX+7mzbwHOHwz2a9S5zKEiqUkfxmw0eUMK9Vt4sxrte2TergwIDAQAB"


//    const val PAYPAL_CLIENT_ID = "AXG6q4YxIzaHkhHu0vlEk1QjYp-tHvMP_JhUjsCehtqZeIZsCs-dNvWxMzFF-KvLesBXYNk4-5gp0_ib" /* Sandbox */
//    const val PAYPAL_CLIENT_ID = "AQCdEBLfbKiVI0JrFrJDubigxC4O5YD78R1WU5xxkgBl69pmNoZs5NY2zIQf_SKTFWQJb_AAEJLF5j2Q" /* Production */
//
//    val PAYPAL_USER_ACTION = UserAction.PAY_NOW
//    val PAYPAL_CURRENCY = CurrencyCode.USD
//
////    val PAYPAL_ENVIRONMENT = Environment.SANDBOX /* Sandbox */
//    val PAYPAL_ENVIRONMENT = Environment.LIVE /* Production */

}