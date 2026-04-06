package com.example.brewnote.navigation

sealed class Screen(val route: String) {

    // Auth
    object Auth : Screen("auth")

    // Tab roots
    object Home : Screen("home")
    object Brews : Screen("brews")
    object Beans : Screen("beans")
    object BeanNotes : Screen("bean_notes")
    object Vendors : Screen("vendors")
    object Profile : Screen("profile")

    // Brew screens
    object NewBrew : Screen("brew_form")
    object BrewDetail : Screen("brew_detail/{id}") {
        fun createRoute(id: String) = "brew_detail/$id"
    }
    object EditBrew : Screen("brew_edit/{id}") {
        fun createRoute(id: String) = "brew_edit/$id"
    }

    // Bean screens
    object NewBean : Screen("bean_form")
    object BeanDetail : Screen("bean_detail/{id}") {
        fun createRoute(id: String) = "bean_detail/$id"
    }
    object EditBean : Screen("bean_edit/{id}") {
        fun createRoute(id: String) = "bean_edit/$id"
    }

    // BeanNote screens
    object NewBeanNote : Screen("bean_note_form")
    object BeanNoteDetail : Screen("bean_note_detail/{id}") {
        fun createRoute(id: String) = "bean_note_detail/$id"
    }
    object EditBeanNote : Screen("bean_note_edit/{id}") {
        fun createRoute(id: String) = "bean_note_edit/$id"
    }

    // Vendor screens
    object NewVendor : Screen("vendor_form")
    object VendorDetail : Screen("vendor_detail/{id}") {
        fun createRoute(id: String) = "vendor_detail/$id"
    }
    object EditVendor : Screen("vendor_edit/{id}") {
        fun createRoute(id: String) = "vendor_edit/$id"
    }

    // Equipment screens (accessed from Home Library)
    object EquipmentList : Screen("equipment_list")
    object NewEquipment : Screen("equipment_form")
    object EquipmentDetail : Screen("equipment_detail/{id}") {
        fun createRoute(id: String) = "equipment_detail/$id"
    }
    object EditEquipment : Screen("equipment_edit/{id}") {
        fun createRoute(id: String) = "equipment_edit/$id"
    }

    // Brew Method screens (accessed from Home Library)
    object BrewMethodList : Screen("brew_method_list")
    object NewBrewMethod : Screen("brew_method_form")
    object BrewMethodDetail : Screen("brew_method_detail/{id}") {
        fun createRoute(id: String) = "brew_method_detail/$id"
    }
    object EditBrewMethod : Screen("brew_method_edit/{id}") {
        fun createRoute(id: String) = "brew_method_edit/$id"
    }
}
