/*  Title:  SerialController.Java
 *  Author: Evan Schober
 *  Email:  evan.e.schober@wmich.edu
 *  Parent Class: ThrustStand.java
 *  Description: 
 *  
 */
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import javafx.application.Platform;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.Arrays;

public class SerialController {
    private static SerialController instance;
    private SerialPort serialPort;
    private List<Consumer<String>> dataReceivedListeners = new ArrayList<>();

    // Data indices for parsing CSV
    private static final int PITOT1_INDEX = 0;
    private static final int PITOT2_INDEX = 1;
    private static final int CURRENT_INDEX = 2;
    private static final int VOLTAGE_INDEX = 3;
    private static final int LOADCELL_INDEX = 4;
    private static final int RPM_INDEX = 5;

    // Command prefixes for sending to Teensy
    private static final String CMD_SET_THROTTLE = "THR:";  // THR:50 for 50% throttle
    private static final String CMD_SET_MODE = "MODE:";     // MODE:LAB or MODE:DYNO
    private static final String CMD_SET_BLADES = "BLADE:";  // BLADE:2 for 2 blades
    private static final String CMD_MOTOR = "MOTOR:";       // MOTOR:ON or MOTOR:OFF

    
    
    private SerialController() {}

    public static SerialController getInstance(){
        if(instance == null){
            instance = new SerialController();
        }
        return instance;
    }

    //Public method for opening a given serial port
    public boolean openPort(String portName, int buadRate){
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(buadRate);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(1);
        serialPort.setParity(SerialPort.NO_PARITY);

        if(serialPort.openPort()){
            System.out.println("Connection with " + serialPort.getSystemPortName() + " opened successfully!");
            addDataListener();
            return true;
        } else {
            System.out.println("Connection with " + serialPort.getSystemPortName() + " failed to open!");
            return false;
        }
    }

    //Public method for closing a serial port
    public void closePort(){
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Closed port: " + serialPort.getSystemPortName());
        }
    }

    // public boolean sendData(String data){
    //     if(serialPort != null && serialPort.isOpen()){
    //         String dataToSend = data + "\n";
    //         byte[] bytes = dataToSend.getBytes(StandardCharsets.UTF_8);
    //         int bytesWritten = serialPort.writeBytes(bytes, bytes.length);
    //         if(bytesWritten == -1){
    //             System.err.println("Failed to write data to serial port");
    //             return false;
    //         } else {
    //             System.out.println("Sent: " + dataToSend.trim());
    //             return true;
    //         }
    //     } else {
    //         System.err.println("Serial port is not open");
    //         return false;
    //     }
    // }

    public boolean sendData(String data) {
        if(serialPort != null && serialPort.isOpen()) {
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            int bytesWritten = serialPort.writeBytes(bytes, bytes.length);
            if(bytesWritten == -1) {
                System.err.println("Failed to write data to serial port");
                return false;
            } else {
                System.out.println("Sent: " + data.trim());
                return true;
            }
        } else {
            System.err.println("Serial port is not open");
            return false;
        }
    }

    public void addDataReceivedListener(Consumer<String> listener){
        dataReceivedListeners.add(listener);
    }

    public void removeDataReceivedListner(Consumer<String> listener){
        dataReceivedListeners.remove(listener);
    }


    //Internal method to create a data listener
    private void addDataListener() {
        serialPort.addDataListener(new SerialPortMessageListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }

            @Override
            public byte[] getMessageDelimiter() { return new byte[] { (byte)'\n' }; }

            @Override
            public boolean delimiterIndicatesEndOfMessage() { return true; }

            @Override
            public void serialEvent(SerialPortEvent event) {
                byte[] newData = event.getReceivedData();
                String message = new String(newData, StandardCharsets.UTF_8).trim();
                Platform.runLater(() -> {
                    for (Consumer<String> listener : dataReceivedListeners) {
                        listener.accept(message);
                    }
                });
            }
        });
    }

    public String[] getAvailablePorts() {
        return Arrays.stream(SerialPort.getCommPorts())
                .map(SerialPort::getSystemPortName)
                .toArray(String[]::new);
    }

    // Method to parse received data
    public void parseData(String data) {
        try {
            String[] values = data.trim().split(",");
            if (values.length == 6) {  // Verify we got all expected values
                float pitot1 = Float.parseFloat(values[PITOT1_INDEX]);
                float pitot2 = Float.parseFloat(values[PITOT2_INDEX]);
                float current = Float.parseFloat(values[CURRENT_INDEX]);
                float voltage = Float.parseFloat(values[VOLTAGE_INDEX]);
                long loadCell = Long.parseLong(values[LOADCELL_INDEX]);
                float rpm = Float.parseFloat(values[RPM_INDEX]);
                
                // Now update UI elements through SharedElements
                SharedElements.getInstance().updateMeasurements(
                    loadCell,    // You'll need to convert raw value to actual units
                    pitot1,      // Convert voltage to airspeed
                    pitot2,      // Convert voltage to airspeed
                    current,     // Convert to actual current
                    voltage,     // Convert to actual voltage
                    rpm
                );
            }
        } catch (NumberFormatException e) {
            System.err.println("Error parsing data: " + data);
            e.printStackTrace();
        }
    }

    // Methods to send commands to Teensy
    public boolean setThrottle(int percentage) {
        String command = String.format("THR:%d\n", percentage); //May change back to previous format for consistency consider updating other methods to match this format
        return sendData(command);
    }

    public boolean setMode(String mode) {
        if (mode.equals("LAB") || mode.equals("DYNO")) {
            return sendData(CMD_SET_MODE + mode);
        }
        return false;
    }

    public boolean setBladeCount(int blades) {
        return sendData(CMD_SET_BLADES + blades);
    }

    public boolean setMotor(boolean on) {
        return sendData(CMD_MOTOR + (on ? "ON" : "OFF"));
    }
}
