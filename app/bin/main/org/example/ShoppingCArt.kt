package org.example

class ShoppingCart(private val inventory: Inventory) {
    private val cartItems: MutableMap<Int, Int> = mutableMapOf() // Product ID -> Quantity
    
    fun addItem(productId: Int, quantity: Int) {
        val product = inventory.getProductById(productId)
        if (product != null) {
            if (quantity > product.stock) {
                throw IllegalArgumentException("Not enough stock for ${product.name}. Available: ${product.stock}")
            }
            
            val currentQuantity = cartItems[productId] ?: 0
            cartItems[productId] = currentQuantity + quantity
        }
    }
    
    fun removeItem(productId: Int) {
        cartItems.remove(productId)
    }
    
    fun clearCart() {
        cartItems.clear()
    }
    
    fun getCartItems(): Map<Products, Int> {
        return cartItems.mapNotNull { (productId, quantity) ->
            inventory.getProductById(productId)?.let { it to quantity }
        }.toMap()
    }
    
    fun getTotalItems(): Int {
        return cartItems.values.sum()
    }
    
    fun getTotalPrice(): Double {
        return getCartItems().entries.sumOf { (product, quantity) ->
            product.price * quantity
        }
    }
    
    fun processPurchase() {
        // Verificar stock antes de procesar
        for ((productId, quantity) in cartItems) {
            val product = inventory.getProductById(productId)
            if (product == null) {
                throw IllegalArgumentException("Product with ID $productId not found")
            }
            if (quantity > product.stock) {
                throw IllegalArgumentException("Not enough stock for ${product.name}. Available: ${product.stock}, Requested: $quantity")
            }
        }
        
        // Procesar la compra y actualizar stock
        for ((productId, quantity) in cartItems) {
            val product = inventory.getProductById(productId)!!
            product.decreaseStock(quantity)
            inventory.updateProductStock(productId, product.stock)
        }
        
        // Limpiar el carrito después de la compra exitosa
        clearCart()
    }
    
    fun updateItemQuantity(productId: Int, newQuantity: Int) {
        val product = inventory.getProductById(productId)
        if (product != null) {
            if (newQuantity <= 0) {
                removeItem(productId)
            } else {
                if (newQuantity > product.stock) {
                    throw IllegalArgumentException("Not enough stock for ${product.name}. Available: ${product.stock}")
                }
                cartItems[productId] = newQuantity
            }
        }
    }
}