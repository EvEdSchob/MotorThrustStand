import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class Configuration extends BaseController{
    @FXML
    private Button returnToMainBtn;

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
