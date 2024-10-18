/*  Title:  DynoMode.Java
 *  Author: Evan Schober
 *  Email:  evan.e.schober@wmich.edu
 *  Parent Class: ThrustStand.java
 *  Description: Controller class for the dyno mode 
 */
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

public class DynoMode extends BaseController{
    @FXML
    private Button returnToMainBtn;

    @FXML
    private ComboBox<Integer> bladeCount;

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
