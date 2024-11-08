#include <Wire.h>
#include <HX711.h>

//Pin Definitions
const int PITOT_FRONT = A0;
const int PITOT_REAR = A1;
const int I_SENS = A2; //Current Sensor
const int V_SENS = A3; //Voltage Sensor
const int HX711_DAT = 18;
const int HX711_CLK = 19;
const int IR_RECIV = 5;
const int ESC_OUT = 2;

//HX711 setup
HX711 loadCell;

//Speed Controller output
const int PWM_FREQ = 50; //50Hz output for standard RC control
const int PWM_RES = 12; //12-bit PWM resolution
const int MIN_PULSE = 164; //~1000us at 50Hz
const int MAX_PULSE = 328; //~2000us at 50Hz

const unsigned long SEND_INTERVAL = 20; //Send interval in ms. 20ms = 1/50th of a second

//Serial control input buffer variables
String inputBuffer = "";
bool msgRDY = false;

//Serial send timer
unsigned long lastSendTime = 0;

//Tachometer variables
volatile unsigned long lastPulseTime = 0;
volatile unsigned long pulseCount = 0;
unsigned long lastRPM = 0;
const unsigned long RPM_CALC_INTERVAL = 100000; //Calculate RPM every 100ms
int numBlades = 2; //Default to 2 blade propeller
float currentRPM = 0.0;
const unsigned long RPM_TIMEOUT = 2000000; //Timeout RPM to 0 after 2 seconds

//Debounce the interrupt
const unsigned long DEBOUNCE_MICROS = 1000; //1ms debounce (May need to be shorter)
volatile unsigned long lastInterruptTime = 0;
unsigned long lastRPMcalc = 0; // Added missing variable declaration

// Global variables for control modes and PID
bool motorEnabled = false;
String currentMode = "LAB"; // Default to LAB mode
float targetRPM = 0;

// PID constants
const float KP = 0.1;  // Start conservative, tune as needed
const float KI = 0.05;
const float KD = 0.02;

// PID variables
float lastError = 0;
float integral = 0;
unsigned long lastPIDTime = 0;

void FASTRUN rpmInterrupt() {
  unsigned long currentInterruptTime = micros(); // Changed to unsigned long
  //Debounce
  if (currentInterruptTime - lastInterruptTime > DEBOUNCE_MICROS) {
    pulseCount++;
    lastPulseTime = currentInterruptTime;
    lastInterruptTime = currentInterruptTime;
  }
}

void setup() {
  Serial.begin(2000000);

  //Configure ADC
  analogReadResolution(12);

  //Configure PWM output
  analogWriteFrequency(ESC_OUT, PWM_FREQ);
  analogWriteResolution(PWM_RES);
  pinMode(ESC_OUT, OUTPUT);
  analogWrite(ESC_OUT, MIN_PULSE);

  //RPM "Trip Wire"
  pinMode(IR_RECIV, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(IR_RECIV), rpmInterrupt, FALLING); 

  //Configure load cell ADC
  loadCell.begin(HX711_DAT, HX711_CLK);
  loadCell.set_gain(128); // Corrected from gain() to set_gain()
}

void loop() {
  unsigned long currentTime = micros();

  if(currentTime - lastRPMcalc >= RPM_CALC_INTERVAL) {
    calculateRPM(currentTime);
    lastRPMcalc = currentTime;
  }

  // Handle PID control for DYNO mode
  if (currentMode == "DYNO" && motorEnabled && targetRPM > 0) {
    float deltaTime = (currentTime - lastPIDTime) / 1000000.0; // Convert to seconds
    
    if (deltaTime >= 0.05) { // Update PID every 50ms
      // Calculate error
      float error = targetRPM - currentRPM;
      
      // Calculate integral term with anti-windup
      integral = constrain(integral + (error * deltaTime), -20, 20);
      
      // Calculate derivative term
      float derivative = (error - lastError) / deltaTime;
      
      // Calculate PID output
      float output = (KP * error) + (KI * integral) + (KD * derivative);
      
      // Convert PID output to throttle percentage
      int throttle = constrain(output, 0, 100);
      
      // Update variables for next iteration
      lastError = error;
      lastPIDTime = currentTime;
      
      // Set the throttle
      setESCThrottle(throttle);
    }
  }  // Added missing closing brace for PID control block

  checkForMSG(); //Read serial for command

  if (msgRDY) {
    processMSG();
    msgRDY = false;
    inputBuffer = "";
  }

  //Send sensor data on send interval
  if (currentTime - lastSendTime >= SEND_INTERVAL * 1000) {
    sendData();
    lastSendTime = currentTime;
  }
}

void calculateRPM(unsigned long currentTime) {
  noInterrupts();
  unsigned long localLastPulseTime = lastPulseTime;
  unsigned long localPulseCount = pulseCount;
  pulseCount = 0;
  interrupts();
  
  if(currentTime - localLastPulseTime > RPM_TIMEOUT) {
    currentRPM = 0;
    return;
  }

  float minutesFrac = (float)RPM_CALC_INTERVAL / 60000000.0;
  currentRPM = (localPulseCount / (float)numBlades) / minutesFrac;
}

void checkForMSG() {
  while (Serial.available()) {
    char inChar = (char)Serial.read();
    if(inChar == '\n') {
      msgRDY = true;
      return;
    }
    inputBuffer += inChar;
  }
}

void processMSG() {
  String cmd = inputBuffer.substring(0, 4); // Get command prefix
  String value = inputBuffer.substring(4);  // Get value after prefix
  
  if (cmd.equals("THR:")) {
    // Only process throttle commands in LAB mode
    if (currentMode == "LAB" && motorEnabled) {
      int throttle = value.toInt();
      setESCThrottle(throttle);
    }
  }
  else if (cmd.equals("MODE")) {
    currentMode = value;
    // Reset control variables when switching modes
    integral = 0;
    lastError = 0;
    targetRPM = 0;
    setESCThrottle(0); // Safety: set throttle to 0 when changing modes
  }
  else if (cmd.equals("BLAD")) {
    numBlades = value.toInt();
  }
  else if (cmd.equals("MOTO")) {
    if (value.equals("ON")) {
      motorEnabled = true;
    } else if (value.equals("OFF")) {
      motorEnabled = false;
      setESCThrottle(0); // Safety: disable motor output
    }
  }
  else if (cmd.equals("RPM:")) {
    if (currentMode == "DYNO") {
      targetRPM = value.toFloat();
    }
  }
}

void setESCThrottle(int throttlePercent) {
  throttlePercent = constrain(throttlePercent, 0, 100);
  int pulseWidth = map(throttlePercent, 0, 100, MIN_PULSE, MAX_PULSE);
  analogWrite(ESC_OUT, pulseWidth);
}

void sendData() {
  float pitot1 = readVoltage(PITOT_FRONT);
  float pitot2 = readVoltage(PITOT_REAR);
  float current = readVoltage(I_SENS);
  float voltage = readVoltage(V_SENS);
  long rawLoadCell = loadCell.read(); // Changed from thrustSensor to loadCell

  String dataPackage = packageData(pitot1, pitot2, current, voltage, rawLoadCell, currentRPM);
  Serial.println(dataPackage);
}

float readVoltage(int pin) {
  int rawValue = analogRead(pin);
  return (rawValue / 4095.0) * 3.3;
}

String packageData(float pitot1, float pitot2, float current, float voltage, long rawLoadCell, float rpm) {
  return String(pitot1, 3) + "," + String(pitot2, 3) + "," + 
         String(current, 3) + "," + String(voltage, 3) + "," + // Changed v_in to voltage
         String(rawLoadCell) + "," + String(rpm, 1);
}

