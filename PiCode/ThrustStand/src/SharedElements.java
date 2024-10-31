import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import com.fazecast.jSerialComm.SerialPortThreadFactory;

import javafx.beans.property.*;

public class SharedElements {
    private TextField thrustMeasurement;
    private Button tearButton;
    private ToggleButton holdToggle;
    private ComboBox<String> thrustUnits;
    private TextField incomingAirspeed;
    private TextField wakeAirspeed;
    private ComboBox<String> incomingAirspeedUnits;
    private ComboBox<String> wakeAirspeedUnits;
    private TextField currentDisplay;
    private TextField voltageDisplay;
    private ComboBox<Integer> bladeCount;
    private ToggleButton motorToggle;
    private ToggleButton loggingToggle;

    private final StringProperty thrustMeasurementProperty = new SimpleStringProperty();
    private final StringProperty incomingAirspeedProperty = new SimpleStringProperty();
    private final StringProperty wakeAirspeedProperty = new SimpleStringProperty();
    private final StringProperty currentProperty = new SimpleStringProperty();
    private final StringProperty voltageProperty = new SimpleStringProperty();
    private final BooleanProperty holdActiveProperty = new SimpleBooleanProperty();
    private final BooleanProperty motorActiveProperty = new SimpleBooleanProperty();
    private final BooleanProperty loggerActiveProperty = new SimpleBooleanProperty();

    public void initializeControls(
            TextField thrustMeasurement,
            Button tearButton,
            ToggleButton holdToggle,
            ComboBox<String> thrustUnits,
            TextField incomingAirspeed,
            TextField wakeAirspeed,
            ComboBox<String> incomingAirspeedUnits,
            ComboBox<String> wakeAirspeedUnits,
            TextField currentDisplay,
            TextField voltageDisplay,
            ComboBox<Integer> bladeCount,
            ToggleButton motorToggle,
            ToggleButton loggingToggle){
        this.thrustMeasurement = thrustMeasurement;
        this.tearButton = tearButton;
        this.holdToggle = holdToggle;
        this.thrustUnits = thrustUnits;
        this.incomingAirspeed = incomingAirspeed;
        this.wakeAirspeed = wakeAirspeed;
        this.incomingAirspeedUnits = incomingAirspeedUnits;
        this.wakeAirspeedUnits = wakeAirspeedUnits;
        this.currentDisplay = currentDisplay;
        this.voltageDisplay = voltageDisplay;
        this.motorToggle = motorToggle;
        this.loggingToggle = loggingToggle;

        setupControls();
        setupBindings();
    }

    private void setupControls(){
        //Thrust controls
        thrustMeasurement.setEditable(false);
        thrustUnits.getItems().addAll("lb","kg","N");
        thrustUnits.setValue("lb");

        //Airspeed controls
        String[] airspeedUnitOptions = {"mph","ft/s","kph","m/s"};
        incomingAirspeedUnits.getItems().addAll(airspeedUnitOptions);
        wakeAirspeedUnits.getItems().addAll(airspeedUnitOptions);
        incomingAirspeedUnits.setValue("m/s");
        wakeAirspeedUnits.setValue("m/s");

        //RPM Controls
        bladeCount.getItems().addAll(1,2,3,4,5,6,7,8);
        bladeCount.setValue(2);

        //Electircal measurements
        currentDisplay.setEditable(false);
        voltageDisplay.setEditable(false);

    }

    private void setupBindings(){
        thrustMeasurement.textProperty().bindBidirectional(thrustMeasurementProperty);
        incomingAirspeed.textProperty().bindBidirectional(incomingAirspeedProperty);
        wakeAirspeed.textProperty().bindBidirectional(wakeAirspeedProperty);
        currentDisplay.textProperty().bindBidirectional(currentProperty);
        voltageDisplay.textProperty().bindBidirectional(voltageProperty);
        holdToggle.selectedProperty().bindBidirectional(holdActiveProperty);
        motorToggle.selectedProperty().bindBidirectional(motorActiveProperty);
        loggingToggle.selectedProperty().bindBidirectional(loggerActiveProperty);

    }

    public StringProperty thrustMeasurementProperty() { return thrustMeasurementProperty; }
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

    }

    public void handleLoggerToggle(){

    }

    public void resetAllFields(){
        thrustMeasurementProperty.set("000.00");
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
