/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package sistemahotel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PROYECTO: SISTEMA DE GESTIÓN HOTELERA "GRAND JAVA"
 * OBJETIVO: Implementar un sistema de reservas múltiples aplicando patrones de diseño,
 * herencia, programación funcional y gestión robusta de errores.
 */

// =============================================================================
// 1. PILARES DE LA POO (ENCAPSULAMIENTO, HERENCIA Y POLIMORFISMO)
// =============================================================================

/**
 * CLASE BASE: Habitacion
 * Representa la abstracción principal. 
 * ENCAPSULAMIENTO: Los atributos son 'private' para proteger la integridad de los datos,
 * obligando al uso de métodos 'getter' para acceder a ellos desde fuera.
 */
class Habitacion {
    private int numero;
    private double precioBase;
    private String tipo;

    // Constructor: Inicializa el estado del objeto al momento de su creación.
    public Habitacion(int numero, double precioBase, String tipo) {
        this.numero = numero;
        this.precioBase = precioBase;
        this.tipo = tipo;
    }

    // Métodos Accesores (Getters): Permiten la lectura controlada de atributos privados.
    public int getNumero() { return numero; }
    public double getPrecioBase() { return precioBase; }
    public String getTipo() { return tipo; }
    
    @Override
    public String toString() {
        // Método sobrescrito para devolver una representación textual del objeto.
        return "Habitacion #" + numero + " [" + tipo + "] - Precio:  " + getPrecioBase();
    }
}

/**
 * SUBCLASE: Suite (Aplica HERENCIA)
 * 'extends' indica que Suite hereda todas las características de Habitacion.
 * POLIMORFISMO: El método 'getPrecioBase' se comporta de forma distinta aquí que en la clase padre.
 */
class Suite extends Habitacion {
    private double bonoLujo;

    public Suite(int numero, double precioBase, double bono, String categoria) {
        // 'super' invoca al constructor de la clase superior para heredar sus atributos.
        super(numero, precioBase, "SUITE " + categoria);
        this.bonoLujo = bono;
    }

    @Override
    public double getPrecioBase() {
        // Sobrescritura (Override): Calcula el precio base de la habitación MÁS el bono extra de la suite.
        return super.getPrecioBase() + bonoLujo;
    }
}

/**
 * CLASE: Reserva (Aplica ASOCIACIÓN)
 * Esta clase no hereda de nadie, sino que 'TIENE' una lista de Habitaciones.
 * Es el puente entre el Cliente (nombre/CC) y los objetos del catálogo.
 */
class Reserva {
    private String cliente;
    private String cc;
    private List<Habitacion> habitacionesReservadas; // Relación de agregación (una reserva tiene muchas habitaciones)

    public Reserva(String cliente, String cc, List<Habitacion> habitaciones) {
        this.cliente = cliente;
        this.cc = cc;
        this.habitacionesReservadas = habitaciones;
    }

    public String getCc() { return cc; }

    @Override
    public String toString() {
        // PROGRAMACIÓN FUNCIONAL (STREAMS): 
        // 1. map(): Extrae solo los números de las habitaciones.
        // 2. collect(): Une los números en un solo String separado por comas para el reporte.
        String nombresHabs = habitacionesReservadas.stream()
                .map(h -> "#" + h.getNumero())
                .collect(Collectors.joining(", "));
        
        // STREAMS + mapToDouble + sum: Recorre la lista, obtiene cada precio y los suma automáticamente.
        double total = habitacionesReservadas.stream()
                .mapToDouble(Habitacion::getPrecioBase)
                .sum();
                
        return "REGISTRO ENCONTRADO -> Cliente: " + cliente + " (CC: " + cc + ") | Habitaciones: [" + nombresHabs + "] | TOTAL:  " + total;
    }
}

// =============================================================================
// 2. PATRONES DE DISEÑO (CREACIONALES)
// =============================================================================

/**
 * PATRÓN: FACTORY METHOD
 * Su función es desacoplar la creación de objetos. El programa principal no sabe 
 * cómo se construye una Suite o una Normal, solo le pide a la fábrica que la "cree".
 */
class HabitacionFactory {
    public static Habitacion crear(String tipo, int num, double precio, String subCat) {
        if (tipo.equalsIgnoreCase("SUITE")) {
            // Lógica de negocio interna: Definimos bonos según la categoría premium.
            double bono = subCat.equals("PREMIUM") ? 150.0 : (subCat.equals("NORMAL") ? 80.0 : 40.0);
            return new Suite(num, precio, bono, subCat);
        }
        // Si no es suite, fabrica una habitación estándar.
        return new Habitacion(num, precio, "NORMAL " + subCat);
    }
}

/**
 * PATRÓN: SINGLETON
 * Garantiza que la clase HotelAdmin tenga una única instancia global.
 * Útil para que todas las partes del programa consulten la MISMA lista de reservas e inventario.
 */
class HotelAdmin {
    private static HotelAdmin instancia; // Variable estática que guarda la única copia.
    private List<Habitacion> inventario = new ArrayList<>();
    private List<Reserva> historialReservas = new ArrayList<>();

    // El constructor es 'private' para bloquear el uso de 'new HotelAdmin()' desde fuera.
    private HotelAdmin() {}

    // Método de acceso global: Crea la instancia si no existe, o devuelve la existente.
    public static HotelAdmin getInstancia() {
        if (instancia == null) instancia = new HotelAdmin();
        return instancia;
    }

    public void agregarAlInventario(Habitacion h) { inventario.add(h); }

    // PROGRAMACIÓN FUNCIONAL: Uso de forEach para recorrer la colección de forma moderna.
    public void verCatalogo() {
        System.out.println("\n--- CATALOGO DE HABITACIONES ACTUALIZADO ---");
        inventario.forEach(System.out::println);
    }

    /**
     * MÉTODO: registrarNuevaReserva
     * Lógica compleja que busca objetos en el inventario basándose en una lista de números.
     */
    public void registrarNuevaReserva(String nombre, String cc, List<Integer> numsElegidos) throws Exception {
        List<Habitacion> encontradas = new ArrayList<>();
        
        for (Integer num : numsElegidos) {
            // LAMBDA Y STREAMS: filter busca la habitación, findFirst la toma.
            // orElseThrow: Si el número no existe en el inventario, dispara una EXCEPCIÓN.
            Habitacion h = inventario.stream()
                .filter(hab -> hab.getNumero() == num)
                .findFirst()
                .orElseThrow(() -> new Exception("La habitacion " + num + " no existe. Operacion cancelada."));
            encontradas.add(h);
        }

        // Se crea el objeto de asociación Reserva y se guarda en el historial.
        historialReservas.add(new Reserva(nombre, cc, encontradas));
        System.out.println(" Exito: Se ha procesado la reserva multiple.");
    }

    public void consultarPorCC(String documento) {
        // STREAM + FILTER: Filtra el historial para mostrar solo las reservas del cliente específico.
        List<Reserva> resultados = historialReservas.stream()
            .filter(r -> r.getCc().equals(documento))
            .collect(Collectors.toList());

        if (resultados.isEmpty()) {
            System.out.println("No existen reservas registradas para la CC: " + documento);
        } else {
            resultados.forEach(System.out::println);
        }
    }
}

// =============================================================================
// 3. CLASE PRINCIPAL (PUNTO DE ENTRADA Y MANEJO DE ERRORES)
// =============================================================================

public class SistemaHotel{ 
    public static void main(String[] args) {
        // Inicialización del Singleton
        HotelAdmin hotel = HotelAdmin.getInstancia();
        Scanner sc = new Scanner(System.in);
        
        // POBLADO DE DATOS: 9 Habitaciones generadas mediante la Fábrica.
        // 3 Económicas ($45-60), 3 Estándar ($125-145) 3 suites(Econ, Estan, Premi) 
        hotel.agregarAlInventario(HabitacionFactory.crear("NORMAL", 101, 60.0, "ECONOMICA"));
        hotel.agregarAlInventario(HabitacionFactory.crear("NORMAL", 102, 55.0, "ECONOMICA"));
        hotel.agregarAlInventario(HabitacionFactory.crear("NORMAL", 103, 45.0, "ECONOMICA"));
        hotel.agregarAlInventario(HabitacionFactory.crear("NORMAL", 201, 125.0,"ESTANDAR"));
        hotel.agregarAlInventario(HabitacionFactory.crear("NORMAL", 202, 135.0, "ESTANDAR"));
        hotel.agregarAlInventario(HabitacionFactory.crear("NORMAL", 203, 145.0, "ESTANDAR")); 
        hotel.agregarAlInventario(HabitacionFactory.crear("SUITE", 301, 200.0, "ECONOMICA"));
        hotel.agregarAlInventario(HabitacionFactory.crear("SUITE", 302, 450.0, "NORMAL"));
        hotel.agregarAlInventario(HabitacionFactory.crear("SUITE", 303, 650.0, "PREMIUM"));

        boolean activo = true;
        // Bucle Principal: Mantiene el programa funcionando hasta que se elija 'Salir'.
        while (activo) {
            System.out.println("\n************************************************");
            System.out.println("       SISTEMA HOTELERO - INTERFAZ DE CONTROL");
            System.out.println("************************************************");
            System.out.println("1. Realizar reservacion");
            System.out.println("2. Ver catalogo de habitaciones");
            System.out.println("3. Verificar reservacion por Cedula (CC)");
            System.out.println("4. Salir");
            System.out.print("Seleccione una opcion: ");

            // GESTIÓN DE ERRORES (Punto 4 de la Guía): Try-Catch-Finally
            try {
                // Intenta convertir la entrada de texto en un número entero.
                int opcion = Integer.parseInt(sc.nextLine());

                switch (opcion) {
                    case 1:
                        // FLUJO GUIADO DE RESERVA
                        System.out.print("Nombre del Cliente: ");
                        String nom = sc.nextLine();
                        System.out.print("Cedula (CC): ");
                        String cc = sc.nextLine();
                        
                        System.out.print(" Cuantas habitaciones desea tomar?: ");
                        int cant = Integer.parseInt(sc.nextLine()); // Puede lanzar NumberFormatException
                        
                        List<Integer> seleccion = new ArrayList<>();
                        for (int i = 1; i <= cant; i++) {
                            System.out.print("Ingrese el numero de la habitacion #" + i + ": ");
                            seleccion.add(Integer.parseInt(sc.nextLine()));
                        }
                        
                        // Envía los datos al administrador para validar y guardar.
                        hotel.registrarNuevaReserva(nom, cc, seleccion);
                        break;
                        
                    case 2:
                        hotel.verCatalogo();
                        break;
                        
                    case 3:
                        System.out.print("Numero de Cedula a buscar: ");
                        hotel.consultarPorCC(sc.nextLine());
                        break;
                        
                    case 4:
                        activo = false;
                        System.out.println("Cerrando base de datos. ¡Hasta pronto!");
                        break;
                        
                    default:
                        System.out.println("️ Error: Opcion no valida en el menu.");
                }

            } catch (NumberFormatException e) {
                // Se ejecuta si el usuario introduce letras o símbolos en campos de números.
                System.err.println(" ERROR: Entrada invalida. Se esperaba un numero entero.");
            } catch (Exception e) {
                // Se ejecuta si ocurre un error lógico (ej: habitación inexistente).
                System.err.println(" NOTA DEL SISTEMA: " + e.getMessage());
            } finally {
                // Este bloque se ejecuta SIEMPRE al final de cada ciclo para dar limpieza visual.
                if(activo) System.out.println("************************************************");
            }
        }
        sc.close(); // Liberación de recursos del sistema.
    }
}