package com.mycompany.analizadorlexico;

import com.mycompany.analizadorlexico.MotorLexico.TokenLexico;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Módulo "Nombre de clase y Tabla de atributos".
 *
 * Recorre la lista de tokens que entrega {@link MotorLexico#analizar(String)}
 * (ya no usa expresiones regulares sobre el texto) y verifica que la clase
 * tenga esta forma:
 *
 * <pre>
 * clase      ::= 'class' ID '{' miembro* '}'
 * miembro    ::= acceso tipo ID cuerpo          (atributo / propiedad)
 *              | acceso ID '(' ')' '{' '}'       (constructor, ID = nombre de la clase)
 * cuerpo     ::= '{' accesor accesor? '}'        ej. { get; set; }
 *              | ';'                             ej. private int edad;
 * accesor    ::= ('get' | 'set') ';'
 * acceso     ::= 'public' | 'private'
 * tipo       ::= 'string' | 'int' | 'decimal'
 * </pre>
 *
 * Se detiene en el primer error de estructura y lo reporta con línea y
 * columna. Los atributos leídos antes del error se conservan.
 */
public final class AnalizadorClase {

    private final List<TokenLexico> tokens;
    private int pos = 0;

    private String nombreClase;
    private final List<Atributo> atributos = new ArrayList<>();
    private final Set<String> nombresUsados = new HashSet<>();
    private String error;

    private AnalizadorClase(List<TokenLexico> tokens) {
        this.tokens = tokens;
    }

    /** Resultado del análisis de la clase. */
    public static final class Resultado {
        private final String nombreClase;
        private final List<Atributo> atributos;
        private final String error;

        Resultado(String nombreClase, List<Atributo> atributos, String error) {
            this.nombreClase = nombreClase;
            this.atributos = List.copyOf(atributos);
            this.error = error;
        }

        /** Nombre de la clase, o null si no se pudo determinar. */
        public String getNombreClase() { return nombreClase; }
        public List<Atributo> getAtributos() { return atributos; }
        /** Mensaje del primer error de estructura, o null si la clase es correcta. */
        public String getError() { return error; }
        public boolean esValida() { return error == null; }
    }

    /**
     * Analiza la estructura de la clase. Los tokens deben venir sin errores
     * léxicos (verificar antes con {@link MotorLexico#contieneErrores}).
     */
    public static Resultado analizar(List<TokenLexico> tokens) {
        Objects.requireNonNull(tokens, "La lista de tokens no puede ser null");
        AnalizadorClase a = new AnalizadorClase(tokens);
        try {
            a.clase();
        } catch (ErrorSintactico e) {
            a.error = e.getMessage();
        }
        return new Resultado(a.nombreClase, a.atributos, a.error);
    }

    // ------------------------------------------------------------------
    // Reglas de la gramática
    // ------------------------------------------------------------------

    private void clase() {
        esperarLexema("class", "la palabra reservada 'class'");
        nombreClase = esperarTipo(TokenLexico.IDENTIFICADOR, "el nombre de la clase").getLexema();
        esperarLexema("{", "'{' para abrir la clase");

        while (!fin() && !actual().getLexema().equals("}")) {
            miembro();
        }
        esperarLexema("}", "'}' para cerrar la clase");

        if (!fin()) {
            throw error(actual(), "No se esperaba '" + actual().getLexema()
                    + "' después de cerrar la clase");
        }
    }

    private void miembro() {
        TokenLexico acceso = actual();
        if (!acceso.getLexema().equals("public") && !acceso.getLexema().equals("private")) {
            throw error(acceso, "Se esperaba 'public' o 'private' pero se encontró '"
                    + acceso.getLexema() + "'");
        }
        avanzar();

        TokenLexico siguiente = requerirToken("un tipo de dato (string, int, decimal) o el constructor");

        if (TokenLexico.TIPO_DATO.equals(siguiente.getTipo())) {
            avanzar();
            TokenLexico nombre = esperarTipo(TokenLexico.IDENTIFICADOR, "el nombre del atributo");
            cuerpoAtributo();
            if (!nombresUsados.add(nombre.getLexema())) {
                throw error(nombre, "El atributo '" + nombre.getLexema() + "' está declarado más de una vez");
            }
            atributos.add(new Atributo(traducirAcceso(acceso.getLexema()),
                    nombre.getLexema(), traducirTipo(siguiente.getLexema())));

        } else if (TokenLexico.IDENTIFICADOR.equals(siguiente.getTipo())
                && siguiente.getLexema().equals(nombreClase)
                && hayDespues("(")) {
            // Constructor: public NombreClase() { }
            avanzar();
            esperarLexema("(", "'('");
            esperarLexema(")", "')'");
            esperarLexema("{", "'{'");
            esperarLexema("}", "'}' para cerrar el constructor");

        } else if (TokenLexico.IDENTIFICADOR.equals(siguiente.getTipo()) && hayDespues("(")) {
            throw error(siguiente, "El constructor debe llamarse igual que la clase ('"
                    + nombreClase + "'), no '" + siguiente.getLexema() + "'");
        } else {
            throw error(siguiente, "Tipo de dato no válido: '" + siguiente.getLexema()
                    + "'. Solo se permiten string, int y decimal");
        }
    }

    private void cuerpoAtributo() {
        TokenLexico t = requerirToken("'{' o ';'");
        if (t.getLexema().equals(";")) {
            avanzar();
            return;
        }
        esperarLexema("{", "'{ get; set; }' o ';'");
        Set<String> accesores = new HashSet<>();
        do {
            TokenLexico acc = requerirToken("'get' o 'set'");
            if (!acc.getLexema().equals("get") && !acc.getLexema().equals("set")) {
                throw error(acc, "Se esperaba 'get' o 'set' pero se encontró '" + acc.getLexema() + "'");
            }
            if (!accesores.add(acc.getLexema())) {
                throw error(acc, "El accesor '" + acc.getLexema() + "' está repetido");
            }
            avanzar();
            esperarLexema(";", "';' después de '" + acc.getLexema() + "'");
        } while (!fin() && !actual().getLexema().equals("}"));
        esperarLexema("}", "'}' para cerrar los accesores");
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    private boolean fin() {
        return pos >= tokens.size();
    }

    private TokenLexico actual() {
        return tokens.get(pos);
    }

    private void avanzar() {
        pos++;
    }

    private boolean hayDespues(String lexema) {
        return pos + 1 < tokens.size() && tokens.get(pos + 1).getLexema().equals(lexema);
    }

    private TokenLexico requerirToken(String descripcion) {
        if (fin()) {
            throw new ErrorSintactico("Fin del texto: se esperaba " + descripcion);
        }
        return actual();
    }

    private TokenLexico esperarLexema(String lexema, String descripcion) {
        TokenLexico t = requerirToken(descripcion);
        if (!t.getLexema().equals(lexema)) {
            throw error(t, "Se esperaba " + descripcion + " pero se encontró '" + t.getLexema() + "'");
        }
        avanzar();
        return t;
    }

    private TokenLexico esperarTipo(String tipo, String descripcion) {
        TokenLexico t = requerirToken(descripcion);
        if (!tipo.equals(t.getTipo())) {
            throw error(t, "Se esperaba " + descripcion + " pero se encontró '" + t.getLexema() + "'");
        }
        avanzar();
        return t;
    }

    private static ErrorSintactico error(TokenLexico t, String mensaje) {
        return new ErrorSintactico("Línea " + t.getLinea() + ", columna " + t.getColumna() + ": " + mensaje);
    }

    private static String traducirAcceso(String acceso) {
        return acceso.equals("private") ? "Privado" : "Público";
    }

    private static String traducirTipo(String tipo) {
        return switch (tipo) {
            case "string" -> "Texto";
            case "int" -> "Entero";
            case "decimal" -> "Decimal";
            default -> throw new IllegalArgumentException("Tipo no soportado: " + tipo);
        };
    }

    private static final class ErrorSintactico extends RuntimeException {
        private static final long serialVersionUID = 1L;

        ErrorSintactico(String mensaje) {
            super(mensaje);
        }
    }
}
