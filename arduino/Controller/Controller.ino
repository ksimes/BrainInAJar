/*
   Basic Pin setup:
   ------------                                  ---u----
   ARDUINO   13|-> SCLK (pin 25)           OUT1 |1     28| OUT channel 0
             12|                           OUT2 |2     27|-> GND (VPRG)
             11|-> SIN (pin 26)            OUT3 |3     26|-> SIN (pin 11)
             10|-> BLANK (pin 23)          OUT4 |4     25|-> SCLK (pin 13)
              9|-> XLAT (pin 24)             .  |5     24|-> XLAT (pin 9)
              8|                             .  |6     23|-> BLANK (pin 10)
              7|                             .  |7     22|-> GND
              6|                             .  |8     21|-> VCC (+5V)
              5|                             .  |9     20|-> 2K Resistor -> GND
              4|                             .  |10    19|-> +5V (DCPRG)
              3|-> GSCLK (pin 18)            .  |11    18|-> GSCLK (pin 3)
              2|                             .  |12    17|-> SOUT
              1|                             .  |13    16|-> XERR
              0|                           OUT14|14    15| OUT channel 15
   ------------                                  --------

   -  Put the longer leg (anode) of the LEDs in the +5V and the shorter leg
        (cathode) in OUT(0-15).
   -  +5V from Arduino -> TLC pin 21 and 19     (VCC and DCPRG)
   -  GND from Arduino -> TLC pin 22 and 27     (GND and VPRG)
   -  digital 3        -> TLC pin 18            (GSCLK)
   -  digital 9        -> TLC pin 24            (XLAT)
   -  digital 10       -> TLC pin 23            (BLANK)
   -  digital 11       -> TLC pin 26            (SIN)
   -  digital 13       -> TLC pin 25            (SCLK)
   -  The 2K resistor between TLC pin 20 and GND will let ~20mA through each
      LED.  To be precise, it's I = 39.06 / R (in ohms).  This doesn't depend
      on the LED driving voltage.
   - (Optional): put a pull-up resistor (~10k) between +5V and BLANK so that
                 all the LEDs will turn off when the Arduino is reset.

   If you are daisy-chaining more than one TLC, connect the SOUT of the first
   TLC to the SIN of the next.  All the other pins should just be connected
   together:
       BLANK on Arduino -> BLANK of TLC1 -> BLANK of TLC2 -> ...
       XLAT on Arduino  -> XLAT of TLC1  -> XLAT of TLC2  -> ...
   The one exception is that each TLC needs it's own resistor between pin 20
   and GND.

   This library uses the PWM output ability of digital pins 3, 9, 10, and 11.
   Do not use analogWrite(...) on these pins.

*/
#include "Tlc5940.h"
#include <Dht11.h>

#define DHT_DATA_PIN 2

#define FULL_ON 4090
#define HALF_ON 300

#define DELAY 100

#define STROBE_START_PIN 0
#define STROBE_END_PIN 6

#define FADE_START_PIN 7
#define FADE_END_PIN 15

#define FADE_STEP 80

#define RSET_START_PIN 16
#define RSET_END_PIN 31

boolean finished = false;
// mesgage array to hold incoming data
String msgs[10] = { "", "", "", "", "", "", "", "", "", ""};
boolean msgAvailable = false;  // whether the msg is complete
int lastMsg = 0;

void setup()
{
  /* Call Tlc.init() to setup the tlc.
     You can optionally pass an initial PWM value (0 - 4095) for all channels.*/
  Tlc.init();
  Serial.begin(19200);

  for (int i = 0; i < 10; i++) {
    msgs[i].reserve(20);
  }

  clearLEDs();
  randomSeed(analogRead(0));
}

void readTemp()
{
  static Dht11 sensor(DHT_DATA_PIN);
  static int count = 0;

  // Read the sensor every 40th pass through. (Every 4 seconds)
  if (count == 40)
  {
    count = 0;
    // Read sensor
    int chk = sensor.read();

    // Check the status
    switch (chk)
    {
      case Dht11::OK:
        Serial.print("H");
        Serial.print((float)sensor.getHumidity(), 2);
        Serial.print(" ");

        Serial.print("T");
        Serial.print((float)sensor.getTemperature(), 2);
        Serial.print(" ");

        break;
      case Dht11::ERROR_CHECKSUM:
        Serial.print("EC");     // Checksum error
        break;
      case Dht11::ERROR_TIMEOUT:
        Serial.print("ET");     // Time out error
        break;
      default:
        Serial.print("EU");     // Unknown error
        break;
    }

    Serial.print("\n");
  }
  else {
    count ++;
  }
}

/* Has the other machine set a stop signal? */
boolean readStatus()
{
  boolean result = false;

  if (msgAvailable) {
    int msgPointer = lastMsg;

    if (msgPointer == 0) {
      msgPointer = 9;
    }
    else {
      msgPointer --;
    }

    if ( msgs[msgPointer] == "END" ) {
      //     Serial.println("OK Ending");
      result = true;
    }
  }

  return result;
}

void doRandom()
{
  static int count = 0;
  int counter = (RSET_END_PIN - RSET_START_PIN) / 2;

  // Set the values of the leds every 10th pass through. (Every second)
  if (count == 10)
  {
    count = 0;
    for (int i = 0; i < counter; i++) {
      int led = random(RSET_START_PIN, RSET_END_PIN + 1);

      switch (random(5))
      {
        case 0:
          Tlc.set(led, 0);    // OFF
          break;
        case 1:
        case 2:
        case 3:
          Tlc.set(led, random(FULL_ON));
          break;
        default :
          Tlc.set(led, FULL_ON);
          break;
      }
    }
  }
  else {
    count ++;
  }
}

/* This function will create a Knight Rider-like effect on a block of LEDS*/
void doStrobe()
{
  static int count = 0;
  static int strobePoint = STROBE_START_PIN;
  static int strobeDirection = 1;

  // Set the values of the leds every 5th pass through. (Every half second)
  if (count == 5)
  {
    count = 0;
    for (int channel = STROBE_START_PIN; channel <= STROBE_END_PIN; channel ++) {
      Tlc.set(channel, 0);
    }

    if (strobePoint == STROBE_START_PIN) {
      strobeDirection = 1;
    } else {
      Tlc.set(strobePoint - 1, HALF_ON);
    }

    Tlc.set(strobePoint, FULL_ON);

    if (strobePoint != STROBE_END_PIN) {
      Tlc.set(strobePoint + 1, HALF_ON);
    } else {
      strobeDirection = -1;
    }

    strobePoint += strobeDirection;
  }
  else {
    count ++;
  }
}

/* This function will create a pulsating effect on a block of LEDS*/
void doFades()
{
  static int fadeDirection = FADE_STEP;
  static int fader = 0;

  fader += fadeDirection;

  if (fader > FULL_ON) {
    fadeDirection = -FADE_STEP;
    fader = FULL_ON;
  }
  else if (fader < 0) {
    fadeDirection = FADE_STEP;
    fader = 0;
  }

  for (int i = FADE_START_PIN; i <= FADE_END_PIN; i++) {
    Tlc.set(i, fader);
  }
}

void clearLEDs()
{
  Tlc.clear();
  for (int channel = 0; channel < NUM_TLCS * 16; channel ++) {
    Tlc.set(channel, 0);
  }
  /* Tlc.update() sends the data to the TLCs.  This is when the LEDs will actually change. */
  Tlc.update();
}

//void test()
//{
//    for (int channel = STROBE_START_PIN; channel <= STROBE_END_PIN; channel ++) {
//      Tlc.set(channel, 0);
//    }
//  
//      Tlc.set(STROBE_START_PIN + 6, FULL_ON);
//}

void setLEDs()
{
//    Tlc.clear();

//    test();

  doStrobe();
  doFades();
  doRandom();

  /* Tlc.update() sends the data to the TLCs.  This is when the LEDs will actually change. */
  Tlc.update();
}

void loop()
{
  if (!finished)
  {
    setLEDs();
    readTemp();
    finished = readStatus();
    delay(DELAY);
  }
  else {
    clearLEDs();
  }
}

/*
  SerialEvent occurs whenever a new data comes in the
  hardware serial RX.  This routine is run between each
  time loop() runs, so using delay inside loop can delay
  response.  Multiple bytes of data may be available.
*/
void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == '\n') {
      msgAvailable = true;
      //      Serial.print(msgs[lastMsg]);

      lastMsg += 1;
      if (lastMsg == 10) {
        lastMsg = 0;
      }
    }
    else {
      // else add it to the inputString:
      msgs[lastMsg] += inChar;
    }
  }
}



