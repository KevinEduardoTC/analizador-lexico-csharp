package com.mycompany.analizadorlexico;

/**
 * Representa una fila de la Tabla de Tokens: un lexema reconocido
 * junto con la descripción del patrón al que corresponde.
 */
public class Token {

    private final String lexema;
    private final String patron;

    public Token(String lexema, String patron) {
        this.lexema = lexema;
        this.patron = patron;
    }

    public String getLexema() {
        return lexema;
    }

    public String getPatron() {
        return patron;
    }

    @Override
    public String toString() {
        return lexema + " -> " + patron;
    }
}
