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
                4 -> inventory.increaseProductStock()
                5 -> inventory.decreaseProductStock()
                6 -> inventoryService.reportsMenu()
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
            ╔═══════════════════════════════╗
            ║         INVENTORY             ║
            ╠═══════════════════════════════╣
            1️⃣  Add product                  ║
            2️⃣  Remove product               ║
            3️⃣  Search product               ║
            4️⃣  Increase stock               ║
            5️⃣  Decrease stock               ║
            6️⃣  Reports                      ║
            0️⃣  Exit                         ║
            ╚═══════════════════════════════╝
            """.trimIndent()
        )
    }
}