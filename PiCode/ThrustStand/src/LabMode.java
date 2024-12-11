/*  Title:  LabMode.Java
 *  Author: Evan Schober
 *  Email:  evan.e.schober@wmich.edu
 *  Parent Class: ThrustStand.java
 *  Description: Controller class for the lab mode 
 */
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LabMode extends BaseController{
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

    //Lab mode specific controls
    @FXML private ComboBox<Integer> rpmCombo;

    @FXML
    public void initialize(){
        serialController.setMode("LAB");
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

        serialController.addDataReceivedListener(this::onDataReceived);

        //Generate RPM values from 1000 to 30000 in increments of 1000 for combo box
        int minRPM = 1000;
        int maxRPM = 30000;
        int delta = 1000; 
        for (minRPM = 1000; minRPM <= maxRPM; minRPM += delta) {
            rpmCombo.getItems().add(minRPM);
        }
        rpmCombo.setValue(1000); // Set default value

        //Action handler for RPM selection
        rpmCombo.setOnAction(event -> {
            int selectedRPM = rpmCombo.getValue();
            serialController.setRPM(selectedRPM);
        });

        //Action handlers
        tearButton.setOnAction(e -> sharedElements.handleTearButton());
        holdToggle.setOnAction(e -> sharedElements.handleHoldToggle());
        loggingToggle.setOnAction(e -> sharedElements.handleLoggerToggle());
        motorToggle.setOnAction(event -> {
            sharedElements.handleMotorToggle();
        });

        
    }

    @FXML
    void returnToMainBtn(ActionEvent rtn){
        System.out.println("Returning to launcher");
        try {
            // Turn off motor before returning
            if (motorToggle.isSelected()) {
                serialController.setMotor(false);
                motorToggle.setSelected(false);
            }
            thrustStand.changeScene("fxml/Launcher.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void onDataReceived(String data) {
        serialController.parseData(data); //Send to serial controller parsing function
    }

}
