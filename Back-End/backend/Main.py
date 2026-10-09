## trio for happy,party, excited, and angry
## mel_15 for sad and relaxed


import os
import tensorflow as tf
import note_seq
from flask import Flask, request, jsonify
from magenta.models.music_vae import TrainedModel, configs

# Suppress TensorFlow warnings
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'  # 0 = all messages, 1 = info, 2 = warnings, 3 = errors
tf.get_logger().setLevel('ERROR')


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


def generate_music(emotion):
    """Generate music based on emotion using the appropriate MusicVAE model(s)."""

    try:
        # Define generation options
        length = 512  # Number of steps
        temperature = 1.0
        num_tracks = 3  # Number of tracks to generate

        midi_files = []  # List to store generated MIDI file paths

        if emotion in ["happy", "party", "excited", "angry"]:
            print(f"Generating {num_tracks} tracks for '{emotion}' using the trio model...")
            for i in range(num_tracks):
                sequence = music_vae_trio.sample(n=1, length=length, temperature=temperature)[0]
                midi_file = f"output_{emotion}_track_{i + 1}.mid"
                note_seq.note_sequence_to_midi_file(sequence, midi_file)
                midi_files.append(midi_file)

        elif emotion in ["sad", "relaxed"]:
            print(f"Generating {num_tracks} tracks for '{emotion}' using the mel model...")
            for i in range(num_tracks):
                sequence = music_vae_mel.sample(n=1, length=length, temperature=temperature)[0]
                midi_file = f"output_{emotion}_track_{i + 1}.mid"
                note_seq.note_sequence_to_midi_file(sequence, midi_file)
                midi_files.append(midi_file)

        elif emotion == "neutral":
            print(f"Generating {num_tracks} tracks for 'neutral' using both trio and mel models...")
            for i in range(num_tracks):
                trio_sequence = music_vae_trio.sample(n=1, length=length, temperature=temperature)[0]
                mel_sequence = music_vae_mel.sample(n=1, length=length, temperature=temperature)[0]
                sequence = note_seq.concatenate_sequences([trio_sequence, mel_sequence])
                midi_file = f"output_{emotion}_track_{i + 1}.mid"
                note_seq.note_sequence_to_midi_file(sequence, midi_file)
                midi_files.append(midi_file)

        else:
            raise ValueError(f"Unsupported emotion: {emotion}")

        print(f"Music generation for '{emotion}' completed. MIDI files saved: {midi_files}")
        return midi_files

    except Exception as e:
        print(f"Error generating music for '{emotion}': {e}")
        raise


# Flask API Route
app = Flask(__name__)


@app.route('/verify-password', methods=['POST'])
def verify_password():
    data = request.get_json()
    password = data.get('password')

    # Add logic to verify the password (e.g., compare with the hashed password in the database)
    is_valid = True  # Replace with actual logic
    return jsonify({"isValid": is_valid})


@app.route('/check-email-availability', methods=['POST'])
def check_email_availability():
    data = request.get_json()
    email = data.get('email')

    # Add logic to check if the email is already in use
    is_available = True  # Replace with actual logic
    return jsonify({"isAvailable": is_available})


@app.route('/update-email', methods=['POST'])
def update_email():
    data = request.get_json()
    email = data.get('email')

    # Add logic to update the email in the database
    is_updated = True  # Replace with actual logic
    return jsonify({"isUpdated": is_updated})


@app.route('/generate-music', methods=['POST'])
def generate_music_route():
    data = request.get_json()
    emotion = data.get("emotion", "neutral").lower()
    print("Received emotion: {emotion}")

    try:
        # Generate music and return MIDI file
        midi_file = generate_music(emotion)
        if midi_file:
            response = {
                "status": "success",
                "message": "Music generated successfully.",
                "midi_file": midi_file
            }
            return jsonify(response), 200
        else:
            raise Exception("Failed to generate music.")
    except Exception as e:
        print("Error: {e}")
        return jsonify({"status": "error", "message": str(e)}), 500


if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5001, debug=True)  # Change this to port 5001
