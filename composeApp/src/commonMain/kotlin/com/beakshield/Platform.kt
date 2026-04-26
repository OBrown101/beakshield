package com.beakshield

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform