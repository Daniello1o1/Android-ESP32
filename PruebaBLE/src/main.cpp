#include <Arduino.h>
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLE2902.h>

char buffer[32];

#define SERVICE_UUID "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

#define pinADC 34
#define resolucion 3.3/4095

int rADC = 0;
int voltaje = 0;

BLEServer *pServer;
BLEService *pService;
BLECharacteristic *pCharacteristic;

void setup() {
  Serial.begin(9600);

  analogReadResolution(12);
  analogSetPinAttenuation(pinADC,ADC_11db);
  Serial.println("Starting BLE");

  BLEDevice::init("Daniel");
  pServer = BLEDevice::createServer();
  pService = pServer->createService(SERVICE_UUID);

  pCharacteristic = pService->createCharacteristic(
    CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_READ |
    BLECharacteristic::PROPERTY_WRITE |
    BLECharacteristic::PROPERTY_NOTIFY
  );

  pCharacteristic->addDescriptor(new BLE2902());

  pCharacteristic->setValue("Pues, aqui. Probando el BLE, oyes");
  pService->start();

  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);
  pAdvertising->setMinPreferred(0x12);

  BLEAdvertisementData advData;
  advData.setName("Daniel");
  advData.setCompleteServices(BLEUUID(SERVICE_UUID));
  pAdvertising->setAdvertisementData(advData);

  BLEDevice::startAdvertising();
  Serial.println("Characteristic defined! Now you can read it in your phone!");
}

void loop() {
  rADC = analogRead(pinADC);
  Serial.print("Valor: ");
  Serial.println(rADC);

  Serial.print("Voltaje: ");
  float voltaje = rADC*resolucion;
  Serial.println(voltaje,2);
  delay(500);
  sprintf(buffer, "Daniel: %f", voltaje);
  Serial.println(buffer);

  pCharacteristic->setValue(buffer);
  pCharacteristic->notify();
  delay(500);
}