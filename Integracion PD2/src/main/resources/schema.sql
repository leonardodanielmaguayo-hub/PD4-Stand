CREATE TABLE IF NOT EXISTS editoriales (
    id TEXT PRIMARY KEY,
    nombre TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS stands (
    id TEXT PRIMARY KEY,
    metros_cuadrados REAL NOT NULL CHECK (metros_cuadrados > 0),
    costo_por_m2 REAL NOT NULL CHECK (costo_por_m2 >= 0),
    estado TEXT NOT NULL CHECK (estado IN ('DISPONIBLE', 'RESERVADO', 'OCUPADO', 'BLOQUEADO')),
    editorial_id TEXT NOT NULL,
    FOREIGN KEY (editorial_id) REFERENCES editoriales(id)
);

CREATE TABLE IF NOT EXISTS reservas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    stand_id TEXT NOT NULL,
    cliente TEXT NOT NULL,
    fecha_reserva TEXT NOT NULL,
    fecha_limite TEXT NOT NULL,
    FOREIGN KEY (stand_id) REFERENCES stands(id)
);
