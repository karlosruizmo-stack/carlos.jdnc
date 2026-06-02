package org.example;

import java.sql.*;
import java.util.Scanner;

public class Main {
    static String url        = "jdbc:mysql://localhost:3306/hipermercado";
    static String usuario    = "root";
    static String contraseña = "root";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("""
                    \n╔══════════════════════════════════════════╗
                    ║          HIPERMERCADO - MENÚ             ║
                    ╠══════════════════════════════════════════╣
                    ║  1. Buscar producto por ID               ║
                    ║  2. Crear producto nuevo                 ║
                    ║  3. Filtrar por sección/categoría        ║
                    ║  4. Eliminar un producto                 ║
                    ║  5. Productos de una marca               ║
                    ║  6. Productos con descuento activo       ║
                    ║  7. Productos por rango de precio        ║
                    ║  8. Buscar producto por nombre           ║
                    ║  0. Salir                                ║
                    ╚══════════════════════════════════════════╝
                    Elige una opción: """);

            opcion = sc.nextInt();
            sc.nextLine();

            switch (opcion) {
                case 1 -> buscarPorId(sc);
                case 2 -> crearProducto(sc);
                case 3 -> filtrarPorSeccion(sc);
                case 4 -> eliminarProducto(sc);
                case 5 -> productosPorMarca(sc);
                case 6 -> productosConDescuento();
                case 7 -> productosPorRangoPrecio(sc);
                case 8 -> buscarPorNombre(sc);
                case 0 -> System.out.println("¡Hasta luego!");
                default -> System.out.println("Opción no válida.");
            }

        } while (opcion != 0);

        sc.close();
    }

    // Devolver producto mediante id
    static void buscarPorId(Scanner sc) {
        System.out.print("Introduce el ID del producto: ");
        int id = sc.nextInt();

        String sql = """
                SELECT p.id_producto, p.nombre, p.precio, p.stock,
                       m.nombre AS marca, s.nombre AS seccion
                FROM productos p
                JOIN marcas   m ON p.id_marca   = m.id_marca
                JOIN secciones s ON p.id_seccion = s.id_seccion
                WHERE p.id_producto = ?
                """;

        try {
            Connection conexion = DriverManager.getConnection(url, usuario, contraseña);

            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet resultado = ps.executeQuery();

            if (resultado.next()) {
                System.out.println("\n--- PRODUCTO ENCONTRADO ---");
                System.out.println("ID:      " + resultado.getInt("id_producto"));
                System.out.println("Nombre:  " + resultado.getString("nombre"));
                System.out.println("Precio:  " + resultado.getDouble("precio") + " €");
                System.out.println("Stock:   " + resultado.getInt("stock"));
                System.out.println("Marca:   " + resultado.getString("marca"));
                System.out.println("Sección: " + resultado.getString("seccion"));
            } else {
                System.out.println("No se encontró ningún producto con ID " + id);
            }

            resultado.close();
            ps.close();
            conexion.close();

        } catch (Exception e) {
            System.out.println("Error en la conexión");
            e.printStackTrace();
        }
    }

    // Crear producto
    static void crearProducto(Scanner sc) {
        System.out.print("Nombre del producto: ");
        String nombre = sc.nextLine();

        System.out.print("ID de sección (1-20): ");
        int idSeccion = sc.nextInt();

        System.out.print("ID de marca (1-50): ");
        int idMarca = sc.nextInt();

        System.out.print("Precio: ");
        double precio = sc.nextDouble();
        sc.nextLine();

        System.out.print("Código de barras (13 dígitos): ");
        String codigoBarras = sc.nextLine();

        String sql = """
                INSERT INTO productos
                    (nombre, id_seccion, id_marca, precio, codigo_barras)
                VALUES (?, ?, ?, ?, ?)
                """;

        try {
            Connection conexion = DriverManager.getConnection(url, usuario, contraseña);

            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setInt(2, idSeccion);
            ps.setInt(3, idMarca);
            ps.setDouble(4, precio);
            ps.setString(5, codigoBarras);

            int filas = ps.executeUpdate();

            if (filas > 0) {
                System.out.println("Producto creado correctamente.");
            } else {
                System.out.println("No se pudo crear el producto.");
            }

            ps.close();
            conexion.close();

        } catch (Exception e) {
            System.out.println("Error en la conexión");
            e.printStackTrace();
        }
    }

    //Filtrar productos
    static void filtrarPorSeccion(Scanner sc) {
        System.out.print("Nombre de la sección (ej: Bebidas, Lácteos, Tecnología): ");
        String seccion = sc.nextLine();

        String sql = """
                SELECT p.id_producto, p.nombre, p.precio, p.stock,
                       m.nombre AS marca
                FROM productos p
                JOIN marcas    m ON p.id_marca   = m.id_marca
                JOIN secciones s ON p.id_seccion = s.id_seccion
                WHERE s.nombre = ?
                ORDER BY p.nombre
                """;

        try {
            Connection conexion = DriverManager.getConnection(url, usuario, contraseña);

            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, seccion);

            ResultSet resultado = ps.executeQuery();

            System.out.println("\n--- PRODUCTOS EN SECCIÓN: " + seccion + " ---");
            System.out.printf("%-6s %-35s %-10s %-8s %s%n",
                    "ID", "Nombre", "Precio", "Stock", "Marca");
            System.out.println("-".repeat(75));

            boolean hayResultados = false;
            while (resultado.next()) {
                hayResultados = true;
                System.out.printf("%-6d %-35s %-10.2f %-8d %s%n",
                        resultado.getInt("id_producto"),
                        resultado.getString("nombre"),
                        resultado.getDouble("precio"),
                        resultado.getInt("stock"),
                        resultado.getString("marca"));
            }

            if (!hayResultados) {
                System.out.println("No hay nada en esa sección.");
            }

            resultado.close();
            ps.close();
            conexion.close();

        } catch (Exception e) {
            System.out.println("Error en la conexión");
            e.printStackTrace();
        }
    }

    // Eliminar producto
    static void eliminarProducto(Scanner sc) {
        System.out.print("ID del producto a eliminar: ");
        int id = sc.nextInt();

        String sql = "DELETE FROM productos WHERE id_producto = ?";

        try {
            Connection conexion = DriverManager.getConnection(url, usuario, contraseña);

            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setInt(1, id);

            int filas = ps.executeUpdate();

            if (filas > 0) {
                System.out.println("Producto con ID " + id + " eliminado correctamente.");
            } else {
                System.out.println("No se encontró ningún producto con ese ID.");
            }

            ps.close();
            conexion.close();

        } catch (Exception e) {
            System.out.println("Error en la conexión");
            e.printStackTrace();
        }
    }

    // Mostrar producto por marca
    static void productosPorMarca(Scanner sc) {
        System.out.print("Nombre de la marca (ej: Coca-Cola, Nike, Samsung): ");
        String marca = sc.nextLine();

        String sql = """
                SELECT p.id_producto, p.nombre, p.precio, p.stock,
                       s.nombre AS seccion
                FROM productos p
                JOIN marcas    m ON p.id_marca   = m.id_marca
                JOIN secciones s ON p.id_seccion = s.id_seccion
                WHERE m.nombre = ?
                ORDER BY p.precio
                """;

        try {
            Connection conexion = DriverManager.getConnection(url, usuario, contraseña);

            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, marca);

            ResultSet resultado = ps.executeQuery();

            System.out.println("\n--- Productos de la marca" +
                    ": " + marca + " ---");
            System.out.printf("%-6s %-35s %-10s %-8s %s%n",
                    "ID", "Nombre", "Precio", "Stock", "Sección");
            System.out.println("-".repeat(75));

            boolean hayResultados = false;
            while (resultado.next()) {
                hayResultados = true;
                System.out.printf("%-6d %-35s %-10.2f %-8d %s%n",
                        resultado.getInt("id_producto"),
                        resultado.getString("nombre"),
                        resultado.getDouble("precio"),
                        resultado.getInt("stock"),
                        resultado.getString("seccion"));
            }

            if (!hayResultados) {
                System.out.println("No se encontraron productos de esa marca.");
            }

            resultado.close();
            ps.close();
            conexion.close();

        } catch (Exception e) {
            System.out.println("Error en la conexión");
            e.printStackTrace();
        }
    }

    // Que productos aun tienen descuento
    static void productosConDescuento() {
        String sql = """
                SELECT p.id_producto, p.nombre, p.precio,
                       p.descuento_pct,
                       ROUND(p.precio - (p.precio * p.descuento_pct / 100), 2) AS precio_final,
                       m.nombre AS marca
                FROM productos p
                JOIN marcas m ON p.id_marca = m.id_marca
                WHERE p.descuento_pct > 0
                ORDER BY p.descuento_pct DESC
                """;

        try {
            Connection conexion = DriverManager.getConnection(url, usuario, contraseña);
            PreparedStatement ps = conexion.prepareStatement(sql);
            ResultSet resultado = ps.executeQuery();

            System.out.println("\n--- PRODUCTOS EN OFERTA ---");
            System.out.printf("%-6s %-30s %-8s %-10s %-12s %s%n",
                    "ID", "Nombre", "Dto.%", "Precio", "Precio final", "Marca");
            System.out.println("-".repeat(80));

            boolean hay = false;
            while (resultado.next()) {
                hay = true;
                System.out.printf("%-6d %-30s %-8d %-10.2f %-12.2f %s%n",
                        resultado.getInt("id_producto"),
                        resultado.getString("nombre"),
                        resultado.getInt("descuento_pct"),
                        resultado.getDouble("precio"),
                        resultado.getDouble("precio_final"),
                        resultado.getString("marca"));
            }

            if (!hay) System.out.println("No hay productos en oferta.");

            resultado.close();
            ps.close();
            conexion.close();

        } catch (Exception e) {
            System.out.println("Error en la conexión");
            e.printStackTrace();
        }
    }

    // Producto en base al precio
    static void productosPorRangoPrecio(Scanner sc) {
        System.out.print("Precio mínimo: ");
        double min = sc.nextDouble();

        System.out.print("Precio máximo: ");
        double max = sc.nextDouble();
        sc.nextLine();

        String sql = """
                SELECT p.id_producto, p.nombre, p.precio,
                       m.nombre AS marca, s.nombre AS seccion
                FROM productos p
                JOIN marcas    m ON p.id_marca   = m.id_marca
                JOIN secciones s ON p.id_seccion = s.id_seccion
                WHERE p.precio BETWEEN ? AND ?
                ORDER BY p.precio ASC
                """;

        try {
            Connection conexion = DriverManager.getConnection(url, usuario, contraseña);
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setDouble(1, min);
            ps.setDouble(2, max);

            ResultSet resultado = ps.executeQuery();

            System.out.println("\n--- PRODUCTOS ENTRE " + min + "€ Y " + max + "€ ---");
            System.out.printf("%-6s %-30s %-10s %-20s %s%n",
                    "ID", "Nombre", "Precio", "Marca", "Sección");
            System.out.println("-".repeat(80));

            boolean hay = false;
            while (resultado.next()) {
                hay = true;
                System.out.printf("%-6d %-30s %-10.2f %-20s %s%n",
                        resultado.getInt("id_producto"),
                        resultado.getString("nombre"),
                        resultado.getDouble("precio"),
                        resultado.getString("marca"),
                        resultado.getString("seccion"));
            }

            if (!hay) System.out.println("No hay productos en ese rango de precio.");

            resultado.close();
            ps.close();
            conexion.close();

        } catch (Exception e) {
            System.out.println("Error en la conexión");
            e.printStackTrace();
        }
    }

    // Buscar por nombre
    static void buscarPorNombre(Scanner sc) {
        System.out.print("Introduce parte del nombre: ");
        String nombre = sc.nextLine();

        String sql = """
                SELECT p.id_producto, p.nombre, p.precio, p.stock,
                       m.nombre AS marca, s.nombre AS seccion
                FROM productos p
                JOIN marcas    m ON p.id_marca   = m.id_marca
                JOIN secciones s ON p.id_seccion = s.id_seccion
                WHERE p.nombre LIKE ?
                ORDER BY p.nombre
                """;

        try {
            Connection conexion = DriverManager.getConnection(url, usuario, contraseña);
            PreparedStatement ps = conexion.prepareStatement(sql);
            ps.setString(1, "%" + nombre + "%");

            ResultSet resultado = ps.executeQuery();

            System.out.println("\n--- RESULTADOS PARA: \"" + nombre + "\" ---");
            System.out.printf("%-6s %-35s %-10s %-8s %-20s %s%n",
                    "ID", "Nombre", "Precio", "Stock", "Marca", "Sección");
            System.out.println("-".repeat(90));

            boolean hay = false;
            while (resultado.next()) {
                hay = true;
                System.out.printf("%-6d %-35s %-10.2f %-8d %-20s %s%n",
                        resultado.getInt("id_producto"),
                        resultado.getString("nombre"),
                        resultado.getDouble("precio"),
                        resultado.getInt("stock"),
                        resultado.getString("marca"),
                        resultado.getString("seccion"));
            }

            if (!hay) System.out.println("No se encontraron productos con ese nombre.");

            resultado.close();
            ps.close();
            conexion.close();

        } catch (Exception e) {
            System.out.println("Error en la conexión");
            e.printStackTrace();
        }
    }
}
