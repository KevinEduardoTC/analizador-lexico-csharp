package com.mycompany.analizadorlexico;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class MotorLexico {

    private static final Set<String> RESERVADAS = Set.of(
            "class", "private", "public", "get", "set"
    );
    private static final Set<String> TIPOS_DATO = Set.of(
            "string", "int", "decimal"
    );

    /** Estados deterministas para reconocer palabras y errores de identificacion. */
    private enum Estado { INICIO, IDENTIFICADOR, IDENTIFICADOR_INVALIDO }

    private MotorLexico() { }

    /**
     * Devuelve los tokens del texto en orden de aparicion.
     * Cada token contiene lexema, tipo, linea y columna.
     * Los caracteres y nombres no validos se devuelven como ERROR.
     * Los espacios en blanco se ignoran.
     *
     * Identificadores: L(L|_|D)* (deben empezar con una letra).
     */
    public static List<TokenLexico> analizar(String texto) {
        Objects.requireNonNull(texto, "El texto a analizar no puede ser null");

        List<TokenLexico> tokens = new ArrayList<>();
        StringBuilder lexema = new StringBuilder();
        Estado estado = Estado.INICIO;
        int indice = 0;
        int linea = 1;
        int columna = 1;
        int lineaToken = 1;
        int columnaToken = 1;

        while (indice < texto.length()) {
            int caracter = texto.codePointAt(indice);
            int longitudCaracter = Character.charCount(caracter);

            switch (estado) {
                case INICIO -> {
                    if (Character.isWhitespace(caracter)) {
                        // Windows: \r\n es un solo salto de linea.
                        if (caracter == '\r') {
                            indice += longitudCaracter;
                            if (indice < texto.length() && texto.charAt(indice) == '\n') {
                                indice++;
                            }
                            linea++;
                            columna = 1;
                        } else if (caracter == '\n') {
                            indice += longitudCaracter;
                            linea++;
                            columna = 1;
                        } else {
                            indice += longitudCaracter;
                            columna++;
                        }
                    } else if (esSimbolo(caracter)) {
                        tokens.add(new TokenLexico(
                                new String(Character.toChars(caracter)),
                                TokenLexico.SIMBOLO, linea, columna));
                        indice += longitudCaracter;
                        columna++;
                    } else if (Character.isLetter(caracter)) {
                        lineaToken = linea;
                        columnaToken = columna;
                        lexema.appendCodePoint(caracter);
                        estado = Estado.IDENTIFICADOR;
                        indice += longitudCaracter;
                        columna++;
                    } else if (Character.isDigit(caracter) || caracter == '_') {
                        // Un identificador no puede comenzar con digito o '_'.
                        lineaToken = linea;
                        columnaToken = columna;
                        lexema.appendCodePoint(caracter);
                        estado = Estado.IDENTIFICADOR_INVALIDO;
                        indice += longitudCaracter;
                        columna++;
                    } else {
                        tokens.add(new TokenLexico(
                                new String(Character.toChars(caracter)),
                                TokenLexico.ERROR, linea, columna));
                        indice += longitudCaracter;
                        columna++;
                    }
                }
                case IDENTIFICADOR -> {
                    if (esParteIdentificador(caracter)) {
                        lexema.appendCodePoint(caracter);
                        indice += longitudCaracter;
                        columna++;
                    } else {
                        String palabra = lexema.toString();
                        tokens.add(new TokenLexico(
                                palabra, clasificarPalabra(palabra), lineaToken, columnaToken));
                        lexema.setLength(0);
                        estado = Estado.INICIO;
                        // No avanzar: este mismo caracter puede ser un simbolo.
                    }
                }
                case IDENTIFICADOR_INVALIDO -> {
                    if (esParteIdentificador(caracter)) {
                        lexema.appendCodePoint(caracter);
                        indice += longitudCaracter;
                        columna++;
                    } else {
                        tokens.add(new TokenLexico(
                                lexema.toString(), TokenLexico.ERROR, lineaToken, columnaToken));
                        lexema.setLength(0);
                        estado = Estado.INICIO;
                    }
                }
            }
        }

        // Reconocer el ultimo lexema aunque el texto termine sin separador.
        if (estado != Estado.INICIO) {
            String palabra = lexema.toString();
            String tipo = estado == Estado.IDENTIFICADOR
                    ? clasificarPalabra(palabra) : TokenLexico.ERROR;
            tokens.add(new TokenLexico(palabra, tipo, lineaToken, columnaToken));
        }

        return List.copyOf(tokens);
    }

    /** Indica si existe por lo menos un error lexico en la lista. */
    public static boolean contieneErrores(List<TokenLexico> tokens) {
        Objects.requireNonNull(tokens, "La lista de tokens no puede ser null");
        return tokens.stream().anyMatch(t -> TokenLexico.ERROR.equals(t.getTipo()));
    }

    private static boolean esSimbolo(int caracter) {
        return caracter == '{' || caracter == '}' || caracter == '('
                || caracter == ')' || caracter == ';';
    }

    private static boolean esParteIdentificador(int caracter) {
        return Character.isLetter(caracter) || Character.isDigit(caracter)
                || caracter == '_';
    }

    private static String clasificarPalabra(String palabra) {
        if (RESERVADAS.contains(palabra)) {
            return TokenLexico.PALABRA_RESERVADA;
        }
        if (TIPOS_DATO.contains(palabra)) {
            return TokenLexico.TIPO_DATO;
        }
        return TokenLexico.IDENTIFICADOR;
    }

    public static final class TokenLexico {
        public static final String PALABRA_RESERVADA = "PALABRA_RESERVADA";
        public static final String TIPO_DATO = "TIPO_DATO";
        public static final String SIMBOLO = "SIMBOLO";
        public static final String IDENTIFICADOR = "IDENTIFICADOR";
        public static final String ERROR = "ERROR";

        private final String lexema;
        private final String tipo;
        private final int linea;
        private final int columna;

        public TokenLexico(String lexema, String tipo, int linea, int columna) {
            this.lexema = Objects.requireNonNull(lexema, "El lexema no puede ser null");
            this.tipo = Objects.requireNonNull(tipo, "El tipo no puede ser null");
            if (linea < 1 || columna < 1) {
                throw new IllegalArgumentException("Linea y columna deben ser mayores que cero");
            }
            this.linea = linea;
            this.columna = columna;
        }

        public String getLexema() { return lexema; }
        public String getTipo() { return tipo; }
        public int getLinea() { return linea; }
        public int getColumna() { return columna; }

        @Override
        public String toString() {
            return lexema + " -> " + tipo + " (linea " + linea
                    + ", columna " + columna + ")";
        }
    }

    /** Demostracion opcional: se puede ejecutar este mismo archivo en NetBeans. */
    public static void main(String[] args) {
        String codigo = "public class Persona {\n"
                + "    private string nombre;\n"
                + "    public int edad1;\n"
                + "    private decimal salario;\n"
                + "}";

        System.out.printf("%-18s | %-20s | %s%n", "LEXEMA", "TIPO", "LINEA:COLUMNA");
        for (TokenLexico token : analizar(codigo)) {
            System.out.printf("%-18s | %-20s | %d:%d%n",
                    token.getLexema(), token.getTipo(),
                    token.getLinea(), token.getColumna());
        }
    }
}
