package com.mycompany.analizadorlexico;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Versión de consola mientras la GUI no está lista.
 *
 * - Sin entrada (ejecutado desde NetBeans con F6): analiza la clase de ejemplo
 *   del enunciado.
 * - Con un archivo como argumento: analiza ese archivo completo.
 *   Ej.: java -cp target/classes com.mycompany.analizadorlexico.AnalizadorLexico pruebas/valida.cs
 * - Con entrada redirigida desde archivo (&lt; clase.cs): analiza ese texto.
 *
 * Cuando Julio termine la GUI, la clase principal pasa a ser la ventana y esta
 * queda solo para pruebas rápidas.
 */
public class AnalizadorLexico {

    static final String EJEMPLO_ENUNCIADO = """
            class NombreClase
            {
                private string nombre_atributo {get;set;}
                public int nombre_atributo_1 {get;set;}
                private decimal nombre_atributo_2 {get;set;}

                public NombreClase()
                {
                }
            }
            """;

    public static void main(String[] args) throws IOException {
        String codigo = EJEMPLO_ENUNCIADO;
        if (args.length > 0) {
            codigo = Files.readString(Path.of(args[0]), StandardCharsets.UTF_8);
        } else if (System.console() == null && System.in.available() > 0) {
            codigo = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        } else {
            System.out.println("Analizando la clase de ejemplo del enunciado:\n");
            System.out.println(codigo);
        }

        ResultadoAnalisis r = Analizador.analizar(codigo);

        System.out.println("TABLA DE TOKENS");
        System.out.println(TablaTokens.imprimir(r.getTablaTokens()));

        System.out.println("NOMBRE DE LA CLASE: "
                + (r.getNombreClase() != null ? r.getNombreClase() : "(no determinado)"));

        System.out.println("\nTABLA DE ATRIBUTOS");
        System.out.printf("%-14s | %-25s | %s%n", "ACCESIBILIDAD", "NOMBRE", "TIPO");
        for (Atributo a : r.getAtributos()) {
            System.out.printf("%-14s | %-25s | %s%n", a.getAccesibilidad(), a.getNombre(), a.getTipo());
        }

        if (!r.esValido()) {
            System.out.println("\nERRORES");
            r.getErrores().forEach(e -> System.out.println(" - " + e));
        }
    }
}
