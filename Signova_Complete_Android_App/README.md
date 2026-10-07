# Signova — Reference UI Edition

This Android Studio project is the Signova smart-glove gesture-to-speech app rebuilt to closely follow the reference interface supplied in the ChatGPT conversation.

## Visual flow
1. Dark red/black splash with Signova logo and hand image.
2. White/lilac onboarding with gesture pattern and “Turn Gestures into Words”.
3. Dark Home dashboard with Connect Glove, Simulation Mode, gesture preview, confidence, generated sentence and Speak control.
4. Light History/communication screen with recent conversations, communication settings summary and inclusive-communication quote.

The three supplied reference images are used in the app interface: the red/black hand, purple gesture pattern, and red silhouette.

## Existing functionality preserved
- Kotlin + XML Android app
- Simulation Mode
- ESP32 Bluetooth connection framework
- Live sensor-stream parser
- Deep Learning/TFLite-ready gateway
- Gesture confidence and sequence builder
- Sentence formation service
- Android Text-to-Speech
- Local conversation history
- Emergency communication screen
- Settings and gesture-learning screen

## Build stack
- Android Gradle Plugin 8.7.3
- Gradle 8.9 (pinned in gradle/wrapper/gradle-wrapper.properties)
- JDK 17
- compileSdk / targetSdk 35
- minSdk 26

Open the project folder containing `settings.gradle.kts` in Android Studio and choose Embedded JDK 17 for Gradle.

## Deep Learning model
The app intentionally does not contain a fake trained model. When the real five-flex-sensor + MPU6050 dataset is ready, train the LSTM/GRU/1D-CNN model and place the exported model at:

`app/src/main/assets/gesture_model.tflite`
