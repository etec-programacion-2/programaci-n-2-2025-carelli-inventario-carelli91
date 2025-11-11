package org.example

// Gestiona el carrito de compras del sistema
class ShoppingCart(private val inventory: InventoryRepository) {
    // Mapa de ID de producto a cantidad
    private val cartItems = mutableMapOf<Int, Int>()

    // Agrega un producto al carrito
    fun addItem(productId: Int, quantity: Int) {
        val product = inventory.getProductById(productId)
            ?: throw IllegalArgumentException("Product not found.")
        require(quantity <= product.stock) { "Not enough stock." }
        cartItems[productId] = (cartItems[productId] ?: 0) + quantity
    }

    // Elimina un producto del carrito
    fun removeItem(productId: Int) {
        cartItems.remove(productId)
    }

    // Vacía el carrito
    fun clear() {
        cartItems.clear()
    }

    // Obtiene los items del carrito
    fun getItems(): Map<Products, Int> =
        cartItems.mapNotNull { (id, qty) ->
            inventory.getProductById(id)?.let { it to qty }
        }.toMap()

    // Calcula la cantidad total de items
    fun getTotalItems(): Int = cartItems.values.sum()

    // Calcula el precio total del carrito
    fun getTotalPrice(): Double =
        getItems().entries.sumOf { (p, q) -> p.price * q }

    // Realiza el checkout y actualiza el inventario
    fun checkout() {
        // Validar stock disponible
        cartItems.forEach { (id, qty) ->
            val product = inventory.getProductById(id)
                ?: throw IllegalArgumentException("Product not found.")
            require(qty <= product.stock) { "Not enough stock." }
        }
        
        // Actualizar stock de cada producto
        cartItems.forEach { (id, qty) ->
            val product = inventory.getProductById(id)!!
            val newStock = product.decreaseStock(qty)
            inventory.updateProduct(product.copy(stock = newStock))
        }
        
        clear() // Vaciar carrito
    }
}