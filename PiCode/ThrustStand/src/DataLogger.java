/*  Title:  DataLogger.Java
 *  Author: Evan Schober
 *  Email:  evan.e.schober@wmich.edu
 *  Parent Class: ThrustStand.java
 *  Description: This class handles logging data from the thrust stand to a CSV file
 *  Referenced by SharedElements.java
 */

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DataLogger {
    private static final String[] HEADERS = {
        "Timestamp",
        "Thrust",
        "Thrust Unit",
        "Incoming Airspeed",
        "Wake Airspeed",
        "Airspeed Unit",
        "Current (A)",
        "Voltage (V)",
        "RPM",
        "Blade Count"
    };
    
    private PrintWriter writer;
    private String baseFilePath;
    private boolean appendTimestamp;
    private String currentFilePath;
    
    public DataLogger() {
        this.baseFilePath = System.getProperty("user.home") + "/thrust_data";
        this.appendTimestamp = true;
    }
    
    public void setFilePath(String path) {
        this.baseFilePath = path;
    }
    
    public void setAppendTimestamp(boolean append) {
        this.appendTimestamp = append;
    }
    
    public String getCurrentFilePath() {
        return currentFilePath;
    }
    
    public void startLogging() throws IOException {
        if (writer != null) {
            stopLogging();
        }
        
        String filePath = baseFilePath;
        if (appendTimestamp) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            filePath += "_" + timestamp;
        }
        filePath += ".csv";
        
        // Create directories if they don't exist
        new File(filePath).getParentFile().mkdirs();
        
        writer = new PrintWriter(new FileWriter(filePath));
        currentFilePath = filePath;
        
        // Write headers
        writer.println(String.join(",", HEADERS));
        writer.flush();
    }
    
    public void logData(String thrust, String thrustUnit, 
                       String incomingAirspeed, String wakeAirspeed, String airspeedUnit,
                       String current, String voltage, 
                       int bladeCount, String rpm) {
        if (writer != null) {
            List<String> values = new ArrayList<>();
            values.add(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            values.add(thrust);
            values.add(thrustUnit);
            values.add(incomingAirspeed);
            values.add(wakeAirspeed);
            values.add(airspeedUnit);
            values.add(current);
            values.add(voltage);
            values.add(rpm);
            values.add(String.valueOf(bladeCount));
            
            writer.println(String.join(",", values));
            writer.flush();
        }
    }
    
    public void stopLogging() {
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }
}