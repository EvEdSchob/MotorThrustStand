/* Project: MotorThrustStand
 * Title: ThrustStand.Java
 * Author: Evan Schober
 * Email: evan.e.schober@wmich.edu
 * Description: This is the parent class for the thrust stand. It initializes the primary
 * application stage and then calls Launcher.fxml and its corresponding controller class
 * to fill the stage with the launcher scene. 
 */

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class ThrustStand extends Application{
    private Stage mainStage; //Generic top level "stage" that we can replace with other scenes.
    private String css = getClass().getResource("/styles/styles.css").toExternalForm(); //Load Stylesheet

    public static void main(String[] args) {
        //System.out.println("Launching Thrust Stand..."); //Debug
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.mainStage = primaryStage;

        //Initialize Singletons
        SerialController.getInstance();
        SharedElements.getInstance();

        changeScene("fxml/Launcher.fxml"); //Create the initial launcher window 
        mainStage.setTitle("ThrustStand"); //Create a title for the scene
        mainStage.initStyle(StageStyle.UNDECORATED);
        mainStage.setFullScreen(true); //Locks stage to fullscreen. Comment out for debugging
        mainStage.show(); //Launch application
    }

    //Scene changer method called by child controller classes
    public void changeScene(String fxml) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml)); //Load the input FXML file
        Parent root = loader.load(); //Create a parent object for insertion into a scene.

        BaseController controller = loader.getController();
        controller.setMainApplication(this);

        if (mainStage.getScene() == null) {
            Scene scene = new Scene(root); //If no scene exists, create one from the input FXML file
            scene.getStylesheets().add(css); //Add stylesheet after inital scene is created
            mainStage.setScene(scene); //Insert the scene into the main stage
        } else {
            mainStage.getScene().setRoot(root); //Replace an existing scene with the next one
            mainStage.getScene().getStylesheets().add(css); //Ensure stylesheet is applied on scene change
        }
    }

    @Override
    public void stop(){
        SerialController.getInstance().closePort(); //Close the serial port when the application closes
    }
}
