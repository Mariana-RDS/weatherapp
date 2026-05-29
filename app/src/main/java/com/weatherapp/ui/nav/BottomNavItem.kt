package com.weatherapp.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
sealed interface Route {
    val route: String

    data object Home : Route {
        override val route = "home"
    }

    data object ListScreen : Route {
        override val route = "list"
    }

    data object Map : Route {
        override val route = "map"
    }
}
sealed class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: Route
) {
    data object HomeButton :
        BottomNavItem("Início", Icons.Default.Home, Route.Home)

    data object ListButton :
        BottomNavItem("Favoritos", Icons.Default.Favorite, Route.ListScreen)

    data object MapButton :
        BottomNavItem("Mapa", Icons.Default.LocationOn, Route.Map)
}


