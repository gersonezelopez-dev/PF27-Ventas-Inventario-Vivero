# PF-27 – Sistema de Ventas e Inventario para Vivero Verde

## 1. Datos generales

**Proyecto:** PF-27 – Ventas e Inventario para Vivero Verde

**Estudiante:** Gerson Ezequiel López Enríquez

**Número de carné:** 9941-25-22144

**Universidad:** Universidad Mariano Gálvez de Guatemala

**Carrera:** Ingeniería en Sistemas

## 2. Descripción del problema

Un vivero enfrenta dificultades para gestionar sus productos, existencias y ventas al realizar estos procesos 
de forma manual. Esto genera errores en el inventario, ventas de productos sin stock disponible y falta de 
claridad sobre las ganancias obtenidas. Además, la ausencia de un registro organizado complica consultar el 
historial de operaciones o gestionar la anulación de una venta.

Para solucionar esta situación, se diseñó una aplicación de ventas e inventario que permite organizar los 
productos por categorías, controlar el stock en tiempo real, registrar las ventas y consultar los ingresos 
diarios. Con esta herramienta, el vivero logra automatizar sus operaciones, mejorar la organización de la 
información y reducir los errores diarios.
## 3. Tecnologías utilizadas

- Java 21.
- Spring Boot 4.0.8.
- Spring Web.
- Spring Data JPA.
- Hibernate.
- PostgreSQL.
- Maven.
- IntelliJ IDEA.
- Postman.
- Git y GitHub.

Las tecnologías específicas del frontend deberán incorporarse según los archivos de la interfaz desarrollada.

## 4. Estructura general del sistema

La aplicación backend utiliza una arquitectura organizada por capas.

**Controller:** recibe las solicitudes HTTP y proporciona los endpoints REST.

**Service:** contiene las validaciones, reglas del negocio y operaciones de inventario.

**Repository:** realiza el acceso a PostgreSQL mediante Spring Data JPA.

**Entity:** representa las entidades de la base de datos.

**DTO:** permite recibir y transferir información entre el cliente y la API.

**Exception:** centraliza el manejo de excepciones y las respuestas de error.

El paquete principal del backend es:

`com.vivero.pf27`

Las entidades principales son:

- Categoria.
- Producto.
- Venta.
- DetalleVenta.

## 5. Requisitos para ejecutar el proyecto

Se necesita contar con:

- JDK 21.
- PostgreSQL instalado y funcionando.
- IntelliJ IDEA u otro entorno compatible.
- Maven Wrapper incluido en el proyecto.
- Un navegador web para acceder al frontend.
- Postman para realizar pruebas a la API.

## 6. Configuración de PostgreSQL

La aplicación utiliza una base de datos PostgreSQL.

Nombre de la base de datos de desarrollo:

`vivero_pf26`

Dirección de conexión:

`jdbc:postgresql://localhost:5432/vivero_pf26`

Usuario configurado:

`postgres`

Las tablas principales son:

- `categoria`
- `producto`
- `venta`
- `detalle_venta`

Antes de ejecutar la aplicación, es necesario crear o restaurar la estructura de la base de datos.

La contraseña se configura mediante una variable de entorno y no debe guardarse en el repositorio.

En PowerShell:

```powershell
$env:DB_PASSWORD = Read-Host "Contraseña PostgreSQL"
```

## 7. Instrucciones para iniciar el backend

**Paso 1:** Abrir el proyecto en IntelliJ IDEA.

**Paso 2:** Ubicarse en la carpeta Maven `pf27-vivero`.

**Paso 3:** Verificar que PostgreSQL esté funcionando y que la variable `DB_PASSWORD` esté configurada.

**Paso 4:** Compilar el proyecto:

```powershell
.\mvnw.cmd clean compile
```

**Paso 5:** Iniciar Spring Boot:

```powershell
.\mvnw.cmd spring-boot:run
```

Cuando la aplicación inicia correctamente, el servidor queda disponible en:

`http://localhost:8080`

## 8. Instrucciones para abrir el frontend

El frontend permite interactuar con las funcionalidades del sistema mediante una interfaz visual.

Para utilizarlo, primero debe estar ejecutándose el backend Spring Boot y debe existir una conexión funcional 
con PostgreSQL.

**Pendiente de completar con la implementación real:** indicar la ubicación del archivo principal del frontend,
la tecnología utilizada y el procedimiento exacto para abrirlo.

No debe afirmarse que la interfaz funciona hasta verificar su conexión con la API y sus operaciones.

## 9. Descripción de los endpoints

### Categorías

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/categorias` | Consultar categorías |
| GET | `/api/categorias/{id}` | Buscar categoría por ID |
| POST | `/api/categorias` | Registrar categoría |
| PUT | `/api/categorias/{id}` | Actualizar categoría |
| DELETE | `/api/categorias/{id}` | Eliminar categoría |

### Productos

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/productos` | Consultar productos |
| GET | `/api/productos/{id}` | Buscar producto por ID |
| POST | `/api/productos` | Registrar producto |
| PUT | `/api/productos/{id}` | Actualizar producto |
| PATCH | `/api/productos/{id}/desactivar` | Desactivar producto |
| GET | `/api/productos/categoria/{categoriaId}` | Consultar productos por categoría |
| GET | `/api/productos/stock-bajo?limite=5` | Consultar productos con pocas existencias |

### Ventas

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/ventas` | Consultar ventas |
| GET | `/api/ventas/{id}` | Buscar venta por ID |
| POST | `/api/ventas` | Registrar venta |
| PATCH | `/api/ventas/{id}/anular` | Anular venta |
| GET | `/api/ventas/fecha?fecha=2026-10-25` | Consultar ventas por fecha |
| GET | `/api/ventas/total-dia?fecha=2026-10-25` | Calcular total vendido por fecha |

### Ejemplo de registro de una venta

Método: `POST`

URL: `http://localhost:8080/api/ventas`

Cuerpo JSON:

```json
{
  "detalles": [
    {
      "productoId": 1,
      "cantidad": 2
    }
  ]
}
```

## 10. Reglas principales del negocio

**Administración de productos:** el sistema permite registrar, consultar, actualizar y desactivar productos.

**Validación del inventario:** no se permite vender cantidades superiores a las existencias disponibles.

**Estado del producto:** los productos desactivados no pueden incluirse en una nueva venta.

**Registro de ventas:** al registrar una venta válida, el sistema calcula el total y descuenta automáticamente 
las unidades vendidas.

**Anulación de ventas:** una venta registrada puede anularse, restituyendo las existencias al inventario.

**Protección contra anulaciones repetidas:** una venta anulada no puede anularse nuevamente.

**Consulta por fecha:** se pueden consultar las ventas correspondientes a un día específico.

**Total diario:** se calcula el importe de las ventas registradas, excluyendo las ventas anuladas.

**Persistencia:** los datos se almacenan en PostgreSQL y las operaciones de ventas y anulaciones utilizan 
transacciones.

## 11. Pruebas realizadas

Se realizaron pruebas funcionales con Postman para comprobar:

- Registro y consulta de categorías.
- Registro y actualización de productos.
- Registro de ventas.
- Descuento de inventario.
- Rechazo de ventas con stock insuficiente.
- Rechazo de ventas de productos desactivados.
- Anulación de ventas.
- Restauración del inventario.
- Consulta de ventas por fecha.
- Cálculo del total diario.

En las pruebas de ventas se comprobó que un producto con 20 unidades disminuía a 18 al vender dos unidades y 
volvía a 20 después de anular la operación.

También se verificó que el total diario pasaba de Q0.00 a Q50.00 al registrar una venta y regresaba a Q0.00 
después de anularla.

## 12. Repositorio de GitHub

https://github.com/gersonezelopez-dev/PF27-Ventas-Inventario-Vivero

## 13. Conclusión

El proyecto PF-27 demuestra cómo el uso de la tecnología facilita la gestión de un vivero, integrando desde 
el control de productos hasta el procesamiento seguro de cada venta. La aplicación permite mantener el
inventario actualizado en tiempo real, registrar transacciones con precisión y consultar los ingresos de forma 
clara y rápida. En conjunto, esta solución reduce errores operativos y brinda un control más organizado sobre 
las actividades diarias del negocio.



-----------------------------------------------------------------------------------------------
## DTO, validaciones y manejo de errores

### Entidad utilizada
Para la Tarea 12 se seleccionó la entidad Producto del sistema PF27 – Ventas e Inventario de Vivero. Esta entidad permite gestionar los productos disponibles en el inventario.

### DTO implementado
Se utiliza ProductoRequestDTO para recibir los datos de registro y actualización de productos. Contiene los campos código, nombre, precio, stock y categoriaId, excluyendo el identificador generado automáticamente por PostgreSQL.

### Validaciones implementadas
Se implementaron validaciones mediante las anotaciones @NotBlank, @NotNull, @Size, @DecimalMin, @Digits y @Min, con el propósito de evitar nombres vacíos, códigos inválidos, precios incorrectos y cantidades negativas.

### Capa Service
ProductoService contiene los métodos listar(), buscarPorId(), registrar(), actualizar() y eliminar(). Esta capa administra la lógica de negocio y la conversión de datos entre el DTO y la entidad.

### Manejo de excepciones
Se implementó RecursoNoEncontradoException para identificar productos o categorías inexistentes. Además, GlobalExceptionHandler permite controlar los errores de validación y devolver respuestas HTTP apropiadas.

### Códigos HTTP utilizados
- 200 OK: consulta exitosa.
- 201 Created: producto registrado correctamente.
- 400 Bad Request: datos inválidos.
- 404 Not Found: producto inexistente.

### Ejecución y pruebas de la API
1. Iniciar PostgreSQL y verificar la existencia de la base de datos configurada.
2. Configurar la variable de entorno DB_PASSWORD.
3. Abrir el proyecto en IntelliJ IDEA.
4. Ejecutar Spring Boot mediante `.\mvnw.cmd spring-boot:run`.
5. Abrir Postman y utilizar `http://localhost:8080/api/productos`.
6. Realizar las seis pruebas de registro, validaciones, consulta y actualización.

Las pruebas permiten comprobar el funcionamiento de las validaciones y del manejo global de excepciones.