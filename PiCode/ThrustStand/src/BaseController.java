/*  Title:  BaseController.Java
 *  Author: Evan Schober
 *  Email:  evan.e.schober@wmich.edu
 *  Parent Class: ThrustStand.java
 *  Description: Abstract class extended by each scene controller to maintain
 *  continuous serial connection as scenes change.  
 */
public abstract class BaseController {
    protected SerialController serialController;
    protected SharedElements sharedElements;
    protected ThrustStand thrustStand;


    public BaseController(){
        this.serialController = SerialController.getInstance();
        this.sharedElements = SharedElements.getInstance();
    }
    
    public void setMainApplication(ThrustStand thrustStand){
        this.thrustStand = thrustStand;
    }
}
