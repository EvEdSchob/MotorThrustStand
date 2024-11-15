/*  Title:  DynoMode.Java
 *  Author: Evan Schober
 *  Email:  evan.e.schober@wmich.edu
 *  Parent Class: ThrustStand.java
 *  Description: Controller class for the dyno mode 
 */
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class DynoMode extends BaseController{
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
    public void initialize(){
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

        //Add action handlers
        tearButton.setOnAction(e -> sharedElements.handleTearButton());
        holdToggle.setOnAction(e -> sharedElements.handleHoldToggle());
        loggingToggle.setOnAction(e -> sharedElements.handleLoggerToggle());
        motorToggle.setOnAction(event -> {
        boolean isSelected = motorToggle.isSelected();
        serialController.setMotor(isSelected);
        sharedElements.handleMotorToggle();
        });
        throttleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            //serialController.setThrottle(newVal.intValue());
        });
        bladeCountCombo.setOnAction(event -> {
            int blades = bladeCountCombo.getValue();
            serialController.setBladeCount(blades);
        });
    }

    @FXML
    void returnToMainBtn(ActionEvent rtn){
        System.out.println("Returning to launcher");
        try {
            thrustStand.changeScene("fxml/Launcher.fxml");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void onDataReceived(String data) {
        serialController.parseData(data); //Send to serial controller parsing function
    }
}
