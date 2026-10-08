package com.mycompany.analizadorlexico.pruebas;

import com.mycompany.analizadorlexico.Analizador;
import com.mycompany.analizadorlexico.Atributo;
import com.mycompany.analizadorlexico.MotorLexico;
import com.mycompany.analizadorlexico.MotorLexico.TokenLexico;
import com.mycompany.analizadorlexico.ResultadoAnalisis;
import com.mycompany.analizadorlexico.Token;
import java.util.List;

/**
 * Batería de pruebas del proyecto. No necesita JUnit ni dependencias:
 * en NetBeans, clic derecho sobre este archivo -> Run File (Shift+F6).
 *
 * Imprime cada caso como [OK] o [FALLA] y al final el total.
 * Termina con código 1 si alguna prueba falla.
 */
public final class PruebasAnalizador {

    private static int aprobadas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) {
        System.out.println("=== 1. Clases válidas ===");
        validas();
        System.out.println("\n=== 2. Identificadores que rompen L(L|_|D)* ===");
        identificadoresInvalidos();
        System.out.println("\n=== 3. Caracteres no reconocidos ===");
        caracteresInvalidos();
        System.out.println("\n=== 4. Errores de estructura de la clase ===");
        estructuraInvalida();
        System.out.println("\n=== 5. Tabla de tokens (patrones y posiciones) ===");
        tablaTokens();

        System.out.printf("%nResultado: %d aprobadas, %d fallidas, %d en total%n",
                aprobadas, fallidas, aprobadas + fallidas);
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------
    // 1. Clases válidas
    // ------------------------------------------------------------------
    private static void validas() {
        String enunciado = """
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
        ResultadoAnalisis r = Analizador.analizar(enunciado);
        verificarValida("V01 ejemplo del enunciado", r, "NombreClase",
                atr("Privado", "nombre_atributo", "Texto"),
                atr("Público", "nombre_atributo_1", "Entero"),
                atr("Privado", "nombre_atributo_2", "Decimal"));
        verificar("V01 cantidad de tokens = 37", r.getTokens().size() == 37,
                "se obtuvieron " + r.getTokens().size());

        verificarValida("V02 todo en una sola línea",
                Analizador.analizar("class A{private int x{get;set;}public string y{get;set;}}"), "A",
                atr("Privado", "x", "Entero"), atr("Público", "y", "Texto"));

        verificarValida("V03 clase vacía", Analizador.analizar("class Vacia { }"), "Vacia");

        verificarValida("V04 saltos de línea de Windows (\\r\\n)",
                Analizador.analizar("class A\r\n{\r\n  public decimal precio {get;set;}\r\n}\r\n"), "A",
                atr("Público", "precio", "Decimal"));

        verificarValida("V05 atributo con ';' en vez de {get;set;}",
                Analizador.analizar("class Persona { private string nombre; public int edad1; }"), "Persona",
                atr("Privado", "nombre", "Texto"), atr("Público", "edad1", "Entero"));

        verificarValida("V06 solo get / set primero",
                Analizador.analizar("class A { public int x {get;} private int y {set;get;} }"), "A",
                atr("Público", "x", "Entero"), atr("Privado", "y", "Entero"));

        verificarValida("V07 identificadores con _ y dígitos después de la primera letra",
                Analizador.analizar("class Cliente_2026 { private string a_b_1 {get;set;} public int Z9 {get;set;} }"),
                "Cliente_2026", atr("Privado", "a_b_1", "Texto"), atr("Público", "Z9", "Entero"));

        verificarValida("V08 tabuladores y espacios extra",
                Analizador.analizar("class\tA\t{\t\tprivate   int    x   {  get ;  set ; }   }"), "A",
                atr("Privado", "x", "Entero"));

        verificarValida("V09 'Class' con mayúscula como nombre (C# distingue mayúsculas)",
                Analizador.analizar("class Class { }"), "Class");
    }

    // ------------------------------------------------------------------
    // 2. Identificadores inválidos
    // ------------------------------------------------------------------
    private static void identificadoresInvalidos() {
        verificarErrorLexico("I01 nombre de clase empieza con dígito", "class 1Mala { }", "1Mala");
        verificarErrorLexico("I02 nombre de clase empieza con '_'", "class _Mala { }", "_Mala");
        verificarErrorLexico("I03 atributo empieza con dígito",
                "class A { private int 2abc {get;set;} }", "2abc");
        verificarErrorLexico("I04 atributo empieza con '_'",
                "class A { private int _x {get;set;} }", "_x");
        verificarErrorLexico("I05 letra con tilde (fuera de [a-zA-Z])",
                "class A { private int válido {get;set;} }", "á");
        verificarErrorLexico("I06 letra ñ", "class Año { }", "ñ");
        verificarErrorLexico("I07 letras griegas", "class A { private int αβ {get;set;} }", "α");
        verificarErrorLexico("I08 dígito no ASCII (٣)", "class A { private int x٣ {get;set;} }", "٣");
        verificarErrorLexico("I09 guion medio dentro del nombre",
                "class A { private int mi-atributo {get;set;} }", "-");
        verificarErrorLexico("I10 solo dígitos", "class 123 { }", "123");

        ResultadoAnalisis r = Analizador.analizar("class 1A { private int 2b {get;set;} public int _c; }");
        verificar("I11 reporta todos los errores léxicos (3)", r.getErrores().size() == 3,
                "errores: " + r.getErrores());
    }

    // ------------------------------------------------------------------
    // 3. Caracteres inválidos
    // ------------------------------------------------------------------
    private static void caracteresInvalidos() {
        verificarErrorLexico("C01 '#' entre identificadores", "class A { private int x#y; }", "#");
        verificarErrorLexico("C02 '=' (asignación no soportada)",
                "class A { private int x = 5; }", "=");
        verificarErrorLexico("C03 corchetes", "class A { private int[] x; }", "[");
        verificarErrorLexico("C04 punto", "class A { private decimal x.y; }", ".");
        verificarErrorLexico("C05 comillas", "class A { private string \"x\"; }", "\"");
    }

    // ------------------------------------------------------------------
    // 4. Estructura inválida
    // ------------------------------------------------------------------
    private static void estructuraInvalida() {
        verificarErrorEstructura("E01 texto vacío", "", "No se ingresó");
        verificarErrorEstructura("E02 solo espacios", "   \n\t ", "No se ingresó");
        verificarErrorEstructura("E03 falta 'class'", "NombreClase { }", "'class'");
        verificarErrorEstructura("E04 'subclass' no es 'class'", "subclass Foo { }", "'class'");
        verificarErrorEstructura("E05 falta el nombre de la clase", "class { }", "nombre de la clase");
        verificarErrorEstructura("E06 nombre de clase es palabra reservada", "class public { }", "nombre de la clase");
        verificarErrorEstructura("E07 falta '{' de la clase", "class A private int x; }", "'{'");
        verificarErrorEstructura("E08 falta '}' final", "class A { private int x {get;set;}", "Fin del texto");
        verificarErrorEstructura("E09 atributo después de cerrar la clase",
                "class A { private int x; } private int fuera;", "después de cerrar");
        verificarErrorEstructura("E10 tipo no soportado (float)",
                "class A { private float x {get;set;} }", "Tipo de dato no válido");
        verificarErrorEstructura("E11 tipo no soportado (bool)",
                "class A { public bool activo; }", "Tipo de dato no válido");
        verificarErrorEstructura("E12 atributo sin modificador de acceso",
                "class A { int x {get;set;} }", "'public' o 'private'");
        verificarErrorEstructura("E13 modificador no soportado (protected)",
                "class A { protected int x; }", "'public' o 'private'");
        verificarErrorEstructura("E14 falta nombre del atributo",
                "class A { private int {get;set;} }", "nombre del atributo");
        verificarErrorEstructura("E15 falta ';' después de get",
                "class A { private int x {get set;} }", "';' después de 'get'");
        verificarErrorEstructura("E16 accesor repetido",
                "class A { private int x {get;get;} }", "repetido");
        verificarErrorEstructura("E17 bloque de accesores vacío",
                "class A { private int x { } }", "'get' o 'set'");
        verificarErrorEstructura("E18 accesor no válido",
                "class A { private int x {get;put;} }", "'get' o 'set'");
        verificarErrorEstructura("E19 atributo sin ';' ni {get;set;}",
                "class A { private int x }", "'{ get; set; }' o ';'");
        verificarErrorEstructura("E20 atributo duplicado",
                "class A { private int x; public string x; }", "más de una vez");
        verificarErrorEstructura("E21 constructor con otro nombre",
                "class A { public B() { } }", "igual que la clase");
        verificarErrorEstructura("E22 constructor sin cerrar paréntesis",
                "class A { public A( { } }", "')'");
        verificarErrorEstructura("E23 dos clases seguidas",
                "class A { } class B { }", "después de cerrar");
        verificarErrorEstructura("E24 'Class' con mayúscula no es palabra reservada",
                "Class A { }", "'class'");

        ResultadoAnalisis r = Analizador.analizar("class A {\n  private int x;\n  private float y;\n}");
        verificar("E25 el error indica línea y columna correctas (3:11)",
                !r.getErrores().isEmpty() && r.getErrores().get(0).startsWith("Línea 3, columna 11"),
                "errores: " + r.getErrores());
        verificar("E26 conserva el nombre y los atributos leídos antes del error",
                "A".equals(r.getNombreClase()) && r.getAtributos().size() == 1,
                "nombre=" + r.getNombreClase() + " atributos=" + r.getAtributos().size());
    }

    // ------------------------------------------------------------------
    // 5. Tabla de tokens
    // ------------------------------------------------------------------
    private static void tablaTokens() {
        ResultadoAnalisis r = Analizador.analizar("class A { private string x {get;set;} public A() { } }");
        boolean sinDesconocidos = r.getTablaTokens().stream()
                .noneMatch(t -> t.getPatron().contains("desconocido"));
        verificar("T01 ningún token queda como 'Patrón desconocido'", sinDesconocidos, imprimir(r));

        verificar("T02 'string' se describe como tipo de dato",
                patronDe(r, "string").contains("tipo de dato"), patronDe(r, "string"));
        verificar("T03 'private' se describe como modificador de acceso",
                patronDe(r, "private").contains("modificador de acceso"), patronDe(r, "private"));
        verificar("T04 '(' se describe como paréntesis abre",
                patronDe(r, "(").equals("Símbolo paréntesis abre."), patronDe(r, "("));
        verificar("T05 'x' se describe como identificador",
                patronDe(r, "x").equals("Identificador."), patronDe(r, "x"));

        ResultadoAnalisis e = Analizador.analizar("class 1A { }");
        verificar("T06 un error léxico aparece en la tabla como error",
                patronDe(e, "1A").startsWith("Error léxico"), patronDe(e, "1A"));
        verificar("T07 con error léxico la tabla de tokens se sigue mostrando completa",
                e.getTablaTokens().size() == 4, "filas=" + e.getTablaTokens().size());

        List<TokenLexico> ts = MotorLexico.analizar("class A\n{\n  private int x;\n}");
        TokenLexico x = ts.stream().filter(t -> t.getLexema().equals("x")).findFirst().orElseThrow();
        verificar("T08 posición de 'x' = línea 3, columna 15",
                x.getLinea() == 3 && x.getColumna() == 15, x.toString());

        verificar("T09 filas para JTable tienen 2 columnas",
                r.filasTokens().length == r.getTablaTokens().size() && r.filasTokens()[0].length == 2, "");
        verificar("T10 filas de atributos tienen 3 columnas",
                r.filasAtributos().length == 1 && r.filasAtributos()[0].length == 3, "");
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------
    private static String[] atr(String acceso, String nombre, String tipo) {
        return new String[]{acceso, nombre, tipo};
    }

    private static void verificarValida(String caso, ResultadoAnalisis r, String nombre, String[]... atributos) {
        StringBuilder detalle = new StringBuilder();
        boolean ok = r.esValido();
        if (!ok) detalle.append("errores: ").append(r.getErrores()).append(". ");
        if (!nombre.equals(r.getNombreClase())) {
            ok = false;
            detalle.append("nombre esperado '").append(nombre).append("', obtenido '")
                    .append(r.getNombreClase()).append("'. ");
        }
        List<Atributo> obtenidos = r.getAtributos();
        if (obtenidos.size() != atributos.length) {
            ok = false;
            detalle.append("se esperaban ").append(atributos.length).append(" atributos, se obtuvieron ")
                    .append(obtenidos.size()).append(". ");
        } else {
            for (int i = 0; i < atributos.length; i++) {
                Atributo a = obtenidos.get(i);
                String[] e = atributos[i];
                if (!e[0].equals(a.getAccesibilidad()) || !e[1].equals(a.getNombre()) || !e[2].equals(a.getTipo())) {
                    ok = false;
                    detalle.append("atributo ").append(i + 1).append(": esperado ")
                            .append(String.join("|", e)).append(", obtenido ")
                            .append(a.getAccesibilidad()).append("|").append(a.getNombre())
                            .append("|").append(a.getTipo()).append(". ");
                }
            }
        }
        verificar(caso, ok, detalle.toString());
    }

    private static void verificarErrorLexico(String caso, String texto, String lexemaEsperado) {
        ResultadoAnalisis r = Analizador.analizar(texto);
        boolean marcado = r.getTokens().stream().anyMatch(t ->
                TokenLexico.ERROR.equals(t.getTipo()) && t.getLexema().contains(lexemaEsperado));
        boolean reportado = r.getErrores().stream().anyMatch(e -> e.contains("error léxico"));
        verificar(caso, !r.esValido() && marcado && reportado,
                "tokens=" + r.getTokens() + " errores=" + r.getErrores());
    }

    private static void verificarErrorEstructura(String caso, String texto, String fragmentoMensaje) {
        ResultadoAnalisis r = Analizador.analizar(texto);
        boolean ok = !r.esValido() && r.getErrores().stream().anyMatch(e -> e.contains(fragmentoMensaje));
        verificar(caso, ok, "errores=" + r.getErrores());
    }

    private static String patronDe(ResultadoAnalisis r, String lexema) {
        return r.getTablaTokens().stream().filter(t -> t.getLexema().equals(lexema))
                .map(Token::getPatron).findFirst().orElse("(no encontrado)");
    }

    private static String imprimir(ResultadoAnalisis r) {
        return r.getTablaTokens().toString();
    }

    private static void verificar(String caso, boolean condicion, String detalle) {
        if (condicion) {
            aprobadas++;
            System.out.println("[OK]    " + caso);
        } else {
            fallidas++;
            System.out.println("[FALLA] " + caso + "\n        -> " + detalle);
        }
    }

    private PruebasAnalizador() { }
}
