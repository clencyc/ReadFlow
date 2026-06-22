from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app import models, schemas

router = APIRouter(prefix="/api/queue", tags=["Queue / Bookmarks"])

@router.get("/{userId}", response_model=List[schemas.BookmarkResponse])
def get_user_queue(userId: str, db: Session = Depends(get_db)):
    # Query all active unarchived bookmarks for this user ID, fetching related article records
    bookmarks = db.query(models.Bookmark).filter(
        models.Bookmark.user_id == userId,
        models.Bookmark.is_archived == False
    ).all()
    return bookmarks