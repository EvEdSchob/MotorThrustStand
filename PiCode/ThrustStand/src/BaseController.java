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
