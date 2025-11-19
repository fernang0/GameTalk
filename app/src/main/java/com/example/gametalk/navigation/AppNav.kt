package com.example.gametalk.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.gametalk.ui.screen.LoginScreen
import com.example.gametalk.ui.screen.RegisterScreen
import com.example.gametalk.ui.screen.HomeScreen
import com.example.gametalk.ui.screen.CategoriesScreen
import com.example.gametalk.ui.screen.TopicsScreen
import com.example.gametalk.ui.screen.EditTopicScreen
import com.example.gametalk.viewmodel.LoginViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Home = "home"
    const val Categories = "categories"
    const val Topics = "topics/{categoryId}/{categoryName}/{userId}"
    const val EditTopic = "edit_topic/{topicId}/{userId}"
    
    fun topics(categoryId: Int, categoryName: String, userId: Int): String {
        return "topics/$categoryId/$categoryName/$userId"
    }
    
    fun editTopic(topicId: Int, userId: Int): String {
        return "edit_topic/$topicId/$userId"
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val viewModel: LoginViewModel = viewModel()

    NavHost(navController = nav, startDestination = Routes.Login) {

        // LOGIN
        composable(Routes.Login) {
            LoginScreen(
                onAuthenticated = {
                    nav.navigate("main_shell") {
                        popUpTo(Routes.Login) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRegister = {
                    nav.navigate(Routes.Register)
                }
            )
        }

        // REGISTER
        composable(Routes.Register) {
            RegisterScreen(
                onRegisterSuccess = {
                    nav.navigate(Routes.Login) {
                        popUpTo(Routes.Register) { inclusive = true }
                    }
                }
            )
        }

        // SHELL PRINCIPAL (Home dentro del drawer)
        navigation(
            startDestination = Routes.Home,
            route = "main_shell"
        ) {
            composable(Routes.Home) {
                DrawerScaffold(
                    currentRoute = Routes.Home,
                    onNavigate = { nav.navigate(it) },
                    onLogout = {
                        viewModel.onLogout()
                        nav.navigate(Routes.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    drawerState = drawerState,
                    scope = scope
                ) {
                    HomeScreen(
                        onNavigateToCategories = {
                            nav.navigate(Routes.Categories)
                        }
                    )
                }
            }

            composable(Routes.Categories) {
                DrawerScaffold(
                    currentRoute = Routes.Categories,
                    onNavigate = { nav.navigate(it) },
                    onLogout = {
                        viewModel.onLogout()
                        nav.navigate(Routes.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    drawerState = drawerState,
                    scope = scope
                ) {
                    CategoriesScreen(
                        onCategoryClick = { category ->
                            // Obtener userId del viewModel o sesión (asumimos userId = 1 por ahora)
                            val userId = 1
                            nav.navigate(Routes.topics(category.id, category.name, userId))
                        }
                    )
                }
            }

            composable(
                route = Routes.Topics,
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.IntType },
                    navArgument("categoryName") { type = NavType.StringType },
                    navArgument("userId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                val userId = backStackEntry.arguments?.getInt("userId") ?: 1

                TopicsScreen(
                    navController = nav,
                    categoryId = categoryId,
                    categoryName = categoryName,
                    userId = userId
                )
            }

            composable(
                route = Routes.EditTopic,
                arguments = listOf(
                    navArgument("topicId") { type = NavType.IntType },
                    navArgument("userId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val topicId = backStackEntry.arguments?.getInt("topicId") ?: 0
                val userId = backStackEntry.arguments?.getInt("userId") ?: 1

                EditTopicScreen(
                    navController = nav,
                    topicId = topicId,
                    userId = userId
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    drawerState: DrawerState,
    scope: CoroutineScope,
    content: @Composable () -> Unit
) {
    val destinations = listOf(
        DrawerItem("Home", Routes.Home),
        DrawerItem("Categorías", Routes.Categories),
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Menú",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )

                destinations.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentRoute != item.route) {
                                onNavigate(item.route)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // item cerrar sesion
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onLogout()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = { Text(appBarTitle(currentRoute)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { padding ->
            Surface(Modifier.padding(padding)) {
                content()
            }
        }
    }
}

private data class DrawerItem(val label: String, val route: String)

@Composable
private fun appBarTitle(route: String?): String = when (route) {
    Routes.Home -> "Home"
    Routes.Categories -> "Categorías"
    else -> ""
}