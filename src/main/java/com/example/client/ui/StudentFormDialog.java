package com.example.client.ui;

import com.example.client.model.Student;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;

public class StudentFormDialog extends JDialog {
    private final JTextField nameField = new JTextField(20);
    private final JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(18, 1, 150, 1));
    private final JTextField correoField = new JTextField(25);
    private Student result;

    // Precompilar regex (doble barra para el punto literal)
    private static final Pattern EMAIL_RX = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public StudentFormDialog(Frame owner, Student initial) {
        super(owner, true);
        setTitle(initial == null ? "Crear estudiante" : "Editar estudiante");
        setSize(400, 220);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridLayout(3, 2, 6, 6));
        form.add(new JLabel("Nombre:")); form.add(nameField);
        form.add(new JLabel("Edad:"));   form.add(ageSpinner);
        form.add(new JLabel("Correo:")); form.add(correoField);

        if (initial != null) {
            nameField.setText(initial.getName());
            ageSpinner.setValue(initial.getAge());
            correoField.setText(initial.getCorreo());
        }

        JButton ok = new JButton("Guardar");
        ok.addActionListener(e -> onOk());
        JButton cancel = new JButton("Cancelar");
        cancel.addActionListener(e -> { result = null; dispose(); });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(ok); south.add(cancel);

        add(form, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private void onOk() {
        String name = nameField.getText().trim();
        int age = (Integer) ageSpinner.getValue();
        String correo = correoField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre requerido");
            return;
        }
        if (!EMAIL_RX.matcher(correo).matches()) {
            JOptionPane.showMessageDialog(this, "Correo inválido");
            return;
        }

        Student s = new Student(name, age, correo);
        this.result = s;
        dispose();
    }

    public Student getResult() { return result; }
}