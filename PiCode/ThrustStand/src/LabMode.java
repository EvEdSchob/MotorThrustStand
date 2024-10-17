/*  Title:  LabMode.Java
 *  Author: Evan Schober
 *  Email:  evan.e.schober@wmich.edu
 *  Parent Class: ThrustStand.java
 *  Description: Controller class for the lab mode 
 */
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class LabMode extends BaseController {


    @FXML
    private Button returnToMainBtn;

    @FXML
    void returnToMainBtn(ActionEvent rtn){
        System.out.println("Returning to launcher");
        try {
            ThrustStand t = new ThrustStand();
            t.changeScene("fxml/Launcher.fxml");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
