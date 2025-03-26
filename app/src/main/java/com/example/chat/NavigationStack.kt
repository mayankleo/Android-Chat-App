package com.example.chat

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chat.ui.theme.ChatTheme

@Composable
fun NavigationStack() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screens.Chat.route) {

        composable(route = Screens.Login.route) {
            OTPLoginScreen(navController = navController)
        }

        composable(route = Screens.Chat.route) {
            ChatScreen(navController = navController)
        }

        composable(
            route = Screens.Home.route + "?text={text}",
            arguments = listOf(
                navArgument("text") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            HomeScreen(text = it.arguments?.getString("text"))
        }
    }
}