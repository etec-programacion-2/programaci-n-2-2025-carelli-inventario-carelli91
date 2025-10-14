package org.example

object CategoryManager {
    
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
}