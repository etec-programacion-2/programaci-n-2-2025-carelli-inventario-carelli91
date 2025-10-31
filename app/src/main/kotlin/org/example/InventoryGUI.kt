package org.example

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.ListSelectionListener

fun main() {
    val inventory = Inventory()
    val gui = InventoryGUI(inventory)
    gui.show()
}

class InventoryGUI(private val inventory: InventoryRepository) {
    private val cart = ShoppingCart(inventory)
    private val frame = JFrame("Product Inventory 📦 Management System")
    private val listModel = DefaultListModel<Products>()
    private val list = JList(listModel)

    // Components
    private val addToCartBtn = JButton("🛒 Add to Cart")
    private val increaseStockBtn = JButton("➕ Increase Stock")
    private val decreaseStockBtn = JButton("➖ Decrease Stock")
    private val searchField = JTextField(15)
    private val sortComboBox = JComboBox(arrayOf("id", "name", "price", "stock", "category"))
    private val filterComboBox = JComboBox(Category.values().map { it.displayName }.toTypedArray().plus("Show All"))
    private val addBtn = JButton("➕ Add Product")
    private val editBtn = JButton("✏️ Edit Product")
    private val delBtn = JButton("❌ Delete Product")
    private val viewCartBtn = JButton("🛒 View Cart") 

    // --- Main Setup ---
    fun show() {
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.layout = BorderLayout(10, 10)
        frame.size = Dimension(1200, 700) 

        // North Panel (Search, Sort, Filter)
        frame.add(createControlPanel(), BorderLayout.NORTH)

        // Center Panel (Product List)
        list.cellRenderer = ProductRenderer() 
        frame.add(JScrollPane(list), BorderLayout.CENTER)

        // South Panel (Action Buttons)
        frame.add(createActionPanel(), BorderLayout.SOUTH)

        // Listeners
        addBtn.addActionListener { addProductDialog() }
        editBtn.addActionListener { editProductUnifiedDialog() } 
        delBtn.addActionListener { deleteProduct() }
        viewCartBtn.addActionListener { cartDialog() }
        addToCartBtn.addActionListener { addProductToCart() }
        increaseStockBtn.addActionListener { adjustStock(true) }
        decreaseStockBtn.addActionListener { adjustStock(false) }
        
        // Listeners that trigger the list refresh
        searchField.document.addDocumentListener(SimpleDocumentListener { applySortAndFilter() })
        sortComboBox.addActionListener { applySortAndFilter() }
        filterComboBox.addActionListener { applySortAndFilter() }

        list.addListSelectionListener(ListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                val isSelected = list.selectedValue != null
                addToCartBtn.isEnabled = isSelected
                increaseStockBtn.isEnabled = isSelected
                decreaseStockBtn.isEnabled = isSelected
            }
        })
        
        refresh()
        updateCartButton()
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
    
    // --- Panel Creation ---

    private fun createControlPanel(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.CENTER, 15, 10))
        panel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Display Controls"),
            EmptyBorder(5, 10, 5, 10)
        )
        
        panel.add(JLabel("🔍 Search:"))
        panel.add(searchField)
        panel.add(Box.createHorizontalStrut(15))
        panel.add(JLabel("↕️ Sort By:"))
        panel.add(sortComboBox)
        panel.add(Box.createHorizontalStrut(15))
        panel.add(JLabel("🏷️ Filter by Category:"))
        panel.add(filterComboBox)
        
        return panel
    }
    
    private fun createActionPanel(): JPanel {
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER, 20, 10))
        buttonPanel.border = EmptyBorder(5, 10, 10, 10)
        
        addToCartBtn.isEnabled = false 
        increaseStockBtn.isEnabled = false
        decreaseStockBtn.isEnabled = false
        
        buttonPanel.add(addBtn)
        buttonPanel.add(editBtn)
        buttonPanel.add(delBtn)
        buttonPanel.add(Box.createHorizontalStrut(30))
        buttonPanel.add(increaseStockBtn)
        buttonPanel.add(decreaseStockBtn)
        buttonPanel.add(Box.createHorizontalStrut(30))
        buttonPanel.add(addToCartBtn)
        buttonPanel.add(viewCartBtn)
        
        return buttonPanel
    }
    
    // --- Data Management & Display ---

    private fun refresh(products: List<Products>? = null) {
        listModel.clear()
        val items = products ?: inventory.getAllProducts()
        items.forEach { listModel.addElement(it) }
    }
    
    private fun updateCartButton() {
        viewCartBtn.text = "🛒 View Cart (${cart.getTotalItems()})"
    }

    /**
     * **FIXED:** This function now correctly applies filters *first*,
     * and *then* sorts the resulting list.
     */
    private fun applySortAndFilter() {
        val sortParam = sortComboBox.selectedItem as String
        val selectedCategoryName = filterComboBox.selectedItem as String
        val query = searchField.text.trim()

        // 1. Start with the full inventory list
        var currentList: List<Products> = inventory.getAllProducts()

        // 2. Apply Search Filter (if any)
        if (query.isNotBlank()) {
            currentList = currentList.filter { 
                it.name.contains(query, ignoreCase = true) || it.desc.contains(query, ignoreCase = true) 
            }
        }

        // 3. Apply Category Filter (if not "Show All")
        if (selectedCategoryName != "Show All") {
            val category = Category.values().first { it.displayName == selectedCategoryName }
            currentList = currentList.filter { it.category == category }
        }
        
        // 4. FIX: Apply Sort *only* to the filtered list
        val sortedList = when (sortParam.lowercase()) {
            "id" -> currentList.sortedBy { it.id }
            "name" -> currentList.sortedBy { it.name }
            "price" -> currentList.sortedBy { it.price }
            "stock" -> currentList.sortedBy { it.stock }
            "category" -> currentList.sortedBy { it.category.displayName }
            else -> currentList // No sorting if parameter is unknown
        }

        // 5. Refresh the JList with the final list
        refresh(sortedList)
    }

    // --- Stock Adjustment ---

    private fun adjustStock(increase: Boolean) {
        val selected = list.selectedValue ?: return
        
        val action = if (increase) "increase" else "decrease"
        val title = if (increase) "➕ Increase Stock" else "➖ Decrease Stock"
        
        val quantityInput = JOptionPane.showInputDialog(
            frame, 
            "Enter quantity to $action stock for ${selected.name} (Current: ${selected.stock}):", 
            title, 
            JOptionPane.QUESTION_MESSAGE
        ) ?: return

        val quantity = quantityInput.toIntOrNull()
        
        if (quantity == null || quantity <= 0) {
            JOptionPane.showMessageDialog(frame, "Invalid quantity entered.", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }

        try {
            val productToUpdate = selected
            if (increase) {
                productToUpdate.increaseStock(quantity)
            } else {
                productToUpdate.decreaseStock(quantity)
            }

            inventory.updateProduct(productToUpdate)
            applySortAndFilter() // Refresh list to show updated stock and maintain filters
            JOptionPane.showMessageDialog(frame, "Stock for ${selected.name} successfully ${action}d by $quantity.", "Success", JOptionPane.INFORMATION_MESSAGE)
        } catch (e: IllegalArgumentException) {
            JOptionPane.showMessageDialog(frame, "Stock Error: ${e.message}", "Error", JOptionPane.ERROR_MESSAGE)
        }
    }

    // --- Dialogs ---

    private fun addProductDialog() {
        val id = inventory.generateNewProductId()
        val name = JOptionPane.showInputDialog(frame, "Name:") ?: return
        val desc = JOptionPane.showInputDialog(frame, "Description:") ?: ""
        val price = JOptionPane.showInputDialog(frame, "Price:").toDoubleOrNull() ?: 0.0
        val stock = JOptionPane.showInputDialog(frame, "Stock:").toIntOrNull() ?: 0
        
        val categories = Category.values().map { it.displayName }.toTypedArray()
        val categoryName = JOptionPane.showInputDialog(frame, "Category:", "Select Category", JOptionPane.PLAIN_MESSAGE, null, categories, categories[0]) as? String ?: return
        val category = Category.values().first { it.displayName == categoryName }

        try {
            val product = Products(id, name, desc, price, stock, category)
            inventory.addProduct(product)
            applySortAndFilter()
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(frame, "Error: ${e.message}")
        }
    }

    // Uses JTextArea in JScrollPane to prevent menu deformation
    private fun editProductUnifiedDialog() {
        val selected = list.selectedValue ?: return

        val nameField = JTextField(selected.name).apply { isEditable = false } // BLOCKED EDITING
        val priceField = JTextField(selected.price.toString())
        val stockField = JTextField(selected.stock.toString()).apply { isEditable = false } 
        
        val descArea = JTextArea(selected.desc, 5, 20).apply {
            lineWrap = true
            wrapStyleWord = true
        }
        val descScroll = JScrollPane(descArea).apply {
             preferredSize = Dimension(250, 80)
        }

        val categories = Category.values().map { it.displayName }.toTypedArray()
        val categoryComboBox = JComboBox(categories).apply {
            selectedItem = selected.category.displayName
        }

        val panel = JPanel(GridBagLayout()).apply {
            val gbc = GridBagConstraints().apply { insets = Insets(4, 4, 4, 4) }

            fun addComponent(label: String, component: Component, y: Int, isTextArea: Boolean = false) {
                gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE
                add(JLabel(label), gbc)
                
                gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.EAST; gbc.fill = if (isTextArea) GridBagConstraints.BOTH else GridBagConstraints.HORIZONTAL
                gbc.gridheight = if (isTextArea) 2 else 1
                add(component, gbc)
                gbc.gridheight = 1 
            }
            
            addComponent("ID:", JLabel(selected.id.toString()), 0)
            addComponent("Name:", nameField, 1)
            addComponent("Price:", priceField, 2)
            addComponent("Category:", categoryComboBox, 3)
            addComponent("Stock:", stockField, 4)
            addComponent("Description:", descScroll, 5, true) 
        }

        val result = JOptionPane.showConfirmDialog(
            frame, panel, "✏️ Edit Product: ${selected.name}", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        )

        if (result == JOptionPane.OK_OPTION) {
            val newDesc = descArea.text.trim()
            val newPrice = priceField.text.toDoubleOrNull()
            val newCategoryName = categoryComboBox.selectedItem as String
            val newCategory = Category.values().first { it.displayName == newCategoryName }

            if (newPrice == null || newPrice < 0) {
                 JOptionPane.showMessageDialog(frame, "Invalid input for Price.", "Error", JOptionPane.ERROR_MESSAGE)
                 return
            }

            try {
                selected.editProduct(selected.name, newDesc, newPrice, newCategory)
                inventory.updateProduct(selected)
                applySortAndFilter()
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(frame, "Error updating product: ${e.message}", "Error", JOptionPane.ERROR_MESSAGE)
            }
        }
    }

    private fun deleteProduct() {
        val selected = list.selectedValue ?: return
        if (JOptionPane.showConfirmDialog(frame, "Delete ${selected.name}?", "Confirm Deletion", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            inventory.removeProductById(selected.id)
            applySortAndFilter()
        }
    }

    private fun cartDialog() {
        val items = cart.getItems()
        val msg = if (items.isEmpty()) "The cart is empty." else buildString {
            append("Items in Cart:\n\n")
            items.forEach { (p, q) -> append("${p.name} (x$q) = $${String.format("%.2f", p.price * q)}\n") }
            append("\nTotal: $${String.format("%.2f", cart.getTotalPrice())}")
        }

        val options = arrayOf("Add Product", "Checkout", "Close")
        val choice = JOptionPane.showOptionDialog(
            frame, msg, "🛒 Shopping Cart",
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[2]
        )

        when (choice) {
            0 -> addProductToCart()
            1 -> checkout()
        }
    }
    
    private fun addProductToCart() {
        val selected = list.selectedValue ?: return
        val quantity = JOptionPane.showInputDialog(frame, "Quantity to add (Stock: ${selected.stock}):").toIntOrNull() ?: 0
        if (quantity > 0) {
            try {
                cart.addItem(selected.id, quantity)
                addToCartBtn.isEnabled = false 
                updateCartButton()
                JOptionPane.showMessageDialog(frame, "$quantity of ${selected.name} added to cart.")
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(frame, "Error adding product: ${e.message}")
            }
        }
    }

    private fun checkout() {
        if (cart.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "The cart is empty. Nothing to checkout.")
            return
        }
        
        if (JOptionPane.showConfirmDialog(frame, "Confirm checkout for $${String.format("%.2f", cart.getTotalPrice())}?", "Checkout Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                cart.checkout()
                JOptionPane.showMessageDialog(frame, "Purchase successful! Stock updated.")
                updateCartButton()
                applySortAndFilter() 
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(frame, "Checkout Error: ${e.message}")
            }
        }
    }

    // --- Helper Classes ---

    // Used to listen to key strokes for immediate search update
    private class SimpleDocumentListener(private val onChange: () -> Unit) : javax.swing.event.DocumentListener {
        override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = onChange()
        override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = onChange()
        override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = onChange()
    }

    // Enhanced Product List Renderer
    private class ProductRenderer : JPanel(), ListCellRenderer<Products> {
        private val infoPanel = JPanel(GridLayout(1, 5, 15, 0)) 
        private val idLabel = JLabel()
        private val nameDescPanel = JPanel() 
        private val nameLabel = JLabel()
        private val descLabel = JLabel()
        private val stockLabel = JLabel()
        private val priceLabel = JLabel()
        private val categoryLabel = JLabel()

        init {
            layout = BorderLayout()
            infoPanel.isOpaque = false 
            
            nameDescPanel.layout = BoxLayout(nameDescPanel, BoxLayout.Y_AXIS) 
            nameDescPanel.isOpaque = false
            nameDescPanel.add(nameLabel)
            nameDescPanel.add(descLabel)

            idLabel.font = Font("Monospaced", Font.BOLD, 12)
            nameLabel.font = Font("SansSerif", Font.BOLD, 14)
            descLabel.font = Font("SansSerif", Font.ITALIC, 11)
            priceLabel.horizontalAlignment = SwingConstants.RIGHT
            categoryLabel.horizontalAlignment = SwingConstants.LEFT
            
            infoPanel.add(idLabel)
            infoPanel.add(nameDescPanel)
            infoPanel.add(priceLabel)
            infoPanel.add(stockLabel)
            infoPanel.add(categoryLabel)
            
            add(infoPanel, BorderLayout.CENTER)
            border = BorderFactory.createEmptyBorder(8, 10, 8, 10) 
        }

        override fun getListCellRendererComponent(list: JList<out Products>?, value: Products?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
            value?.let { p ->
                idLabel.text = "ID: ${p.id}"
                nameLabel.text = p.name
                descLabel.text = "<html><i>${p.desc.take(40)}${if (p.desc.length > 40) "..." else ""}</i></html>"
                priceLabel.text = "$${String.format("%.2f", p.price)}"
                stockLabel.text = "Stock: ${p.stock}"
                categoryLabel.text = p.category.toString()

                stockLabel.foreground = when {
                    p.stock == 0 -> Color(200, 0, 0) 
                    p.stock < 5 -> Color(255, 140, 0) 
                    else -> Color.BLACK
                }
            }

            background = if (isSelected) Color(173, 216, 230) else Color.WHITE
            isOpaque = true
            infoPanel.background = background
            nameDescPanel.background = background

            return this
        }
    }
}