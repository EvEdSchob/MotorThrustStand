import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

public class SharedElements {
    private static final SharedElements instance = new SharedElements();

    //Creates the list for the total number of blades on the propeller.
    private final ObservableList<Integer> bladeCount = FXCollections.observableArrayList(
        1,2,3,4,5,6
    );

    //Creates the list of units that can be selected for the airspeed
    private final ObservableList<String> speedUnits = FXCollections.observableArrayList(
        "mph","ft/sec","kph","m/sec"
    );

    //Creates the list of units that can be selected for the thrust
    private final ObservableList<String> thrustUnits = FXCollections.observableArrayList(
        "lbs","kg","N"
    );

    //Create initial states for UI controls
    private String thrustValue = "000.00";
    private String currentValue = "000.00";
    private String voltageValue = "000.00";
    private String airspeed1Value = "000.00";
    private String airspeed2Value = "000.00";
    
    private boolean holdEnabled = false;
    private boolean motorEnabled = false;
    private boolean loggerEnabled = false;
    

    public static SharedElements getInstance(){
        return instance;
    }
    
    public ObservableList<Integer> getBladeCount(){
        return bladeCount;
    }

    public ObservableList<String> getSpeedUnits(){
        return speedUnits;
    }

    public ObservableList<String> getThrustUnits(){
        return thrustUnits;
    }

    // Getters for thrust-related controls
    public TextField getThrustField() {
        return thrustField;
    }
    
    public Button getTearButton() {
        return tearButton;
    }
    
    public Button getHoldButton() {
        return holdButton;
    }
    
    public ComboBox<String> getThrustUnitCombo() {
        return thrustUnitCombo;
    }
    
    // Getters for other fields
    public TextField getCurrentField() {
        return currentField;
    }
    
    public TextField getVoltageField() {
        return voltageField;
    }
    
    public TextField getAirspeed1Field() {
        return airspeed1Field;
    }
    
    public TextField getAirspeed2Field() {
        return airspeed2Field;
    }
    
    public TextField getRPMField() {
        return rpmField;
    }
    
    // Update methods for all fields
    public void setThrust(String value) {
        thrustField.setText(value);
    }
    
    public void setCurrent(String value) {
        currentField.setText(value);
    }
    
    public void setVoltage(String value) {
        voltageField.setText(value);
    }
    
    public void setAirspeed1(String value) {
        airspeed1Field.setText(value);
    }
    
    public void setAirspeed2(String value) {
        airspeed2Field.setText(value);
    }
    
    public void setRPM(String value) {
        rpmField.setText(value);
    }
}
