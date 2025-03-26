package com.example.chat

sealed class Screens(val route: String) {
    object Login: Screens("login_screen")
    object Home: Screens("home_screen")
    object Chat: Screens("chat_screen")
}