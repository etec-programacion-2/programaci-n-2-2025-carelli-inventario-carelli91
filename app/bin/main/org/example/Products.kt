package org.example

data class Products(
    val id: Int,
    var name: String,
    var desc: String,
    var price: Double,
    var stock: Int,
    var category: Category
) {
    init {
        require(id in 10000..99999) { "Product ID must be a 5-digit number" }
        require(name.isNotBlank()) { "Product name cannot be blank" }
        require(price >= 0) { "Price cannot be negative" }
        require(stock >= 0) { "Stock cannot be negative" }
    }

    fun increaseStock(amount: Int): Int {
        require(amount > 0)
        stock += amount
        return stock
    }

    fun decreaseStock(amount: Int): Int {
        require(amount > 0)
        if (amount > stock) throw IllegalArgumentException("Not enough stock for $name.")
        stock -= amount
        return stock
    }

    fun editProduct(newName: String, newDesc: String, newPrice: Double, newCategory: Category) {
        require(newName.isNotBlank())
        require(newPrice >= 0)
        name = newName
        desc = newDesc
        price = newPrice
        category = newCategory
    }
}
