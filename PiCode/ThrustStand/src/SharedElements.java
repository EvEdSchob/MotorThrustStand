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
        private static final double DEFAULT_LOADCELL_ZERO_OFFSET = 42363.0;
        private static final double DEFAULT_LOADCELL_SCALE = 0.002296;
        private static final double DEFAULT_INCOMING_PITOT = 1.0;
        private static final double DEFAULT_WAKE_PITOT = 1.0;
        private static final double DEFAULT_CURRENT_ZERO_OFFSET = 0.394;
        private static final double DEFAULT_CURRENT_SENSITIVITY = 0.02;
        private static final double DEFAULT_VOLTAGE_RATIO = 0.1;

        // Instance variables initialized with default values
        private double loadCellZeroOffset = DEFAULT_LOADCELL_ZERO_OFFSET;
        private double loadCellScale = DEFAULT_LOADCELL_SCALE;
        private double incomingPitotCalibration = DEFAULT_INCOMING_PITOT;
        private double wakePitotCalibration = DEFAULT_WAKE_PITOT;
        private double currentSensorZeroOffset = DEFAULT_CURRENT_ZERO_OFFSET;
        private double currentSensorSensitivity = DEFAULT_CURRENT_SENSITIVITY;
        private double voltageDividerRatio = DEFAULT_VOLTAGE_RATIO;
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
        thrustUnitCombo.setValue("kg");

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
                //System.out.println("Motor Started");  //Debug
                motorActiveProperty.set(true);    
            } else {
                //If serial command failed, revert toggle and alert user
                //System.out.println("Failed to start motor");  //Debug
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
                //System.out.println("Motor Stopped"); //Debug  
                motorActiveProperty.set(false);
            } else {
                //If serial command failed, revert toggle and alert user
                //System.out.println("Failed to stop motor");   //Debug
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
                //System.out.println("Data logging started: " + dataLogger.getCurrentFilePath());   //Debug
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
            //System.out.println("Data logging stopped");   //Debug
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
        //Update zero offset to current raw value when taring
        calibration.loadCellZeroOffset = lastRawThrust;
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
        //Store raw value for calibration
        lastRawThrust = rawValue;

        //Apply zero offset and scaling
        double zeroedValue = rawValue - calibration.loadCellZeroOffset;
        double gramsForce = zeroedValue * calibration.loadCellScale;
        
        //Convert to selected units (lb, kg, or N)
        String unit = thrustUnitCombo.getValue();
        return switch (unit) {
            case "lb" -> String.format("%.3f", gramsForce * 0.00220462);  // 1g ≈ 0.002 lb
            case "kg" -> String.format("%.3f", gramsForce * 0.001);       // 1g = 0.001 kg
            case "N" -> String.format("%.3f", gramsForce * 0.00981);      // 1g ≈ 0.01 N
            default -> String.format("%.0f", gramsForce);                 // 1g for grams
        };
    }
    

    private String convertVoltageToAirspeed(float voltage, boolean isIncoming) {
        final double SENSOR_SENSITIVITY = 0.270; //V/kPa
        final double QUIESCENT_VOLTAGE = 0.283;  //Update this after measuring

        //Remove quiescent voltage 
        double differentialVoltage = voltage - QUIESCENT_VOLTAGE;
        //Clamp negative values to 0 since sensors are unidirectional
        if (differentialVoltage < 0) differentialVoltage = 0;
        
        //Convert voltage to pressure:
        //1. Convert to kPa using sensitivity (V / (V/kPa) = kPa)
        double pressureKPa = differentialVoltage / SENSOR_SENSITIVITY;
        //2. Convert kPa to Pa
        double pressurePa = pressureKPa * 1000;
        
        //Apply installation calibration factor
        double calibrationFactor = isIncoming ? 
            calibration.incomingPitotCalibration : calibration.wakePitotCalibration;
        pressurePa *= calibrationFactor;
        
        //Calculate airspeed using Bernoulli's equation
        double airDensity = 1.225; //kg/m³ at sea level, 15°C
        double speedMS = Math.sqrt(2 * pressurePa / airDensity);
        
        //Convert to selected units
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
        //        Vq = quiescent voltage (0.6V - Nominal)
        double current = (voltage - calibration.currentSensorZeroOffset) / calibration.currentSensorSensitivity;

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
        //System.out.println("Raw Current Value: " + currentV);  // Debug
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
            writer.println("loadcell_offset=" + calibration.loadCellZeroOffset);
            writer.println("loadcell_scale=" + calibration.loadCellScale);
            writer.println("incoming_pitot=" + calibration.incomingPitotCalibration);
            writer.println("wake_pitot=" + calibration.wakePitotCalibration);
            writer.println("current_zero=" + calibration.currentSensorZeroOffset);
            writer.println("current_sensitivity=" + calibration.currentSensorSensitivity);
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
                        case "loadcell_offset" -> calibration.loadCellZeroOffset = value;
                        case "loadcell_scale" -> calibration.loadCellScale = value;
                        case "incoming_pitot" -> calibration.incomingPitotCalibration = value;
                        case "wake_pitot" -> calibration.wakePitotCalibration = value;
                        case "current_zero" -> calibration.currentSensorZeroOffset = value;
                        case "current_sensitivity" -> calibration.currentSensorSensitivity = value;
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

    public long getCurrentRawLoadCell() {
        return lastRawThrust;
    }

    public double getCurrentSensorZeroOffset() {
        return calibration.currentSensorZeroOffset;
    }
    
    public void setCurrentSensorZeroOffset(double offset) {
        this.calibration.currentSensorZeroOffset = offset;
    }

    public void setLoadCellZeroOffset(double offset){
        this.calibration.loadCellZeroOffset = offset;
    }

    public void setLoadCellScale(double scale){
        this.calibration.loadCellScale = scale;
    }

    public double getLoadCellZeroOffset(){
        return calibration.loadCellZeroOffset;
    }

    public double getLoadCellScale(){
        return calibration.loadCellScale;
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

    public void setCurrentSensorSensitivity(double sensitivity) {
        this.calibration.currentSensorSensitivity = sensitivity;
    }

    public void setVoltageDividerRatio(double ratio) {
        this.calibration.voltageDividerRatio = ratio;
    }

    //Reset calibration to defaults
    public void resetCalibration() {
        calibration.loadCellZeroOffset = CalibrationConstants.DEFAULT_LOADCELL_ZERO_OFFSET;
        calibration.loadCellScale = CalibrationConstants.DEFAULT_LOADCELL_SCALE;
        calibration.incomingPitotCalibration = CalibrationConstants.DEFAULT_INCOMING_PITOT;
        calibration.wakePitotCalibration = CalibrationConstants.DEFAULT_WAKE_PITOT;
        calibration.currentSensorZeroOffset = CalibrationConstants.DEFAULT_CURRENT_ZERO_OFFSET;
        calibration.currentSensorSensitivity = CalibrationConstants.DEFAULT_CURRENT_SENSITIVITY;
        calibration.voltageDividerRatio = CalibrationConstants.DEFAULT_VOLTAGE_RATIO;
    }
}
