package org.example

class InventoryApp {
    
    private val inventory = Inventory()
    private val inventoryService = InventoryService(inventory)
    
    fun run() {
        var running = true
        
        while (running) {
            showMenu()
            print("Choose an option: ")
            val option = readLine()?.toIntOrNull() ?: -1

            when(option) {
                1 -> inventory.addProduct()
                2 -> inventory.deleteProduct()
                3 -> inventory.searchProduct()
                4 -> inventory.showInventory()
                5 -> inventory.increaseProductStock()
                6 -> inventory.decreaseProductStock()
                7 -> inventoryService.reportsMenu()
                8 -> CategoryManager.manageCategoriesMenu(inventory)
                0 -> {
                    println("Exiting...")
                    running = false
                }
                else -> println("Invalid option")
            }
            
            if (running) {
                println()
            }
        }
    }
    
    private fun showMenu() {
        println(
            """
            ╔════════════════════════════════╗
            ║         INVENTORY              ║
            ╠════════════════════════════════╣
            1️⃣  Add product                   ║
            2️⃣  Remove product                ║
            3️⃣  Search product                ║
            4️⃣  Show inventory                ║
            5️⃣  Increase stock                ║
            6️⃣  Decrease stock                ║
            7️⃣  Reports                       ║
            8️⃣  Category Management           ║
            0️⃣  Exit                          ║
            ╚════════════════════════════════╝
            """.trimIndent()
        )
    }
}