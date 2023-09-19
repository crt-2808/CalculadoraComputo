package com.example.calculadoracomputo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HelloController {

    @FXML
    private Label disp;

    private double numero1 = 0;
    private String operador = "";
    private boolean nuevaOperacion = true;

    public void initialize() {
        // Inicializa el contenido del Label
        disp.setText("");
    }

    @FXML
    private void onButtonClick(ActionEvent event) {
        if (nuevaOperacion) {
            disp.setText("");
            nuevaOperacion = false;
        }

        Button button = (Button) event.getSource();
        String buttonText = button.getText();

        // Obtiene el contenido actual del Label y agrega el número presionado
        String currentText = disp.getText();
        disp.setText(currentText + buttonText);
    }

    @FXML
    private void onOperacionClick(ActionEvent event) {
        Button button = (Button) event.getSource();
        operador = button.getText();
        numero1 = Double.parseDouble(disp.getText());
        nuevaOperacion = true;
    }

    @FXML
    private void onIgualClick(ActionEvent event) {
        if (!nuevaOperacion) {
            double numero2 = Double.parseDouble(disp.getText());
            double resultado = realizarOperacion(numero1, numero2, operador);
            disp.setText(Double.toString(resultado));
            nuevaOperacion = true;
        }
    }

    private double realizarOperacion(double num1, double num2, String operador) {
        switch (operador) {
            case "+":
                return num1 + num2;
            case "-":
                return num1 - num2;
            case "*":
                return num1 * num2;
            case "/":
                if (num2 != 0) {
                    return num1 / num2;
                } else {
                    return 0; // Manejar la división por cero
                }
            default:
                return num2;
        }
    }
}
