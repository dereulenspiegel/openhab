#include <DallasTemperature.h>

#include <OneWire.h>

/*
Simple controller for heating controlled via LTC1257
*/

#define LATCHTIMING	0x01				/* spend some time to the latch	*/
#define ONE_WIRE_BUS 8

int CLOCK = 3;
int DATA = 4;
int LOAD = 5;

OneWire oneWire(ONE_WIRE_BUS);
DallasTemperature sensors(&oneWire);

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
  sensors.begin();
  setHeating(0);
}

void loop() {
 sensors.requestTemperatures();
 if(sensors.isConversionComplete()){
   float temp = sensors.getTempCByIndex(0)
 }
 if(Serial.available()>0) {
  int inByte = Serial.read();
  setHeating(inByte);
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
  Serial.print(value);
  Serial.print("/");
  Serial.println("4095");
}
