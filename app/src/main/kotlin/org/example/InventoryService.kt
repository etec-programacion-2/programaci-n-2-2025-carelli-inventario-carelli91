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
        val category = selectCategory() ?: return
        
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

    // 🔽 Nueva función interna para elegir categoría (reemplaza CategoryManager)
    private fun selectCategory(): Category? {
        val categories = Category.values()

        println("\n╔════════════════════════════════╗")
        println("║      SELECT CATEGORY           ║")
        println("╠════════════════════════════════╣")
        categories.forEachIndexed { index, category ->
            println("${index + 1}. $category")
        }
        println("0. Cancel")
        println("╚════════════════════════════════╝")

        print("Choose a category: ")
        val option = readLine()?.toIntOrNull() ?: -1

        return when {
            option == 0 -> null
            option in 1..categories.size -> categories[option - 1]
            else -> {
                println("Invalid option")
                null
            }
        }
    }
}
