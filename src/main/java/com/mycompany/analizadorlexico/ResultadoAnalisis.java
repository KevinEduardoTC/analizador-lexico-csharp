package com.mycompany.analizadorlexico;

import com.mycompany.analizadorlexico.MotorLexico.TokenLexico;
import java.util.List;

/**
 * Todo lo que la GUI necesita mostrar después de presionar "Analizar":
 * tabla de tokens, nombre de la clase, tabla de atributos y errores.
 */
public final class ResultadoAnalisis {

    public static final String[] COLUMNAS_TOKENS = {"LEXEMA", "PATRÓN"};
    public static final String[] COLUMNAS_ATRIBUTOS = {"ACCESIBILIDAD", "NOMBRE ATRIBUTO", "TIPO DATO"};

    private final List<TokenLexico> tokens;
    private final List<Token> tablaTokens;
    private final String nombreClase;
    private final List<Atributo> atributos;
    private final List<String> errores;

    ResultadoAnalisis(List<TokenLexico> tokens, List<Token> tablaTokens, String nombreClase,
                      List<Atributo> atributos, List<String> errores) {
        this.tokens = List.copyOf(tokens);
        this.tablaTokens = List.copyOf(tablaTokens);
        this.nombreClase = nombreClase;
        this.atributos = List.copyOf(atributos);
        this.errores = List.copyOf(errores);
    }

    /** Tokens crudos del motor léxico (lexema, tipo, línea, columna). */
    public List<TokenLexico> getTokens() { return tokens; }

    /** Filas Lexema / Patrón ya descritas por TablaTokens. */
    public List<Token> getTablaTokens() { return tablaTokens; }

    /** Nombre de la clase, o null si no se pudo determinar. */
    public String getNombreClase() { return nombreClase; }

    public List<Atributo> getAtributos() { return atributos; }

    /** Errores léxicos o de estructura, con línea y columna. Vacía si todo está bien. */
    public List<String> getErrores() { return errores; }

    public boolean esValido() { return errores.isEmpty(); }

    /** Filas listas para un DefaultTableModel (columnas COLUMNAS_TOKENS). */
    public String[][] filasTokens() {
        return TablaTokens.comoFilas(tablaTokens);
    }

    /** Filas listas para un DefaultTableModel (columnas COLUMNAS_ATRIBUTOS). */
    public String[][] filasAtributos() {
        String[][] filas = new String[atributos.size()][3];
        for (int i = 0; i < atributos.size(); i++) {
            Atributo a = atributos.get(i);
            filas[i][0] = a.getAccesibilidad();
            filas[i][1] = a.getNombre();
            filas[i][2] = a.getTipo();
        }
        return filas;
    }
}
