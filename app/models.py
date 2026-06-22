import uuid
from sqlalchemy import Column, String, Text, Boolean, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from datetime import datetime
from app.database import Base

def generate_uuid():
    return str(uuid.uuid4())

class Article(Base):
    __tablename__ = "articles"

    id = Column(String, primary_key=True, default=generate_uuid)
    url = Column(String, unique=True, index=True, nullable=False)
    title = Column(String, nullable=True)
    content = Column(Text, nullable=True)
    author = Column(String, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)

    # Relationships
    audio_files = relationship("AudioFile", back_populates="article", cascade="all, delete-orphan")
    bookmarks = relationship("Bookmark", back_populates="article", cascade="all, delete-orphan")

class AudioFile(Base):
    __tablename__ = "audio_files"

    id = Column(String, primary_key=True, default=generate_uuid)
    article_id = Column(String, ForeignKey("articles.id"), nullable=False)
    file_path = Column(String, nullable=False)  # Local path or Cloud Storage URL
    provider = Column(String, default="google")
    created_at = Column(DateTime, default=datetime.utcnow)

    article = relationship("Article", back_populates="audio_files")

class Bookmark(Base):
    __tablename__ = "bookmarks"

    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, index=True, nullable=False)
    article_id = Column(String, ForeignKey("articles.id"), nullable=False)
    is_archived = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)

    article = relationship("Article", back_populates="bookmarks")