package org.example

// Clase que representa un producto del inventario
data class Products(
    val id: Int,
    var name: String,
    var desc: String,
    var price: Double,
    var stock: Int,
    var category: Category
) {
    init {
        // Validaciones al crear el producto
        require(id in 10000..99999) { "Product ID must be a 5-digit number" }
        require(name.isNotBlank()) { "Product name cannot be blank" }
        require(price >= 0) { "Price cannot be negative" }
        require(stock >= 0) { "Stock cannot be negative" }
    }

    // Incrementa el stock del producto
    fun increaseStock(amount: Int): Int {
        require(amount > 0) { "Amount must be positive" }
        stock += amount
        return stock
    }

    // Decrementa el stock del producto
    fun decreaseStock(amount: Int): Int {
        require(amount > 0) { "Amount must be positive" }
        require(amount <= stock) { "Not enough stock for $name." }
        stock -= amount
        return stock
    }

    // Edita los atributos del producto
    fun editProduct(newName: String, newDesc: String, newPrice: Double, newCategory: Category) {
        require(newName.isNotBlank()) { "Name cannot be blank" }
        require(newPrice >= 0) { "Price cannot be negative" }
        name = newName
        desc = newDesc
        price = newPrice
        category = newCategory
    }
}