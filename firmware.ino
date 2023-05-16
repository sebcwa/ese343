#include <UMS3.h>
UMS3 ums3;
#include <BLEDevice.h>
#include <BLEServer.h>

#include <BLEUtils.h>
#include <BLE2902.h>
#define SERVICE_UUID "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define analogUV

//BLE server name
#define bleServerName "UltraView_ESP32"
// Timer variables
unsigned long lastTime = 0;
unsigned long timerDelay = 3000;

bool deviceConnected = false;

BLECharacteristic uvReadCharacteristics("cba1d466-344c-4be3-ab3f-189f80dd7518", BLECharacteristic::PROPERTY_NOTIFY);
BLEDescriptor uvReadDescriptor(BLEUUID((uint16_t) 0x2902));

// Battery Characteristic and Descriptor
BLECharacteristic batteryCharacteristics("ca73b3ba-39f6-4ab3-91ae-186dc9577d99", BLECharacteristic::PROPERTY_NOTIFY);
BLEDescriptor batteryDescriptor(BLEUUID((uint16_t) 0x2903));

//Setup callbacks onConnect and onDisconnect
class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer * pServer) {
        deviceConnected = true;
    };
    void onDisconnect(BLEServer * pServer) {
        deviceConnected = false;
    }
};

float uvLevel;
float analogValue;
float batteryLevel;

void setup() {
    Serial.begin(115200);

    analogReadResolution(12);

    Serial.println("Starting BLE work!");

    // Create the BLE Device
    BLEDevice::init(bleServerName);

    // Create the BLE Server
    BLEServer * pServer = BLEDevice::createServer();
    pServer -> setCallbacks(new MyServerCallbacks());

    // Create the BLE Service
    BLEService * bmeService = pServer -> createService(SERVICE_UUID);

    // Create BLE Characteristics and Create a BLE Descriptor
    bmeService -> addCharacteristic( & uvReadCharacteristics);
    uvReadDescriptor.setValue("UV Analog Read");
    uvReadCharacteristics.addDescriptor( & uvReadDescriptor);

    // Battery
    bmeService -> addCharacteristic( & batteryCharacteristics);
    batteryDescriptor.setValue("Battery Charge");
    batteryCharacteristics.addDescriptor(new BLE2902());

    // Start the service
    bmeService -> start();

    // Start advertising
    BLEAdvertising * pAdvertising = BLEDevice::getAdvertising();
    pAdvertising -> addServiceUUID(SERVICE_UUID);
    pServer -> getAdvertising() -> start();
    Serial.println("Waiting a client connection to notify...");
}

void loop() {

    if (deviceConnected) {
        if ((millis() - lastTime) > timerDelay) {
            // Read UV sensor's analog value
            analogValue = analogReadMilliVolts(2);
            //convert analog value to UV Index
            uvLevel = (analogValue / 1000) / 0.1;
            // Read battery voltage
            batteryLevel = ums3.getBatteryVoltage();

            //Notify reading from the uv sensor
            static char uvStr[6];
            dtostrf(uvLevel, 6, 2, uvStr);
            //Set Characteristic value and notify connected client
            uvReadCharacteristics.setValue(uvStr);
            uvReadCharacteristics.notify();
            Serial.print("UV Level: ");
            Serial.print(uvLevel);

            //Notify battery reading
            static char batLevel[6];
            dtostrf(batteryLevel, 6, 2, batLevel);
            //Set Characteristic value and notify connected client
            batteryCharacteristics.setValue(batLevel);
            batteryCharacteristics.notify();
            Serial.print(" - Battery Level: ");
            Serial.print(batteryLevel);
            Serial.println(" %");

            lastTime = millis();
        }
    } }
