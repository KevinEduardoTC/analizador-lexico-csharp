package com.mycompany.analizadorlexico;

import java.util.List;
import java.util.Map;

/**
 * Módulo "Tabla de Tokens" del proyecto de Autómatas y Lenguajes Formales.
 *
 * Responsabilidad: tomar los lexemas que entrega el motor léxico (el autómata
 * que recorre el texto de la clase) y producir la Tabla de Tokens pedida en
 * el enunciado: columnas LEXEMA | PATRÓN.
 *
 * Este módulo NO valida la gramática (eso lo hace el motor léxico); solo
 * clasifica cada lexema ya reconocido y arma su descripción.
 *
 * Contrato con el resto del equipo:
 *  - Si el motor léxico solo entrega el texto crudo del lexema -> usar clasificar(lexema).
 *  - Si el motor léxico ya entrega el tipo del token (recomendado, para no
 *    reclasificar) -> usar construirDesdeTipo(lexema, tipo), con tipo en
 *    {"PALABRA_RESERVADA", "TIPO_DATO", "SIMBOLO", "IDENTIFICADOR", "ERROR"}
 *    (los mismos valores que usa MotorLexico.TokenLexico).
 */
public class TablaTokens {

    // Descripciones de las palabras reservadas del lenguaje de la clase C#.
    // La redacción queda a criterio del estudiante (según el enunciado);
    // se puede ajustar el texto sin tocar la lógica.
    private static final Map<String, String> PALABRAS_RESERVADAS = Map.ofEntries(
            Map.entry("class", "Palabra reservada class."),
            Map.entry("private", "Palabra reservada private (modificador de acceso)."),
            Map.entry("public", "Palabra reservada public (modificador de acceso)."),
            Map.entry("string", "Palabra reservada string (tipo de dato)."),
            Map.entry("int", "Palabra reservada int (tipo de dato)."),
            Map.entry("decimal", "Palabra reservada decimal (tipo de dato)."),
            Map.entry("get", "Palabra reservada get."),
            Map.entry("set", "Palabra reservada set.")
    );

    // Descripciones de los símbolos del lenguaje.
    private static final Map<String, String> SIMBOLOS = Map.of(
            "{", "Símbolo llave abre.",
            "}", "Símbolo llave cierra.",
            "(", "Símbolo paréntesis abre.",
            ")", "Símbolo paréntesis cierra.",
            ";", "Símbolo punto y coma."
    );

    /**
     * Clasifica un lexema crudo (sin tipo) y devuelve su Token con la
     * descripción de patrón correspondiente. Útil si el motor léxico
     * todavía no entrega el tipo de cada token.
     */
    public static Token clasificar(String lexema) {
        if (PALABRAS_RESERVADAS.containsKey(lexema)) {
            return new Token(lexema, PALABRAS_RESERVADAS.get(lexema));
        }
        if (SIMBOLOS.containsKey(lexema)) {
            return new Token(lexema, SIMBOLOS.get(lexema));
        }
        // No es palabra reservada ni símbolo: por descarte, es un identificador
        // (el motor léxico ya debió validar que respeta L(L|_|D)*).
        return new Token(lexema, "Identificador.");
    }

    /**
     * Construye el Token cuando el motor léxico ya entrega el tipo
     * (evita que este módulo tenga que reclasificar el lexema).
     */
    public static Token construirDesdeTipo(String lexema, String tipo) {
        String patron = switch (tipo) {
            case "PALABRA_RESERVADA", "TIPO_DATO" ->
                    PALABRAS_RESERVADAS.getOrDefault(lexema, "Palabra reservada " + lexema + ".");
            case "SIMBOLO" ->
                    SIMBOLOS.getOrDefault(lexema, "Símbolo.");
            case "IDENTIFICADOR" -> "Identificador.";
            case "ERROR" -> "Error léxico: lexema no válido.";
            default -> "Patrón desconocido.";
        };
        return new Token(lexema, patron);
    }

    /**
     * Genera la Tabla de Tokens en texto plano alineado, lista para copiar
     * o mostrar en consola mientras no está lista la GUI.
     */
    public static String imprimir(List<Token> tokens) {
        int anchoLexema = tokens.stream()
                .mapToInt(t -> t.getLexema().length())
                .max()
                .orElse(6);
        anchoLexema = Math.max(anchoLexema, "LEXEMA".length());

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-" + anchoLexema + "s | %s%n", "LEXEMA", "PATRÓN"));
        sb.append("-".repeat(anchoLexema)).append("-+-").append("-".repeat(30)).append("\n");
        for (Token t : tokens) {
            sb.append(String.format("%-" + anchoLexema + "s | %s%n", t.getLexema(), t.getPatron()));
        }
        return sb.toString();
    }

    /**
     * Misma tabla pero como filas de datos (String[]{lexema, patron}),
     * lista para que Julio la cargue directo en un DefaultTableModel de
     * un JTable en la GUI.
     */
    public static String[][] comoFilas(List<Token> tokens) {
        String[][] filas = new String[tokens.size()][2];
        for (int i = 0; i < tokens.size(); i++) {
            filas[i][0] = tokens.get(i).getLexema();
            filas[i][1] = tokens.get(i).getPatron();
        }
        return filas;
    }
}
