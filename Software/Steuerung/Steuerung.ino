/*
  Arduino-Programm der FHTW-Drohne
*/

/*
**  Bibliotheken
*/
#include<Servo.h>

#define THROTTLE 1
#define ROLL 2
#define PITCH 3
#define YAW 4

#define ARM 11
#define DISARM 12
#define HOLD_ON 13
#define HOLD_OFF 14

#define START_CMD_CHAR '*'

/*
**  Pinbelegung
**  Throttle  Arduino-Pin  3
**  Roll      Arduino-Pin  6
**  Pitch     Arduino-Pin  7
**  Yaw       Arduino-Pin  9
**  Aux1      Arduino-Pin 10
**  Aux2      Arduino-Pin 11
**  Bluetooth-Modul Rx(0), Tx(1) als Serial1
*/
unsigned int Pin_Throttle=3;
/*
**  Beschreibung Throttle
**  Gas
*/
unsigned int Pin_R=6;
unsigned int Pin_P=7;
unsigned int Pin_Y=9;
/*
**  Beschreibung R/P/Y
**  Lenkwinkel der Drohne
*/
unsigned int Pin_Aux_1=10;
/*
**  Beschreibung Aux1
**  Einschalten des Altitude Hold Modus
*/
unsigned int Pin_Aux_2=11;
/*
**  Beschreibung Aux2
**  Armen der Motoren
*/
int tmp=-1;

int min_width=1000;
int max_width=2000;

Servo ServoT, ServoR, ServoP, ServoY, ServoA1, ServoA2;


void setup(void)
{
  /**
    * Initialisierung der Servosignale zur Steuerung des FC
    */
  ServoT.attach(Pin_Throttle, min_width,max_width);
  ServoR.attach(Pin_R, min_width,max_width);
  ServoP.attach(Pin_P, min_width,max_width);
  ServoY.attach(Pin_Y, min_width,max_width);
  ServoA1.attach(Pin_Aux_1, min_width,max_width);
  ServoA2.attach(Pin_Aux_2, min_width,max_width);
  ServoT.write(0);
  ServoR.write(90);
  ServoP.write(90);
  ServoY.write(90);
  ServoA1.write(0);
  ServoA2.write(0);
  analogWrite(13,13);

  Serial1.begin(9600);
}
void loop(void)
{
  int command = 0;
  int value = 0;
  char get_char = '\0';
  
  // wiat for incoming data
  if(Serial1.available() < 1) return;
  
  // parse incoming command
  get_char = (char)Serial1.read();
  if (get_char != START_CMD_CHAR) return;
  
  // parse incoming command type
  command = Serial1.parseInt(); // read command

  // parse incoming command value
  value = Serial1.parseInt();
  
  // FOLLOW THE COMMAND
  switch(command) {
    case THROTTLE:
      Throttle(value);
      break;
    case ROLL:
      Roll(value);
      break;
    case PITCH:
      Pitch(value);
      break;
    case YAW:
      Yaw(value);
      break;
    case ARM:
      Arm();
      break;
    case DISARM:
      Disarm();
      break;
    case HOLD_ON:
      Hold(true);
      break;
    case HOLD_OFF:
      Hold(false);
      break;
  }
}

void HelpMsg(void)
{
  Serial.println("Steuerung der FHTW-Drohne");
  Serial.println("Befehle");
  Serial.println("Help\tDiese Nachricht");
  Serial.println("Hilfe\tDiese Nachricht");
  Serial.println("ARM\tArmen des Quadcopters");
  Serial.println("DISARM\tDisrmen des Quadcopters");
  Serial.println("HOLD ON\tAktuelle Hoehe halten");
  Serial.println("HOLD OFF\tAktuelle Hoehe nicht mehr halten");
}
void Arm(void)
{
  Serial.println("Arming...");
  Serial.println("Throttle must be 0!");
  Throttle(0);
  if(0==ServoA2.read())
    ServoA2.write(180);
  analogWrite(13,255);
}
void Disarm(void)
{
  Serial.println("Disarming...");
  if(180==ServoA2.read())
    ServoA2.write(0);
  analogWrite(13,13);
}
void Throttle(int DC)
{
  tmp=map(DC, 0, 1000, min_width, max_width);
  Serial.print("Throttle-DC: ");
  Serial.print(DC);
  Serial.print(" Pulsweite: ");
  Serial.println(tmp);
  ServoT.writeMicroseconds(tmp);
}
void Roll(int DC)
{
  tmp=map(DC, -500, 500, min_width, max_width);
  Serial.print("Roll-DC: ");
  Serial.print(DC);
  Serial.print(" Pulsweite: ");
  Serial.println(tmp);
  ServoR.writeMicroseconds(tmp);
}
void Pitch(int DC)
{
  tmp=map(DC, -500, 500, min_width, max_width);
  Serial.print("Pitch-DC: ");
  Serial.print(DC);
  Serial.print(" Pulsweite: ");
  Serial.println(tmp);
  ServoP.writeMicroseconds(tmp);
}
void Yaw(int DC)
{
  tmp=map(DC, -500, 500, min_width, max_width);
  Serial.print("Yaw-DC: ");
  Serial.print(DC);
  Serial.print(" Pulsweite: ");
  Serial.println(tmp);
  ServoY.writeMicroseconds(tmp);
}
void Hold(boolean state)
{
  Serial.print("Altitude Hold ");
  if(state)
  {
    Serial.println("on...");
    if(0==ServoA1.read())
    {
      ServoA1.write(180);
    }
  }else
  {
    Serial.println("off...");
    if(180==ServoA1.read())
    {
      ServoA1.write(0);
    }
  }
}
