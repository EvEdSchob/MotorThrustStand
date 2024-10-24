#include <Wire.h>

//Sensor libraries
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

//Speed Controller ouptut
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

//Thrust sensor variables
volatile unsigned long lastPulseTime = 0;
volatile unsigned long pulseCount = 0;
unsigned long lastRPM = 0;
const unsigned long RPM_CALC_INTERVAL = 100000; //Calculate RPM every 100ms
int numBlades = 2; //Default to 2 blade propeller
float currentRPM = 0.0;
const unsigned long RPM_TIMEOUT = 2000000; //Timeout RPM to 0 after 2 seconds

//Debounce the interrupt
const unsigned long DEBOUNCE_MICROS = 1000; //1ms debouce (May need to be shorter)
volatile unsigned long lastInterruptTime = 0;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(2000000);

  analogReadResolution(12); //Use maximum resolution of the onboard ADC

  //Configure load cell ADC
  loadCell.begin(HX711_DAT, HX711_CLK); //Set DAT and CLK pins
  loadCell.gain(128); //High-gain mode

  //Configure PWM output
  analogWriteFrequency(ESC_OUT, PWM_FREQ);
  analogWriteResolution(PWM_RES);
  pinMode(ESC_OUT, OUTPUT);
  analogWrite(ESC_OUT, MIN_PULSE); //Start the speed controller at minimum


  

}

void loop() {
  // put your main code here, to run repeatedly:
  unsigned long currentTime = micros();
  //Read serial for command
  receiveMSG();

  if (msgRDY){
    processMSG();
    msgRDY = false;
    inputBuffer = "";
  }

}

void calculateRPM(unsigned long currentTime){
  noInterrupts(); //Disable interrupt while reading volatiles
  unsigned long localLastPulseTime = lastPulseTime;
  unsigned long localPulseCount = pulseCount;
  pulseCount = 0; //Reset pulse count for next reading;
  interrupts(); //Re-enable interrupts

  //Check if motor has stopped
  if(currentTime - localLastPulseTime > RPM_TIMEOUT){
    currentRPM = 0;
    return;
  }

  //Calculate RPM based on pulse count and time interval
  float minutesFrac = (float)RPM_CALC_INTERVAL / 60000000.0; // Convert microseconds to minutes
  currentRPM = (localPulseCount / (float)numBlades) / minutesFrac;
}

void checkForMSG() {
  while (Serial.available()){
    char inChar = (char)Serial.read();
    if(inChar == '\n'){
      msgRDY = true;
      return;
    }
    inputBuffer += inChar;
  }
}
void processMSG(){
  //TODO: Define message flags and handle accordingly
}

void setESCThrottle(int throttlePercent){
  throttlePercent = constrain(throttlePercent, 0, 100);
  int pulseWidth = map(throttlePercent, 0, 100, MIN_PULSE, MAX_PULSE);
  analogWrite(ESC_OUT, pulseWidth);
}
void sendData(){
  float pitot1 = readVoltage(PITOT_FRONT);
  float pitot2 = readVoltage(PITOT_REAR);
  float current = readVoltage(I_SENS);
  float voltage = readVoltage(V_SENS);
  long rawLoadCell = thrustSensor.read();

  String dataPackage = packageData(pitot1, pitot2, current, voltage, rawLoadCell, currentRPM);
  Serial.println(dataPackage);
}


void lab() {
  //Control throttle level directly as a percentage
  //Check periodically for new commands
}

void dyno() {
  //Control throttle as a fixed RPM and use PID to keep consistent
}
