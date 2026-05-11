package com.example.ktorkmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform