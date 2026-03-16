package com.example.demo;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

@EnableCaching
@SpringBootApplication
public class ServidorPruebaDbApplication extends JFrame {
	private JTextArea log;
	private String[] args;
	private ConfigurableApplicationContext contexto;

	public static void main(String[] args) {
		new ServidorPruebaDbApplication().iniciarInterfaz(args);
	}

	private void iniciarInterfaz(String[] args) {
		this.args = args;

		JFrame frame = new JFrame("Spring Boot");
		frame.setSize(500,350);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout(8,8));

		log = new JTextArea();
		log.setEditable(false);
		log.setFont(new Font("Monospaced", Font.PLAIN, 12));
		PrintStream printStream = new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				SwingUtilities.invokeLater(() -> log.append(String.valueOf((char) b)));
			}
		});
		System.setOut(printStream);
		System.setErr(printStream);

		JPanel botones = getJPanel();

		frame.add(botones, BorderLayout.NORTH);
		frame.add(new JScrollPane(log), BorderLayout.CENTER);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private @NonNull JPanel getJPanel() {
		JButton btnStart = new JButton("Iniciar servidor");
		JButton btnStop = new JButton("Detener servidor");

		btnStart.addActionListener(e -> {
			contexto = SpringApplication.run(ServidorPruebaDbApplication.class, this.args);
			log.append("\n¡Bienvenido!\n");
		});

		btnStop.addActionListener(e -> {
			if (contexto != null){
				contexto.close();
				log.append("\n¡Hasta la próxima!\n");
			}
		});

		JPanel botones = new JPanel(new GridLayout(1, 2, 8, 0));
		botones.add(btnStart);
		botones.add(btnStop);
		return botones;
	}

}
