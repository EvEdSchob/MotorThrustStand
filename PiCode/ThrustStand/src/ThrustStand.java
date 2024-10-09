import javax.sound.midi.SysexMessage;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;


public class ThrustStand extends Application{
    public static void main(String[] args) {
        System.out.println("Launching Thrust Stand...");
        try {
            //Verify two-way serial communication with Teensy 4.0
            System.out.println("Initializing Serial Communication...");
        } catch (Exception e) {
            // TODO: handle exception
            //If unable to establish serial connection 
            System.out.println("Unable to reach Teensy");
            Alert serial = new Alert(AlertType.ERROR);
            serial.setTitle("Thrust Stand: Error");
            serial.setHeaderText("Serial Communication Error");
            serial.setContentText("Issue establishing serial connection with thrust stand.\nPlease check connection and restart application.");
            serial.showAndWait();
            System.exit(1); //Indicate program did not close succesfully

        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("Launcher.fxml")); //Load FXML file for launcher window
        Scene scene = new Scene(root); //Create a new scene
        primaryStage.setTitle("ThrustStand"); //Create a title for the scene
        primaryStage.setScene(scene); //Add the scene to the existing stage
        primaryStage.show(); //Launch application
    }
    public void labMode(ActionEvent lab){
        System.out.println("Entering Lab Mode");
        //Switch to "Lab Mode" window
    }

    public void dynoMode(ActionEvent dyno){
        System.out.println("Entering Dyno Mode");
        //Switch to "Dyno Mode" window
    }

    public void exit(ActionEvent exit){
        //Exit the program gracefully
        System.exit(0);
    }

}
