package com.example.calculadoracomputo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Nodo extends Application{
    // JAVAFX VARS
    public Label label;
    public Label label_puerto;
    public StackPane pane;
    public TextArea logger;

    // Logic VARS
    public static InetAddress host;
    private static ServerSocket nodo_socket;
    private static int port = 3332;

    public static ArrayList<Socket> active_connections = new ArrayList<Socket>();
    public static ArrayList<ObjectOutputStream> active_output_streams = new ArrayList<ObjectOutputStream>();

    public static int port_min = 3000;
    public static int port_max = 3100;

    // Main Thread
    public static void main(String[] args) throws ClassNotFoundException
    {
        launch(); //JAVAFX
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("NODO");

        label_puerto = new Label("Socket PORT: XXXX");
        label_puerto.setTranslateX(-50);
        label_puerto.setTranslateY(-100);
        label_puerto.setTextFill(Color.WHITE);
        label_puerto.setFont(new Font(20));

        logger = new TextArea();
        logger.setTranslateX(0);
        logger.setTranslateY(100);
        logger.setStyle("-fx-control-inner-background:GRAY; -fx-text-fill: white ;");

        //Change color
        pane = new StackPane(label_puerto, logger);
        pane.setStyle("-fx-background-color:GRAY");

        Scene scene = new Scene(pane, 300, 300);
        stage.setScene(scene);
        stage.show();

        // Iniciar Lógica del server
        t.start();


    }

    Thread t = new Thread(() -> {

        ServerSocket nodo_server_socket = null;

        // Find other NODES that are already running
        for (int i = port_min; i < port_max; i++) {
            try {
                //Create socket
                host = InetAddress.getLocalHost();
                Socket s = new Socket(host.getHostName(), i);


                //Indicate that this is a node searching for a free port
                ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                oos.writeObject("tipo-nodo");

                ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                System.out.println("Conexión establecida con Nodo: " + i + " | (Mensaje): " + ois.readObject());


                Platform.runLater(() -> {
                    logger.appendText("Conexión establecida con Nodo " + "\n");
                });
                // create handler
                NodeHandler nodoSock = new NodeHandler(s, oos, ois, logger);
                new Thread(nodoSock).start();


            } catch(ConnectException e) {
                //If connection was not possible the port is available
                System.out.println("Puerto disponible encontrado: " + i);
                Platform.runLater(() -> {
                    logger.appendText("Puerto disponible encontrado " + Integer.toString(port) + "\n");
                });
                try {
                    //Create a server socket to listen this port
                    nodo_server_socket = new ServerSocket(i);
                    port = i;
                    Platform.runLater(() -> {
                        label_puerto.setText("Socket PORT: " + Integer.toString(port));
                    });
                    break;
                } catch (IOException ex) {
                    System.out.println(ex);
                }
                break;
            } catch (Exception e) {
                System.out.println(e);
            }

        }

        try {

            // cuando encontró un socket disponible para ofertar conexión
            nodo_socket = nodo_server_socket;
            nodo_socket.setReuseAddress(true);

            // ---- HANDLE Connections to this node ----
            while (true) {

                if(active_connections.isEmpty()){
                    System.out.println("[nodo] Esperando conexiones en el puerto: " + Integer.toString(port));
                    Platform.runLater(() -> {
                        logger.appendText("[nodo] Esperando conexiones en el puerto: " + Integer.toString(port) + "\n");
                    });
                }
                else {
                    System.out.println("[nodo] Conexiones actuales: " + "("+ Integer.toString(active_connections.size()) +") " + active_connections);
                    Platform.runLater(() -> {
                        logger.appendText("[nodo] Conexiones actuales: " + "("+ Integer.toString(active_connections.size()) + "\n");
                    });
                }

                // socket object to receive incoming client requests
                System.out.println("[nodo] Esperando conexiones en el puerto: " + Integer.toString(port));
                Platform.runLater(() -> {
                    logger.appendText("[nodo] Esperando conexiones en el puerto: " + Integer.toString(port) + "\n");
                });
                Socket connection = nodo_socket.accept();

                // Respond to this connection
                ObjectOutputStream temp_oos = new ObjectOutputStream(connection.getOutputStream());
                ObjectInputStream temp_ois = new ObjectInputStream(connection.getInputStream());

                String recieved_msg = (String) temp_ois.readObject();

                if(recieved_msg.equals("tipo-nodo")){
                    temp_oos.writeObject("quehubo bro");
                    System.out.println("[nodo] Nueva conexion nodo: " + connection.getRemoteSocketAddress());
                    Platform.runLater(() -> {
                        logger.appendText("[nodo] Nueva conexion nodo: " + connection.getRemoteSocketAddress() + "\n");
                    });
                    // Handle connection with another node
                    NodeHandler nodoSock = new NodeHandler(connection, temp_oos, temp_ois, logger);

                    new Thread(nodoSock).start();
                }
                else{
                    temp_oos.writeObject("Hola soy un nodo");
                    System.out.println("[nodo] Nueva conexion cliente/server: " + connection.getRemoteSocketAddress());
                    Platform.runLater(() -> {
                        logger.appendText("[nodo] Nueva conexion cliente/server: " + connection.getRemoteSocketAddress() + "\n");
                    });
                    // create a new thread object
                    NodeHandler clientSock = new NodeHandler(connection, temp_oos, temp_ois, logger);

                    // This thread will handle the client
                    // separately
                    new Thread(clientSock).start();
                }

            }
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            if (nodo_socket != null) {
                try {
                    nodo_socket.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    });
}
