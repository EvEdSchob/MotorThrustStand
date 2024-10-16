


import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class ThrustStand extends Application{
    private static Stage mainStage; //Generic top level "stage" that we can replace with other scenes.

    public static void main(String[] args) {
        System.out.println("Launching Thrust Stand..."); //Output a startup message to the terminal
        try {
            //Verify two-way serial communication with Teensy 4.0
            System.out.println("Initializing Serial Communication...");
            //TODO: Write method for serial handshake
            /* Standard 3-way handshake:
             * Syn -> Teensy
             * Syn + Ack <- Teensy
             * Ack -> Teensy
             */
            //May need serial helper class
        } catch (Exception e) {
            // TODO: handle exception
            //If unable to establish serial connection create a popup error message
            System.out.println("Unable to reach Teensy"); //Write the error message to the terminal

            //Create a new alert message and present it to the user
            Alert serial = new Alert(AlertType.ERROR);
            serial.setTitle("Thrust Stand: Error");
            serial.setHeaderText("Serial Communication Error");
            serial.setContentText("Issue establishing serial connection with thrust stand.\nPlease check connection and restart application.");
            serial.showAndWait(); //Wait for the user to acknowledge the error

            System.exit(1); //Exit and indicate program did not close succesfully
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        mainStage = primaryStage;
        //TODO: Set fullscreen here? Test whether size params are sufficient

        //Create the initial launcher window 
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/Launcher.fxml")); //Load FXML file for launcher window
        Scene scene = new Scene(root, 800, 480); //Create a new scene and set size to the max of the Pi's display
        primaryStage.setTitle("ThrustStand"); //Create a title for the scene
        primaryStage.setScene(scene); //Add the scene to the existing stage
        primaryStage.show(); //Launch application
    }

    //Scene changer method called by child controller classes
    public void changeScene(String fxml) throws Exception{
        Parent pane = FXMLLoader.load(getClass().getResource(fxml));
        mainStage.getScene().setRoot(pane);
    }
}
