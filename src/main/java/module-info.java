module com.example.calculadoracomputo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.calculadoracomputo to javafx.fxml;
    exports com.example.calculadoracomputo;
}