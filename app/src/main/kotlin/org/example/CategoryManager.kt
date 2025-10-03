package org.example

import java.io.File

object CategoryManager {
    
    private val customCategoriesFile = "custom_categories.txt"
    private val customCategories = mutableMapOf<String, String>() // name -> emoji
    
    init {
        loadCustomCategories()
    }
    
    fun selectCategory(): Category? {
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
    
    fun manageCategoriesMenu(inventory: Inventory) {
        var running = true
        
        while (running) {
            println("\n╔════════════════════════════════╗")
            println("║    CATEGORY MANAGEMENT         ║")
            println("╠════════════════════════════════╣")
            println("1. View all categories")
            println("2. Delete category and its products")
            println("0. Back to main menu")
            println("╚════════════════════════════════╝")
            
            print("Choose an option: ")
            val option = readLine()?.toIntOrNull() ?: -1
            
            when(option) {
                1 -> showAllCategories()
                2 -> deleteCategoryWithProducts(inventory)
                0 -> running = false
                else -> println("Invalid option")
            }
        }
    }
    
    private fun showAllCategories() {
        println("\nAvailable categories:")
        Category.values().forEach { 
            println("  $it")
        }
    }
    
    private fun deleteCategoryWithProducts(inventory: Inventory) {
        val category = selectCategory() ?: return
        
        print("Are you sure you want to delete category '$category' and all its products? (yes/no): ")
        val confirm = readLine()?.lowercase()
        
        if (confirm == "yes") {
            val deleted = inventory.deleteProductsByCategory(category)
            println("Category '$category' deleted with $deleted products.")
        } else {
            println("Operation cancelled.")
        }
    }
    
    private fun loadCustomCategories() {
        val file = File(customCategoriesFile)
        if (!file.exists()) return
        
        file.forEachLine { line ->
            val parts = line.split(",")
            if (parts.size == 2) {
                customCategories[parts[0]] = parts[1]
            }
        }
    }
    
    private fun saveCustomCategories() {
        val file = File(customCategoriesFile)
        file.printWriter().use { out ->
            customCategories.forEach { (name, emoji) ->
                out.println("$name,$emoji")
            }
        }
    }
}