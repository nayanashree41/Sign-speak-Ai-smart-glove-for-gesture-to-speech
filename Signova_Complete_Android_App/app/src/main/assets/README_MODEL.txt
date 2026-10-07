SIGNOVA DEEP LEARNING MODEL PLACEHOLDER

The app is fully usable in Simulation Mode now.

For real glove inference:
1. Collect real sequences from ESP32: 5 flex + Ax Ay Az + Gx Gy Gz = 11 features.
2. Train your selected 1D-CNN / LSTM / GRU using Python/TensorFlow.
3. Export as TensorFlow Lite: gesture_model.tflite.
4. Put gesture_model.tflite in this assets folder.
5. Add the TensorFlow Lite Android dependency to app/build.gradle.kts.
6. Replace DeepLearningGateway.classifyWindow() with Interpreter inference.

The Bluetooth framework already expects CSV lines with exactly 11 numeric values.
Example:
430,810,720,690,840,0.32,-0.51,9.48,2.1,-1.4,0.8
