import javafx.collections.FXCollections;
import javafx.collections.ObservableList
;
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

    
}
