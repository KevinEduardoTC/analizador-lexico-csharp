package com.mycompany.analizadorlexico;

import com.mycompany.analizadorlexico.MotorLexico.TokenLexico;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Punto de integración: es lo único que la GUI necesita llamar al presionar
 * "Analizar".
 *
 * <pre>
 * ResultadoAnalisis r = Analizador.analizar(areaTexto.getText());
 * modeloTokens.setDataVector(r.filasTokens(), ResultadoAnalisis.COLUMNAS_TOKENS);
 * etiquetaClase.setText(r.getNombreClase());
 * modeloAtributos.setDataVector(r.filasAtributos(), ResultadoAnalisis.COLUMNAS_ATRIBUTOS);
 * if (!r.esValido()) mostrar(r.getErrores());
 * </pre>
 *
 * Flujo: MotorLexico (Alex) -> TablaTokens (Diego) -> AnalizadorClase (Jose Andrés).
 */
public final class Analizador {

    private Analizador() { }

    public static ResultadoAnalisis analizar(String texto) {
        Objects.requireNonNull(texto, "El texto a analizar no puede ser null");

        // 1. Motor léxico
        List<TokenLexico> tokens = MotorLexico.analizar(texto);

        // 2. Tabla de tokens (siempre se muestra, aunque haya errores)
        List<Token> tabla = new ArrayList<>();
        for (TokenLexico t : tokens) {
            tabla.add(TablaTokens.construirDesdeTipo(t.getLexema(), t.getTipo()));
        }

        List<String> errores = new ArrayList<>();
        if (tokens.isEmpty()) {
            errores.add("No se ingresó ninguna clase.");
            return new ResultadoAnalisis(tokens, tabla, null, List.of(), errores);
        }

        // 3. Errores léxicos: se reportan todos y no se analiza la estructura,
        //    porque un lexema inválido no se puede clasificar como nombre/tipo.
        if (MotorLexico.contieneErrores(tokens)) {
            for (TokenLexico t : tokens) {
                if (TokenLexico.ERROR.equals(t.getTipo())) {
                    errores.add("Línea " + t.getLinea() + ", columna " + t.getColumna()
                            + ": error léxico, '" + t.getLexema() + "' no es un lexema válido"
                            + descripcionError(t.getLexema()));
                }
            }
            return new ResultadoAnalisis(tokens, tabla, null, List.of(), errores);
        }

        // 4. Nombre de clase y tabla de atributos
        AnalizadorClase.Resultado clase = AnalizadorClase.analizar(tokens);
        if (!clase.esValida()) {
            errores.add(clase.getError());
        }
        return new ResultadoAnalisis(tokens, tabla, clase.getNombreClase(),
                clase.getAtributos(), errores);
    }

    private static String descripcionError(String lexema) {
        char c = lexema.charAt(0);
        if (Character.isDigit(c) || c == '_') {
            return " (un identificador debe empezar con una letra: L(L|_|D)*)";
        }
        return "";
    }
}
