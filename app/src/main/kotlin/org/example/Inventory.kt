package org.example
import java.io.File

class Inventory {

    private val products: MutableMap<Int, Products> = mutableMapOf()
    private val fileName = "products.txt"

    init {
        loadFromFile()
    }

    fun addProduct() {
        println("Enter product ID: ")
        val id = readLine()!!.toInt()

        println("Enter product name: ")
        val name = readLine()!!

        println("Enter product description: ")
        val desc = readLine()!!

        println("Enter product price: ")
        val price = readLine()!!.toDouble()

        println("Enter product stock: ")
        val stock = readLine()!!.toInt()

        val category = CategoryManager.selectCategory() ?: return

        val product = Products(id, name, desc, price, stock, category)
        products[id] = product
        println("Product added: $product")

        saveToFile()
    }

    fun deleteProduct() {
        println("Enter product ID to delete: ")
        val id = readLine()!!.toInt()

        if (products.remove(id) != null) {
            println("Product with ID $id removed.")
            saveToFile()
        } else {
            println("Product with ID $id not found.")
        }
    }

    fun searchProduct() {
        println("Enter product ID to search: ")
        val id = readLine()!!.toInt()

        val product = products[id]
        if (product != null) {
            println("Found: $product")
        } else {
            println("Product with ID $id not found.")
        }
    }

    fun increaseProductStock() {
        println("Enter product ID to increase stock: ")
        val id = readLine()!!.toInt()
        
        val product = products[id]
        if (product != null) {
            println("Enter amount to increase: ")
            val amount = readLine()!!.toInt()
            product.increaseStock(amount)
            saveToFile()
        } else {
            println("Product with ID $id not found.")
        }
    }

    fun decreaseProductStock() {
        println("Enter product ID to decrease stock: ")
        val id = readLine()!!.toInt()
        
        val product = products[id]
        if (product != null) {
            println("Enter amount to decrease: ")
            val amount = readLine()!!.toInt()
            product.decreaseStock(amount)
            saveToFile()
        } else {
            println("Product with ID $id not found.")
        }
    }

    private fun saveToFile() {
        val file = File(fileName)
        file.printWriter().use { out ->
            products.values.forEach { p ->
                out.println("${p.id},${p.name},${p.desc},${p.price},${p.stock},${p.category.name}")
            }
        }
    }

    private fun loadFromFile() {
        val file = File(fileName)
        if (!file.exists()) return

        file.forEachLine { line ->
            val parts = line.split(",")
            if (parts.size == 6) {
                try {
                    val product = Products(
                        parts[0].toInt(),
                        parts[1],
                        parts[2],
                        parts[3].toDouble(),
                        parts[4].toInt(),
                        Category.valueOf(parts[5])
                    )
                    products[product.id] = product
                } catch (e: IllegalArgumentException) {
                    println("Warning: Unknown category ${parts[5]} for product ${parts[1]}")
                }
            }
        }
    }

    fun getAllProducts(): List<Products> {
        return products.values.toList()
    }

}