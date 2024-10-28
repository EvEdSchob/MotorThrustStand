/*  Title:  LabMode.Java
 *  Author: Evan Schober
 *  Email:  evan.e.schober@wmich.edu
 *  Parent Class: ThrustStand.java
 *  Description: Controller class for the lab mode 
 */
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class LabMode extends BaseController {
    //FXML variables for linking to scenebuilder controls
    @FXML private TextField thrustField;
    @FXML private TextField currentField;
    @FXML private TextField voltageField;
    @FXML private TextField airspeed1Field;
    @FXML private TextField airspeed2Field;
    //@FXML private TextField rpmField; //Not needed for lab mode
    @FXML private ComboBox<String> thrustUnitCombo;
    @FXML private ComboBox<String> airspeed1UnitCombo;
    @FXML private ComboBox<String> airspeed2UnitCombo;
    @FXML private ComboBox<Integer> bladeCountCombo;
    @FXML private ComboBox<Integer> rpmCombo; //Not needed for dyno mode
    @FXML private Button tearButton;
    @FXML private Button holdButton;
    @FXML private Button motorButton;
    @FXML private Button loggerButton;
    @FXML private Button returnToMainBtn;

    private SharedElements sharedElements = SharedElements.getInstance();

    @FXML
    public void initialize(){

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
    
}
