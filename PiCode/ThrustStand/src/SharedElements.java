/*  Title:  SharedElements.Java
 *  Author: Evan Schober
 *  Email:  evan.e.schober@wmich.edu
 *  Parent Class: ThrustStand.java
 *  Description: Class used to define and managed shared resources between various scene controllers
 */
import javafx.scene.control.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javafx.beans.property.*;

public class SharedElements{
    private static SharedElements instance;

    //Simplifies calibration constant handling
    private static final class CalibrationConstants {
        private double loadCellCalibration = 1.0;
        private double incomingPitotCalibration = 1.0;  // Changed from pitot1
        private double wakePitotCalibration = 1.0;      // Changed from pitot2
        private double currentSensorSensitivity = 0.185;
        private double voltageDividerRatio = 0.1;
    }
    private final CalibrationConstants calibration = new CalibrationConstants();

    private float lastRawVoltage = 0.0f;
    private long lastRawThrust = 0;
    private float lastRawCurrent = 0;

    private boolean holdEnabled = false;

    //FXML Element variables
    private TextField thrustField;
    private Button tearButton;
    private ToggleButton holdToggle;
    private ComboBox<String> thrustUnitCombo;
    private TextField incomingAirspeedField;
    private TextField wakeAirspeedField;
    private ComboBox<String> incomingAirspeedUnitCombo;
    private ComboBox<String> wakeAirspeedUnitCombo;
    private TextField currentField;
    private TextField voltageField;
    private ComboBox<Integer> bladeCountCombo;
    private ToggleButton motorToggle;
    private ToggleButton loggingToggle;

    //Property values from FXML elements
    private final StringProperty thrustProperty = new SimpleStringProperty();
    private final StringProperty incomingAirspeedProperty = new SimpleStringProperty();
    private final StringProperty wakeAirspeedProperty = new SimpleStringProperty();
    private final StringProperty currentProperty = new SimpleStringProperty();
    private final StringProperty voltageProperty = new SimpleStringProperty();
    private final BooleanProperty holdActiveProperty = new SimpleBooleanProperty();
    private final BooleanProperty motorActiveProperty = new SimpleBooleanProperty();
    private final BooleanProperty loggerActiveProperty = new SimpleBooleanProperty();

    //DataLogger class instantiation
    private final DataLogger dataLogger;

    private SharedElements(){
        dataLogger = new DataLogger();
    }

    public DataLogger getDataLogger(){
        return dataLogger;
    }

    public static SharedElements getInstance(){
        if (instance == null) {
            instance = new SharedElements();
        }
        return instance;
    }

    public void initializeControls(
            TextField thrustField,
            Button tearButton,
            ToggleButton holdToggle,
            ComboBox<String> thrustUnitCombo,
            TextField incomingAirspeedField,
            TextField wakeAirspeedField,
            ComboBox<String> incomingAirspeedUnitCombo,
            ComboBox<String> wakeAirspeedUnitCombo,
            TextField currentField,
            TextField voltageField,
            ComboBox<Integer> bladeCountCombo,
            ToggleButton motorToggle,
            ToggleButton loggingToggle){
        this.thrustField = thrustField;
        this.tearButton = tearButton;
        this.holdToggle = holdToggle;
        this.thrustUnitCombo = thrustUnitCombo;
        this.incomingAirspeedField = incomingAirspeedField;
        this.wakeAirspeedField = wakeAirspeedField;
        this.incomingAirspeedUnitCombo = incomingAirspeedUnitCombo;
        this.wakeAirspeedUnitCombo = wakeAirspeedUnitCombo;
        this.currentField = currentField;
        this.voltageField = voltageField;
        this.bladeCountCombo = bladeCountCombo;
        this.motorToggle = motorToggle;
        this.loggingToggle = loggingToggle;
        
        setupControls();
        setupBindings();
    }

    private void setupControls(){
        //Thrust controls
        thrustField.setEditable(false);
        thrustUnitCombo.getItems().addAll("lb","kg","N");
        thrustUnitCombo.setValue("lb");

        //Airspeed controls
        String[] airspeedUnitOptions = {"mph","ft/s","kph","m/s"};
        incomingAirspeedUnitCombo.getItems().addAll(airspeedUnitOptions);
        wakeAirspeedUnitCombo.getItems().addAll(airspeedUnitOptions);
        incomingAirspeedUnitCombo.setValue("m/s");
        wakeAirspeedUnitCombo.setValue("m/s");

        //RPM Controls
        bladeCountCombo.getItems().addAll(1,2,3,4,5,6,7,8);
        bladeCountCombo.setValue(2);

        //Electircal measurements
        currentField.setEditable(false);
        voltageField.setEditable(false);

    }

    private void setupBindings(){
        thrustField.textProperty().bindBidirectional(thrustProperty);
        incomingAirspeedField.textProperty().bindBidirectional(incomingAirspeedProperty);
        wakeAirspeedField.textProperty().bindBidirectional(wakeAirspeedProperty);
        currentField.textProperty().bindBidirectional(currentProperty);
        voltageField.textProperty().bindBidirectional(voltageProperty);
        holdToggle.selectedProperty().bindBidirectional(holdActiveProperty);
        motorToggle.selectedProperty().bindBidirectional(motorActiveProperty);
        loggingToggle.selectedProperty().bindBidirectional(loggerActiveProperty);

    }

    public StringProperty thrustProperty() { return thrustProperty; }
    public StringProperty incomingAirspeedProperty() { return incomingAirspeedProperty; }
    public StringProperty wakeAirspeedProperty() { return wakeAirspeedProperty; }
    public StringProperty currentProperty() { return currentProperty; }
    public StringProperty voltageProperty() { return voltageProperty; }
    public BooleanProperty motorRunningProperty() { return motorActiveProperty; }
    public BooleanProperty dataLoggingProperty() { return loggerActiveProperty; }

    public void handleTearButton(){
        tearButton.getOnAction();
        tearThrust();
    }

    public void handleMotorToggle(){
        if(motorToggle.isSelected()){
            //Motor is being turned on
            if (SerialController.getInstance().setMotor(true)) {
                System.out.println("Motor Started");
                motorActiveProperty.set(true);    
            } else {
                //If serial command failed, revert toggle and alert user
                System.out.println("Failed to start motor");
                motorToggle.setSelected(false);
                motorActiveProperty.set(false);

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Motor Control Error");
                alert.setHeaderText("Failed to Start Motor");
                alert.setContentText("Unable to send motor start command. Please check serial connection and try again.");
                alert.showAndWait();
            }
            
        } else {
            //Motor is being turned off
            if(SerialController.getInstance().setMotor(false)){
                System.out.println("Motor Stopped");
                motorActiveProperty.set(false);
            } else {
                //If serial command failed, revert toggle and alert user
                System.out.println("Failed to stop motor");
                motorToggle.setSelected(true);
                motorActiveProperty.set(true);
                    
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Motor Control Error");
                alert.setHeaderText("Failed to Stop Motor");
                alert.setContentText("Unable to send motor stop command. Please check serial connection and try again. If problems persist, consider emergency stop procedures.");
                alert.showAndWait();
            }
        }

    }

    public void handleHoldToggle() {
        holdEnabled = holdToggle.isSelected();
    }

    public void handleLoggerToggle(){
        if (loggingToggle.isSelected()) {
            //Start logging data
            try {
                dataLogger.startLogging();
                loggerActiveProperty.set(true);
                System.out.println("Data logging started: " + dataLogger.getCurrentFilePath());
            } catch (IOException e) {
                e.printStackTrace();
                loggingToggle.setSelected(false);
                loggerActiveProperty.set(false);
                
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Logging Error");
                alert.setHeaderText("Failed to Start Logging");
                alert.setContentText("Unable to create log file. Please check permissions and try again.");
                alert.showAndWait();
            }
        } else {
            // Stop logging data
            dataLogger.stopLogging();
            loggerActiveProperty.set(false);
            System.out.println("Data logging stopped");
        }
    }

    public void resetAllFields(){
        thrustProperty.set("000.00");
        incomingAirspeedProperty.set("000.00");
        wakeAirspeedProperty.set("000.00");
        currentProperty.set("000.00");
        voltageProperty.set("000.00");
        motorActiveProperty.set(false);
        loggerActiveProperty.set(false);
        tearThrust();
    }

    public void tearThrust(){
        thrustProperty.set("000.00");
        holdEnabled = false;
        holdActiveProperty.set(false);
    }

    public void updateThrustValue(String value) {
        if (!holdEnabled) {
            thrustProperty.set(value);
        }
    }

    private String convertRawToThrust(long rawValue) {
        // Convert raw value to grams using calibration factor
        double gramsForce = rawValue * calibration.loadCellCalibration;
        
        // Convert to selected units (lb, kg, or N)
        String unit = thrustUnitCombo.getValue();
        double convertedValue = switch (unit) {
            case "lb" -> gramsForce * 0.00220462;  // grams to pounds
            case "kg" -> gramsForce * 0.001;       // grams to kg
            case "N" -> gramsForce * 0.00981;      // grams to Newtons
            default -> gramsForce * 0.001;         // default to kg
        };
        
        return String.format("%.2f", convertedValue);
    }
    

    private String convertVoltageToAirspeed(float voltage, boolean isIncoming) {
        // Use appropriate calibration constant based on which sensor
        double calibrationFactor = isIncoming ? 
            calibration.incomingPitotCalibration : calibration.wakePitotCalibration;
            
        // Convert voltage to differential pressure
        double pressurePa = voltage * calibrationFactor;
        double airDensity = 1.225; // kg/m³ at sea level, 15°C
        
        // Calculate airspeed in m/s
        double speedMS = Math.sqrt(2 * pressurePa / airDensity);
        
        // Convert to selected units
        String unit = incomingAirspeedUnitCombo.getValue();
        double convertedSpeed = switch (unit) {
            case "mph" -> speedMS * 2.23694;
            case "ft/s" -> speedMS * 3.28084;
            case "kph" -> speedMS * 3.6;
            case "m/s" -> speedMS;
            default -> speedMS;
        };
        
        return String.format("%.2f", convertedSpeed);
    }

    /**
     * Convert voltage sensor reading to current
     * @param voltage Raw voltage from current sensor
     * @return Formatted current string in amperes
     */
    private String convertVoltageToCurrent(float voltage) {
        // ACS758ECB-200U specifications:
        // Sensitivity: 20mV/A = 0.02V/A
        // Quiescent voltage (zero current): 0.6V

        // Current = (Vout - Vq) / sensitivity
        // Where: Vout = measured voltage
        //        Vq = quiescent voltage (0.6V)
        double current = (voltage - 0.6) / calibration.currentSensorSensitivity;
        
        return String.format("%.2f", current);
    }

    /**
     * Convert voltage sensor reading to actual voltage
     * @param voltage Raw voltage from voltage divider
     * @return Formatted voltage string
     */
    private String convertVoltageToVoltage(float voltage) {
        // Convert based on voltage divider ratio
        double actualVoltage = voltage / calibration.voltageDividerRatio;

        return String.format("%.2f", actualVoltage);
    }

    
    //Update all measurements with converted values
    public void updateMeasurements(long rawThrust, float incomingPitotV, float wakePitotV, float currentV, float voltageV, float rpm) {
        this.lastRawThrust = rawThrust;
        this.lastRawCurrent = currentV;
        
        lastRawVoltage = voltageV; //Saves the last raw value of the input voltage for calibration purposes
        
        //Convert all values
        String thrustValue = convertRawToThrust(rawThrust);
        String incomingSpeed = convertVoltageToAirspeed(incomingPitotV, true);
        String wakeSpeed = convertVoltageToAirspeed(wakePitotV, false);
        String currentValue = convertVoltageToCurrent(currentV);
        String voltageValue = convertVoltageToVoltage(voltageV);

        //Update UI fields only if hold is disabled
        if (!holdEnabled) {
            thrustProperty.set(thrustValue);
            incomingAirspeedProperty.set(incomingSpeed);
            wakeAirspeedProperty.set(wakeSpeed);
            currentProperty.set(currentValue);
            voltageProperty.set(voltageValue);
        }

        //Log data to CSV when toggle is enabled
        if (loggerActiveProperty.get()) {
            dataLogger.logData(
                thrustValue,
                thrustUnitCombo.getValue(),
                incomingSpeed,
                wakeSpeed, 
                incomingAirspeedUnitCombo.getValue(),
                currentValue,
                voltageValue,
                bladeCountCombo.getValue(),
                String.format("%.1f", rpm));
        }
    }

    public void setIncomingPitotCalibration(double calibration) {
        this.calibration.incomingPitotCalibration = calibration;
    }

    public void setWakePitotCalibration(double calibration) {
        this.calibration.wakePitotCalibration = calibration;
    }

    //Save calibration to a local file
    public void saveCalibration(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("loadcell=" + calibration.loadCellCalibration);
            writer.println("incoming_pitot=" + calibration.incomingPitotCalibration);
            writer.println("wake_pitot=" + calibration.wakePitotCalibration);
            writer.println("current=" + calibration.currentSensorSensitivity);
            writer.println("voltage=" + calibration.voltageDividerRatio);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Load saved calibration data from a file
    public void loadCalibration(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    double value = Double.parseDouble(parts[1]);
                    switch (parts[0]) {
                        case "loadcell" -> calibration.loadCellCalibration = value;
                        case "incoming_pitot" -> calibration.incomingPitotCalibration = value;
                        case "wake_pitot" -> calibration.wakePitotCalibration = value;
                        case "current" -> calibration.currentSensorSensitivity = value;
                        case "voltage" -> calibration.voltageDividerRatio = value;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Calibration helper method
    public void calibratePitotSensor(boolean isIncoming, double knownAirspeed, float measuredVoltage) {
        double airDensity = 1.225; // kg/m³
        double pressure = 0.5 * airDensity * knownAirspeed * knownAirspeed;
        double calibrationFactor = pressure / measuredVoltage;
        
        if (isIncoming) {
            calibration.incomingPitotCalibration = calibrationFactor;
        } else {
            calibration.wakePitotCalibration = calibrationFactor;
        }
    }

    public float getRawVoltage(){
        return lastRawVoltage;
    }

    public long getRawThrust(){
        return lastRawThrust;
    }

    public float getRawCurrent(){
        return lastRawCurrent;
    }

    public double getLoadCellCalibration() {
        return calibration.loadCellCalibration;
    }

    public double getIncomingPitotCalibration() {
        return calibration.incomingPitotCalibration;
    }

    public double getWakePitotCalibration() {
        return calibration.wakePitotCalibration;
    }

    public double getCurrentSensorSensitivity() {
        return calibration.currentSensorSensitivity;
    }

    public double getVoltageDividerRatio() {
        return calibration.voltageDividerRatio;
    }

    public void setLoadCellCalibration(double calibration) {
        this.calibration.loadCellCalibration = calibration;
    }

    public void setCurrentSensorSensitivity(double sensitivity) {
        this.calibration.currentSensorSensitivity = sensitivity;
    }

    public void setVoltageDividerRatio(double ratio) {
        this.calibration.voltageDividerRatio = ratio;
    }

    //Reset calibration to defaults
    public void resetCalibration() {
        calibration.loadCellCalibration = 1.0;
        calibration.incomingPitotCalibration = 1.0;
        calibration.wakePitotCalibration = 1.0;
        calibration.currentSensorSensitivity = 0.02;
        calibration.voltageDividerRatio = 0.1;
    }
}
