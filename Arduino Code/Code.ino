#include <deprecated.h>
#include <MFRC522.h>
#include <MFRC522Extended.h>
#include <require_cpp11.h>
#include <SPI.h>
#include "MFRC522.h"

#include <ESP8266WiFi.h>
#include <ESP8266WiFiAP.h>
#include <ESP8266WiFiGeneric.h>
#include <ESP8266WiFiMulti.h>
#include <ESP8266WiFiScan.h>
#include <ESP8266WiFiSTA.h>
#include <ESP8266WiFiType.h>
#include <WiFiClient.h>
#include <WiFiClientSecure.h>
#include <WiFiServer.h>
#include <WiFiUdp.h>

#include <Firebase.h>
#include <FirebaseArduino.h>
#include <FirebaseCloudMessaging.h>
#include <FirebaseError.h>
#include <FirebaseHttpClient.h>
#include <FirebaseObject.h>

#include <Wire.h> //I2C library
#include <RtcDS3231.h> //RTC library

RtcDS3231<TwoWire> rtcObject(Wire);

#define FIREBASE_HOST "--"
#define FIREBASE_AUTH "--"
#define WIFI_SSID "Monish"
#define WIFI_PASSWORD "123456789"

#define RST_PIN  D3
#define SS_PIN   D4

MFRC522 mfrc522(SS_PIN, RST_PIN); // Create MFRC522 instance

String Arrival_Time,Departed_Time; 
void setup() {
  
    Serial.begin(115200);
    delay(5000);            // 5 seconds
    Serial.println("Booting....");

    SPI.begin();           // Init SPI bus
    mfrc522.PCD_Init();    // Init MFRC522
    
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print("connecting");

    rtcObject.Begin(); //Starts I2C
    RtcDateTime currentTime = RtcDateTime(18,04,27,18,58,0); //define date and time object
    rtcObject.SetDateTime(currentTime);                      //configure the RTC with object

    while (WiFi.status() != WL_CONNECTED) { 
        Serial.print(".");  
        delay(500);
    }

    int retries = 0;
    while ((WiFi.status() != WL_CONNECTED) && (retries < 30)) {
        retries++;
        delay(500);
        Serial.print(".");
    }

    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("WiFi connected");
    }
  
    Serial.println("Ready!");
    Serial.println("======================================================"); 
    Serial.println("Scan for Card and print UID:");
  
    Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
}

void loop() {
     
    if ( !mfrc522.PICC_IsNewCardPresent()) { // Look for new cards
        delay(50);
        return;
    }
    if ( !mfrc522.PICC_ReadCardSerial()) {   // Select one of the cards
        delay(50);
        return;
    }

    // Show some details of the PICC (that is: the tag/card)
    Serial.print("Card UID:");
    String content= "";
    for (byte i = 0; i < mfrc522.uid.size; i++) {
        Serial.print(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " ");
        Serial.print(mfrc522.uid.uidByte[i], HEX);
        content.concat(String(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " "));
        content.concat(String(mfrc522.uid.uidByte[i], HEX));
    }
    
    Serial.print(content);
    SendData(content);
    if (Firebase.failed()){
        Serial.print("setting /number failed:");
        Serial.println(Firebase.error());
        return;
    }
    
    delay(1000);

}

void SendData(String uid){

    RtcDateTime currentTime = rtcObject.GetDateTime();    //get the time from the RTC
char Date[20];
char Time[20];
    sprintf(Date, "%d-%d-%d",
          currentTime.Day(),
          currentTime.Month(),
          currentTime.Year()
         );
         
    sprintf(Time, "%d",
          currentTime.Hour()
         );

    Serial.print("\n");
    Serial.print("Date: ");
    Serial.print(Date);
    Serial.print("\nTime: ");
    Serial.print(Time);
    
      Firebase.setString(uid + "/" + Date + "/" + "Date" ,Date);
  
  Arrival_Time = Firebase.getString(uid + "/" + Date + "/" + "Arrival Time");
  Departed_Time = Firebase.getString(uid + "/" + Date + "/" + "Departed Time");

  if (Arrival_Time != NULL){
    if(Arrival_Time != Time){
      Firebase.setString(uid + "/" + Date + "/" + "Departed Time" ,Time);
      Serial.print("\nUploaded Departed_Time");
    }else{
        Serial.print("\nwait an hour");
    }
    
  }
  if (Arrival_Time == NULL && Departed_Time == NULL){
    Firebase.setString(uid + "/" + Date + "/" + "Arrival Time" ,Time);  
    Serial.print("\nUploaded Arrival_Time");
  }
  if (Arrival_Time != NULL && Departed_Time != NULL){
    Serial.print("\nDone For the Day");  
    }   

    Serial.print("\nUploaded\n");
}
