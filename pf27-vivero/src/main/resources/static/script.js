const API = "/api";

// Mostrar mensajes en la pantalla
function mostrarMensaje(elementoId, mensaje, esError = false) {
    const elemento = document.getElementById(elementoId);

    if (elemento) {
        elemento.textContent = mensaje;
        elemento.style.color = esError ? "#b42318" : "#176b40";
    }
}

// Obtener mensajes de las respuestas HTTP
async function solicitar(url, opciones = {}) {
    const respuesta = await fetch(url, opciones);

    const datos = await respuesta.json().catch(() => null);

    if (!respuesta.ok) {
        throw new Error(
            datos?.mensaje || `Error HTTP ${respuesta.status}`
        );
    }

    return datos;
}

// Consultar productos
async function cargarProductos() {
    const tabla = document.getElementById("tablaProductos");

    try {
        mostrarMensaje("mensaje", "Consultando inventario...");

        const productos = await solicitar(`${API}/productos`);

        tabla.replaceChildren();

        productos.forEach(producto => {
            const fila = document.createElement("tr");

            const valores = [
                producto.id,
                producto.nombre,
                `Q${Number(producto.precio).toFixed(2)}`,
                producto.stock,
                producto.activo ? "Activo" : "Inactivo"
            ];

            valores.forEach(valor => {
                const celda = document.createElement("td");
                celda.textContent = valor;
                fila.appendChild(celda);
            });

            tabla.appendChild(fila);
        });

        mostrarMensaje(
            "mensaje",
            `Se encontraron ${productos.length} productos.`
        );

    } catch (error) {
        mostrarMensaje("mensaje", error.message, true);
    }
}

// Registrar una venta
async function registrarVenta(evento) {
    evento.preventDefault();

    const productoId = Number(
        document.getElementById("productoId").value
    );

    const cantidad = Number(
        document.getElementById("cantidad").value
    );

    if (!Number.isInteger(productoId) || productoId <= 0 ||
        !Number.isInteger(cantidad) || cantidad <= 0) {

        mostrarMensaje(
            "resultadoVenta",
            "Ingrese un producto y una cantidad válidos.",
            true
        );
        return;
    }

    try {
        const venta = await solicitar(`${API}/ventas`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                detalles: [
                    {
                        productoId: productoId,
                        cantidad: cantidad
                    }
                ]
            })
        });

        mostrarMensaje(
            "resultadoVenta",
            `Venta ${venta.id} registrada. Total: Q${Number(venta.total).toFixed(2)}`
        );

        document.getElementById("formVenta").reset();

        await cargarProductos();
        await cargarVentas();
        await consultarTotal();

    } catch (error) {
        mostrarMensaje("resultadoVenta", error.message, true);
    }
}

// Consultar todas las ventas
async function cargarVentas() {
    const contenedor = document.getElementById("listaVentas");

    try {
        const ventas = await solicitar(`${API}/ventas`);

        contenedor.replaceChildren();

        if (ventas.length === 0) {
            contenedor.textContent = "Todavía no hay ventas registradas.";
            return;
        }

        ventas.forEach(venta => {
            const tarjeta = document.createElement("div");

            const informacion = document.createElement("span");
            informacion.textContent =
                `Venta #${venta.id} | ` +
                `Total: Q${Number(venta.total).toFixed(2)} | ` +
                `Estado: ${venta.estado} `;

            tarjeta.appendChild(informacion);

            if (venta.estado === "REGISTRADA") {
                const boton = document.createElement("button");
                boton.textContent = "Anular venta";

                boton.addEventListener("click", () => {
                    anularVenta(venta.id);
                });

                tarjeta.appendChild(boton);
            }

            contenedor.appendChild(tarjeta);
        });

    } catch (error) {
        contenedor.textContent = error.message;
    }
}

// Anular una venta
async function anularVenta(id) {
    const confirmado = confirm(
        `¿Está seguro de anular la venta #${id}?`
    );

    if (!confirmado) return;

    try {
        await solicitar(`${API}/ventas/${id}/anular`, {
            method: "PATCH"
        });

        mostrarMensaje("mensaje", `Venta #${id} anulada correctamente.`);

        await cargarProductos();
        await cargarVentas();
        await consultarTotal();

    } catch (error) {
        mostrarMensaje("mensaje", error.message, true);
    }
}

// Consultar total diario
async function consultarTotal() {
    const fecha = document.getElementById("fechaConsulta").value;

    if (!fecha) {
        document.getElementById("totalDia").textContent =
            "Seleccione una fecha.";
        return;
    }

    try {
        const resultado = await solicitar(
            `${API}/ventas/total-dia?fecha=${encodeURIComponent(fecha)}`
        );

        document.getElementById("totalDia").textContent =
            `Total: Q${Number(resultado.totalVendido).toFixed(2)}`;

    } catch (error) {
        document.getElementById("totalDia").textContent =
            `Error: ${error.message}`;
    }
}

// Preparar la página al abrirla
document.addEventListener("DOMContentLoaded", () => {

    // Fecha local en formato AAAA-MM-DD
    const fechaActual = new Date();
    const anio = fechaActual.getFullYear();
    const mes = String(fechaActual.getMonth() + 1).padStart(2, "0");
    const dia = String(fechaActual.getDate()).padStart(2, "0");

    document.getElementById("fechaConsulta").value =
        `${anio}-${mes}-${dia}`;

    document.getElementById("btnProductos")
        .addEventListener("click", cargarProductos);

    document.getElementById("formVenta")
        .addEventListener("submit", registrarVenta);

    document.getElementById("btnVentas")
        .addEventListener("click", cargarVentas);

    document.getElementById("btnTotal")
        .addEventListener("click", consultarTotal);

    cargarProductos();
    cargarVentas();
    consultarTotal();
});
