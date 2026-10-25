INSERT INTO categoria (nombre, descripcion)
VALUES
('Plantas ornamentales', 'Plantas para decoracion'),
('Arboles frutales', 'Arboles que producen frutas'),
('Suculentas y cactus', 'Plantas de bajo consumo de agua'),
('Insumos de jardineria', 'Productos para el cuidado de plantas')
ON CONFLICT (nombre) DO NOTHING;


INSERT INTO producto
(codigo, nombre, precio, stock, activo, categoria_id)
VALUES
('PL-001', 'Rosa roja', 25.00, 20, TRUE,
 (SELECT id FROM categoria WHERE nombre = 'Plantas ornamentales')),

('PL-002', 'Orquidea', 85.00, 8, TRUE,
 (SELECT id FROM categoria WHERE nombre = 'Plantas ornamentales')),

('PL-003', 'Girasol', 18.00, 15, TRUE,
 (SELECT id FROM categoria WHERE nombre = 'Plantas ornamentales')),

('FR-001', 'Arbol de limon', 65.00, 10, TRUE,
 (SELECT id FROM categoria WHERE nombre = 'Arboles frutales')),

('FR-002', 'Arbol de mango', 75.00, 6, TRUE,
 (SELECT id FROM categoria WHERE nombre = 'Arboles frutales')),

('CA-001', 'Cactus pequeno', 30.00, 18, TRUE,
 (SELECT id FROM categoria WHERE nombre = 'Suculentas y cactus')),

('CA-002', 'Suculenta verde', 22.00, 25, TRUE,
 (SELECT id FROM categoria WHERE nombre = 'Suculentas y cactus')),

('JA-001', 'Abono organico', 40.00, 12, TRUE,
 (SELECT id FROM categoria WHERE nombre = 'Insumos de jardineria')),

('JA-002', 'Maceta biodegradable', 15.00, 30, TRUE,
 (SELECT id FROM categoria WHERE nombre = 'Insumos de jardineria')),

('JA-003', 'Tierra preparada', 35.00, 4, TRUE,
 (SELECT id FROM categoria WHERE nombre = 'Insumos de jardineria'))
ON CONFLICT (codigo) DO NOTHING;
