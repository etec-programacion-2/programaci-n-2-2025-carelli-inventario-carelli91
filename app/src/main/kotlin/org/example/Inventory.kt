package org.example

import java.io.File

interface InventoryRepository {
    fun addProduct(product: Products)
    fun removeProductById(id: Int): Boolean
    fun getProductById(id: Int): Products?
    fun getAllProducts(): List<Products>
    fun searchProducts(query: String): List<Products>
    fun sortProductsBy(parameter: String, ascending: Boolean = true): List<Products>
    fun sortProductsByCategory(ascending: Boolean = true): List<Products>
    fun filterByCategory(category: Category): List<Products>
    fun updateProduct(product: Products)
    fun generateNewProductId(): Int
    fun isProductNameExists(name: String): Boolean
}

class Inventory : InventoryRepository {
    private val products = mutableMapOf<Int, Products>()
    private val fileName = "products.txt"

    init {
        loadFromFile()
    }

    override fun addProduct(product: Products) {
        if (products.containsKey(product.id)) throw IllegalArgumentException("Product ID already exists.")
        if (isProductNameExists(product.name)) throw IllegalArgumentException("Product name already exists.")
        products[product.id] = product
        saveToFile()
    }

    override fun removeProductById(id: Int): Boolean {
        val removed = products.remove(id)
        if (removed != null) saveToFile()
        return removed != null
    }

    override fun getProductById(id: Int): Products? = products[id]

    override fun getAllProducts(): List<Products> = products.values.sortedBy { it.id }

    override fun searchProducts(query: String): List<Products> {
        val q = query.lowercase()
        return products.values.filter {
            it.name.lowercase().contains(q) || it.desc.lowercase().contains(q)
        }
    }

    override fun sortProductsBy(parameter: String, ascending: Boolean): List<Products> {
        val sorted = when (parameter.lowercase()) {
            "id" -> products.values.sortedBy { it.id }
            "name" -> products.values.sortedBy { it.name }
            "price" -> products.values.sortedBy { it.price }
            "stock" -> products.values.sortedBy { it.stock }
            "category" -> products.values.sortedBy { it.category.displayName }
            else -> products.values.toList()
        }
        return if (ascending) sorted else sorted.reversed()
    }

    override fun sortProductsByCategory(ascending: Boolean): List<Products> {
        val grouped = products.values.groupBy { it.category.displayName }
        val order = if (ascending) grouped.keys.sorted() else grouped.keys.sortedDescending()
        return order.flatMap { grouped[it] ?: emptyList() }
    }

    override fun filterByCategory(category: Category): List<Products> {
        return products.values.filter { it.category == category }
    }

    override fun updateProduct(product: Products) {
        if (!products.containsKey(product.id)) throw IllegalArgumentException("Product not found.")
        products[product.id] = product
        saveToFile()
    }

    override fun generateNewProductId(): Int {
        var id = 10000
        while (id <= 99999) {
            if (!products.containsKey(id)) return id
            id++
        }
        return 99999
    }

    override fun isProductNameExists(name: String): Boolean {
        return products.values.any { it.name.equals(name, ignoreCase = true) }
    }

    private fun saveToFile() {
        File(fileName).printWriter().use { out ->
            products.values.forEach {
                out.println("${it.id},${it.name},${it.desc},${it.price},${it.stock},${it.category.name}")
            }
        }
    }

    private fun loadFromFile() {
        val file = File(fileName)
        if (!file.exists()) return
        file.forEachLine { line ->
            val parts = line.split(",")
            if (parts.size == 6) {
                val p = Products(
                    id = parts[0].toInt(),
                    name = parts[1],
                    desc = parts[2],
                    price = parts[3].toDouble(),
                    stock = parts[4].toInt(),
                    category = Category.valueOf(parts[5])
                )
                products[p.id] = p
            }
        }
    }
}
