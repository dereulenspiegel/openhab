/*
Simple controller for heating controlled via LTC1257
*/
//Dummy code, but without it we run into some kind of compiler troubel
int i = 0;
#ifndef EMERGENCY_ADDON
#define EMERGENCY_ADDON true
#endif

#if EMERGENCY_ADDON
#define REQUIRESALARMS false
#include <DallasTemperature.h>
#include <OneWire.h>

#include <EEPROM.h>

#define ONE_WIRE_BUS 8
#define COMMAND_SET_MIN_TEMP 0x02
#define COMMAND_SET_EMERGENCY_SETTING 0x03
#define COMMAND_REQUEST_TEMPERATURE 0x04
const int EEPROM_TEMP_THRESHOLD = 0;
const int EEPROM_EMERGENCY_SETTING = 1;

OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensors(&oneWire);

float measuredTemp=300.0;

int tempThreshold = -1;
int emergencySetting = -1;

DeviceAddress tempSensorAddress;

boolean tempSensorAvailable = false;
#endif

#define LATCHTIMING	0x01				/* spend some time to the latch	*/
#define COMMAND_LENGTH 2

#define COMMAND_SET_HEATER 0x01

int CLOCK = 3;
int DATA = 4;
int LOAD = 5;

int currentPercent = 0;

int buffer[COMMAND_LENGTH];

void ltc1257_ll_init(void)
{
	/* Initial port/pin state */
  digitalWrite(CLOCK,LOW);				/* clock pin low -> idle	*/
  digitalWrite(LOAD,HIGH);				/* load pin high -> idle	*/
}

void ltc1257_ll_write(unsigned int data)
{
  volatile unsigned char bitctr = 0;
	
  for(bitctr = 0; bitctr < 0x0C; bitctr++)
  {
    digitalWrite(DATA,((data & 0x800)==0)?LOW:HIGH); 			
    /* output MSB (bits [11..0]!)		*/
    data <<= 1;				/* shift next bit to MSB		*/
    digitalWrite(CLOCK,HIGH);			/* rising edge -> load bit		*/
    digitalWrite(CLOCK,LOW);			/* -> await rising edge			*/
  }
	
  digitalWrite(CLOCK,LOW);				/* clock pin low -> idle		*/
  digitalWrite(LOAD,LOW);				/* load pulled low -> output		*/
	
  for (bitctr = 0; bitctr < LATCHTIMING; bitctr++)
    ;
	
  digitalWrite(LOAD,HIGH);				/* load pulled high -> idle		*/
}


void ioinit(void)
{
  pinMode(CLOCK,OUTPUT);
  pinMode(DATA,OUTPUT);
  pinMode(LOAD,OUTPUT);
 
  /* Low-level init of DAC: */
  ltc1257_ll_init();
}



void setup() {
  ioinit();
  Serial.begin(9600);
  while(!Serial) {
    ; //Wait
  }
  #if EMERGENCY_ADDON
  tempThreshold = EEPROM.read(EEPROM_TEMP_THRESHOLD);
  emergencySetting = EEPROM.read(EEPROM_EMERGENCY_SETTING);
  sensors.begin();
  sensors.setWaitForConversion(false);
  if(sensors.getDeviceCount() == 1){
    tempSensorAvailable = true;
    sensors.getAddress(tempSensorAddress, 0);
    sensors.requestTemperatures();
  } else {
   Serial.println("WARNING: No temp sensor found"); 
  }
  #endif
  setHeating(0);
}

void loop() {
  #if EMERGENCY_ADDON
 if(tempSensorAvailable && sensors.isConversionAvailable(tempSensorAddress)){
   measuredTemp = sensors.getTempC(tempSensorAddress);
   sensors.requestTemperatures();
 }
 #endif
 int counter = 0;
 while(Serial.available() > 0 && counter < COMMAND_LENGTH){
   buffer[counter] = Serial.read();
   counter++;
 }
 int percent = currentPercent;
 if(buffer[0] == COMMAND_SET_HEATER){
   percent = buffer[1];
 }
 #if EMERGENCY_ADDON 
 else if(buffer[0] == COMMAND_SET_MIN_TEMP){
   EEPROM.write(EEPROM_TEMP_THRESHOLD, (byte)buffer[1]);
   tempThreshold = buffer[1];
 } else if(buffer[0] == COMMAND_SET_EMERGENCY_SETTING){
   EEPROM.write(EEPROM_EMERGENCY_SETTING,(byte)buffer[1]);
   emergencySetting = buffer[1];
 } else if(buffer[0] == COMMAND_REQUEST_TEMPERATURE){
   Serial.print("TEMP:");
   Serial.println(measuredTemp);
 }
 #endif 
 
 #if EMERGENCY_ADDON
 if(tempSensorAvailable && measuredTemp <= tempThreshold && tempThreshold != 0xFF){
   percent = emergencySetting;
 }
 #endif
 if(percent != currentPercent){
   setHeating(percent);
 }
}

void setHeating(int percent) {
  int value = -1;
  if(percent == 0) {
    value = 0;
  } else if(percent > 99) {
    value = 4095;
  } else {
    value = (4095.0 * percent)/100.0;
  }
  ltc1257_ll_write(value);
  Serial.print("SET:");
  Serial.print(value);
  Serial.print("/");
  Serial.println("4095");
  currentPercent = percent;
}
