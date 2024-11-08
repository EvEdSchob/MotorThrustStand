import javafx.scene.control.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javafx.beans.property.*;

public class SharedElements{
    private static SharedElements instance;

    // Updated naming for calibration constants
    private static final class CalibrationConstants {
        private double loadCellCalibration = 1.0;
        private double incomingPitotCalibration = 1.0;  // Changed from pitot1
        private double wakePitotCalibration = 1.0;      // Changed from pitot2
        private double currentSensorSensitivity = 0.185;
        private double voltageDividerRatio = 0.2;
    }
    private final CalibrationConstants calibration = new CalibrationConstants();


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

    private SharedElements(){
            
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
        if(motorToggle.isArmed()){
            System.out.println("Motor Started");
            //TODO: Send serial: Motor Armed;
        } else {
            System.out.println("Motor Stopped");
            //TODO: Send serial: Motor Disarmed;
        }

    }

    public void handleLoggerToggle(){
        //TODO: Build logic for 
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
        //TODO: Complete Function
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
        // For ACS712: 0V = -5A, VCC/2 = 0A, VCC = 5A
        // Assuming 3.3V reference voltage
        double zeroCurrentVoltage = 3.3 / 2;
        double current = (voltage - zeroCurrentVoltage) / calibration.currentSensorSensitivity;
        
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

    /**
     * Update all measurements with converted values
     */
    public void updateMeasurements(long rawThrust, float incomingPitotV, float wakePitotV, 
                                 float currentV, float voltageV, float rpm) {
        if (!holdEnabled) {
            thrustProperty.set(convertRawToThrust(rawThrust));
            incomingAirspeedProperty.set(convertVoltageToAirspeed(incomingPitotV, true));
            wakeAirspeedProperty.set(convertVoltageToAirspeed(wakePitotV, false));
            currentProperty.set(convertVoltageToCurrent(currentV));
            voltageProperty.set(convertVoltageToVoltage(voltageV));
        }
    }

    public void setIncomingPitotCalibration(double calibration) {
        this.calibration.incomingPitotCalibration = calibration;
    }

    public void setWakePitotCalibration(double calibration) {
        this.calibration.wakePitotCalibration = calibration;
    }

    // Updated save method
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

    // Updated load method
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

    // Updated calibration helper method
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

    // Add method to reset calibration to defaults
    public void resetCalibration() {
        calibration.loadCellCalibration = 1.0;
        calibration.incomingPitotCalibration = 1.0;
        calibration.wakePitotCalibration = 1.0;
        calibration.currentSensorSensitivity = 0.185;
        calibration.voltageDividerRatio = 0.2;
    }

    // Add methods to get current raw sensor values
    public float getCurrentPitotVoltage(boolean isIncoming) {
        //TODO: Complete function
        // Return the current raw voltage reading from the appropriate pitot sensor
        // You'll need to implement this based on how you're getting sensor data
        return 0.0f; // Placeholder
    }

    public long getCurrentRawReading() {
        //TODO: Complete function
        // Return the current raw reading from the load cell
        // You'll need to implement this based on how you're getting sensor data
        return 0L; // Placeholder
    }
}
