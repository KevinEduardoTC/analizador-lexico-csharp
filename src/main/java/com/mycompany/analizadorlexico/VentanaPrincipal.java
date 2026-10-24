package com.mycompany.analizadorlexico;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 * Interfaz gráfica del analizador léxico de clases C#.
 *
 * Solo llama a {@link Analizador#analizar(String)} y muestra el
 * {@link ResultadoAnalisis}: tabla de tokens, nombre de la clase,
 * tabla de atributos y errores.
 */
public class VentanaPrincipal extends JFrame {

    private static final Font FUENTE_CODIGO = new Font(Font.MONOSPACED, Font.PLAIN, 14);
    private static final Color COLOR_OK = new Color(0, 120, 40);
    private static final Color COLOR_ERROR = new Color(180, 0, 0);

    private final JTextArea areaCodigo = new JTextArea();
    private final JTextArea areaErrores = new JTextArea();
    private final JLabel lblNombreClase = new JLabel("—");
    private final JLabel lblEstado = new JLabel("Escribe o abre una clase C# y presiona Analizar.");
    private final DefaultTableModel modeloTokens = crearModelo(ResultadoAnalisis.COLUMNAS_TOKENS);
    private final DefaultTableModel modeloAtributos = crearModelo(ResultadoAnalisis.COLUMNAS_ATRIBUTOS);

    public VentanaPrincipal() {
        super("Analizador léxico de clases C#");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(crearBarraBotones(), BorderLayout.NORTH);
        add(crearZonaCentral(), BorderLayout.CENTER);
        add(crearPanelErrores(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(900, 600));
        setSize(1100, 700);
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------
    // Construcción de la interfaz
    // ------------------------------------------------------------------

    private JPanel crearBarraBotones() {
        JButton btnAbrir = new JButton("Abrir archivo .cs");
        JButton btnAnalizar = new JButton("Analizar");
        JButton btnLimpiar = new JButton("Limpiar");

        btnAbrir.addActionListener(e -> abrirArchivo());
        btnAnalizar.addActionListener(e -> analizar());
        btnLimpiar.addActionListener(e -> limpiar());

        getRootPane().setDefaultButton(btnAnalizar);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.add(btnAbrir);
        barra.add(btnAnalizar);
        barra.add(btnLimpiar);
        barra.add(lblEstado);
        return barra;
    }

    private JSplitPane crearZonaCentral() {
        // Izquierda: código fuente
        areaCodigo.setFont(FUENTE_CODIGO);
        areaCodigo.setTabSize(4);
        JScrollPane scrollCodigo = new JScrollPane(areaCodigo);
        scrollCodigo.setBorder(BorderFactory.createTitledBorder("Código C#"));

        // Derecha: nombre de clase + tablas
        JPanel panelNombre = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JLabel etiqueta = new JLabel("Nombre de la clase:");
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD));
        lblNombreClase.setFont(FUENTE_CODIGO.deriveFont(Font.BOLD));
        panelNombre.add(etiqueta);
        panelNombre.add(lblNombreClase);

        JTable tablaTokens = crearTabla(modeloTokens);
        tablaTokens.getColumnModel().getColumn(0).setPreferredWidth(120);
        tablaTokens.getColumnModel().getColumn(1).setPreferredWidth(320);
        JScrollPane scrollTokens = new JScrollPane(tablaTokens);
        scrollTokens.setBorder(BorderFactory.createTitledBorder("Tabla de tokens"));

        JScrollPane scrollAtributos = new JScrollPane(crearTabla(modeloAtributos));
        scrollAtributos.setBorder(BorderFactory.createTitledBorder("Tabla de atributos"));
        scrollAtributos.setPreferredSize(new Dimension(300, 170));

        JPanel derecha = new JPanel(new BorderLayout(0, 4));
        derecha.add(panelNombre, BorderLayout.NORTH);
        derecha.add(scrollTokens, BorderLayout.CENTER);
        derecha.add(scrollAtributos, BorderLayout.SOUTH);

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollCodigo, derecha);
        division.setResizeWeight(0.35);
        division.setDividerLocation(400);
        division.setContinuousLayout(true);
        return division;
    }

    private JScrollPane crearPanelErrores() {
        areaErrores.setEditable(false);
        areaErrores.setFont(FUENTE_CODIGO.deriveFont(13f));
        areaErrores.setForeground(COLOR_ERROR);
        areaErrores.setRows(5);
        JScrollPane scroll = new JScrollPane(areaErrores);
        scroll.setBorder(BorderFactory.createTitledBorder("Errores"));
        return scroll;
    }

    private static DefaultTableModel crearModelo(String[] columnas) {
        // Celdas no editables, con sus encabezados desde el inicio.
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private static JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(22);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        return tabla;
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    private void analizar() {
        String texto = areaCodigo.getText();
        ResultadoAnalisis r = Analizador.analizar(texto);

        modeloTokens.setDataVector(r.filasTokens(), ResultadoAnalisis.COLUMNAS_TOKENS);
        modeloAtributos.setDataVector(r.filasAtributos(), ResultadoAnalisis.COLUMNAS_ATRIBUTOS);
        lblNombreClase.setText(r.getNombreClase() != null ? r.getNombreClase() : "—");

        if (r.esValido()) {
            areaErrores.setText("");
            lblEstado.setForeground(COLOR_OK);
            lblEstado.setText("Análisis correcto: " + r.getTablaTokens().size() + " tokens, "
                    + r.getAtributos().size() + " atributo(s).");
        } else {
            areaErrores.setText(String.join("\n", r.getErrores()));
            areaErrores.setCaretPosition(0);
            lblEstado.setForeground(COLOR_ERROR);
            lblEstado.setText("Se encontraron " + r.getErrores().size() + " error(es).");
        }
    }

    private void abrirArchivo() {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Abrir archivo de C#");
        selector.setFileFilter(new FileNameExtensionFilter("Archivos C# (*.cs) y texto (*.txt)", "cs", "txt"));
        File ejemplos = new File("ejemplos");
        if (ejemplos.isDirectory()) {
            selector.setCurrentDirectory(ejemplos);
        }
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            String contenido = Files.readString(selector.getSelectedFile().toPath(), StandardCharsets.UTF_8);
            areaCodigo.setText(contenido);
            areaCodigo.setCaretPosition(0);
            analizar();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo leer el archivo:\n" + ex.getMessage(),
                    "Error al abrir", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar() {
        areaCodigo.setText("");
        areaErrores.setText("");
        modeloTokens.setRowCount(0);
        modeloAtributos.setRowCount(0);
        lblNombreClase.setText("—");
        lblEstado.setForeground(UIManager.getColor("Label.foreground"));
        lblEstado.setText("Escribe o abre una clase C# y presiona Analizar.");
    }

    // ------------------------------------------------------------------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorada) {
                // Si falla, se queda con el aspecto por defecto de Swing.
            }
            new VentanaPrincipal().setVisible(true);
        });
    }
}
