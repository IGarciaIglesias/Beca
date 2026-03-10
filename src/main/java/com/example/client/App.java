
package com.example.client;

import com.example.client.ui.MainWindow;

public class App {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
