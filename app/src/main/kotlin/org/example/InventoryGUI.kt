package org.example

import java.awt.*
import javax.swing.*

fun main() {
    SwingUtilities.invokeLater { InventoryGUI().createAndShow() }
}

class InventoryGUI {
    private val inventory = Inventory()
    private val frame = JFrame("Inventory Management System")
    private val productListModel = DefaultListModel<Products>()
    private val productList = JList(productListModel)
    private val infoLabel = JLabel("Select a product to view details.")
    private val btnIncreaseStock = JButton("Increase Stock")
    private val btnDecreaseStock = JButton("Decrease Stock")
    private val btnDelete = JButton("Delete Product")
    private val btnViewDescription = JButton("View Description")

    fun createAndShow() {
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.size = Dimension(1000, 600)
        frame.layout = BorderLayout(10, 10)
        frame.minimumSize = Dimension(900, 500)

        val topPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        val btnAdd = JButton("Add Product")
        val btnFilter = JButton("Filter by Category")
        val btnLowStock = JButton("Low Stock Report")
        val btnRefresh = JButton("Refresh List")

        listOf(btnAdd, btnFilter, btnLowStock, btnRefresh).forEach {
            it.preferredSize = Dimension(160, 32)
            topPanel.add(it)
        }

        frame.add(topPanel, BorderLayout.NORTH)

        productList.cellRenderer = ProductRenderer()
        productList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        productList.addListSelectionListener { updateButtonsState() }

        val scrollPane = JScrollPane(productList)
        frame.add(scrollPane, BorderLayout.CENTER)

        val bottomPanel = JPanel(BorderLayout())
        val actionPanel = JPanel(FlowLayout(FlowLayout.CENTER, 15, 5))
        actionPanel.add(btnIncreaseStock)
        actionPanel.add(btnDecreaseStock)
        actionPanel.add(btnViewDescription)
        actionPanel.add(btnDelete)
        bottomPanel.add(actionPanel, BorderLayout.NORTH)
        infoLabel.horizontalAlignment = SwingConstants.CENTER
        bottomPanel.add(infoLabel, BorderLayout.SOUTH)
        frame.add(bottomPanel, BorderLayout.SOUTH)

        btnAdd.addActionListener { showAddDialog() }
        btnFilter.addActionListener { showFilterByCategoryDialog() }
        btnLowStock.addActionListener { showLowStockDialog() }
        btnRefresh.addActionListener { refreshProducts() }
        btnIncreaseStock.addActionListener { showIncreaseStockDialog() }
        btnDecreaseStock.addActionListener { showDecreaseStockDialog() }
        btnViewDescription.addActionListener { showDescriptionDialog() }
        btnDelete.addActionListener { deleteSelectedProduct() }

        updateButtonsState()
        refreshProducts()

        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }

    private fun updateButtonsState() {
        val selected = productList.selectedValue != null
        btnIncreaseStock.isEnabled = selected
        btnDecreaseStock.isEnabled = selected
        btnViewDescription.isEnabled = selected
        btnDelete.isEnabled = selected
    }

    private fun refreshProducts(list: List<Products>? = null) {
        productListModel.clear()
        val items = list ?: inventory.getAllProducts().sortedBy { it.id }
        items.forEach { productListModel.addElement(it) }
        infoLabel.text = "Showing ${items.size} products."
        updateButtonsState()
    }

    private fun showAddDialog() {
        val panel = JPanel(GridLayout(0, 1, 4, 4))
        val tfId = JTextField().apply { 
            text = generateNewProductId().toString()
            isEditable = false
            background = Color.LIGHT_GRAY
        }
        val tfName = JTextField()
        val tfDesc = JTextField()
        val tfPrice = JTextField()
        val tfStock = JTextField()
        val cbCategory = JComboBox(Category.values())

        panel.add(JLabel("Product ID (Auto-generated):")); panel.add(tfId)
        panel.add(JLabel("Name:")); panel.add(tfName)
        panel.add(JLabel("Description:")); panel.add(tfDesc)
        panel.add(JLabel("Price:")); panel.add(tfPrice)
        panel.add(JLabel("Stock:")); panel.add(tfStock)
        panel.add(JLabel("Category:")); panel.add(cbCategory)

        val result = JOptionPane.showConfirmDialog(frame, panel, "Add Product", JOptionPane.OK_CANCEL_OPTION)
        if (result != JOptionPane.OK_OPTION) return

        try {
            val id = tfId.text.toInt()
            val name = tfName.text.trim()
            val desc = tfDesc.text.trim()
            
            // Validar que el ID tenga exactamente 5 dígitos
            if (id < 10000 || id > 99999) {
                JOptionPane.showMessageDialog(frame, "Invalid ID. Must be a 5-digit number.", "Error", JOptionPane.ERROR_MESSAGE)
                return
            }
            
            // Verificar que el ID no exista
            if (inventory.getProductById(id) != null) {
                JOptionPane.showMessageDialog(frame, "ID already exists. Please try again.", "Error", JOptionPane.ERROR_MESSAGE)
                return
            }
            
            // Verificar que el nombre no exista
            if (inventory.isProductNameExists(name)) {
                JOptionPane.showMessageDialog(frame, "Product name already exists. Please use a different name.", "Error", JOptionPane.ERROR_MESSAGE)
                return
            }
            
            val product = Products(
                id,
                name,
                desc,
                tfPrice.text.toDouble(),
                tfStock.text.toInt(),
                cbCategory.selectedItem as Category
            )
            inventory.addProduct(product)
            refreshProducts()
            JOptionPane.showMessageDialog(frame, "Product added successfully with ID: $id")
        } catch (_: Exception) {
            JOptionPane.showMessageDialog(frame, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE)
        }
    }

    private fun generateNewProductId(): Int {
        val existingIds = inventory.getAllProducts().map { it.id }.toSet()
        
        // Buscar el siguiente ID disponible empezando desde 10000
        var newId = 10000
        while (newId <= 99999) {
            if (!existingIds.contains(newId)) {
                return newId
            }
            newId++
        }
        
        // Si todos los IDs están ocupados (muy improbable), buscar huecos
        for (id in 10000..99999) {
            if (!existingIds.contains(id)) {
                return id
            }
        }
        
        // Último recurso: ID temporal (debería mostrar error)
        return 99999
    }

    private fun showIncreaseStockDialog() {
        val selected = productList.selectedValue ?: return
        val amountStr = JOptionPane.showInputDialog(frame, "Enter amount to increase stock for ${selected.name}:", "0")
        val amount = amountStr?.toIntOrNull() ?: return
        
        if (amount <= 0) {
            JOptionPane.showMessageDialog(frame, "Amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }
        
        val newStock = selected.stock + amount
        inventory.updateProductStock(selected.id, newStock)
        refreshProducts()
        JOptionPane.showMessageDialog(frame, "Stock increased by $amount. New stock: $newStock")
    }

    private fun showDecreaseStockDialog() {
        val selected = productList.selectedValue ?: return
        val amountStr = JOptionPane.showInputDialog(frame, "Enter amount to decrease stock for ${selected.name}:", "0")
        val amount = amountStr?.toIntOrNull() ?: return
        
        if (amount <= 0) {
            JOptionPane.showMessageDialog(frame, "Amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }
        
        if (amount > selected.stock) {
            JOptionPane.showMessageDialog(frame, "Cannot decrease stock by $amount. Current stock is only ${selected.stock}.", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }
        
        val newStock = selected.stock - amount
        inventory.updateProductStock(selected.id, newStock)
        refreshProducts()
        JOptionPane.showMessageDialog(frame, "Stock decreased by $amount. New stock: $newStock")
    }

    private fun showDescriptionDialog() {
        val selected = productList.selectedValue ?: return
        JOptionPane.showMessageDialog(
            frame, 
            "Name: ${selected.name}\n\nID: ${selected.id}\n\nDescription:\n${selected.desc}",
            "Product Description - ${selected.name}",
            JOptionPane.INFORMATION_MESSAGE
        )
    }

    private fun deleteSelectedProduct() {
        val selected = productList.selectedValue ?: return
        val confirm = JOptionPane.showConfirmDialog(frame, "Delete ${selected.name}?", "Confirm", JOptionPane.YES_NO_OPTION)
        if (confirm == JOptionPane.YES_OPTION) {
            inventory.removeProductById(selected.id)
            refreshProducts()
            JOptionPane.showMessageDialog(frame, "Product deleted.")
        }
    }

    private fun showFilterByCategoryDialog() {
        val categories = Category.values()
        val categoryComboBox = JComboBox(categories)
        
        val panel = JPanel(GridLayout(0, 1, 4, 4))
        panel.add(JLabel("Select Category:"))
        panel.add(categoryComboBox)
        
        val result = JOptionPane.showConfirmDialog(frame, panel, "Filter by Category", JOptionPane.OK_CANCEL_OPTION)
        if (result != JOptionPane.OK_OPTION) return
        
        val selectedCategory = categoryComboBox.selectedItem as Category
        val results = inventory.getAllProducts().filter { it.category == selectedCategory }
        
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No products found in category: ${selectedCategory.displayName}")
        } else {
            refreshProducts(results)
            JOptionPane.showMessageDialog(frame, "Showing ${results.size} products in ${selectedCategory.displayName}")
        }
    }

    private fun showLowStockDialog() {
        val input = JOptionPane.showInputDialog(frame, "Enter stock threshold:") ?: return
        val threshold = input.toIntOrNull() ?: return
        val results = inventory.getAllProducts().filter { it.stock < threshold }
        if (results.isEmpty()) JOptionPane.showMessageDialog(frame, "No products below that stock level.")
        else refreshProducts(results)
    }

    private class ProductRenderer : JLabel(), ListCellRenderer<Products> {
        override fun getListCellRendererComponent(
            list: JList<out Products>?,
            value: Products?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            text = if (value != null)
                "ID: ${value.id} | ${value.name} | $${"%.2f".format(value.price)} | Stock: ${value.stock} | ${value.category.emoji} ${value.category.displayName}"
            else ""
            font = Font("Segoe UI", Font.PLAIN, 14)
            isOpaque = true
            background = if (isSelected) Color(220, 235, 255) else Color.WHITE
            foreground = Color.BLACK
            border = BorderFactory.createEmptyBorder(5, 10, 5, 10)
            return this
        }
    }
}