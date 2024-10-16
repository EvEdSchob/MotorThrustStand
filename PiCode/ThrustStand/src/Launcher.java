/* Project: MotorThrustStand 
 * 
 * Title: Launcher.java
 * Author: Evan Schober
 * Email: evan.e.schober@wmich.edu
 * Description: This is the controller class for the primary launch window.
 * It is referenced by Launcher.fxml to control the actions of the buttons
 * shown within that view. The program can also be exited from this window.
 */
import javafx.event.ActionEvent;

public class Launcher {
        public void labMode(ActionEvent lab) throws Exception{
        //Switch to "Lab Mode" window
        System.out.println("Entering Lab Mode");
        try {
            ThrustStand t = new ThrustStand();
            t.changeScene("LabMode.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dynoMode(ActionEvent dyno) throws Exception{
        //Switch to "Dyno Mode" window
        System.out.println("Entering Dyno Mode");
        try {
            ThrustStand t = new ThrustStand();
            t.changeScene("DynoMode.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exit(ActionEvent exit){
        //Exit the program gracefully
        System.out.println("Goodbye!");
        System.exit(0);
    }
}
