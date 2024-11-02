import javafx.scene.control.*;

import com.fazecast.jSerialComm.SerialPortThreadFactory;

import javafx.beans.property.*;

public class SharedElements {
    private static SharedElements instance;

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
        tearThrust();
    }

    public void handleMotorToggle(){
        if(motorToggle.isArmed()){
            System.out.println("Motor Started");
            //Send serial: Motor Armed;
        } else {
            System.out.println("Motor Stopped");
            //Send serial: Motor Disarmed;
        }

    }

    public void handleLoggerToggle(){

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

    }
    
}
