package org.example

import java.io.File

// Interface del repositorio de inventario (Principio de Inversión de Dependencias)
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

// Implementación del repositorio de inventario con persistencia en archivo
class Inventory : InventoryRepository {
    // Almacenamiento en memoria
    private val products = mutableMapOf<Int, Products>()
    private val fileName = "products.txt"

    init {
        loadFromFile() // Cargar productos al iniciar
    }

    // Agrega un producto al inventario
    override fun addProduct(product: Products) {
        require(!products.containsKey(product.id)) { "Product ID already exists." }
        require(!isProductNameExists(product.name)) { "Product name already exists." }
        products[product.id] = product
        saveToFile()
    }

    // Elimina un producto por ID
    override fun removeProductById(id: Int): Boolean {
        val removed = products.remove(id)
        removed?.let { saveToFile() }
        return removed != null
    }

    // Obtiene un producto por ID
    override fun getProductById(id: Int): Products? = products[id]

    // Obtiene todos los productos ordenados por ID
    override fun getAllProducts(): List<Products> = products.values.sortedBy { it.id }

    // Busca productos por nombre o descripción
    override fun searchProducts(query: String): List<Products> {
        val q = query.lowercase()
        return products.values.filter {
            it.name.lowercase().contains(q) || it.desc.lowercase().contains(q)
        }
    }

    // Ordena productos según el parámetro especificado
    override fun sortProductsBy(parameter: String, ascending: Boolean): List<Products> {
        val sorted = when (parameter.lowercase()) {
            "id" -> products.values.sortedBy { it.id }
            "name" -> products.values.sortedBy { it.name }
            "price" -> products.values.sortedBy { it.price }
            "stock" -> products.values.sortedBy { it.stock }
            "category" -> products.values.sortedBy { it.category.displayName }
            else -> products.values.toList()
        }
        return when {
            ascending -> sorted
            else -> sorted.reversed()
        }
    }

    // Ordena productos agrupados por categoría
    override fun sortProductsByCategory(ascending: Boolean): List<Products> {
        val grouped = products.values.groupBy { it.category.displayName }
        val order = when {
            ascending -> grouped.keys.sorted()
            else -> grouped.keys.sortedDescending()
        }
        return order.flatMap { grouped[it] ?: emptyList() }
    }

    // Filtra productos por categoría
    override fun filterByCategory(category: Category): List<Products> =
        products.values.filter { it.category == category }

    // Actualiza un producto existente
    override fun updateProduct(product: Products) {
        require(products.containsKey(product.id)) { "Product not found." }
        products[product.id] = product
        saveToFile()
    }

    // Genera un ID único para un nuevo producto
    override fun generateNewProductId(): Int =
        (10000..99999).firstOrNull { !products.containsKey(it) } ?: 99999

    // Verifica si ya existe un producto con el nombre dado
    override fun isProductNameExists(name: String): Boolean =
        products.values.any { it.name.equals(name, ignoreCase = true) }

    // Guarda todos los productos en archivo
    private fun saveToFile() {
        File(fileName).printWriter().use { out ->
            products.values.forEach {
                out.println("${it.id},${it.name},${it.desc},${it.price},${it.stock},${it.category.name}")
            }
        }
    }

    // Carga productos desde archivo
    private fun loadFromFile() {
        val file = File(fileName)
        when {
            !file.exists() -> return
            else -> file.forEachLine { line ->
                val parts = line.split(",")
                when {
                    parts.size == 6 -> {
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
    }
}