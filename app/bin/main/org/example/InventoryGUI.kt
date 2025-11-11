package org.example

import java.awt.*
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.ListSelectionListener

fun main() {
    InventoryGUI(Inventory()).show()
}

// Interfaz gráfica del sistema de inventario
class InventoryGUI(private val inventory: InventoryRepository) {
    private val cart = ShoppingCart(inventory)
    private val frame = JFrame("Product Inventory 📦 Management System")
    private val listModel = DefaultListModel<Products>()
    private val list = JList(listModel)
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

    fun show() {
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.layout = BorderLayout(10, 10)
        frame.size = Dimension(1200, 700) 
        frame.add(createControlPanel(), BorderLayout.NORTH)
        list.cellRenderer = createProductRenderer()
        frame.add(JScrollPane(list), BorderLayout.CENTER)
        frame.add(createActionPanel(), BorderLayout.SOUTH)
        setupListeners()
        refresh()
        updateCartButton()
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
    
    // Configura todos los listeners de eventos
    private fun setupListeners() {
        addBtn.addActionListener { addProductDialog() }
        editBtn.addActionListener { editProductUnifiedDialog() } 
        delBtn.addActionListener { deleteProduct() }
        viewCartBtn.addActionListener { cartDialog() }
        addToCartBtn.addActionListener { addProductToCart() }
        increaseStockBtn.addActionListener { adjustStock(true) }
        decreaseStockBtn.addActionListener { adjustStock(false) }
        
        val docListener = object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = applySortAndFilter()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = applySortAndFilter()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = applySortAndFilter()
        }
        searchField.document.addDocumentListener(docListener)
        sortComboBox.addActionListener { applySortAndFilter() }
        filterComboBox.addActionListener { applySortAndFilter() }
        list.addListSelectionListener(ListSelectionListener { e ->
            when { !e.valueIsAdjusting -> {
                val selected = list.selectedValue != null
                addToCartBtn.isEnabled = selected
                increaseStockBtn.isEnabled = selected
                decreaseStockBtn.isEnabled = selected
            }}
        })
    }
    
    // Crea el panel de controles
    private fun createControlPanel(): JPanel = JPanel(FlowLayout(FlowLayout.CENTER, 15, 10)).apply {
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Display Controls"),
            EmptyBorder(5, 10, 5, 10)
        )
        add(JLabel("🔍 Search:"))
        add(searchField)
        add(Box.createHorizontalStrut(15))
        add(JLabel("↕️ Sort By:"))
        add(sortComboBox)
        add(Box.createHorizontalStrut(15))
        add(JLabel("🏷️ Filter by Category:"))
        add(filterComboBox)
    }
    
    // Crea el panel de botones de acción
    private fun createActionPanel(): JPanel = JPanel(FlowLayout(FlowLayout.CENTER, 20, 10)).apply {
        border = EmptyBorder(5, 10, 10, 10)
        addToCartBtn.isEnabled = false 
        increaseStockBtn.isEnabled = false
        decreaseStockBtn.isEnabled = false
        add(addBtn)
        add(editBtn)
        add(delBtn)
        add(Box.createHorizontalStrut(30))
        add(increaseStockBtn)
        add(decreaseStockBtn)
        add(Box.createHorizontalStrut(30))
        add(addToCartBtn)
        add(viewCartBtn)
    }
    
    // Crea el renderer personalizado para la lista
    private fun createProductRenderer(): ListCellRenderer<Products> = ListCellRenderer { _, value, _, isSelected, _ ->
        JPanel(BorderLayout()).apply {
            val infoPanel = JPanel(GridLayout(1, 5, 15, 0)).apply { isOpaque = false }
            val idLabel = JLabel().apply { font = Font("Monospaced", Font.BOLD, 12) }
            val nameDescPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                isOpaque = false
            }
            val nameLabel = JLabel().apply { font = Font("SansSerif", Font.BOLD, 14) }
            val descLabel = JLabel().apply { font = Font("SansSerif", Font.ITALIC, 11) }
            val stockLabel = JLabel()
            val priceLabel = JLabel().apply { horizontalAlignment = SwingConstants.RIGHT }
            val categoryLabel = JLabel().apply { horizontalAlignment = SwingConstants.LEFT }
            
            nameDescPanel.add(nameLabel)
            nameDescPanel.add(descLabel)
            infoPanel.add(idLabel)
            infoPanel.add(nameDescPanel)
            infoPanel.add(priceLabel)
            infoPanel.add(stockLabel)
            infoPanel.add(categoryLabel)
            add(infoPanel, BorderLayout.CENTER)
            border = BorderFactory.createEmptyBorder(8, 10, 8, 10)
            
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
            
            background = when { isSelected -> Color(173, 216, 230) else -> Color.WHITE }
            isOpaque = true
            infoPanel.background = background
            nameDescPanel.background = background
        }
    }
    
    // Actualiza la lista de productos
    private fun refresh(products: List<Products>? = null) {
        listModel.clear()
        (products ?: inventory.getAllProducts()).forEach { listModel.addElement(it) }
    }
    
    // Actualiza el botón del carrito
    private fun updateCartButton() {
        viewCartBtn.text = "🛒 View Cart (${cart.getTotalItems()})"
    }

    // Aplica filtros y ordenamiento
    private fun applySortAndFilter() {
        val sortParam = sortComboBox.selectedItem as String
        val categoryName = filterComboBox.selectedItem as String
        val query = searchField.text.trim()
        var list = inventory.getAllProducts()

        when { query.isNotBlank() -> list = list.filter { 
            it.name.contains(query, ignoreCase = true) || it.desc.contains(query, ignoreCase = true) 
        }}

        when (categoryName) {
            "Show All" -> {}
            else -> list = list.filter { it.category == Category.values().first { c -> c.displayName == categoryName } }
        }
        
        refresh(when (sortParam.lowercase()) {
            "id" -> list.sortedBy { it.id }
            "name" -> list.sortedBy { it.name }
            "price" -> list.sortedBy { it.price }
            "stock" -> list.sortedBy { it.stock }
            "category" -> list.sortedBy { it.category.displayName }
            else -> list
        })
    }

    // Ajusta el stock de un producto
    private fun adjustStock(increase: Boolean) {
        val selected = list.selectedValue ?: return
        val action = when { increase -> "increase" else -> "decrease" }
        val title = when { increase -> "➕ Increase Stock" else -> "➖ Decrease Stock" }
        val input = JOptionPane.showInputDialog(frame, "Enter quantity to $action stock for ${selected.name} (Current: ${selected.stock}):", title, JOptionPane.QUESTION_MESSAGE) ?: return
        val qty = input.toIntOrNull()
        
        when {
            qty == null || qty <= 0 -> {
                JOptionPane.showMessageDialog(frame, "Invalid quantity entered.", "Error", JOptionPane.ERROR_MESSAGE)
                return
            }
        }

        try {
            when { increase -> selected.increaseStock(qty) else -> selected.decreaseStock(qty) }
            inventory.updateProduct(selected)
            applySortAndFilter()
            JOptionPane.showMessageDialog(frame, "Stock for ${selected.name} successfully ${action}d by $qty.", "Success", JOptionPane.INFORMATION_MESSAGE)
        } catch (e: IllegalArgumentException) {
            JOptionPane.showMessageDialog(frame, "Stock Error: ${e.message}", "Error", JOptionPane.ERROR_MESSAGE)
        }
    }

    // Muestra el diálogo para agregar producto
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
            inventory.addProduct(Products(id, name, desc, price, stock, category))
            applySortAndFilter()
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(frame, "Error: ${e.message}")
        }
    }

    // Muestra el diálogo para editar producto
    private fun editProductUnifiedDialog() {
        val selected = list.selectedValue ?: return
        val nameField = JTextField(selected.name).apply { isEditable = false }
        val priceField = JTextField(selected.price.toString())
        val stockField = JTextField(selected.stock.toString()).apply { isEditable = false } 
        val descArea = JTextArea(selected.desc, 5, 20).apply { lineWrap = true; wrapStyleWord = true }
        val descScroll = JScrollPane(descArea).apply { preferredSize = Dimension(250, 80) }
        val categories = Category.values().map { it.displayName }.toTypedArray()
        val categoryComboBox = JComboBox(categories).apply { selectedItem = selected.category.displayName }
        
        val panel = JPanel(GridBagLayout()).apply {
            val gbc = GridBagConstraints().apply { insets = Insets(4, 4, 4, 4) }
            fun add(label: String, comp: Component, y: Int, area: Boolean = false) {
                gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE
                add(JLabel(label), gbc)
                gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.EAST
                gbc.fill = when { area -> GridBagConstraints.BOTH else -> GridBagConstraints.HORIZONTAL }
                gbc.gridheight = when { area -> 2 else -> 1 }
                add(comp, gbc)
                gbc.gridheight = 1 
            }
            add("ID:", JLabel(selected.id.toString()), 0)
            add("Name:", nameField, 1)
            add("Price:", priceField, 2)
            add("Category:", categoryComboBox, 3)
            add("Stock:", stockField, 4)
            add("Description:", descScroll, 5, true) 
        }

        when (JOptionPane.showConfirmDialog(frame, panel, "✏️ Edit Product: ${selected.name}", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
            JOptionPane.OK_OPTION -> {
                val newDesc = descArea.text.trim()
                val newPrice = priceField.text.toDoubleOrNull()
                val newCategory = Category.values().first { it.displayName == categoryComboBox.selectedItem as String }
                when {
                    newPrice == null || newPrice < 0 -> {
                        JOptionPane.showMessageDialog(frame, "Invalid input for Price.", "Error", JOptionPane.ERROR_MESSAGE)
                        return
                    }
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
    }

    // Elimina el producto seleccionado
    private fun deleteProduct() {
        val selected = list.selectedValue ?: return
        when (JOptionPane.showConfirmDialog(frame, "Delete ${selected.name}?", "Confirm Deletion", JOptionPane.YES_NO_OPTION)) {
            JOptionPane.YES_OPTION -> {
                inventory.removeProductById(selected.id)
                applySortAndFilter()
            }
        }
    }

    // Muestra el diálogo del carrito
    private fun cartDialog() {
        val items = cart.getItems()
        val msg = when {
            items.isEmpty() -> "The cart is empty."
            else -> buildString {
                append("Items in Cart:\n\n")
                items.forEach { (p, q) -> append("${p.name} (x$q) = $${String.format("%.2f", p.price * q)}\n") }
                append("\nTotal: $${String.format("%.2f", cart.getTotalPrice())}")
            }
        }

        when (JOptionPane.showOptionDialog(frame, msg, "🛒 Shopping Cart", JOptionPane.YES_NO_CANCEL_OPTION, 
            JOptionPane.PLAIN_MESSAGE, null, arrayOf("Add Product", "Checkout", "Close"), "Close")) {
            0 -> addProductToCart()
            1 -> checkout()
        }
    }
    
    // Agrega producto al carrito
    private fun addProductToCart() {
        val selected = list.selectedValue ?: return
        val qty = JOptionPane.showInputDialog(frame, "Quantity to add (Stock: ${selected.stock}):").toIntOrNull() ?: 0
        when {
            qty > 0 -> {
                try {
                    cart.addItem(selected.id, qty)
                    addToCartBtn.isEnabled = false 
                    updateCartButton()
                    JOptionPane.showMessageDialog(frame, "$qty of ${selected.name} added to cart.")
                } catch (e: Exception) {
                    JOptionPane.showMessageDialog(frame, "Error adding product: ${e.message}")
                }
            }
        }
    }

    // Realiza el checkout
    private fun checkout() {
        when {
            cart.getItems().isEmpty() -> {
                JOptionPane.showMessageDialog(frame, "The cart is empty. Nothing to checkout.")
                return
            }
        }
        when (JOptionPane.showConfirmDialog(frame, "Confirm checkout for $${String.format("%.2f", cart.getTotalPrice())}?", 
            "Checkout Confirmation", JOptionPane.YES_NO_OPTION)) {
            JOptionPane.YES_OPTION -> {
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
    }
}