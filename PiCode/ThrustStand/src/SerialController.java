
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

    public boolean sendData(String data){
        if(serialPort != null && serialPort.isOpen()){
            String dataToSend = data + "\n";
            byte[] bytes = dataToSend.getBytes(StandardCharsets.UTF_8);
            int bytesWritten = serialPort.writeBytes(bytes, bytes.length);
            if(bytesWritten == -1){
                System.err.println("Failed to write data to serial port");
                return false;
            } else {
                System.out.println("Sent: " + dataToSend.trim());
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
}
