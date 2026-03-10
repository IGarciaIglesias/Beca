package com.example.client.ui;

import com.example.client.api.StudentApiClient;
import com.example.client.model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class MainWindow extends JFrame {
    private final JTable table;
    private final DefaultTableModel model;
    private final JTextField baseUrlField;
    private StudentApiClient api;

    public MainWindow() {
        super("Students - Swing Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);

        baseUrlField = new JTextField("http://localhost:8080", 30);
        JButton connectBtn = new JButton("Conectar");
        connectBtn.addActionListener(e -> {
            api = new StudentApiClient(baseUrlField.getText().trim());
            loadData();
        });

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(new JLabel("Backend:"));
        north.add(baseUrlField);
        north.add(connectBtn);

        model = new DefaultTableModel(new Object[]{"ID", "Nombre", "Edad", "Correo"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> Long.class;
                    case 2 -> Integer.class;
                    default -> String.class;
                };
            }
        };
        table = new JTable(model);

        JButton reloadBtn = new JButton("Recargar");
        reloadBtn.addActionListener(e -> loadData());

        JButton addBtn = new JButton("Crear");
        addBtn.addActionListener(e -> openForm(null));

        JButton editBtn = new JButton("Editar");
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona una fila"); return; }
            Student s = rowToStudent(row);
            openForm(s);
        });

        // Soft delete (por defecto en tu backend)
        JButton delBtn = new JButton("Borrar (soft)");
        delBtn.addActionListener(e -> deleteSelectedSoft());

        // NUEVO: Hard delete
        JButton hardDelBtn = new JButton("Borrar definitivo");
        hardDelBtn.addActionListener(e -> deleteSelectedHard());

        // NUEVO: Restaurar por ID
        JButton restoreBtn = new JButton("Restaurar por ID");
        restoreBtn.addActionListener(e -> restoreByIdPrompt());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(reloadBtn);
        south.add(addBtn);
        south.add(editBtn);
        south.add(delBtn);
        south.add(hardDelBtn);
        south.add(restoreBtn);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private Student rowToStudent(int row) {
        Student s = new Student();
        s.setId(((Number) model.getValueAt(row, 0)).longValue());
        s.setName((String) model.getValueAt(row, 1));
        s.setAge((Integer) model.getValueAt(row, 2));
        s.setCorreo((String) model.getValueAt(row, 3));
        return s;
    }

    private void openForm(Student s) {
        StudentFormDialog dlg = new StudentFormDialog(this, s);
        dlg.setVisible(true);
        if (dlg.getResult() != null) {
            try {
                if (s == null) {
                    api.create(dlg.getResult());
                } else {
                    api.update(s.getId(), dlg.getResult());
                }
                loadData();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedSoft() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona una fila"); return; }
        long id = ((Number) model.getValueAt(row, 0)).longValue();
        int opt = JOptionPane.showConfirmDialog(this,
                "¿Borrar (soft) el estudiante " + id + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                api.delete(id); // soft
                loadData();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedHard() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona una fila"); return; }
        long id = ((Number) model.getValueAt(row, 0)).longValue();
        int opt = JOptionPane.showConfirmDialog(this,
                "¿Eliminar DEFINITIVAMENTE el estudiante " + id + "?",
                "Confirmar borrado definitivo", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                api.deleteHard(id); // hard
                loadData();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void restoreByIdPrompt() {
        String input = JOptionPane.showInputDialog(this, "ID a restaurar (soft deleted):");
        if (input == null || input.isBlank()) return;

        long id;
        try {
            id = Long.parseLong(input.trim());
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "ID inválido", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            api.restore(id); // PATCH deleted=false
            JOptionPane.showMessageDialog(this, "Restaurado correctamente");
            loadData();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadData() {
        if (api == null) { JOptionPane.showMessageDialog(this, "Pulsa Conectar"); return; }
        try {
            List<Student> list = api.list();
            model.setRowCount(0);
            for (Student s : list) {
                model.addRow(new Object[]{ s.getId(), s.getName(), s.getAge(), s.getCorreo() });
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}