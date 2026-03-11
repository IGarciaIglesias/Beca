package org.example;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.example.CRUD.Read.*;

public class PanelDeGestion extends JFrame{
    ArrayList<String> datos = new ArrayList<>();
    private JTable tabla;
    private DefaultTableModel modelo;
    public PanelDeGestion() throws IOException, InterruptedException {
        JFrame panelDeGestion = new JFrame();
        panelDeGestion.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        panelDeGestion.setSize(600, 400);

        modelo = new DefaultTableModel() {};

        tabla = new JTable(modelo);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        /*JPanel panel = new JPanel();
        JButton btn1 = new JButton();
        JButton btn2 = new JButton();

        btn1.addActionListener(e -> {
            try {
                cargar(mostrarUsuariosBorrados());
            } catch (IOException | InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
        btn2.addActionListener(e -> {
            try {
                cargar(mostrarUsuariosNoBorrados());
            } catch (IOException | InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        panel.add(btn1);
        panel.add(btn2);

        add(panel, BorderLayout.SOUTH);*/

        cargar(mostrarTodosLosUsuario());
    }

    public static boolean columnaExiste(JTable table, String nombreColumna) {
        if (table == null || nombreColumna == null) {
            System.out.println("Tabla o columna no encontrada");
            return false;
        }
        TableColumnModel columnModel = table.getColumnModel();
        System.out.println(columnModel);
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            String colName = columnModel.getColumn(i).getHeaderValue().toString();
            if (nombreColumna.equalsIgnoreCase(colName)) {
                System.out.println("Columna creada correctamente");
                return true; // Encontrada
            }
        }
        return false; // No encontrada
    }

    private void cargar(String json){
        Vector<Object> data = new Vector<>();

        if(tabla == null){
            System.out.println("Tabla no encontrada");
            return;
        } else {
            System.out.println("Tabla encontrada");
        }
        json = json.replaceAll("\\[|\\]|\\{|\\}|\"", " ");
        System.out.println(json);
        json = json.replaceAll(" ", "");
        System.out.println(json);
        String[] partes = json.split(",");
        for(String parte : partes){
            String[] dividirDatos = parte.split(":");
            datos.clear();
            datos.addAll(Arrays.asList(dividirDatos));
            System.out.println(datos);
            for(int i = 0; i < datos.size(); i++){
                if(i == 0){
                    System.out.println("Columna: " + datos.get(i));
                    String columnaBuscada = datos.get(i);
                    System.out.println("Columna buscada: " + columnaBuscada);
                    if (!columnaExiste(tabla, columnaBuscada)) {
                        System.out.println("no se pudo crear la columna");
                        continue;
                    }
                    modelo.addColumn(datos.get(i));
                    int num = tabla.getColumnCount();
                    System.out.println(num);
                } else if(i == 1){
                    System.out.println("Contenido: " + datos.get(i));
                    data.add(datos.get(i));
                }
            }
        }
    }
}