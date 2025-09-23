package org.example

data class Products (
    val id: Int, 
    val name: String, 
    val desc: String, 
    val price: Double, 
    var stock: Int,
    val category: Category
) {

    fun increaseStock(amount: Int): Int {
        stock += amount
        println("Stock updated. Actual stock of $name: $stock.")
        return stock
    }

    fun decreaseStock(amount: Int): Int {
        if (amount < 0) {
            println("Error: stock cannot be negative.")
        } else if (amount > stock) {
            println("Error: not enough stock of $name.")
        } else {
            stock -= amount
            println("Stock updated. Actual stock of $name: $stock.")
        }
        return stock
    }
}
