package org.example

class ShoppingCart(private val inventory: InventoryRepository) {
    private val cartItems = mutableMapOf<Int, Int>()

    fun addItem(productId: Int, quantity: Int) {
        val product = inventory.getProductById(productId)
            ?: throw IllegalArgumentException("Product not found.")
        if (quantity > product.stock) throw IllegalArgumentException("Not enough stock.")
        cartItems[productId] = (cartItems[productId] ?: 0) + quantity
    }

    fun removeItem(productId: Int) {
        cartItems.remove(productId)
    }

    fun clear() {
        cartItems.clear()
    }

    fun getItems(): Map<Products, Int> {
        return cartItems.mapNotNull { (id, qty) ->
            inventory.getProductById(id)?.let { it to qty }
        }.toMap()
    }

    fun getTotalItems(): Int = cartItems.values.sum()

    fun getTotalPrice(): Double {
        return getItems().entries.sumOf { (p, q) -> p.price * q }
    }

    fun checkout() {
        for ((id, qty) in cartItems) {
            val product = inventory.getProductById(id)
                ?: throw IllegalArgumentException("Product not found.")
            if (qty > product.stock) throw IllegalArgumentException("Not enough stock.")
        }
        for ((id, qty) in cartItems) {
            val product = inventory.getProductById(id)!!
            val newStock = product.decreaseStock(qty)
            inventory.updateProduct(product.copy(stock = newStock))
        }
        clear()
    }
}
