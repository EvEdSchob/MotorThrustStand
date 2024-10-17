/*  Title:  BaseController.Java
 *  Author: Evan Schober
 *  Email:  evan.e.schober@wmich.edu
 *  Parent Class: ThrustStand.java
 *  Description: Abstract class extended by each scene controller to maintain
 *  continuous serial connection as scenes change.git  
 */
public abstract class BaseController {
    protected SerialController serialController;
    protected ThrustStand thrustStand;

    public void setSerialController(SerialController serialController){
        this.serialController = serialController;
    }
    
    public void setMainApplication(ThrustStand thrustStand){
        this.thrustStand = thrustStand;
    }
}
