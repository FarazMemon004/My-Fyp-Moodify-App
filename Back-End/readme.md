Moodify - Emotion-Based Music Generation

Project Overview

Moodify is an AI-powered music generation application that detects a user's emotions through facial recognition and
generates music accordingly using machine learning models. The system utilizes OpenCV for face detection, DeepFace for
emotion analysis, and Magenta’s MusicVAE for generating music compositions based on detected emotions.

Features

Facial Recognition: Detects user emotions in real-time using DeepFace.

Emotion-Based Music Generation: Generates music tailored to the detected emotion.

User Authentication: Firebase authentication for secure login and sign-up.

Music Storage & Playback: Stores generated music and allows users to play and download it.

Mobile Application: Developed in Kotlin with seamless interaction with the Flask back-end.

Technologies Used

Back-End: Python 3.8 , Flask, OpenCV, DeepFace, Magenta, TensorFlow, Firebase Admin

Front-End: Kotlin (Android Studio), Firebase Authentication

Database: Firebase Firestore (User Data & Authentication)

Deployment: ngrok for testing mobile connectivity

Installation Instructions

Back-End Setup

Install dependencies:

[pip install -r requirements.txt](requirement.txt)

Run the Flask server:

[python app.py](backend/OpenCv-Magenta.py)

(Optional) Use ngrok to expose the local server for mobile testing:

[ngrok http 5000]()

Front-End Setup (Android Studio)

Open the project in Android Studio.

Connect Firebase to the project for authentication.

Update flaskUrl in GenerateMusicActivity.kt with your server URL.
Update flaskUrl in FacialRecognitionActivity.kt with your server URL.
Update flaskUrl in MusicPlayerActivity.kt with your server URL

Build and run the application on an emulator or real device.

API Endpoints

**Emotion Detection**

POST /detect-emotion

Accepts: Image file (JPEG/PNG)

Returns: JSON with detected emotion

**Generate Music**

POST /generate-music

Accepts: JSON with emotion type

Returns: List of MIDI file URLs

**Download Music**

GET /generated_music/<filename>

Returns: MIDI file download

Future Enhancements

Improve emotion detection accuracy using deep learning.

Add support for cloud-based music storage.

Implement real-time emotion tracking for dynamic music generation.

Deploy the back-end on a cloud server for better performance.

Contributors

Muhammad Faraz & team - AI & Back-End Development

- Front-End, UI/UX, Testing

License

This project is licensed under the MIT License - see the LICENSE file for details.

