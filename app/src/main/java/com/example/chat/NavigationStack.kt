package com.example.chat

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun NavigationStack(viewModel: ChatViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screens.Login.route) {

        composable(route = Screens.Login.route) {
            LoginPage(navController = navController, viewModel = viewModel)
        }

        composable(route = Screens.Chat.route) {
            ChatPage(navController = navController)
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