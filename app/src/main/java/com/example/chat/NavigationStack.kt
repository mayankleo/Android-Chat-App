package com.example.chat

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun NavigationStack(chatViewModel: ChatViewModel, socketViewModel: SocketViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screens.Chat.route) {

        composable(route = Screens.Login.route) {
            LoginPage(navController = navController, chatViewModel = chatViewModel)
        }

        composable(route = Screens.Chat.route) {
            ChatPage(socketViewModel = socketViewModel)
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
            HomePage(text = it.arguments?.getString("text"))
        }
    }
}