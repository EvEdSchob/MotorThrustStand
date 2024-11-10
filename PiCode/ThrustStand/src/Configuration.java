/*  Title:  Configuration.Java
 *  Author: Evan Schober
 *  Email:  evan.e.schober@wmich.edu
 *  Parent Class: ThrustStand.java
 *  Description: Controller class for the configuration window. 
 *  Referenced by configuration.fxml
 */

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class Configuration extends BaseController {
    // Serial port selection
    @FXML private ComboBox<String> serialPortCombo;
    
    // Load cell calibration
    @FXML private TextField knownWeightField;
    @FXML private Button calibrateLoadCellBtn;
    @FXML private Label loadCellCalibrationLabel;
    @FXML private ComboBox<String> weightUnitCombo;
    
    // Pitot sensor calibration
    @FXML private TextField incomingKnownSpeedField;
    @FXML private TextField wakeKnownSpeedField;
    @FXML private Button calibrateIncomingBtn;
    @FXML private Button calibrateWakeBtn;
    @FXML private Label incomingCalibrationLabel;
    @FXML private Label wakeCalibrationLabel;
    @FXML private ComboBox<String> airspeedUnitCombo;
    
    // Electrical sensor calibration
    @FXML private TextField knownCurrentField;
    @FXML private TextField knownVoltageField;
    @FXML private Button calibrateCurrentBtn;
    @FXML private Button calibrateVoltageBtn;
    @FXML private Label currentCalibrationLabel;
    @FXML private Label voltageCalibrationLabel;
    
    // Control buttons
    @FXML private Button saveCalibrationBtn;
    @FXML private Button loadCalibrationBtn;
    @FXML private Button resetCalibrationBtn;
    @FXML private Button returnToMainBtn;

    // Unit conversion constants
    private static final class UnitConversion {
        // Weight conversion factors (to grams)
        static final double LB_TO_G = 453.592;
        static final double KG_TO_G = 1000.0;
        static final double N_TO_G = 101.972; // at standard gravity
        
        // Airspeed conversion factors (to m/s)
        static final double MPH_TO_MS = 0.44704;
        static final double FPS_TO_MS = 0.3048;
        static final double KPH_TO_MS = 0.277778;
    }

    @FXML
    public void initialize() {
        // Initialize serial port combo box
        serialPortCombo.getItems().addAll(serialController.getAvailablePorts());
        
        // Load cell calibration
        calibrateLoadCellBtn.setOnAction(e -> {
            try {
                double inputWeight = Double.parseDouble(knownWeightField.getText());
                // Convert to grams for internal storage
                double weightInGrams = convertToGrams(inputWeight, weightUnitCombo.getValue());
                double currentThrust = Double.parseDouble(sharedElements.thrustProperty().get());
                double calibration = weightInGrams / currentThrust;
                sharedElements.setLoadCellCalibration(calibration);
                updateCalibrationLabels();
            } catch (NumberFormatException ex) {
                showError("Please enter a valid weight value");
            }
        });

        // Initialize unit ComboBoxes
        weightUnitCombo.getItems().addAll("g", "kg", "lb", "N");
        weightUnitCombo.setValue("g");
        
        // Pitot sensor calibration
        calibrateIncomingBtn.setOnAction(e -> calibratePitotSensor(true));
        calibrateWakeBtn.setOnAction(e -> calibratePitotSensor(false));
        airspeedUnitCombo.getItems().addAll("m/s", "mph", "ft/s", "kph");
        airspeedUnitCombo.setValue("m/s");
        
        // Save/Load calibration
        saveCalibrationBtn.setOnAction(e -> {
            sharedElements.saveCalibration("calibration.txt");
            showInfo("Calibration saved successfully");
        });
        
        loadCalibrationBtn.setOnAction(e -> {
            sharedElements.loadCalibration("calibration.txt");
            updateCalibrationLabels();
            showInfo("Calibration loaded successfully");
        });
        
        resetCalibrationBtn.setOnAction(e -> {
            sharedElements.resetCalibration();
            updateCalibrationLabels();
            showInfo("Calibration reset to defaults");
        });
        
        // Load current calibration values
        updateCalibrationLabels();
    }

    private double convertToGrams(double value, String unit) {
        return switch (unit) {
            case "g" -> value;
            case "kg" -> value * UnitConversion.KG_TO_G;
            case "lb" -> value * UnitConversion.LB_TO_G;
            case "N" -> value * UnitConversion.N_TO_G;
            default -> value;
        };
    }

    private double convertToMetersPerSecond(double value, String unit) {
        return switch (unit) {
            case "m/s" -> value;
            case "mph" -> value * UnitConversion.MPH_TO_MS;
            case "ft/s" -> value * UnitConversion.FPS_TO_MS;
            case "kph" -> value * UnitConversion.KPH_TO_MS;
            default -> value;
        };
    }

    private void calibratePitotSensor(boolean isIncoming) {
        try {
            TextField field = isIncoming ? incomingKnownSpeedField : wakeKnownSpeedField;
            double inputSpeed = Double.parseDouble(field.getText());
            // Convert to m/s for internal storage
            double speedInMS = convertToMetersPerSecond(inputSpeed, airspeedUnitCombo.getValue());
            float currentVoltage;
            if (isIncoming) {
                currentVoltage = Float.parseFloat(sharedElements.incomingAirspeedProperty().get());
            } else {
                currentVoltage = Float.parseFloat(sharedElements.wakeAirspeedProperty().get());
            }
            
            sharedElements.calibratePitotSensor(isIncoming, speedInMS, currentVoltage);
            updateCalibrationLabels();
        } catch (NumberFormatException ex) {
            showError("Please enter a valid airspeed value");
        }
    }

    private void updateCalibrationLabels() {
        // Add unit information to labels
        String weightUnit = weightUnitCombo.getValue();
        String speedUnit = airspeedUnitCombo.getValue();
        
        loadCellCalibrationLabel.setText(String.format("Current calibration: %.6f (g/%s)", 
            sharedElements.getLoadCellCalibration(), weightUnit));
        incomingCalibrationLabel.setText(String.format("Current calibration: %.6f (Pa/(m/s)²)", 
            sharedElements.getIncomingPitotCalibration(), speedUnit));
        wakeCalibrationLabel.setText(String.format("Current calibration: %.6f (Pa/(m/s)²)", 
            sharedElements.getWakePitotCalibration(), speedUnit));
        currentCalibrationLabel.setText(String.format("Current calibration: %.6f (V/A)", 
            sharedElements.getCurrentSensorSensitivity()));
        voltageCalibrationLabel.setText(String.format("Current calibration: %.6f (ratio)", 
            sharedElements.getVoltageDividerRatio()));
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void returnToMainBtn(ActionEvent rtn) {
        System.out.println("Returning to launcher");
        try {
            thrustStand.changeScene("fxml/Launcher.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
