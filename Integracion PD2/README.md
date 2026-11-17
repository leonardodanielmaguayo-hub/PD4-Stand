# Sistema de stands (Java + JDBC)

La aplicación Java conserva las opciones originales para consultar stands, cambiar su estado y salir. También permite reservar stands, consultar reservas y ver editoriales.

## Requisitos y ejecución

- JDK 25 (LTS).
- Apache Maven 3.10.1.

Maven debe ejecutarse con `JAVA_HOME` apuntando al JDK 25.

Desde esta carpeta, ejecuta:

```powershell
mvn clean compile exec:java
```

La primera ejecución crea `stands.db` en el directorio actual y las tablas `stands`, `editoriales` y `reservas`. El controlador SQLite JDBC se descarga mediante Maven según la dependencia declarada en `pom.xml`.

La URL predeterminada es `jdbc:sqlite:stands.db`. Puede cambiarse con la variable `STANDS_JDBC_URL` o con la propiedad Java `stands.jdbc.url`.

## Separación de responsabilidades

- `model`: las entidades `Stand`, `Editorial`, `Reserva` y el estado del stand.
- `controller`: las operaciones y el menú de consola.
- `persistence`: consultas y cambios de datos por JDBC.
- `Main`: crea las editoriales, los stands de ejemplo y los componentes que conecta.

Cada stand tiene una editorial; las reservas referencian un stand. Los IDs de stands y editoriales son claves primarias y las relaciones usan claves foráneas. La base se inicializa de manera idempotente: al volver a ejecutar, no se duplican los datos de ejemplo ni se sobreescriben estados existentes.


