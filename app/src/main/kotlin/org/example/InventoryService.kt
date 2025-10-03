package org.example

class InventoryService(private val inventory: Inventory) {

    fun reportsMenu() {
        var running = true
        
        while (running) {
            println("\n╔════════════════════════════════╗")
            println("║         REPORTS MENU           ║")
            println("╠════════════════════════════════╣")
            println("1. Search by Category")
            println("2. Low Stock Products")
            println("0. Back to main menu")
            println("╚════════════════════════════════╝")
            
            print("Choose an option: ")
            val option = readLine()?.toIntOrNull() ?: -1
            
            when(option) {
                1 -> getProductsPerCategory()
                2 -> getLowStockProducts()
                0 -> running = false
                else -> println("Invalid option")
            }
        }
    }

    fun getProductsPerCategory() {
        val category = CategoryManager.selectCategory() ?: return
        
        val products = inventory.getAllProducts().filter { it.category == category }
        if (products.isEmpty()) {
            println("No products found in category $category.")
        } else {
            println("\nProducts in $category category:")
            products.forEach { println(it) }
        }
    }

    fun getLowStockProducts() {
        println("Enter stock threshold: ")
        val threshold = readLine()!!.toInt()
        
        val products = inventory.getAllProducts().filter { it.stock < threshold }
        if (products.isEmpty()) {
            println("No low stock products found (threshold: $threshold).")
        } else {
            println("Low stock products (less than $threshold):")
            products.forEach { println(it) }
        }
    }

}