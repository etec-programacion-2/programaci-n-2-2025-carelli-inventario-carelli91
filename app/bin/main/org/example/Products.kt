package org.example

data class Products (
    val id: Int, 
    val name: String, 
    val desc: String, 
    val price: Double, 
    var stock: Int,
    val category: Category
) {
    init {
        // Validar que el ID tenga 5 dígitos al crear el producto
        require(id in 10000..99999) { "Product ID must be a 5-digit number" }
        require(name.isNotBlank()) { "Product name cannot be blank" }
        require(price >= 0) { "Price cannot be negative" }
        require(stock >= 0) { "Stock cannot be negative" }
    }

    fun increaseStock(amount: Int): Int {
        require(amount > 0) { "Amount must be positive" }
        stock += amount
        println("Stock updated. Actual stock of $name: $stock.")
        return stock
    }

    fun decreaseStock(amount: Int): Int {
        require(amount > 0) { "Amount must be positive" }
        if (amount > stock) {
            throw IllegalArgumentException("Error: not enough stock of $name.")
        }
        stock -= amount
        println("Stock updated. Actual stock of $name: $stock.")
        return stock
    }
}