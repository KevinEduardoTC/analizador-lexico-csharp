package com.mycompany.analizadorlexico;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalizadorClase {

    public static String obtenerNombreClase(String codigo) {

        Pattern patron = Pattern.compile("class\\s+(\\w+)");
        Matcher matcher = patron.matcher(codigo);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "Clase no encontrada";
    }

    public static ArrayList<Atributo> obtenerAtributos(String codigo) {

        ArrayList<Atributo> lista = new ArrayList<>();

        Pattern patron = Pattern.compile(
                "(private|public)\\s+(string|int|decimal)\\s+(\\w+)"
        );

        Matcher matcher = patron.matcher(codigo);

        while (matcher.find()) {

            String acceso = matcher.group(1);
            String tipo = matcher.group(2);
            String nombre = matcher.group(3);

            acceso = acceso.equals("private")
                    ? "Privado"
                    : "Público";

            switch (tipo) {
                case "string":
                    tipo = "Texto";
                    break;
                case "int":
                    tipo = "Entero";
                    break;
                case "decimal":
                    tipo = "Decimal";
                    break;
            }

            lista.add(new Atributo(acceso, nombre, tipo));
        }

        return lista;
    }
}