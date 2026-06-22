import os
import uuid
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.responses import FileResponse
from sqlalchemy.orm import Session
from app.database import get_db
from app import models, schemas
from app.services.tts_service import TTSService

router = APIRouter(prefix="/api/tts", tags=["TTS"])

@router.post("/generate")
def generate_audio(payload: schemas.TTSGenerateRequest, db: Session = Depends(get_db)):
    # Verify the target article exists
    article = db.query(models.Article).filter(models.Article.id == payload.article_id).first()
    if not article:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Article not found")

    # Establish localized storage mapping
    static_dir = "static/audio"
    filename = f"{uuid.uuid4()}.mp3"
    filepath = os.path.join(static_dir, filename)

    # Render audio payload via our service layer
    filepath, provider = TTSService.generate_speech(payload.text, filepath)

    # Record tracking metadata inside the DB
    audio_record = models.AudioFile(
        article_id=article.id,
        file_path=filepath,
        provider=provider
    )
    db.add(audio_record)
    db.commit()

    # Stream the rendered physical MP3 file cleanly back to client apps
    return FileResponse(path=filepath, media_type="audio/mpeg", filename=f"{article.title or 'audio'}.mp3")