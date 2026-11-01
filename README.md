# 🌿 PF-27 – Ventas e Inventario para Vivero Verde

## 1. Datos del estudiante

**Nombre completo:** Gerson Ezequiel López Enríquez  
**Número de carné:** 9941-25-22144  
**Proyecto asignado:** PF-27 – Ventas e Inventario para Vivero Verde

## 2. Descripción del problema

Los viveros necesitan mantener un control adecuado de sus productos, existencias y ventas. Cuando estos 
procesos se realizan manualmente, pueden presentarse errores en el inventario, dificultades para consultar 
las ventas y problemas para conocer los productos disponibles.

Para atender estas necesidades, se desarrolló Vivero Verde, un sistema que permite administrar categorías, 
productos, ventas y movimientos de inventario de manera organizada.

## 3. Tecnologías utilizadas

- Java y Spring Boot para el backend.
- PostgreSQL para almacenar la información.
- HTML, CSS y JavaScript para la interfaz web.
- Postman para realizar pruebas de la API.
- IntelliJ IDEA para el desarrollo.
- GitHub para almacenar el proyecto.

## 4. Estructura general del sistema

El sistema está organizado en diferentes componentes:

- **Controller:** recibe las solicitudes del usuario.
- **Service:** contiene las reglas del negocio.
- **Repository:** permite consultar y guardar los datos.
- **Entity:** representa las tablas de PostgreSQL.
- **DTO:** organiza la información que recibe y devuelve la API.
- **Frontend:** permite utilizar el sistema desde un navegador.

La aplicación incluye las funciones de administración de categorías, productos, ventas y movimientos de 
inventario.

## 5. Requisitos para ejecutar el proyecto

Para utilizar el sistema se necesita:

- Java instalado y compatible con el proyecto.
- IntelliJ IDEA o un entorno compatible.
- PostgreSQL.
- Una base de datos llamada `vivero_pf27`.
- Un navegador web.

## 6. Configuración de PostgreSQL

El sistema utiliza PostgreSQL para almacenar las categorías, los productos, las ventas y los movimientos de 
inventario.

La base de datos debe llamarse `vivero_pf27`.

Se deben ejecutar los scripts SQL incluidos en el proyecto para crear las tablas y preparar los datos 
necesarios.

La conexión se configura en `application.properties`. La contraseña de PostgreSQL se proporciona mediante 
la variable de entorno `DB_PASSWORD`, evitando incluirla directamente en el código.

## 7. Instrucciones para iniciar el backend

1. Iniciar PostgreSQL.
2. Abrir el proyecto `pf27-vivero` en IntelliJ IDEA.
3. Comprobar que la base de datos esté configurada.
4. Configurar la variable de entorno `DB_PASSWORD` con la contraseña local de PostgreSQL.
5. Ejecutar la aplicación Spring Boot.
6. Verificar que la aplicación inicie correctamente en el puerto `8080`.

## 8. Instrucciones para abrir el frontend

Una vez iniciado el backend:

1. Abrir Google Chrome u otro navegador.
2. Ingresar a la dirección:

   http://localhost:8080/index.html

3. Utilizar la interfaz para administrar categorías, productos, inventario y ventas.

## 9. Descripción de los endpoints

| Método | Endpoint | Función |
|---|---|---|
| GET | `/api/categorias` | Consultar categorías |
| POST | `/api/categorias` | Registrar categoría |
| PUT | `/api/categorias/{id}` | Actualizar categoría |
| DELETE | `/api/categorias/{id}` | Eliminar categoría |
| GET | `/api/productos` | Consultar productos |
| POST | `/api/productos` | Registrar producto |
| PUT | `/api/productos/{id}` | Actualizar producto |
| PATCH | `/api/productos/{id}/desactivar` | Desactivar producto |
| GET | `/api/ventas` | Consultar ventas |
| POST | `/api/ventas` | Registrar venta |
| PATCH | `/api/ventas/{id}/anular` | Anular venta |
| GET | `/api/ventas/total-dia?fecha=AAAA-MM-DD` | Consultar total vendido por fecha |
| POST | `/api/movimientos` | Registrar movimiento |
| GET | `/api/movimientos/producto/{id}` | Consultar historial de inventario |

## 10. Reglas principales del negocio

El sistema aplica las siguientes reglas:

- Los productos deben tener un precio mayor que cero.
- Las existencias no pueden ser negativas.
- No se permiten códigos de productos duplicados.
- Únicamente pueden venderse productos activos.
- No se permiten ventas con cantidades inválidas o sin artículos.
- Una venta puede contener varios productos.
- Al registrar una venta se descuentan automáticamente las existencias.
- No se puede registrar una venta cuando el stock es insuficiente.
- Al anular una venta se devuelven las existencias.
- Una venta no puede anularse dos veces.
- Los ajustes de inventario se realizan mediante movimientos registrados.
- El stock inicial de los nuevos productos queda registrado en el historial cuando es mayor que cero.

## 11. Pruebas y evidencias

Se realizaron pruebas de validaciones, excepciones, reglas del negocio y funcionamiento del frontend.

También se realizaron consultas en PostgreSQL para comprobar que la información se almacena correctamente.

Las solicitudes de la API se encuentran en la carpeta `postman` y las capturas de funcionamiento en `evidencias`.

## Conclusión

El proyecto Vivero Verde permitió aplicar los conocimientos adquiridos durante el curso mediante el desarrollo de un sistema de ventas e inventario.

Su implementación facilita el registro de productos, el control de existencias y la administración de ventas de un vivero.

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