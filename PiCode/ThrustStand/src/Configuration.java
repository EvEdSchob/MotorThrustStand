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
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import java.io.File;

public class Configuration extends BaseController {
    //Serial port selection
    @FXML private ComboBox<String> serialPortCombo;
    @FXML private HBox serialPortControls;
    @FXML private Button refreshPortsBtn;
    @FXML private Button connectPortBtn;
    
    //Load cell calibration
    @FXML private Button calibrateZeroBtn;
    @FXML private Label zeroOffsetLabel;
    @FXML private TextField knownWeightField;
    @FXML private Button calibrateLoadCellBtn;
    @FXML private Label loadCellCalibrationLabel;
    @FXML private ComboBox<String> weightUnitCombo;
    
    //Pitot sensor calibration
    @FXML private TextField incomingKnownSpeedField;
    @FXML private TextField wakeKnownSpeedField;
    @FXML private Button calibrateIncomingBtn;
    @FXML private Button calibrateWakeBtn;
    @FXML private Label incomingCalibrationLabel;
    @FXML private Label wakeCalibrationLabel;
    @FXML private ComboBox<String> airspeedUnitCombo;
    
    //Electrical sensor calibration
    @FXML private TextField knownCurrentField;
    @FXML private TextField knownVoltageField;
    @FXML private Button calibrateCurrentZeroBtn;
    @FXML private Button calibrateCurrentBtn;
    @FXML private Button calibrateVoltageBtn;
    @FXML private Label currentZeroLabel;
    @FXML private Label currentCalibrationLabel;
    @FXML private Label voltageCalibrationLabel;
    
    //Data Logger Controls
    @FXML private TextField logFilePathField;
    @FXML private Button browseButton;
    @FXML private CheckBox appendTimestampCheckbox;
    @FXML private Label currentLogFileLabel;

    //Control buttons
    @FXML private Button saveCalibrationBtn;
    @FXML private Button loadCalibrationBtn;
    @FXML private Button resetCalibrationBtn;
    @FXML private Button returnToMainBtn;

    private static final int BAUD_RATE = 2000000; //Serial data rate (Match to arduino code)

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
        // Initialize serial port selection controls
        initializeSerialControls();
        
        // Initialize calibration buttons
        initializeCalibrationButtons();
        
        // Initialize unit ComboBoxes
        initializeUnitComboBoxes();
        
        // Initialize logging controls
        initializeLoggingControls();
        
        // Load current calibration values
        updateCalibrationLabels();

        //Add data listener for live updates from serial
        serialController.addDataReceivedListener(this::onDataReceived);
    }

    //Initialization functions
    private void initializeSerialControls(){
        serialPortCombo.setPrefWidth(200);
        serialPortCombo.setVisibleRowCount(5);
        refreshPortsBtn.setOnAction(e -> refreshSerialPorts());
        connectPortBtn.setOnAction(e -> handleSerialConnect());
        refreshSerialPorts();
    }

    private void initializeCalibrationButtons() {
        calibrateZeroBtn.setOnAction(e -> handleZeroCalibration());
        calibrateLoadCellBtn.setOnAction(e -> handleLoadCellCalibration());
        calibrateCurrentZeroBtn.setOnAction(e -> handleCurrentZeroCalibration());
        calibrateCurrentBtn.setOnAction(e -> handleCurrentCalibration());
        calibrateVoltageBtn.setOnAction(e -> handleVoltageCalibration());
        calibrateIncomingBtn.setOnAction(e -> handlePitotCalibration(true));
        calibrateWakeBtn.setOnAction(e -> handlePitotCalibration(false));
        
        saveCalibrationBtn.setOnAction(e -> handleSaveCalibration());
        loadCalibrationBtn.setOnAction(e -> handleLoadCalibration());
        resetCalibrationBtn.setOnAction(e -> handleResetCalibration());
    }

    private void initializeUnitComboBoxes() {
        weightUnitCombo.getItems().addAll("g", "kg", "lb", "N");
        weightUnitCombo.setValue("g");
        
        airspeedUnitCombo.getItems().addAll("m/s", "mph", "ft/s", "kph");
        airspeedUnitCombo.setValue("m/s");
    }

    private void initializeLoggingControls() {
        DataLogger logger = sharedElements.getDataLogger();
        logFilePathField.setText(logger.getCurrentFilePath());
        appendTimestampCheckbox.setSelected(true);
        
        browseButton.setOnAction(e -> handleDirectorySelection());
        logFilePathField.setOnAction(e -> handleManualPathEntry());
        appendTimestampCheckbox.setOnAction(e -> {
            sharedElements.getDataLogger().setAppendTimestamp(appendTimestampCheckbox.isSelected());
        });
    }

    //Control handlers
    private void handleSerialConnect() {
        String selectedPort = serialPortCombo.getValue();
        if (selectedPort != null && !selectedPort.isEmpty()) {
            connectToSerialPort(selectedPort);
        } else {
            showError("Please select a port first");
        }
    }

    private void handleZeroCalibration() {
        // Get current raw value from shared elements
        long currentRawValue = sharedElements.getCurrentRawLoadCell();
        sharedElements.setLoadCellZeroOffset(currentRawValue);
        updateCalibrationLabels();
    }

    private void handleLoadCellCalibration() {
        try {
            double inputWeight = Double.parseDouble(knownWeightField.getText());
            double weightInGrams = convertToGrams(inputWeight, weightUnitCombo.getValue());
            
            //Get current raw value and apply zero offset
            long currentRawValue = sharedElements.getCurrentRawLoadCell();
            double zeroedValue = currentRawValue - sharedElements.getLoadCellZeroOffset();
            
            double scaleFactor = weightInGrams / zeroedValue;
            sharedElements.setLoadCellScale(scaleFactor);
            
            updateCalibrationLabels();
        } catch (NumberFormatException ex) {
            showError("Please enter a valid weight value");
        }
    }

    private void handleCurrentCalibration() {
        try {
            double knownCurrent = Double.parseDouble(knownCurrentField.getText());
            if (knownCurrent == 0) {
                showError("Known current must be non-zero for sensitivity calibration");
                return;
            }
            float rawVoltage = sharedElements.getRawCurrent();
            double zeroOffset = sharedElements.getCurrentSensorZeroOffset();

            //System.out.println("Current Calibration:");  // Debug
            //System.out.println("Known Current: " + knownCurrent);  // Debug
            //System.out.println("Raw Voltage: " + rawVoltage);  // Debug
            //System.out.println("Zero Offset: " + zeroOffset);  // Debug

            //Calculate sensitivity using zero-offset voltage
            double sensitivity = (rawVoltage - zeroOffset) / knownCurrent;
            //System.out.println("Calculated Sensitivity: " + sensitivity);  // Debug
            
            if (sensitivity <= 0) {
                showError("Invalid calibration result. Check current direction and measurements.");
                return;
            }
            
            sharedElements.setCurrentSensorSensitivity(sensitivity);
            updateCalibrationLabels();
        } catch (NumberFormatException ex) {
            showError("Please enter a valid current value");
        }
    }

    private void handleCurrentZeroCalibration() {
        float currentRawVoltage = sharedElements.getRawCurrent();
        //System.out.println("Zero Calibration - Raw Voltage: " + currentRawVoltage);   //Debug
        sharedElements.setCurrentSensorZeroOffset(currentRawVoltage);
        currentZeroLabel.setText(String.format("%.3f V", currentRawVoltage));
        updateCalibrationLabels();
    }

    private void handleVoltageCalibration() {
        try {
            double knownVoltage = Double.parseDouble(knownVoltageField.getText());
            float rawVoltage = sharedElements.getRawVoltage();
            
            double newRatio = rawVoltage / knownVoltage;
            
            sharedElements.setVoltageDividerRatio(newRatio);
            updateCalibrationLabels();
        } catch (NumberFormatException ex) {
            showError("Please enter a valid voltage value");
        }
    }

    private void handlePitotCalibration(boolean isIncoming) {
        try {
            TextField field = isIncoming ? incomingKnownSpeedField : wakeKnownSpeedField;
            double inputSpeed = Double.parseDouble(field.getText());
            double speedInMS = convertToMetersPerSecond(inputSpeed, airspeedUnitCombo.getValue());
            
            float currentVoltage = isIncoming ? 
                Float.parseFloat(sharedElements.incomingAirspeedProperty().get()) :
                Float.parseFloat(sharedElements.wakeAirspeedProperty().get());
            
            //Calculate expected pressure at this speed using Bernoulli's equation
            double airDensity = 1.225; // kg/m³ at sea level, 15°C
            double expectedPressurePa = 0.5 * airDensity * speedInMS * speedInMS;

            //Convert voltage to pressure using sensor sensitivity
            final double SENSOR_SENSITIVITY = 0.270; // V/kPa
            double measuredPressureKPa = currentVoltage / SENSOR_SENSITIVITY;
            double measuredPressurePa = measuredPressureKPa * 1000;

            //Calculate calibration factor
            double calibrationFactor = expectedPressurePa / measuredPressurePa;
            
            if (isIncoming) {
                sharedElements.setIncomingPitotCalibration(calibrationFactor);
            } else {
                sharedElements.setWakePitotCalibration(calibrationFactor);
            }
            
            updateCalibrationLabels();
        } catch (NumberFormatException ex) {
            showError("Please enter a valid airspeed value");
        }
    }

    private void handleSaveCalibration() {
        sharedElements.saveCalibration("calibration.txt");
        showInfo("Calibration saved successfully");
    }

    private void handleLoadCalibration() {
        sharedElements.loadCalibration("calibration.txt");
        updateCalibrationLabels();
        showInfo("Calibration loaded successfully");
    }

    private void handleResetCalibration() {
        sharedElements.resetCalibration();
        updateCalibrationLabels();
        showInfo("Calibration reset to defaults");
    }

    private void handleDirectorySelection() {
        try {
            File selectedDirectory = showDirectoryChooser();
            if (selectedDirectory != null) {
                updateLogDirectory(selectedDirectory.getAbsolutePath());
            }
        } catch (Exception ex) {
            showManualDirectoryInputDialog();
        }
    }


    private void refreshSerialPorts(){
        String currentSelection = serialPortCombo.getValue();
        serialPortCombo.getItems().clear();
        serialPortCombo.getItems().addAll(serialController.getAvailablePorts());

        // Restore previous selection if it still exists
        if (currentSelection != null && serialPortCombo.getItems().contains(currentSelection)) {
            serialPortCombo.setValue(currentSelection);
        }
    }

    private void connectToSerialPort(String portName) {
        // Close existing connection if any
        serialController.closePort();
        
        // Attempt to open new connection
        if (serialController.openPort(portName, BAUD_RATE)) {
            showInfo("Successfully connected to " + portName);
        } else {
            showError("Failed to connect to " + portName);
            // Clear selection to indicate failed connection
            serialPortCombo.setValue(null);
        }
    }

    private void showManualDirectoryInputDialog() {
        TextInputDialog dialog = new TextInputDialog(logFilePathField.getText());
        dialog.setTitle("Enter Log Directory");
        dialog.setHeaderText("Please enter the full path for log files:");
        dialog.setContentText("Path:");
        
        dialog.showAndWait().ifPresent(path -> validateAndUpdateDirectory(path));
    }
    
    private void handleManualPathEntry() {
        validateAndUpdateDirectory(logFilePathField.getText());
    }
    
    private void validateAndUpdateDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                updateLogDirectory(path);
            } else {
                showError("Could not create directory: " + path);
            }
        } else if (!dir.isDirectory()) {
            showError("Selected path is not a directory: " + path);
        } else if (!dir.canWrite()) {
            showError("Cannot write to selected directory: " + path);
        } else {
            updateLogDirectory(path);
        }
    }
    
    private void updateLogDirectory(String path) {
        logFilePathField.setText(path);
        sharedElements.getDataLogger().setFilePath(path + "/thrust_data");
        updateCurrentLogFileLabel();
    }
    
    private void updateCurrentLogFileLabel() {
        String currentFile = sharedElements.getDataLogger().getCurrentFilePath();
        currentLogFileLabel.setText("Current log file: " + 
            (currentFile != null && !currentFile.isEmpty() ? currentFile : "None"));
    }

    private File showDirectoryChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Log File Directory");
        
        // Set initial directory to current path or user home
        String currentPath = logFilePathField.getText();
        if (currentPath != null && !currentPath.isEmpty()) {
            File currentDir = new File(currentPath);
            if (currentDir.exists()) {
                directoryChooser.setInitialDirectory(currentDir);
            }
        } else {
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }
        
        try {
            return directoryChooser.showDialog(browseButton.getScene().getWindow());
        } catch (IllegalArgumentException ex) {
            // If the native dialog fails, try with user.home as fallback
            directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            return directoryChooser.showDialog(browseButton.getScene().getWindow());
        }
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
    
    private void updateCalibrationLabels() {
        // Add unit information to labels
        String weightUnit = weightUnitCombo.getValue();
        String speedUnit = airspeedUnitCombo.getValue();
        
        loadCellCalibrationLabel.setText(String.format("Current zero offset: %.2f, scale factor: %.6f (g/%s)", 
            sharedElements.getLoadCellZeroOffset(), 
            sharedElements.getLoadCellScale(), 
            weightUnit));
        incomingCalibrationLabel.setText(String.format("Current calibration: %.6f (Pa/(m/s)²)", 
            sharedElements.getIncomingPitotCalibration(), speedUnit));
        wakeCalibrationLabel.setText(String.format("Current calibration: %.6f (Pa/(m/s)²)", 
            sharedElements.getWakePitotCalibration(), speedUnit));
        currentCalibrationLabel.setText(String.format("Current calibration: Zero=%.3fV, Sensitivity=%.3f V/A", 
                sharedElements.getCurrentSensorZeroOffset(),
                sharedElements.getCurrentSensorSensitivity()));
        if (currentZeroLabel != null) {
            currentZeroLabel.setText(String.format("%.3f V", 
                sharedElements.getCurrentSensorZeroOffset()));
        }
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

    private void onDataReceived(String data) {
        serialController.parseData(data);
    }

    @FXML
    void returnToMainBtn(ActionEvent rtn) {
        //System.out.println("Returning to launcher"); //Debug
        try {
            thrustStand.changeScene("fxml/Launcher.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
