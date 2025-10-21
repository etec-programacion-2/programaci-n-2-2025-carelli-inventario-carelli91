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
    private val btnEditProduct = JButton("Edit Product")
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
        val btnRefresh = JButton("↻") // Solo ícono de refresh

        // Botón refresh más pequeño y solo ícono
        btnRefresh.preferredSize = Dimension(50, 32)
        btnRefresh.toolTipText = "Refresh List"
        
        listOf(btnAdd, btnFilter, btnLowStock, btnRefresh).forEach {
            if (it != btnRefresh) it.preferredSize = Dimension(160, 32)
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
        actionPanel.add(btnEditProduct)
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
        btnEditProduct.addActionListener { showEditProductDialog() }
        btnViewDescription.addActionListener { showDescriptionDialog() }
        btnDelete.addActionListener { deleteSelectedProduct() }

        updateButtonsState()
        refreshProducts()

        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }

    private fun updateButtonsState() {
        val selected = productList.selectedValue != null
        btnEditProduct.isEnabled = selected
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
        val dialog = JDialog(frame, "Add Product", true)
        dialog.layout = BorderLayout()
        dialog.size = Dimension(400, 500)
        dialog.setLocationRelativeTo(frame)

        val panel = JPanel(GridLayout(0, 1, 4, 4))
        val tfId = JTextField().apply { 
            text = generateNewProductId().toString()
            isEditable = false
            background = Color.LIGHT_GRAY
        }
        val tfName = JTextField()
        val tfDesc = JTextArea(3, 20).apply {
            lineWrap = true
            wrapStyleWord = true
        }
        val tfPrice = JTextField()
        val tfStock = JTextField()
        val cbCategory = JComboBox(Category.values())

        panel.add(JLabel("Product ID (Auto-generated):")); panel.add(tfId)
        panel.add(JLabel("Name:")); panel.add(tfName)
        panel.add(JLabel("Description:")); panel.add(JScrollPane(tfDesc))
        panel.add(JLabel("Price:")); panel.add(tfPrice)
        panel.add(JLabel("Stock:")); panel.add(tfStock)
        panel.add(JLabel("Category:")); panel.add(cbCategory)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val btnOk = JButton("OK")
        val btnCancel = JButton("Cancel")

        btnOk.addActionListener {
            try {
                val id = tfId.text.toInt()
                val name = tfName.text.trim()
                val desc = tfDesc.text.trim()
                
                if (id < 10000 || id > 99999) {
                    JOptionPane.showMessageDialog(dialog, "Invalid ID. Must be a 5-digit number.", "Error", JOptionPane.ERROR_MESSAGE)
                    return@addActionListener
                }
                
                if (inventory.getProductById(id) != null) {
                    JOptionPane.showMessageDialog(dialog, "ID already exists. Please try again.", "Error", JOptionPane.ERROR_MESSAGE)
                    return@addActionListener
                }
                
                if (inventory.isProductNameExists(name)) {
                    JOptionPane.showMessageDialog(dialog, "Product name already exists. Please use a different name.", "Error", JOptionPane.ERROR_MESSAGE)
                    return@addActionListener
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
                dialog.dispose()
                JOptionPane.showMessageDialog(frame, "Product added successfully with ID: $id")
            } catch (_: Exception) {
                JOptionPane.showMessageDialog(dialog, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE)
            }
        }

        btnCancel.addActionListener { dialog.dispose() }

        buttonPanel.add(btnOk)
        buttonPanel.add(btnCancel)

        dialog.add(panel, BorderLayout.CENTER)
        dialog.add(buttonPanel, BorderLayout.SOUTH)
        dialog.isVisible = true
    }

    private fun showEditProductDialog() {
        val selected = productList.selectedValue ?: return
        
        val dialog = JDialog(frame, "Edit Product", true)
        dialog.layout = BorderLayout()
        dialog.size = Dimension(450, 450)
        dialog.setLocationRelativeTo(frame)

        val mainPanel = JPanel(BorderLayout(10, 10))
        mainPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // Panel superior con información del producto
        val infoPanel = JPanel(GridLayout(2, 1, 5, 5))
        infoPanel.border = BorderFactory.createTitledBorder("Product Information")
        infoPanel.add(JLabel("Name: ${selected.name}"))
        infoPanel.add(JLabel("ID: ${selected.id}"))
        mainPanel.add(infoPanel, BorderLayout.NORTH)

        // Panel central con controles de edición
        val centerPanel = JPanel()
        centerPanel.layout = BoxLayout(centerPanel, BoxLayout.Y_AXIS)

        // Panel de control de stock
        val stockPanel = JPanel(FlowLayout(FlowLayout.CENTER, 10, 10))
        stockPanel.border = BorderFactory.createTitledBorder("Stock Management")
        val btnIncrease = JButton("Increase Stock")
        val btnDecrease = JButton("Decrease Stock")
        val lblCurrentStock = JLabel("Current Stock: ${selected.stock}")

        btnIncrease.addActionListener {
            val amountStr = JOptionPane.showInputDialog(dialog, "Enter amount to increase:", "Increase Stock", JOptionPane.PLAIN_MESSAGE)
            val amount = amountStr?.toIntOrNull()
            if (amount != null && amount > 0) {
                try {
                    val product = inventory.getProductById(selected.id)
                    if (product != null) {
                        val newStock = product.increaseStock(amount)
                        inventory.updateProductStock(selected.id, newStock)
                        lblCurrentStock.text = "Current Stock: $newStock"
                        refreshProducts()
                    }
                } catch (e: IllegalArgumentException) {
                    JOptionPane.showMessageDialog(dialog, e.message, "Error", JOptionPane.ERROR_MESSAGE)
                }
            }
        }

        btnDecrease.addActionListener {
            val amountStr = JOptionPane.showInputDialog(dialog, "Enter amount to decrease:", "Decrease Stock", JOptionPane.PLAIN_MESSAGE)
            val amount = amountStr?.toIntOrNull()
            if (amount != null && amount > 0) {
                try {
                    val product = inventory.getProductById(selected.id)
                    if (product != null) {
                        val newStock = product.decreaseStock(amount)
                        inventory.updateProductStock(selected.id, newStock)
                        lblCurrentStock.text = "Current Stock: $newStock"
                        refreshProducts()
                    }
                } catch (e: IllegalArgumentException) {
                    JOptionPane.showMessageDialog(dialog, e.message, "Error", JOptionPane.ERROR_MESSAGE)
                }
            }
        }

        stockPanel.add(btnIncrease)
        stockPanel.add(btnDecrease)
        stockPanel.add(lblCurrentStock)

        // Panel de edición de detalles
        val editPanel = JPanel(BorderLayout(5, 5))
        editPanel.border = BorderFactory.createTitledBorder("Edit Product Details")
        editPanel.preferredSize = Dimension(400, 150)
        
        // Descripción - ocupa todo el ancho
        val descPanel = JPanel(BorderLayout(5, 5))
        val tfDesc = JTextArea(selected.desc, 3, 20).apply {
            lineWrap = true
            wrapStyleWord = true
        }
        
        val descScrollPane = JScrollPane(tfDesc).apply {
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            preferredSize = Dimension(380, 60)
        }
        
        descPanel.add(JLabel("Description:"), BorderLayout.NORTH)
        descPanel.add(descScrollPane, BorderLayout.CENTER)
        
        // Precio y Categoría - en la misma línea debajo de descripción
        val priceCategoryPanel = JPanel(FlowLayout(FlowLayout.LEFT, 10, 5))
        
        val pricePanel = JPanel(BorderLayout(5, 5))
        val tfPrice = JTextField(selected.price.toString()).apply {
            preferredSize = Dimension(120, 25)
        }
        pricePanel.add(JLabel("Price:"), BorderLayout.NORTH)
        pricePanel.add(tfPrice, BorderLayout.CENTER)
        
        val categoryPanel = JPanel(BorderLayout(5, 5))
        val cbCategory = JComboBox(Category.values()).apply { 
            selectedItem = selected.category
            preferredSize = Dimension(180, 25)
        }
        categoryPanel.add(JLabel("Category:"), BorderLayout.NORTH)
        categoryPanel.add(cbCategory, BorderLayout.CENTER)
        
        priceCategoryPanel.add(pricePanel)
        priceCategoryPanel.add(categoryPanel)

        editPanel.add(descPanel, BorderLayout.NORTH)
        editPanel.add(priceCategoryPanel, BorderLayout.CENTER)

        centerPanel.add(stockPanel)
        centerPanel.add(editPanel)
        mainPanel.add(centerPanel, BorderLayout.CENTER)

        // Panel de botones
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val btnSave = JButton("Save Changes")
        val btnCancel = JButton("Cancel")

        btnSave.addActionListener {
            try {
                val newDesc = tfDesc.text.trim()
                val newPrice = tfPrice.text.toDouble()
                val newCategory = cbCategory.selectedItem as Category
                
                if (newPrice < 0) {
                    JOptionPane.showMessageDialog(dialog, "Price cannot be negative.", "Error", JOptionPane.ERROR_MESSAGE)
                    return@addActionListener
                }
                
                // Actualizar el producto
                val updatedProduct = Products(
                    selected.id,
                    selected.name,
                    newDesc,
                    newPrice,
                    selected.stock, // Stock se mantiene igual (se modifica con los botones)
                    newCategory
                )
                
                // Reemplazar el producto en el inventario
                inventory.removeProductById(selected.id)
                inventory.addProduct(updatedProduct)
                
                refreshProducts()
                dialog.dispose()
                JOptionPane.showMessageDialog(frame, "Product updated successfully!")
                
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(dialog, "Error updating product: ${e.message}", "Error", JOptionPane.ERROR_MESSAGE)
            }
        }

        btnCancel.addActionListener { dialog.dispose() }

        buttonPanel.add(btnSave)
        buttonPanel.add(btnCancel)
        mainPanel.add(buttonPanel, BorderLayout.SOUTH)

        dialog.add(mainPanel)
        dialog.isVisible = true
    }

    private fun generateNewProductId(): Int {
        val existingIds = inventory.getAllProducts().map { it.id }.toSet()
        
        var newId = 10000
        while (newId <= 99999) {
            if (!existingIds.contains(newId)) {
                return newId
            }
            newId++
        }
        
        for (id in 10000..99999) {
            if (!existingIds.contains(id)) {
                return id
            }
        }
        
        return 99999
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
        val confirm = JOptionPane.showConfirmDialog(frame, "Delete ${selected.name}?", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
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
        
        val result = JOptionPane.showConfirmDialog(frame, panel, "Filter by Category", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
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
        val input = JOptionPane.showInputDialog(frame, "Enter stock threshold:", "Low Stock Report", JOptionPane.PLAIN_MESSAGE) ?: return
        val threshold = input.toIntOrNull() ?: return
        val results = inventory.getAllProducts().filter { it.stock < threshold }
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No products below that stock level.")
        } else {
            refreshProducts(results)
            JOptionPane.showMessageDialog(frame, "Found ${results.size} products with stock below $threshold")
        }
    }

    private class ProductRenderer : JLabel(), ListCellRenderer<Products> {
        override fun getListCellRendererComponent(
            list: JList<out Products>?,
            value: Products?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            val lightGray = Color(245, 245, 245)    // Gris muy claro para filas pares
            val lighterGray = Color(252, 252, 252)  // Gris casi blanco para filas impares
            
            text = if (value != null)
                "ID: ${value.id} | ${value.name} | $${"%.2f".format(value.price)} | Stock: ${value.stock} | ${value.category.emoji} ${value.category.displayName}"
            else ""
            
            // Fuente normal como antes
            font = Font("Segoe UI", Font.PLAIN, 14)
            isOpaque = true
            
            // Alternar colores para filas sin bordes bruscos
            background = if (isSelected) {
                Color(220, 235, 255)  // Azul claro para selección
            } else {
                if (index % 2 == 0) lighterGray else lightGray
            }
            
            foreground = Color.BLACK
            // Sin bordes, solo padding
            border = BorderFactory.createEmptyBorder(8, 10, 8, 10)
            return this
        }
    }
}