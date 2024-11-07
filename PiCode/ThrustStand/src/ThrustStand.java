/* Project: MotorThrustStand
 * Title: ThrustStand.Java
 * Author: Evan Schober
 * Email: evan.e.schober@wmich.edu
 * Description: This is the parent class for the thrust stand it initializes the primary
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
    //Load Stylesheet
    private String css = getClass().getResource("/styles/styles.css").toExternalForm();

    public static void main(String[] args) {
        System.out.println("Launching Thrust Stand..."); //Output a startup message to the terminal
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.mainStage = primaryStage;

        
        

        //Initialize Singletons
        SerialController.getInstance();
        SharedElements.getInstance();

        //Create the initial launcher window 
        changeScene("fxml/Launcher.fxml");
        mainStage.setTitle("ThrustStand"); //Create a title for the scene
        mainStage.initStyle(StageStyle.UNDECORATED);
        //mainStage.setFullScreen(true); //Locks stage to fullscreen. Comment out for debugging
        mainStage.show(); //Launch application
    }

    //Scene changer method called by child controller classes
    public void changeScene(String fxml) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Parent root = loader.load();

        BaseController controller = loader.getController();
        controller.setMainApplication(this);

        if (mainStage.getScene() == null) {
            Scene scene = new Scene(root);
            //Add stylesheet after inital scene is created
            scene.getStylesheets().add(css);
            mainStage.setScene(scene);
        } else {
            mainStage.getScene().setRoot(root);
            //Ensure stylesheet is applied on scene change
            mainStage.getScene().getStylesheets().add(css);
        }
    }

    @Override
    public void stop(){
        //Close the serial port when the application closes
        SerialController.getInstance().closePort();
    }
}
