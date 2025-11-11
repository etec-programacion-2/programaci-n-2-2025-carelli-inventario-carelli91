Alumno: Gonzalo Carelli 4to I
Materia: Programacion II


Requisitos Minimos y ejecucion
==============================


Java: JDK 8 o superior (recomendado: JDK 11 o JDK 17)
El código usa Swing que está disponible desde Java 8

Gradle: Gradle 7.0 o superior (recomendado: Gradle 7.6 o 8.x)
O puedes usar la Gradle Wrapper (no necesitas instalar Gradle)

Kotlin: Kotlin 1.5 o superior (recomendado: Kotlin 1.8 o 1.9)



Comandos en orden para la correcta ejecucion desde la consola en el escritorio: 


git clone git@github.com:etec-programacion-2/programaci-n-2-2025-carelli-inventario-carelli91.git

cd programaci-n-2-2025-carelli-inventario-carelli91/

gradle run




Manual de Usuario - Sistema de Gestión de Inventario
====================================================

Tabla de Contenidos
-------------------
- Introducción  
- Interfaz Principal  
- Gestión de Productos  
- Búsqueda y Filtros  
- Control de Stock y Carrito  
- Categorías Disponibles  
- Solución de Problemas  
- Consejos Prácticos  

Introducción
-------------
Sistema de escritorio para gestionar inventario de productos con las siguientes características:

- Gestión completa de productos (crear, editar, eliminar)  
- 15 categorías predefinidas  
- Búsqueda en tiempo real, filtrado y ordenamiento  
- Control de stock y carrito de compras  
- Persistencia automática de datos  

Al iniciar, el sistema crea automáticamente el archivo "products.txt" donde guarda todos los datos.  
Los cambios se guardan de forma automática.

Interfaz Principal
------------------
Panel Superior - Controles  
- Campo de búsqueda: Busca por nombre o descripción  
- Menú de ordenamiento: Ordena por id, name, price, stock o category  
- Filtro de categorías: Muestra productos de una categoría o todos  

Panel Central - Lista de Productos  
Muestra: ID (5 dígitos), Nombre, Descripción (primeros 40 caracteres), Precio, Stock y Categoría.  

Código de colores del stock:  
- Rojo: Sin stock (0 unidades)  
- Naranja: Stock bajo (menos de 5)  
- Negro: Stock normal (5 o más)  

Panel Inferior - Botones de Acción  
Gestión: Add Product, Edit Product, Delete Product  
Stock: Increase Stock, Decrease Stock  
Compras: Add to Cart, View Cart  

Los botones de stock y carrito se habilitan al seleccionar un producto.

Gestión de Productos
--------------------
Agregar Producto  
1. Clic en "Add Product"  
2. Ingresar los datos en orden:  
   - Nombre: Obligatorio, no puede repetirse  
   - Descripción: Opcional  
   - Precio: Número positivo (valores inválidos = 0.0)  
   - Stock: Entero positivo (valores inválidos = 0)  
   - Categoría: Seleccionar del menú  
3. El sistema asigna automáticamente un ID único (10000-99999).  

Editar Producto  
1. Seleccionar producto de la lista  
2. Clic en "Edit Product"  
3. Modificar campos editables:  
   - Precio: Valor numérico positivo  
   - Categoría: Nueva opción  
   - Descripción: Texto editable con scroll  
4. Campos no editables:  
   - ID (permanente)  
   - Nombre (protegido)  
   - Stock (usar botones específicos)  

Eliminar Producto  
1. Seleccionar producto  
2. Clic en "Delete Product"  
3. Confirmar eliminación  

Advertencia: Acción permanente, no se puede deshacer.  

Búsqueda y Filtros
------------------
Búsqueda en Tiempo Real  
- Escribe en el campo de búsqueda para resultados instantáneos.  
- Busca en nombres y descripciones sin distinguir mayúsculas/minúsculas.  

Ordenamiento  
- Selecciona criterio: id, name, price, stock o category.  
- El orden es siempre ascendente (menor a mayor / A-Z).  

Filtrado por Categorías  
- Selecciona una categoría específica o "Show All" para ver todos los productos.  
- Puedes combinar búsqueda, filtro y ordenamiento simultáneamente.  
- El ordenamiento se aplica sobre los resultados filtrados.  

Control de Stock y Carrito
--------------------------
Aumentar Stock  
1. Seleccionar producto  
2. Clic en "Increase Stock"  
3. Ingresar cantidad positiva  
4. Confirmar  

Disminuir Stock  
1. Seleccionar producto  
2. Clic en "Decrease Stock"  
3. Ingresar cantidad (no puede exceder stock disponible)  
4. Confirmar  

Validación: Solo acepta números enteros positivos. Disminuir stock no puede exceder la cantidad disponible.  

Carrito de Compras  
Agregar al Carrito  
1. Seleccionar producto  
2. Clic en "Add to Cart"  
3. Ingresar cantidad (no puede exceder stock)  
4. Confirmar  

El contador del carrito se actualiza automáticamente.  

Ver y Gestionar Carrito  
1. Clic en "View Cart"  
2. Revisar productos, cantidades y total  
3. Opciones disponibles:  
   - Add Product: Agregar más ítems  
   - Checkout: Finalizar compra  
   - Close: Cerrar sin cambios  

Proceso de Checkout  
1. En ventana del carrito, clic en "Checkout"  
2. Confirmar el total  
3. El sistema:  
   - Verifica stock suficiente de todos los productos  
   - Reduce stock automáticamente  
   - Guarda cambios permanentemente  
   - Vacía el carrito  

Validación: Si cualquier producto no tiene stock suficiente, toda la compra se cancela (todo o nada).  

Categorías Disponibles
----------------------
- Fresh Food - Alimentos frescos  
- Non-Perishable Food - Alimentos no perecederos  
- Dairy & Derivatives - Lácteos  
- Beverages - Bebidas  
- Cleaning & Household - Limpieza del hogar  
- Personal Care - Cuidado personal  
- Pet Care - Cuidado de mascotas  
- Baby Care - Productos para bebés  
- Home Essentials - Esenciales del hogar  
- Beauty & Cosmetics - Belleza y cosméticos  
- Stationery & School Supplies - Papelería  
- Tools - Herramientas  
- Clothing & Accessories - Ropa  
- Pharmacy & Health - Farmacia y salud  
- Others - Otros  

Solución de Problemas
---------------------
Producto no aparece en lista  
Solución: Limpiar búsqueda, seleccionar "Show All" en filtro, ordenar por "id".  

No puedo editar nombre  
Solución: El nombre está bloqueado para evitar duplicados. Eliminar producto y crear uno nuevo si necesita cambiar el nombre.  

Error "Product name already exists"  
Solución: Usar nombre diferente o agregar calificadores (por ejemplo: "Leche Deslactosada 1L").  

No puedo agregar al carrito  
Solución: Verificar que el producto tiene stock y está seleccionado en la lista.  

Checkout falla  
Solución: Cerrar carrito, verificar stock actual, ajustar cantidades e intentar nuevamente.  

Datos no se guardan  
Solución: Verificar permisos de escritura. Hacer respaldo de products.txt, eliminarlo y reiniciar la aplicación.  

Consejos Prácticos
------------------
Gestión eficiente  
- Usar nombres descriptivos y únicos  
- Categorizar correctamente desde el inicio  
- Revisar periódicamente productos con stock bajo (naranja)  

Uso del carrito  
- Verificar stock antes de agregar cantidades grandes  
- El checkout es irreversible  
- El carrito no se guarda al cerrar la aplicación  

Respaldo  
- Realizar copias periódicas de products.txt  
- No editar manualmente el archivo  

Información técnica  
- Archivo: products.txt (formato CSV)  
- Capacidad: 90,000 productos (IDs 10000-99999)  
- Guardado automático en cada operación  

Fin del Manual
---------------
