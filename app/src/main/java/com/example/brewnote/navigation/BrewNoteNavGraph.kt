package com.example.brewnote.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.brewnote.ui.auth.AuthScreen
import com.example.brewnote.ui.beans.BeanDetailScreen
import com.example.brewnote.ui.beans.BeanFormScreen
import com.example.brewnote.ui.beans.BeansListScreen
import com.example.brewnote.ui.beannotes.BeanNoteDetailScreen
import com.example.brewnote.ui.beannotes.BeanNoteFormScreen
import com.example.brewnote.ui.beannotes.BeanNotesListScreen
import com.example.brewnote.ui.brewmethods.BrewMethodDetailScreen
import com.example.brewnote.ui.brewmethods.BrewMethodFormScreen
import com.example.brewnote.ui.brewmethods.BrewMethodsListScreen
import com.example.brewnote.ui.brews.BrewDetailScreen
import com.example.brewnote.ui.brews.BrewFormScreen
import com.example.brewnote.ui.brews.BrewsListScreen
import com.example.brewnote.ui.equipment.EquipmentDetailScreen
import com.example.brewnote.ui.equipment.EquipmentFormScreen
import com.example.brewnote.ui.equipment.EquipmentListScreen
import com.example.brewnote.ui.home.HomeScreen
import com.example.brewnote.ui.profile.ProfileScreen
import com.example.brewnote.ui.vendors.VendorDetailScreen
import com.example.brewnote.ui.vendors.VendorFormScreen
import com.example.brewnote.ui.vendors.VendorsListScreen

private data class TabDestination(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
)

private val tabs = listOf(
    TabDestination(Screen.Home, "Home", Icons.Filled.Home),
    TabDestination(Screen.Brews, "Brews", Icons.Filled.LocalCafe),
    TabDestination(Screen.Beans, "Beans", Icons.Filled.Eco),
    TabDestination(Screen.BeanNotes, "Notes", Icons.Filled.Description),
    TabDestination(Screen.Vendors, "Vendors", Icons.Filled.Storefront),
    TabDestination(Screen.Profile, "Profile", Icons.Filled.Person),
)

private val tabRoutes = tabs.map { it.screen.route }.toSet()

@Composable
fun BrewNoteNavGraph(isAuthenticated: Boolean) {
    if (!isAuthenticated) {
        AuthScreen()
        return
    }

    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BrewNoteBottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToNewBrew = { navController.navigate(Screen.NewBrew.route) },
                    onNavigateToEquipmentList = { navController.navigate(Screen.EquipmentList.route) },
                    onNavigateToBrewMethodList = { navController.navigate(Screen.BrewMethodList.route) },
                    onNavigateToBeanDetail = { id -> navController.navigate(Screen.BeanDetail.createRoute(id)) },
                    onNavigateToBrewDetail = { id -> navController.navigate(Screen.BrewDetail.createRoute(id)) },
                )
            }

            composable(Screen.Brews.route) {
                BrewsListScreen(
                    onNavigateToDetail = { id -> navController.navigate(Screen.BrewDetail.createRoute(id)) },
                    onNavigateToNew = { navController.navigate(Screen.NewBrew.route) },
                )
            }
            composable(Screen.BrewDetail.route) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                BrewDetailScreen(
                    id = id,
                    onNavigateToEdit = { navController.navigate(Screen.EditBrew.createRoute(id)) },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(Screen.NewBrew.route) {
                BrewFormScreen(id = null, onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.EditBrew.route) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                BrewFormScreen(id = id, onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.Beans.route) {
                BeansListScreen(
                    onNavigateToDetail = { id -> navController.navigate(Screen.BeanDetail.createRoute(id)) },
                    onNavigateToNew = { navController.navigate(Screen.NewBean.route) },
                )
            }
            composable(Screen.BeanDetail.route) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                BeanDetailScreen(
                    id = id,
                    onNavigateToEdit = { navController.navigate(Screen.EditBean.createRoute(id)) },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(Screen.NewBean.route) {
                BeanFormScreen(id = null, onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.EditBean.route) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                BeanFormScreen(id = id, onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.BeanNotes.route) {
                BeanNotesListScreen(
                    onNavigateToDetail = { id -> navController.navigate(Screen.BeanNoteDetail.createRoute(id)) },
                    onNavigateToNew = { navController.navigate(Screen.NewBeanNote.route) },
                )
            }
            composable(Screen.BeanNoteDetail.route) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                BeanNoteDetailScreen(
                    id = id,
                    onNavigateToEdit = { navController.navigate(Screen.EditBeanNote.createRoute(id)) },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(Screen.NewBeanNote.route) {
                BeanNoteFormScreen(id = null, onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.EditBeanNote.route) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                BeanNoteFormScreen(id = id, onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.Vendors.route) {
                VendorsListScreen(
                    onNavigateToDetail = { id -> navController.navigate(Screen.VendorDetail.createRoute(id)) },
                    onNavigateToNew = { navController.navigate(Screen.NewVendor.route) },
                )
            }
            composable(Screen.VendorDetail.route) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                VendorDetailScreen(
                    id = id,
                    onNavigateToEdit = { navController.navigate(Screen.EditVendor.createRoute(id)) },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(Screen.NewVendor.route) {
                VendorFormScreen(id = null, onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.EditVendor.route) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                VendorFormScreen(id = id, onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.EquipmentList.route) {
                EquipmentListScreen(
                    onNavigateToDetail = { id -> navController.navigate(Screen.EquipmentDetail.createRoute(id)) },
                    onNavigateToNew = { navController.navigate(Screen.NewEquipment.route) },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(Screen.EquipmentDetail.route) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                EquipmentDetailScreen(
                    id = id,
                    onNavigateToEdit = { navController.navigate(Screen.EditEquipment.createRoute(id)) },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(Screen.NewEquipment.route) {
                EquipmentFormScreen(id = null, onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.EditEquipment.route) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                EquipmentFormScreen(id = id, onNavigateBack = { navController.popBackStack() })
            }

            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            composable(Screen.BrewMethodList.route) {
                BrewMethodsListScreen(
                    onNavigateToDetail = { id -> navController.navigate(Screen.BrewMethodDetail.createRoute(id)) },
                    onNavigateToNew = { navController.navigate(Screen.NewBrewMethod.route) },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(Screen.BrewMethodDetail.route) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                BrewMethodDetailScreen(
                    id = id,
                    onNavigateToEdit = { navController.navigate(Screen.EditBrewMethod.createRoute(id)) },
                    onNavigateBack = { navController.popBackStack() },
                )
            }
            composable(Screen.NewBrewMethod.route) {
                BrewMethodFormScreen(id = null, onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.EditBrewMethod.route) { entry ->
                val id = entry.arguments?.getString("id") ?: return@composable
                BrewMethodFormScreen(id = id, onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun BrewNoteBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route ?: return

    if (currentRoute !in tabRoutes) return

    NavigationBar {
        tabs.forEach { tab ->
            val selected = currentDestination.hierarchy.any { it.route == tab.screen.route }
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
            )
        }
    }
}
