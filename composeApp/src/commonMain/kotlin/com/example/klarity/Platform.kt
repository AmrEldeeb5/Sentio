package com.example.klarity

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
