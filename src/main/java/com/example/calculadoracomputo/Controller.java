package com.example.calculadoracomputo;

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

public class Controller {
    @FXML Label label_resultado;
    @FXML Label label_acuses;
    @FXML Label label_id;
    @FXML Label label_conexion;
    @FXML Label label_acuses_recibidos;
    @FXML TextField operacionField;
    @FXML TextArea logger;

    //get the localhost IP address
    public InetAddress host;
    public Socket socket = null;
    public ObjectOutputStream oos = null;
    public ObjectInputStream ois = null;

    UUID cellID;
    public int min_acuses = 3;
    public Integer acuses_recibidos = 0;
    public int nodo_port = 3332;

    public boolean already_asked_clonacion = false;
    public boolean socket_connection = false;

    public void initialize() throws IOException, ClassNotFoundException, InterruptedException {
        cellID = UUID.randomUUID();
        label_resultado.setText("");

        // get the localhost IP address
        host = InetAddress.getLocalHost();

        // Attempt connection to node
        connection_thread.start();

        //Listening thread ObjectInputStream
        listening_thread.start();

    }

    Thread listening_thread = new Thread(() -> {
        // Execute on background
        while(true){
            if(socket_connection){
                String message;
                try {
                    System.out.println("[cliente] esperando respuesta... ");
                    Platform.runLater(() -> {
                        logger.appendText("[cliente] esperando respuesta... "+ "\n");
                    });

                    message = (String) ois.readObject();
                    System.out.println("[cliente] Respuesta recibida: " + message);
                    Platform.runLater(() -> {
                        logger.appendText("[cliente] Respuesta recibida: " + message + "\n");
                    });

                    String parts[] = message.split(","); // {type of message},{content},{flag},{id_hash}

                    if(parts[0].equals("resultado") && parts[3].equals(cellID.toString())){
                        acuses_recibidos++;
                        Platform.runLater(() -> {
                            label_acuses_recibidos.setText("Acuses recibidos: " + acuses_recibidos.toString());
                        });
                        Thread.sleep(10);
                        if(acuses_recibidos < min_acuses){
                            Platform.runLater(() -> {
                                label_resultado.setText("Acuses insuficientes");
                            });
                            Thread.sleep(10);

                            if(!already_asked_clonacion){
                                already_asked_clonacion = true;

                                new java.util.Timer().schedule(
                                        new java.util.TimerTask() {
                                            @Override
                                            public void run() {

                                                //NECESITO MAS SERVIDORES!!!
                                                Integer n = min_acuses - acuses_recibidos;
                                                String id_server = parts[4];

                                                try {
                                                    askMoreServers(id_server, n);
                                                } catch (ClassNotFoundException | IOException
                                                         | InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        },
                                        1500
                                );


                            }




                        }
                    }

                    if(parts[0].equals("resultado") && acuses_recibidos >= min_acuses){
                        if(parts[3].equals(cellID.toString())){
                            Platform.runLater(() -> {
                                label_resultado.setText(parts[1]);
                            });
                            Thread.sleep(10);
                        }
                    }

                } catch (ClassNotFoundException | IOException e) {
                    //e.printStackTrace();
                    //Platform.runLater(() -> {
                    //    label_resultado.setText("Sin conexión");
                    //});
                    socket_connection = false;

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    });

    Thread connection_thread = new Thread(() -> {

        while(true) {
            if(!socket_connection)
            {
                try {
                    System.out.println("[cliente] Buscando conexión con nodo...");
                    Platform.runLater(() -> {
                        logger.appendText("[cliente] Buscando conexión con nodo..." + "\n");
                    });

                    nodo_port = getRandomNumber(3000,3100);
                    socket = new Socket(host.getHostName(), nodo_port);
                    System.out.println("[cliente] Conexion establecida con nodo: " + Integer.toString(nodo_port));
                    logger.appendText("[cliente] Conexion establecida con nodo: " + Integer.toString(nodo_port) + "\n");

                    //Objects OI Stream
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    ois = new ObjectInputStream(socket.getInputStream());

                    //Indicate that this is a client
                    oos.writeObject("tipo-cliente");
                    String message = (String) ois.readObject();
                    System.out.println("[cliente] mensaje del nodo recibido: " + message);
                    Platform.runLater(() -> {
                        logger.appendText("[cliente] mensaje del nodo recibido: " + message + "\n");
                    });

                    // Set calc info
                    Platform.runLater(() -> {
                        label_acuses.setText("Min Acuses: " + Integer.toString(min_acuses));
                        label_id.setText("Id: " + cellID.toString());
                        label_conexion.setText("Socket: " + Integer.toString(nodo_port));
                    });

                    socket_connection = true;

                }
                catch(Exception e){
                    //nothing happens
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    });

    private void askMoreServers(String idServer, Integer n) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
        System.out.println("[cliente] Enviando datos al nodo: " + "clonacion,"+n+",0,"+cellID+","+idServer);
        logger.appendText("[cliente] Enviando datos al nodo: " + "clonacion,"+n+",0,"+cellID+","+idServer+ "\n");
        oos.writeObject("clonacion,"+n+",0,"+cellID+","+idServer);
    }


    @FXML
    private void sendString(ActionEvent event) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {
        event.consume();
        //Get text from textfield
        already_asked_clonacion = false;

        String operacion = operacionField.getText();

        //operacion should not contain other symbols than */+- and numbers
        if(!operacion.matches(".*[a-zA-Z].*")){
            acuses_recibidos = 0;

            // write to socket using ObjectOutputStream
            System.out.println("[cliente] Enviando datos al nodo: " + "operacion,"+operacion+",0,"+cellID+",idServer");
            logger.appendText("[cliente] Enviando datos al nodo: " + "operacion,"+operacion+",0,"+cellID+",idServer"+ "\n");
            // {type of message},{content},{flag},{id_hash},{id_server}
            oos.writeObject("operacion,"+operacion+",0,"+cellID+",idServer");

        }
        else {
            label_resultado.setText("Error en la expresión");
        }

    }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

}





