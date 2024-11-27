/*  Title:  DynoMode.Java
 *  Author: Evan Schober
 *  Email:  evan.e.schober@wmich.edu
 *  Parent Class: ThrustStand.java
 *  Description: Controller class for the dyno mode 
 */


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
public class DynoMode extends BaseController {
    //Generic controls shared between the lab and dyno scenes
    @FXML private TextField thrustField;
    @FXML private TextField currentField;
    @FXML private TextField voltageField;
    @FXML private TextField incomingAirspeedField;
    @FXML private TextField wakeAirspeedField;
    @FXML private ComboBox<String> thrustUnitCombo;
    @FXML private ComboBox<String> incomingAirspeedUnitCombo;
    @FXML private ComboBox<String> wakeAirspeedUnitCombo;
    @FXML private ComboBox<Integer> bladeCountCombo;
    @FXML private Button tearButton;
    @FXML private ToggleButton holdToggle;
    @FXML private ToggleButton motorToggle;
    @FXML private ToggleButton loggingToggle;
    @FXML private Button returnToMainBtn;

    //Dyno mode specific controls
    @FXML private Slider throttleSlider;
    @FXML private TextField rpmField;

    @FXML
    public void initialize() {
        System.out.println("DynoMode initializing...");
        
        // Initialize shared controls
        sharedElements.initializeControls(
            thrustField,
            tearButton,
            holdToggle,
            thrustUnitCombo,
            incomingAirspeedField,
            wakeAirspeedField,
            incomingAirspeedUnitCombo,
            wakeAirspeedUnitCombo,
            currentField,
            voltageField,
            bladeCountCombo,
            motorToggle,
            loggingToggle
        );

        // Set mode to DYNO
        serialController.sendData("MODE:DYNO\n");
        
        // Add throttle slider listener
        throttleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (motorToggle.isSelected()) {
                String cmd = String.format("THR:%d\n", newVal.intValue());
                System.out.println("Sending: " + cmd);
                serialController.sendData(cmd);
            }
        });
        
        // Motor toggle listener
        motorToggle.setOnAction(event -> {
            String cmd = motorToggle.isSelected() ? "MOTO:ON\n" : "MOTO:OFF\n";
            System.out.println("Sending: " + cmd);
            serialController.sendData(cmd);
        });

        // Add data received listener for telemetry
        serialController.addDataReceivedListener(this::onDataReceived);

        // Other control handlers
        tearButton.setOnAction(e -> sharedElements.handleTearButton());
        holdToggle.setOnAction(e -> sharedElements.handleHoldToggle());
        loggingToggle.setOnAction(e -> sharedElements.handleLoggerToggle());
        
        bladeCountCombo.setOnAction(event -> {
            int blades = bladeCountCombo.getValue();
            serialController.sendData("BLAD:" + blades + "\n");
        });
    }

    private void onDataReceived(String data) {
        serialController.parseData(data);
    }

    @FXML
    void returnToMainBtn() {
        System.out.println("Returning to launcher");
        try {
            // Turn off motor before returning
            if (motorToggle.isSelected()) {
                serialController.sendData("MOTO:OFF\n");
                motorToggle.setSelected(false);
            }
            thrustStand.changeScene("fxml/Launcher.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}