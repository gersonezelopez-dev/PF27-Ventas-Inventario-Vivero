
const API = "/api";

let categorias = [];
let productos = [];
let carrito = [];
let guardandoVenta = false;

// Utilidades generales
const elemento = id => document.getElementById(id);

function dinero(valor) {
    return `Q${Number(valor).toFixed(2)}`;
}

function mostrarMensaje(id, mensaje, error = false) {
    const destino = elemento(id);
    if (!destino) return;

    destino.textContent = mensaje;
    destino.style.color = error ? "#b42318" : "#176b40";
}

async function solicitar(url, opciones = {}) {
    const respuesta = await fetch(url, opciones);
    const datos = await respuesta.json().catch(() => null);

    if (!respuesta.ok) {
        const errores = datos?.errores
            ? " - " + Object.values(datos.errores).join(", ")
            : "";

        throw new Error(
            (datos?.mensaje || `Error HTTP ${respuesta.status}`) +
            errores
        );
    }

    return datos;
}

function opcionesJSON(metodo, datos) {
    return {
        method: metodo,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(datos)
    };
}

function crearCelda(fila, valor) {
    const celda = document.createElement("td");
    celda.textContent = String(valor ?? "");
    fila.appendChild(celda);
    return celda;
}

function crearBoton(texto, accion) {
    const boton = document.createElement("button");
    boton.type = "button";
    boton.textContent = texto;
    boton.addEventListener("click", accion);
    return boton;
}

function llenarSelect(id, datos, texto, soloActivos = false) {
    const select = elemento(id);
    const anterior = select.value;

    select.replaceChildren();

    const inicial = document.createElement("option");
    inicial.value = "";
    inicial.textContent = "Seleccione una opción";
    select.appendChild(inicial);

    datos
        .filter(item => !soloActivos || item.activo === true)
        .forEach(item => {
            const opcion = document.createElement("option");
            opcion.value = item.id;
            opcion.textContent = texto(item);
            select.appendChild(opcion);
        });

    if ([...select.options].some(op => op.value === anterior)) {
        select.value = anterior;
    }
}

// ==================================================
// CATEGORÍAS
// ==================================================

async function cargarCategorias() {
    try {
        categorias = await solicitar(`${API}/categorias`);

        const tabla = elemento("tablaCategorias");
        tabla.replaceChildren();

        categorias.forEach(categoria => {
            const fila = document.createElement("tr");

            crearCelda(fila, categoria.id);
            crearCelda(fila, categoria.nombre);

            const acciones = document.createElement("td");

            acciones.appendChild(
                crearBoton("Editar", () => editarCategoria(categoria))
            );

            acciones.appendChild(
                crearBoton("Eliminar", () => eliminarCategoria(categoria.id))
            );

            fila.appendChild(acciones);
            tabla.appendChild(fila);
        });

        llenarSelect(
            "productoCategoriaId",
            categorias,
            categoria => categoria.nombre
        );

        return categorias;

    } catch (error) {
        mostrarMensaje("resultadoCategoria", error.message, true);
        return [];
    }
}

function editarCategoria(categoria) {
    elemento("categoriaEditarId").value = categoria.id;
    elemento("categoriaNombre").value = categoria.nombre;
    elemento("btnGuardarCategoria").textContent = "Actualizar categoría";
    mostrarMensaje(
        "resultadoCategoria",
        `Editando categoría #${categoria.id}`
    );
}

function cancelarCategoria() {
    elemento("formCategoria").reset();
    elemento("categoriaEditarId").value = "";
    elemento("btnGuardarCategoria").textContent = "Guardar categoría";
}

async function guardarCategoria(evento) {
    evento.preventDefault();

    const id = elemento("categoriaEditarId").value;
    const nombre = elemento("categoriaNombre").value.trim();

    if (!nombre) {
        mostrarMensaje(
            "resultadoCategoria",
            "Ingrese el nombre de la categoría.",
            true
        );
        return;
    }

    try {
        const url = id
            ? `${API}/categorias/${id}`
            : `${API}/categorias`;

        await solicitar(
            url,
            opcionesJSON(id ? "PUT" : "POST", { nombre })
        );

        cancelarCategoria();
        await cargarCategorias();

        mostrarMensaje(
            "resultadoCategoria",
            "Categoría guardada correctamente."
        );

    } catch (error) {
        mostrarMensaje("resultadoCategoria", error.message, true);
    }
}

async function eliminarCategoria(id) {
    if (!confirm(`¿Desea eliminar la categoría #${id}?`)) return;

    try {
        await solicitar(`${API}/categorias/${id}`, {
            method: "DELETE"
        });

        await cargarCategorias();
        mostrarMensaje(
            "resultadoCategoria",
            "Categoría eliminada correctamente."
        );

    } catch (error) {
        mostrarMensaje(
            "resultadoCategoria",
            error.message,
            true
        );
    }
}

// ==================================================
// PRODUCTOS
// ==================================================

async function cargarProductos() {
    try {
        mostrarMensaje("mensaje", "Consultando inventario...");

        productos = await solicitar(`${API}/productos`);

        const tabla = elemento("tablaProductos");
        tabla.replaceChildren();

        productos.forEach(producto => {
            const fila = document.createElement("tr");

            crearCelda(fila, producto.id);
            crearCelda(fila, producto.nombre);
            crearCelda(fila, dinero(producto.precio));
            crearCelda(fila, producto.stock);
            crearCelda(
                fila,
                producto.activo ? "Activo" : "Inactivo"
            );

            const acciones = document.createElement("td");

            acciones.appendChild(
                crearBoton("Editar", () => editarProducto(producto))
            );

            if (producto.activo) {
                acciones.appendChild(
                    crearBoton(
                        "Desactivar",
                        () => desactivarProducto(producto.id)
                    )
                );
            }

            fila.appendChild(acciones);
            tabla.appendChild(fila);
        });

        llenarSelect(
            "productoId",
            productos,
            p => `${p.nombre} - ${dinero(p.precio)} (stock: ${p.stock})`,
            true
        );

        llenarSelect(
            "movimientoProductoId",
            productos,
            p => `${p.nombre} (stock: ${p.stock})`,
            true
        );

        llenarSelect(
            "historialProductoId",
            productos,
            p => p.nombre
        );

        mostrarMensaje(
            "mensaje",
            `Se encontraron ${productos.length} productos.`
        );

        dibujarCarrito();

    } catch (error) {
        mostrarMensaje("mensaje", error.message, true);
    }
}

function editarProducto(producto) {
    elemento("productoEditarId").value = producto.id;
    elemento("productoCodigo").value = producto.codigo;
    elemento("productoNombre").value = producto.nombre;
    elemento("productoPrecio").value = producto.precio;
    elemento("productoStock").value = producto.stock;
    elemento("productoCategoriaId").value = producto.categoriaId;

    // El stock no puede editarse desde PUT /api/productos
    elemento("productoStock").readOnly = true;

    elemento("btnGuardarProducto").textContent = "Actualizar producto";

    mostrarMensaje(
        "resultadoProducto",
        "Para cambiar existencias utilice movimientos de inventario."
    );
}

function cancelarProducto() {
    elemento("formProducto").reset();
    elemento("productoEditarId").value = "";
    elemento("productoStock").readOnly = false;
    elemento("btnGuardarProducto").textContent = "Guardar producto";
}

async function guardarProducto(evento) {
    evento.preventDefault();

    const id = elemento("productoEditarId").value;

    const codigo = elemento("productoCodigo").value.trim();
    const nombre = elemento("productoNombre").value.trim();
    const precio = Number(elemento("productoPrecio").value);
    const categoriaId = Number(elemento("productoCategoriaId").value);

    let stock;

    if (id) {
        // Consultar stock actualizado para no modificarlo por PUT
        try {
            const actual = await solicitar(`${API}/productos/${id}`);
            stock = actual.stock;
        } catch (error) {
            mostrarMensaje("resultadoProducto", error.message, true);
            return;
        }
    } else {
        stock = Number(elemento("productoStock").value);
    }

    if (!codigo || !nombre || !Number.isFinite(precio) ||
        precio <= 0 || !Number.isInteger(stock) || stock < 0 ||
        !Number.isInteger(categoriaId) || categoriaId <= 0) {

        mostrarMensaje(
            "resultadoProducto",
            "Revise el código, nombre, precio, stock y categoría.",
            true
        );
        return;
    }

    try {
        const datos = {
            codigo,
            nombre,
            precio,
            stock,
            categoriaId
        };

        await solicitar(
            id ? `${API}/productos/${id}` : `${API}/productos`,
            opcionesJSON(id ? "PUT" : "POST", datos)
        );

        cancelarProducto();
        await cargarProductos();

        mostrarMensaje(
            "resultadoProducto",
            "Producto guardado correctamente."
        );

    } catch (error) {
        mostrarMensaje("resultadoProducto", error.message, true);
    }
}

async function desactivarProducto(id) {
    if (!confirm(`¿Desea desactivar el producto #${id}?`)) return;

    try {
        await solicitar(`${API}/productos/${id}/desactivar`, {
            method: "PATCH"
        });

        await cargarProductos();

        mostrarMensaje(
            "resultadoProducto",
            `Producto #${id} desactivado correctamente.`
        );

    } catch (error) {
        mostrarMensaje("resultadoProducto", error.message, true);
    }
}

// ==================================================
// MOVIMIENTOS DE INVENTARIO
// ==================================================

async function registrarMovimiento(evento) {
    evento.preventDefault();

    const productoId = Number(elemento("movimientoProductoId").value);
    const tipo = elemento("movimientoTipo").value;
    const cantidad = Number(elemento("movimientoCantidad").value);
    const motivo = elemento("movimientoMotivo").value.trim();

    if (!Number.isInteger(productoId) || productoId <= 0 ||
        !Number.isInteger(cantidad) || cantidad <= 0 ||
        !motivo || !["ENTRADA", "SALIDA"].includes(tipo)) {

        mostrarMensaje(
            "resultadoMovimiento",
            "Complete correctamente todos los campos.",
            true
        );
        return;
    }

    try {
        const resultado = await solicitar(
            `${API}/movimientos`,
            opcionesJSON("POST", {
                productoId,
                tipo,
                cantidad,
                motivo
            })
        );

        mostrarMensaje(
            "resultadoMovimiento",
            `Movimiento #${resultado.id} registrado. ` +
            `Stock: ${resultado.stockAnterior} → ${resultado.stockNuevo}`
        );

        elemento("formMovimiento").reset();

        await cargarProductos();
        elemento("historialProductoId").value = productoId;
        await consultarHistorial();

    } catch (error) {
        mostrarMensaje(
            "resultadoMovimiento",
            error.message,
            true
        );
    }
}

async function consultarHistorial() {
    const productoId = Number(elemento("historialProductoId").value);
    const contenedor = elemento("listaMovimientos");

    contenedor.replaceChildren();

    if (!productoId) {
        contenedor.textContent = "Seleccione un producto.";
        return;
    }

    try {
        const movimientos = await solicitar(
            `${API}/movimientos/producto/${productoId}`
        );

        if (movimientos.length === 0) {
            contenedor.textContent = "No hay movimientos registrados.";
            return;
        }

        movimientos.forEach(movimiento => {
            const tarjeta = document.createElement("div");

            tarjeta.textContent =
                `${movimiento.tipo} | ` +
                `Cantidad: ${movimiento.cantidad} | ` +
                `Stock: ${movimiento.stockAnterior} → ` +
                `${movimiento.stockNuevo} | ` +
                `Motivo: ${movimiento.motivo}`;

            contenedor.appendChild(tarjeta);
        });

    } catch (error) {
        contenedor.textContent = error.message;
    }
}

// ==================================================
// VENTAS CON VARIOS ARTÍCULOS
// ==================================================

function agregarArticulo(evento) {
    evento.preventDefault();

    const productoId = Number(elemento("productoId").value);
    const cantidad = Number(elemento("cantidad").value);

    const producto = productos.find(p => p.id === productoId);

    if (!producto || !producto.activo) {
        mostrarMensaje(
            "resultadoVenta",
            "Seleccione un producto activo.",
            true
        );
        return;
    }

    if (!Number.isInteger(cantidad) || cantidad <= 0) {
        mostrarMensaje(
            "resultadoVenta",
            "La cantidad debe ser mayor que cero.",
            true
        );
        return;
    }

    const existente = carrito.find(p => p.productoId === productoId);
    const acumulada = (existente?.cantidad || 0) + cantidad;

    if (acumulada > producto.stock) {
        mostrarMensaje(
            "resultadoVenta",
            "La cantidad supera las existencias disponibles.",
            true
        );
        return;
    }

    if (existente) {
        existente.cantidad = acumulada;
    } else {
        carrito.push({ productoId, cantidad });
    }

    elemento("formAgregarArticulo").reset();
    mostrarMensaje("resultadoVenta", "Artículo agregado a la venta.");
    dibujarCarrito();
}

function eliminarArticulo(productoId) {
    carrito = carrito.filter(p => p.productoId !== productoId);
    dibujarCarrito();
}

function dibujarCarrito() {
    const tabla = elemento("tablaCarrito");
    tabla.replaceChildren();

    let total = 0;

    carrito.forEach(item => {
        const producto = productos.find(p => p.id === item.productoId);
        const precio = Number(producto?.precio || 0);
        const subtotal = precio * item.cantidad;

        total += subtotal;

        const fila = document.createElement("tr");

        crearCelda(fila, producto?.nombre || `Producto #${item.productoId}`);
        crearCelda(fila, item.cantidad);
        crearCelda(fila, dinero(subtotal));

        const acciones = document.createElement("td");
        acciones.appendChild(
            crearBoton(
                "Quitar",
                () => eliminarArticulo(item.productoId)
            )
        );

        fila.appendChild(acciones);
        tabla.appendChild(fila);
    });

    elemento("totalCarrito").textContent =
        `Total estimado: ${dinero(total)}`;
}

async function registrarVenta() {
    if (guardandoVenta) return;

    if (carrito.length === 0) {
        mostrarMensaje(
            "resultadoVenta",
            "Agregue al menos un artículo.",
            true
        );
        return;
    }

    if (!confirm("¿Desea confirmar esta venta?")) return;

    guardandoVenta = true;
    elemento("btnRegistrarVenta").disabled = true;

    try {
        const venta = await solicitar(
            `${API}/ventas`,
            opcionesJSON("POST", {
                detalles: carrito.map(item => ({
                    productoId: item.productoId,
                    cantidad: item.cantidad
                }))
            })
        );

        carrito = [];
        dibujarCarrito();

        mostrarMensaje(
            "resultadoVenta",
            `Venta #${venta.id} registrada. Total: ${dinero(venta.total)}`
        );

        await cargarProductos();
        await cargarVentas();
        await consultarTotal();

    } catch (error) {
        mostrarMensaje("resultadoVenta", error.message, true);
    } finally {
        guardandoVenta = false;
        elemento("btnRegistrarVenta").disabled = false;
    }
}

function limpiarCarrito() {
    carrito = [];
    dibujarCarrito();
    mostrarMensaje("resultadoVenta", "Artículos eliminados.");
}

// ==================================================
// CONSULTAR Y ANULAR VENTAS
// ==================================================

async function cargarVentas() {
    const contenedor = elemento("listaVentas");

    try {
        const ventas = await solicitar(`${API}/ventas`);
        contenedor.replaceChildren();

        if (ventas.length === 0) {
            contenedor.textContent = "Todavía no hay ventas.";
            return;
        }

        ventas.forEach(venta => {
            const tarjeta = document.createElement("div");

            const encabezado = document.createElement("p");
            encabezado.textContent =
                `Venta #${venta.id} | ` +
                `Total: ${dinero(venta.total)} | ` +
                `Estado: ${venta.estado}`;

            tarjeta.appendChild(encabezado);

            if (Array.isArray(venta.detalles)) {
                const lista = document.createElement("ul");

                venta.detalles.forEach(detalle => {
                    const linea = document.createElement("li");
                    linea.textContent =
                        `${detalle.productoNombre} | ` +
                        `${detalle.cantidad} unidad(es) | ` +
                        `Subtotal: ${dinero(detalle.subtotal)}`;

                    lista.appendChild(linea);
                });

                tarjeta.appendChild(lista);
            }

            if (venta.estado === "REGISTRADA") {
                tarjeta.appendChild(
                    crearBoton(
                        "Anular venta",
                        () => anularVenta(venta.id)
                    )
                );
            }

            contenedor.appendChild(tarjeta);
        });

    } catch (error) {
        contenedor.textContent = error.message;
    }
}

async function anularVenta(id) {
    if (!confirm(`¿Desea anular la venta #${id}?`)) return;

    try {
        await solicitar(`${API}/ventas/${id}/anular`, {
            method: "PATCH"
        });

        mostrarMensaje("mensaje", `Venta #${id} anulada.`);

        await cargarProductos();
        await cargarVentas();
        await consultarTotal();

    } catch (error) {
        mostrarMensaje("mensaje", error.message, true);
    }
}

// ==================================================
// TOTAL VENDIDO POR FECHA
// ==================================================

async function consultarTotal() {
    const fecha = elemento("fechaConsulta").value;

    if (!fecha) {
        elemento("totalDia").textContent = "Seleccione una fecha.";
        return;
    }

    try {
        const resultado = await solicitar(
            `${API}/ventas/total-dia?fecha=${encodeURIComponent(fecha)}`
        );

        elemento("totalDia").textContent =
            `Total: ${dinero(resultado.totalVendido)}`;

    } catch (error) {
        elemento("totalDia").textContent =
            `Error: ${error.message}`;
    }
}

// ==================================================
// INICIAR LA INTERFAZ
// ==================================================

document.addEventListener("DOMContentLoaded", async () => {

    const hoy = new Date();

    const anio = hoy.getFullYear();
    const mes = String(hoy.getMonth() + 1).padStart(2, "0");
    const dia = String(hoy.getDate()).padStart(2, "0");

    elemento("fechaConsulta").value = `${anio}-${mes}-${dia}`;

    elemento("formCategoria")
        .addEventListener("submit", guardarCategoria);

    elemento("btnCategorias")
        .addEventListener("click", cargarCategorias);

    elemento("btnCancelarCategoria")
        .addEventListener("click", cancelarCategoria);

    elemento("formProducto")
        .addEventListener("submit", guardarProducto);

    elemento("btnCancelarProducto")
        .addEventListener("click", cancelarProducto);

    elemento("btnProductos")
        .addEventListener("click", cargarProductos);

    elemento("formMovimiento")
        .addEventListener("submit", registrarMovimiento);

    elemento("btnHistorial")
        .addEventListener("click", consultarHistorial);

    elemento("formAgregarArticulo")
        .addEventListener("submit", agregarArticulo);

    elemento("btnRegistrarVenta")
        .addEventListener("click", registrarVenta);

    elemento("btnLimpiarCarrito")
        .addEventListener("click", limpiarCarrito);

    elemento("btnVentas")
        .addEventListener("click", cargarVentas);

    elemento("btnTotal")
        .addEventListener("click", consultarTotal);

    await cargarCategorias();
    await cargarProductos();
    await cargarVentas();
    await consultarTotal();
});
