/* Project: MotorThrustStand 
 * 
 * Title: ThrustStand.Java
 * Author: Evan Schober
 * Email: evan.e.schober@wmich.edu
 * Description: This is the parent class for the thrust stand it initializes the primary
 * application stage and then calls Launcher.fxml and its corresponding controller class
 * to fill the stage with the launcher scene. 
 */

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class ThrustStand extends Application{
    private SerialController serialController; //Serial communication object for maintaining data 
    private Stage mainStage; //Generic top level "stage" that we can replace with other scenes.

    public static void main(String[] args) {
        System.out.println("Launching Thrust Stand..."); //Output a startup message to the terminal
        // try {
        //     //Verify two-way serial communication with Teensy 4.0
        //     System.out.println("Initializing Serial Communication...");
        //     //TODO: Write method for serial handshake
        //     /* Standard 3-way handshake:
        //      * Syn -> Teensy
        //      * Syn + Ack <- Teensy
        //      * Ack -> Teensy
        //      */
        //     //May need serial helper class
        // } catch (Exception e) {
        //     // TODO: handle exception
        //     //If unable to establish serial connection create a popup error message
        //     System.out.println("Unable to reach Teensy"); //Write the error message to the terminal
        //     //Create a new alert message and present it to the user
        //     Alert serial = new Alert(AlertType.ERROR);
        //     serial.setTitle("Thrust Stand: Error");
        //     serial.setHeaderText("Serial Communication Error");
        //     serial.setContentText("Issue establishing serial connection with thrust stand.\nPlease check connection and restart application.");
        //     serial.showAndWait(); //Wait for the user to acknowledge the error
        //     System.exit(1); //Exit and indicate program did not close succesfully
        // }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.mainStage = primaryStage;
        this.serialController = new SerialController();

        //Create the initial launcher window 
        changeScene("fxml/Launcher.fxml");
        //Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/Launcher.fxml")); //Load FXML file for launcher window
        //Scene scene = new Scene(root); //Create a new scene and set size to the max of the Pi's display
        mainStage.setTitle("ThrustStand"); //Create a title for the scene
        //mainStage.setScene(scene); //Add the scene to the existing stage
        mainStage.show(); //Launch application
    }

    //Scene changer method called by child controller classes
    public void changeScene(String fxml) throws Exception{
        //Parent pane = FXMLLoader.load(getClass().getResource(fxml));
        //mainStage.getScene().setRoot(pane);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        controller.setSerialController(serialController);
        controller.setMainApplication(this);

        if (mainStage.getScene() == null) {
            Scene scene = new Scene(root);
            mainStage.setScene(scene);
        } else {
            mainStage.getScene().setRoot(root);
        }
    }

    @Override
    public void stop(){
        //Close the serial port when the application closes
        if (serialController != null) {
            serialController.closePort();
        }
    }
}
