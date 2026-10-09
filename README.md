# Analizador léxico de clases C#

Proyecto del curso **Autómatas y Lenguajes Formales** — Universidad Mariano Gálvez de Guatemala, Campus Villa Nueva.

El programa lee la definición de una clase en C# y, al presionar **Analizar**, muestra:

1. **Tabla de tokens**: cada lexema reconocido con la descripción de su patrón.
2. **Nombre de la clase** definida.
3. **Tabla de atributos**: accesibilidad, nombre y tipo (Texto / Entero / Decimal).

## Equipo

| Integrante | Módulo | Archivos |
|---|---|---|
| Alex Jose Enrique Rodriguez Portillo | Motor léxico (autómata) | `MotorLexico.java` |
| Diego Andres Pirir Rosil | Tabla de tokens | `Token.java`, `TablaTokens.java` |
| Jose Andres Morales Culajay | Nombre de clase y tabla de atributos | `AnalizadorClase.java`, `Atributo.java` |
| Julio Javier Ramírez Xicón | Interfaz gráfica (GUI) | *pendiente* |
| Kevin Eduardo Tzoc Castro | Integración, pruebas y documentación | `Analizador.java`, `ResultadoAnalisis.java`, `pruebas/`, README |

## Lenguaje reconocido

| Categoría | Lexemas |
|---|---|
| Palabras reservadas | `class` `public` `private` `get` `set` |
| Tipos de dato | `string` `int` `decimal` |
| Símbolos | `{` `}` `(` `)` `;` |
| Identificadores | `L(L|_|D)*` con L = `[a-zA-Z]`, D = `[0-9]` |

Los espacios, tabuladores y saltos de línea (`\n` y `\r\n`) separan tokens y se ignoran.
Cualquier otro carácter (`#`, `=`, `ñ`, `á`, etc.) o un identificador que empiece con dígito o `_` es un **error léxico**.

### Autómata de identificadores (MotorLexico)

```
              L                 L | D | _
  INICIO ───────────► IDENTIFICADOR ◄──┐
     │                     │   └───────┘
     │ D | _               │ otro carácter → emite token
     ▼                     ▼  (palabra reservada, tipo o identificador)
  IDENTIFICADOR_INVALIDO ──► emite ERROR
     ▲   └──┐
     └──────┘ L | D | _
```

### Estructura de la clase (AnalizadorClase)

```
clase    ::= 'class' ID '{' miembro* '}'
miembro  ::= acceso tipo ID cuerpo                 atributo
           | acceso ID '(' ')' '{' '}'             constructor (ID = nombre de la clase)
cuerpo   ::= '{' accesor accesor? '}'  |  ';'
accesor  ::= ('get' | 'set') ';'                   sin repetir
acceso   ::= 'public' | 'private'
tipo     ::= 'string' | 'int' | 'decimal'
```

Ejemplo válido (el del enunciado):

```csharp
class NombreClase
{
    private string nombre_atributo {get;set;}
    public int nombre_atributo_1 {get;set;}
    private decimal nombre_atributo_2 {get;set;}

    public NombreClase()
    {
    }
}
```

## Flujo entre módulos

```
texto ──► MotorLexico.analizar()          lista de TokenLexico (lexema, tipo, línea, columna)
            │
            ├──► TablaTokens.construirDesdeTipo()   tabla Lexema | Patrón
            │
            └──► AnalizadorClase.analizar()          nombre de clase + atributos
                       (solo si no hay errores léxicos)
```

`Analizador.analizar(texto)` hace todo el flujo y devuelve un `ResultadoAnalisis`.
**Es lo único que la GUI necesita llamar.**

### Errores

- **Léxicos**: se reportan todos, con línea y columna. La tabla de tokens se muestra igual (los lexemas inválidos aparecen como *Error léxico*), pero no se determina el nombre ni los atributos.
- **De estructura**: se reporta el primero, con línea y columna. Se conservan el nombre y los atributos leídos antes del error.

## Para la GUI (Julio)

```java
ResultadoAnalisis r = Analizador.analizar(areaTexto.getText());

modeloTokens.setDataVector(r.filasTokens(), ResultadoAnalisis.COLUMNAS_TOKENS);
lblNombreClase.setText(r.getNombreClase() != null ? r.getNombreClase() : "—");
modeloAtributos.setDataVector(r.filasAtributos(), ResultadoAnalisis.COLUMNAS_ATRIBUTOS);

if (!r.esValido()) {
    areaErrores.setText(String.join("\n", r.getErrores()));
}
```

Cuando la ventana esté lista, cambiar `exec.mainClass` en `pom.xml` a la clase de la ventana.

## Cómo ejecutar

Requisitos: **JDK 17 o superior**. Es un proyecto Maven, así que en NetBeans se abre con *File → Open Project* y se selecciona la carpeta.

| Qué | En NetBeans | Desde consola |
|---|---|---|
| Demo con el ejemplo del enunciado | Run Project (F6) | `java -cp target/classes com.mycompany.analizadorlexico.AnalizadorLexico` |
| Analizar un archivo | — | `java -cp target/classes com.mycompany.analizadorlexico.AnalizadorLexico ejemplos/valida.cs` |
| Batería de pruebas | Clic derecho en `pruebas/PruebasAnalizador.java` → Run File (Shift+F6) | `java -cp target/classes com.mycompany.analizadorlexico.pruebas.PruebasAnalizador` |

Sin Maven: `javac -encoding UTF-8 -d out $(find src -name "*.java")` y luego usar `-cp out`.

En `ejemplos/` hay tres archivos: una clase válida, una con errores léxicos y una con error de estructura.

## Pruebas

`PruebasAnalizador` no necesita JUnit. Tiene **63 casos** en 5 grupos:

| Grupo | Casos | Qué cubre |
|---|---|---|
| 1. Clases válidas | 11 | ejemplo del enunciado (y su texto exacto del PDF), una sola línea, clase vacía, `\r\n`, `;` en vez de `{get;set;}`, solo `get`, `_` y dígitos internos, tabs |
| 2. Identificadores inválidos | 11 | empieza con dígito o `_`, tildes, `ñ`, letras griegas, dígitos no ASCII, guion, solo dígitos |
| 3. Caracteres no reconocidos | 5 | `#` `=` `[]` `.` comillas |
| 4. Estructura inválida | 26 | falta `class`/nombre/llaves, tipos `float`/`bool`, sin modificador, `protected`, accesores mal escritos o repetidos, atributo duplicado, constructor con otro nombre, texto después de la clase, posición del error |
| 5. Tabla de tokens | 10 | descripciones de cada patrón, errores en la tabla, línea y columna, filas para JTable |

Resultado actual: **63/63 aprobadas.**
Corridas contra las versiones originales de los módulos, fallan 7 (ver historial de cambios).

## Historial de cambios de integración

Los módulos se subieron primero **tal como se entregaron** (primer commit) y los cambios van en commits separados para que cada quien vea qué se tocó de su parte:

- **MotorLexico**: L y D limitados a ASCII. `Character.isLetter`/`isDigit` aceptaban `Año`, `válido`, `αβ` y `x٣` como identificadores.
- **TablaTokens**: se agregaron los tipos `TIPO_DATO` y `ERROR` que manda el motor; antes salían como "Patrón desconocido.". Se agregó la línea `package`.
- **AnalizadorClase**: reescrito para recorrer los tokens del motor en lugar de usar expresiones regulares sobre el texto. La versión con regex aceptaba `1Mala`, `2abc`, `_x`, `subclass Foo` y atributos fuera de la clase.
- **AnalizadorLexico** (consola): ahora usa todo el flujo integrado y lee el texto completo (antes solo leía una línea).
- `Main.java` de Diego (prueba con lista fija de tokens) no se incluyó: lo cubre la batería de pruebas.

## Decisiones de diseño

- `L = {a-z, A-Z}` y `D = {0-9}`, como define el enunciado: `ñ`, letras con tilde y dígitos no ASCII son error léxico.
- Se aceptan atributos con `{ get; set; }` (como en el enunciado), solo `{ get; }` / `{ set; }`, o terminados en `;`.
- Si hay errores léxicos se muestra la tabla de tokens completa, pero no se determina el nombre ni los atributos.
- Las columnas de las tablas usan los encabezados del enunciado: `LEXEMA | PATRÓN` y `ACCESIBILIDAD | NOMBRE ATRIBUTO | TIPO DATO`.

## Fechas

| Fase | Fecha | Contenido |
|---|---|---|
| 1 | 10 de octubre de 2026 | Motor léxico, nombre/atributos y tabla de tokens funcionando |
| 2 | 24 de octubre de 2026 | GUI completa, integración, pruebas y entrega final |
