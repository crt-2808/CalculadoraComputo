/*
package com.example.calculadoracomputo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

 */
package com.example.calculadoracomputo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

public class Main extends Application {

    @FXML Label label_resultado;
    @FXML Label label_acuses;
    @FXML Label label_id;
    @FXML Label label_conexion;
    @FXML Label label_acuses_recibidos;
    @FXML TextField operacionField;
    @FXML TextArea logger;

    public InetAddress host;
    public Socket socket = null;
    public ObjectOutputStream oos = null;
    public ObjectInputStream ois = null;

    UUID cellID;
    public int min_acuses = 3;
    public Integer acuses_recibidos = 0;
    public int nodo_port = 3332;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("cliente.fxml"));
        primaryStage.setTitle("Calculadora");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
        initialize();
    }

    public void initialize() {
        cellID = UUID.randomUUID();
        label_resultado.setText("");
        try {
            host = InetAddress.getLocalHost();
            System.out.println("[cliente] Buscando conexión con nodo...");
            logger.appendText("[cliente] Buscando conexión con nodo..." + "\n");

            boolean socket_connection = false;
            while (!socket_connection) {
                try {
                    nodo_port = getRandomNumber(3000, 3100);
                    socket = new Socket(host.getHostName(), nodo_port);
                    System.out.println("[cliente] Conexion establecida con nodo: " + Integer.toString(nodo_port));
                    logger.appendText("[cliente] Conexion establecida con nodo: " + Integer.toString(nodo_port) + "\n");
                    socket_connection = true;

                    oos = new ObjectOutputStream(socket.getOutputStream());
                    ois = new ObjectInputStream(socket.getInputStream());

                    oos.writeObject("tipo-cliente");
                    String message = (String) ois.readObject();
                    System.out.println("[cliente] mensaje del nodo recibido: " + message);
                    logger.appendText("[cliente] mensaje del nodo recibido: " + message + "\n");

                    t.start();

                    label_acuses.setText("Min Acuses: " + Integer.toString(min_acuses));
                    label_id.setText("Id: " + cellID.toString());
                    label_conexion.setText("Socket: " + Integer.toString(nodo_port));
                } catch (Exception e) {
                    // Nothing happens
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sendString(ActionEvent event) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
        event.consume();
        String operacion = operacionField.getText();
        if (!operacion.matches(".*[a-zA-Z].*")) {
            acuses_recibidos = 0;
            System.out.println("[cliente] Enviando datos al nodo: " + "operacion," + operacion + ",0," + cellID);
            logger.appendText("[cliente] Enviando datos al nodo: " + "operacion," + operacion + ",0," + cellID + "\n");
            oos.writeObject("operacion," + operacion + ",0," + cellID);
        } else {
            label_resultado.setText("Error en la expresión");
        }
    }

    Thread t = new Thread(() -> {
        while (true) {
            String message;
            try {
                System.out.println("[cliente] esperando respuesta... ");
                Platform.runLater(() -> {
                    logger.appendText("[cliente] esperando respuesta... " + "\n");
                });
                message = (String) ois.readObject();
                System.out.println("[cliente] Respuesta recibida: " + message);
                Platform.runLater(() -> {
                    logger.appendText("[cliente] Respuesta recibida: " + message + "\n");
                });

                String parts[] = message.split(",");
                if (parts[0].equals("resultado") && parts[3].equals(cellID.toString())) {
                    acuses_recibidos++;
                    Platform.runLater(() -> {
                        label_acuses_recibidos.setText("Acuses recibidos: " + acuses_recibidos.toString());
                    });
                    Thread.sleep(10);
                }

                if (parts[0].equals("resultado") && acuses_recibidos >= 3) {
                    if (parts[3].equals(cellID.toString())) {
                        Platform.runLater(() -> {
                            label_resultado.setText(parts[1]);
                        });
                        Thread.sleep(10);
                    }
                }
            } catch (ClassNotFoundException | IOException | InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    label_resultado.setText("Sin conexión");
                });
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    });

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}
