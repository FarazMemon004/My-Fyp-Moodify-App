from flask import Flask, request, jsonify, send_from_directory
import cv2
import numpy as np
from deepface import DeepFace
import os
import tensorflow as tf
import note_seq
from magenta.models.music_vae import TrainedModel, configs

app = Flask(__name__)

# Suppress TensorFlow warnings
os.environ["TF_CPP_MIN_LOG_LEVEL"] = "3"
tf.get_logger().setLevel("ERROR")

# ✅ Set Directory to Save Generated MIDI Files
MUSIC_DIR = "generated_music"
os.makedirs(MUSIC_DIR, exist_ok=True)

# ✅ Load OpenCV Face Detection Model
face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + "haarcascade_frontalface_default.xml")


# ✅ Load MusicVAE Models
def load_music_vae_model1():
    try:
        print("Loading MusicVAE model (hierdec-trio_16bar)...")
        hierdec_trio_16bar = configs.CONFIG_MAP['hierdec-trio_16bar']
        music_vae1 = TrainedModel(
            config=hierdec_trio_16bar,
            checkpoint_dir_or_path='/Users/apple/workspace/fyp2/Moodify/Checkpoint/Music-vae/hierdec-trio_16bar.tar',
            batch_size=64
        )
        print("MusicVAE model (hierdec-trio_16bar) loaded successfully.")
        return music_vae1
    except Exception as e:
        print("Error loading MusicVAE model (hierdec-trio_16bar): {e}")
        return None


def load_music_vae_model2():
    try:
        print("Loading MusicVAE model (hierdec-mel_16bar)...")
        hierdec_mel_16bar = configs.CONFIG_MAP['hierdec-mel_16bar']
        music_vae2 = TrainedModel(
            config=hierdec_mel_16bar,
            checkpoint_dir_or_path='/Users/apple/workspace/fyp2/Moodify/Checkpoint/Music-vae/hierdec-mel_16bar.tar',
            batch_size=512
        )
        print("MusicVAE model (hierdec-mel_16bar) loaded successfully.")
        return music_vae2
    except Exception as e:
        print("Error loading MusicVAE model (hierdec-mel_16bar): {e}")
        return None


# Load the MusicVAE models
music_vae_trio = load_music_vae_model1()
music_vae_mel = load_music_vae_model2()


# ✅ Emotion Detection Route
@app.route("/detect-emotion", methods=["POST"])
def detect_emotion():
    try:
        file = request.files["image"]
        nparr = np.frombuffer(file.read(), np.uint8)
        frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        # Convert to grayscale and detect faces
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        faces = face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(30, 30))

        if len(faces) == 0:
            return jsonify({"status": "error", "message": "No face detected"}), 400

        x, y, w, h = max(faces, key=lambda box: box[2] * box[3])
        face_crop = frame[y: y + h, x: x + w]

        try:
            analysis = DeepFace.analyze(face_crop, actions=["emotion"], enforce_detection=False)
            detected_emotion = analysis[0]["dominant_emotion"]
        except Exception:
            detected_emotion = "unknown"

        return jsonify({"status": "success", "emotion": detected_emotion}), 200

    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500


# ✅ Music Generation Function
def generate_music(emotion):
    try:
        length = 128  # Steps
        temperature = 0.5
        num_tracks = 1  # Generate 3 tracks

        # ✅ Ensure the directory exists before saving MIDI files
        if not os.path.exists(MUSIC_DIR):
            os.makedirs(MUSIC_DIR, exist_ok=True)

        midi_files = []

        # Choose Model Based on Emotion
        if emotion in ["happy", "party", "excited", "angry"]:
            model = music_vae_trio
        elif emotion in ["sad", "relaxed", "surprise", "neutral", "unknown"]:
            model = music_vae_mel
        else:
            return []

        for i in range(num_tracks):
            sequence = model.sample(n=1, length=length, temperature=temperature)[0]
            midi_file = os.path.join(MUSIC_DIR, "output_{emotion}_track_{i+1}.mid")  # ✅ Corrected file path
            note_seq.note_sequence_to_midi_file(sequence, midi_file)
            midi_files.append(midi_file)

        return midi_files

    except Exception as e:
        print(f"Error generating music for '{emotion}': {e}")
        return []


# ✅ Route to Confirm Emotion & Generate Music
@app.route("/generate-music", methods=["POST"])
def generate_music_route():
    data = request.get_json()
    emotion = data.get("emotion", "neutral").lower()

    if not emotion:
        return jsonify({"status": "error", "message": "Emotion not provided"}), 400

    midi_files = generate_music(emotion)
    if midi_files:
        file_urls = [f"https://9c4e-45-199-187-139.ngrok-free.app/generated_music/{os.path.basename(f)}" for f in
                     midi_files]

        return jsonify(
            {"status": "success", "message": "Music generated successfully.", "emotion": emotion,
             "midi_files": file_urls}
        ), 200
    else:
        return jsonify({"status": "error", "message": "Failed to generate music"}), 500


# ✅ Serve Generated MIDI Files
@app.route("/generated_music/<filename>")
def serve_music(filename):
    return send_from_directory(MUSIC_DIR, filename, as_attachment=True)


# ✅ Start Flask App
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
