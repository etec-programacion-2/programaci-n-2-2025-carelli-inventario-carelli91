package org.example

class InventoryService(private val inventory: Inventory) {

    fun getProductsPerCategory() {
        println("Enter category (ELECTRONICS, CARE, FOOD, CANDIES, CLOTHING, OTHERS): ")
        val categoryInput = readLine()!!.uppercase()
        val category = Category.valueOf(categoryInput)
        
        val products = inventory.getAllProducts().filter { it.category == category }
        if (products.isEmpty()) {
            println("No products found in category $category.")
        } else {
            println("Products in $category category:")
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