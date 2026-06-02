executeUpdate() vs executeQuery()

ps.executeQuery(): Se usa para las consultas SELECT. Devuelve un objeto ResultSet lleno de filas que tienes que recorrer.

ps.executeUpdate(): Se usa para operaciones que modifican los datos: INSERT, UPDATE y DELETE. No devuelve filas, sino un número entero (int). Ese número representa a cuántas filas afecta a  la operación.

printStackTrace(): Es el método que traduce toda la información interna del error en un texto legible para el desarrollador.

PreparedStatement ps = conexion.prepareStatement(sql);Prepara la consulta SQL dentro de un objeto especial llamado PreparedStatement.

ps.setInt(1, id);Significa "el dato que te voy a pasar ahora mismo es un número entero

No suelo hacer read me porque mi codigo solo es lo que aprendo en classe y buscar ejemplos en internet pero considero que como me ha costado más hacer esta y he necesitado usar la ia para algunas partes pues que no fuera copy paste.
