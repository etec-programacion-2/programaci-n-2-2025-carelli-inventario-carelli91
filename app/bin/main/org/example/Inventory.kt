package org.example
import java.io.File

class Inventory {

    private val products: MutableMap<Int, Products> = mutableMapOf()
    private val fileName = "products.txt"

    init {
        loadFromFile()
    }

    fun addProduct(product: Products) {
        // Validar que el ID tenga 5 dígitos
        if (product.id < 10000 || product.id > 99999) {
            throw IllegalArgumentException("Product ID must be a 5-digit number")
        }
        
        // Validar que el ID no exista
        if (products.containsKey(product.id)) {
            throw IllegalArgumentException("Product ID already exists")
        }
        
        // Validar que el nombre no exista
        if (isProductNameExists(product.name)) {
            throw IllegalArgumentException("Product name already exists")
        }
        
        products[product.id] = product
        saveToFile()
    }

    fun removeProductById(id: Int): Boolean {
        val removed = products.remove(id)
        if (removed != null) saveToFile()
        return removed != null
    }

    fun updateProductStock(id: Int, newStock: Int): Boolean {
        val product = products[id]
        return if (product != null) {
            product.stock = newStock
            saveToFile()
            true
        } else false
    }

    fun getProductById(id: Int): Products? = products[id]

    fun isProductNameExists(name: String): Boolean {
        return products.values.any { it.name.equals(name, ignoreCase = true) }
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
                } catch (_: Exception) { }
            }
        }
    }

    fun getAllProducts(): List<Products> = products.values.toList()

    // 🔍 Buscar productos por nombre o descripción
    fun searchProducts(query: String): List<Products> {
        val lowerQuery = query.trim().lowercase()
        return products.values.filter {
            it.name.lowercase().contains(lowerQuery) || it.desc.lowercase().contains(lowerQuery)
        }
    }

    // 🔢 Ordenar productos según distintos parámetros
    fun sortProductsBy(parameter: String, ascending: Boolean = true): List<Products> {
        val sortedList = when (parameter.lowercase()) {
            "id" -> products.values.sortedBy { it.id }
            "name" -> products.values.sortedBy { it.name.lowercase() }
            "price" -> products.values.sortedBy { it.price }
            "stock" -> products.values.sortedBy { it.stock }
            "category" -> products.values.sortedBy { it.category.displayName }
            else -> products.values.toList() // Si no reconoce el parámetro, devuelve sin ordenar
        }
        return if (ascending) sortedList else sortedList.reversed()
    }

    // 🧾 Ordenar productos agrupados por categoría
    fun sortProductsByCategory(ascending: Boolean = true): List<Products> {
        val grouped = products.values.groupBy { it.category.displayName }
        val sortedCategories = if (ascending)
            grouped.keys.sorted()
        else
            grouped.keys.sortedDescending()

        return sortedCategories.flatMap { categoryName ->
            grouped[categoryName]?.sortedBy { it.name.lowercase() } ?: emptyList()
        }
    }
}