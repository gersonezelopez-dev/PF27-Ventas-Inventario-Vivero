CREATE TABLE IF NOT EXISTS movimiento_inventario (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     producto_id BIGINT NOT NULL,
                                                     tipo VARCHAR(20) NOT NULL,
    cantidad INTEGER NOT NULL,
    stock_anterior INTEGER NOT NULL,
    stock_nuevo INTEGER NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_movimiento_producto
    FOREIGN KEY (producto_id)
    REFERENCES producto(id),

    CONSTRAINT chk_movimiento_tipo
    CHECK (tipo IN ('ENTRADA', 'SALIDA', 'AJUSTE')),

    CONSTRAINT chk_movimiento_cantidad
    CHECK (cantidad > 0),

    CONSTRAINT chk_movimiento_stock
    CHECK (stock_anterior >= 0 AND stock_nuevo >= 0)
    );
