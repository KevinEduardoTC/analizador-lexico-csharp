package com.mycompany.analizadorlexico;

public class Atributo {

    private String accesibilidad;
    private String nombre;
    private String tipo;

    public Atributo(String accesibilidad, String nombre, String tipo) {
        this.accesibilidad = accesibilidad;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public String getAccesibilidad() {
        return accesibilidad;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }
}