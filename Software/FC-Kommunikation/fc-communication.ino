#include <AltSoftSerial.h>

// Software Serial
AltSoftSerial SerialS; // RX 5, TX 13

void setup() {
  Serial.begin(9600);
  SerialS.begin(115200);
}


uint8_t rc_bytes[16] = { 0 };
// The opcode (message type ID) and message length for
// the MultiWii serial protocol's "SET_RAW_RC" message type.

// Send a message using the MultiWii serial protocol.
void send_msp(uint8_t opcode, uint8_t * data, uint8_t n_bytes) {
  uint8_t checksum = 0;
   
  // Send the MSP header and message length
  SerialS.write((byte *)"$M<", 3);
  SerialS.write(n_bytes);
  checksum ^= n_bytes;
 
  // Send the op-code
  SerialS.write(opcode);
  checksum ^= opcode;
   
  // Send the data bytes
  for(int i = 0; i < n_bytes; i++) {
    SerialS.write(data[i]);
    checksum ^= data[i];
  }
   
  // Send the checksum
  SerialS.write(checksum);
}

void loop() {
  Serial.flush();

  // request data from the flight controller
  send_msp(108, 0, 0);
  
  byte txt;
  // int32_t alt = 0;
  int16_t head = 0;	// heading of the drone will be read
  int i = 0;
  
  if(SerialS.available() > 0) {
    while(SerialS.available() > 0) {
      txt = SerialS.read();
      i++;
      /*
      // ANGX
      if (i == 4) {
        Serial.print(txt, DEC);
        Serial.print("-");
      }
      if (i == 6) {
        angx += ((int)txt);
      }
      if (i == 7) {
        angx += ((int)txt)*256;
        Serial.print(angx, DEC);
        Serial.print("||");
      } 
      */
      
      // HEADING
      // message-bytes are read here
      if (i == 4) {
	// debugging output over the usb serial connection
        Serial.print(txt, DEC);
        Serial.print("-");
      }
      if (i == 10) {
        head += ((int)txt);
      }
      if (i == 11) {
        head += ((int)txt)*256;
	// debugging output over the usb serial connection
        Serial.print(head, DEC);
        Serial.print("||");
      }
/*
      // BARO
      if (i == 4) {
        Serial.print(txt, DEC);
        Serial.print("-");
      }
      if (i == 6) {
        alt += ((int)txt);
      }
      if (i == 7) {
        alt += ((int)txt)*256;
      }
      if (i == 8) {
        alt += ((int)txt)*256*256;
      }
      if (i == 9) {
        alt += ((int)txt)*256*256*256;
        Serial.print(alt, DEC);
        Serial.print("||");
      }
*/
    }
    // debugging output over the usb serial connection
    Serial.println(i, DEC);
  }
  delay(10);
}


