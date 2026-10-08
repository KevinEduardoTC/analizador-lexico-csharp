package com.mycompany.analizadorlexico;

import java.util.ArrayList;
import java.util.Scanner;

public class AnalizadorLexico {

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);

        System.out.println("Ingrese la clase:");

String codigo = teclado.nextLine();

        System.out.println("Nombre de la clase:");
        System.out.println(
                AnalizadorClase.obtenerNombreClase(codigo)
        );

        System.out.println("\nTabla de atributos:");

        ArrayList<Atributo> atributos =
                AnalizadorClase.obtenerAtributos(codigo);

        for (Atributo a : atributos) {

            System.out.println(
                    a.getAccesibilidad()
                    + " | "
                    + a.getNombre()
                    + " | "
                    + a.getTipo()
            );
        }
    }
}