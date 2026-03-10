package org.example;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.example.CRUD.Read.*;

public class PanelDeGestion extends JFrame{

    private JTable tabla;
    private DefaultTableModel modelo;
    public PanelDeGestion() throws IOException, InterruptedException {
        JFrame panelDeGestion = new JFrame();
        panelDeGestion.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        panelDeGestion.setSize(600, 400);

        modelo = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c){
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(26);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panel = new JPanel();
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

        add(panel, BorderLayout.SOUTH);

        cargar(mostrarTodosLosUsuario());
    }

    private void cargar(String json){
        json = json.replaceAll("\\[|\\]|\\{|\\}|\"", " ");
        json = json.replaceAll(" ", "");
        String[] partes = json.split(",");
        for(String parte : partes){
            System.out.println(parte);
        }
    }
}