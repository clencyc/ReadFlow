import os
import asyncio
from gtts import gTTS
from google.cloud import texttospeech
import edge_tts
from app.config import settings

class TTSService:
    @staticmethod
    def generate_speech(text: str, output_filename: str) -> tuple[str, str]:
        """
        Converts text to speech, saving it locally. 
        Falls back to Edge TTS, then gTTS if Google Cloud credentials aren't provided or fail.
        Returns a tuple of (output_filename, provider_name).
        """
        # Ensure the output directory exists
        os.makedirs(os.path.dirname(output_filename), exist_ok=True)

        # 1. Attempt Google Cloud TTS (Premium)
        if settings.TTS_PROVIDER == "google" and os.getenv("GOOGLE_APPLICATION_CREDENTIALS"):
            try:
                client = texttospeech.TextToSpeechClient()
                synthesis_input = texttospeech.SynthesisInput(text=text)
                
                voice = texttospeech.VoiceSelectionParams(
                    language_code="en-US",
                    ssml_gender=texttospeech.SsmlVoiceGender.NEUTRAL
                )
                audio_config = texttospeech.AudioConfig(
                    audio_encoding=texttospeech.AudioEncoding.MP3
                )
                
                response = client.synthesize_speech(
                    input=synthesis_input, voice=voice, audio_config=audio_config
                )
                
                with open(output_filename, "wb") as out:
                    out.write(response.audio_content)
                return output_filename, "google"
                
            except Exception as e:
                print(f"Google Cloud TTS failed: {e}. Falling back to Edge TTS...")
        
        # 2. Attempt Edge TTS (Free Neural)
        try:
            async def run_edge_tts():
                communicate = edge_tts.Communicate(text, "en-US-AvaNeural")
                await communicate.save(output_filename)

            asyncio.run(run_edge_tts())
            return output_filename, "edge"
        except Exception as e:
            print(f"Edge TTS failed: {e}. Falling back to gTTS...")

        # 3. Fallback Local/Free gTTS Implementation (Robotic voice, last resort)
        try:
            tts = gTTS(text=text, lang='en')
            tts.save(output_filename)
            return output_filename, "gtts"
        except Exception as e:
            print(f"gTTS failed: {e}")
            raise e