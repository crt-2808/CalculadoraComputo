package com.mycompany.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author ramos
 */
public class Server extends Application {
    //get the localhost IP address 
    public static InetAddress host;
    public static Socket socket = null;
    public static ObjectOutputStream oos = null;
    public static ObjectInputStream ois = null;

    //socket server port on which it will listen
    private static int nodo_port = 3332;

    // JAVAFX VARS
    public Label label;
    public Label label_puerto;
    public StackPane pane;

    // MAIN THREAD
    public static void main(String args[]) throws IOException, ClassNotFoundException, InterruptedException {
        launch(); //JAVAFX
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("SERVIDOR");
        label_puerto = new Label("Socket PORT: XXXX");
        label_puerto.setTranslateX(-50);
        label_puerto.setTranslateY(-100);
        label_puerto.setTextFill(Color.WHITE);
        label = new Label("SERVIDOR");
        label.setTextFill(Color.WHITE);
        label.setFont(new Font(30));
        //Change color
        pane = new StackPane(label, label_puerto);
        pane.setStyle("-fx-background-color:GREEN");

        Scene scene = new Scene(pane, 250, 250);
        stage.setScene(scene);
        stage.show();

        // Iniciar Lógica del server
        t.start();

    }

    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    Thread t = new Thread(() -> {

        try {
            // Execute on background
            host = InetAddress.getLocalHost();
            System.out.println("[server] Buscando conexión con nodo... ");

            boolean socket_connection = false;
            while (!socket_connection) {
                try {
                    nodo_port = getRandomNumber(3000, 3100);
                    socket = new Socket(host.getHostName(), nodo_port);
                    System.out.println("[server] Conexion establecida con nodo: " + Integer.toString(nodo_port));
                    socket_connection = true;

                    //Objects OI Stream
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    ois = new ObjectInputStream(socket.getInputStream());

                    //Indicate that this is a server
                    oos.writeObject("tipo-server");

                    Platform.runLater(() -> {
                        label_puerto.setText("Socket PORT: " + Integer.toString(nodo_port));
                    });
                } catch (Exception e) {
                    //nothing happens
                }
            }

            //keep listens indefinitely until receives 'exit' call or program terminates
            while (true) {

                System.out.println("[server] Esperando un request");

                //convert ObjectInputStream object to String
                String message = (String) ois.readObject();

                System.out.println("[server] Mensaje recibido: " + message);

                String parts[] = message.split(","); // {type of message},{content}

                if (parts[0].equals("operacion")) {
                    System.out.println("[server] Evaluando expresión recibida: " + parts[1]);

                    // solve expression from message
                    //Expression expression = new ExpressionBuilder(parts[1]).build();
                    //double result = expression.evaluate();

                    try {
                        String result = MicroServicio(parts[1]);
                        //write object to Socket

                        // {type of message},{content},{flag},{id_hash}


                        oos.writeObject("resultado," + result + ",0," + parts[3]);
                        System.out.println("[server] Resultado enviado: " + result);

                        Platform.runLater(() -> {
                            label.setText(parts[1] + "\n = \n" + result);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                //terminate the server if client sends exit request
                if (message.equalsIgnoreCase("exit")) break;
            }
            System.out.println("Shutting down Socket server!!");
            //close resources
            ois.close();
            oos.close();
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    });

    private String MicroServicio(String pOperacion) throws Exception {
        String result;
        File dir = new File("C:/Users/ramos/Downloads/Entrega2/microservicios/microservicio.jar");
        Class<?> cls = new URLClassLoader(new URL[]{dir.toURI().toURL()}).loadClass("com.mycompany.microservicio.Microservicio");
        Method subMethod = cls.getMethod("evaluar", String.class);
        Object objInstance = cls.getDeclaredConstructor().newInstance();
        result = (String) subMethod.invoke(objInstance, pOperacion);
        return result;
    }
}