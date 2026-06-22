from pydantic import BaseModel, HttpUrl, field_validator, model_validator
from typing import Optional, List
from datetime import datetime

# --- ARTICLE SCHEMAS ---
class ArticleExtractRequest(BaseModel):
    url: HttpUrl

class ArticleResponse(BaseModel):
    id: str
    url: str
    title: str = ""
    content: str = ""
    summary: str = ""
    author: str = ""
    source: str = ""
    image_url: Optional[str] = None
    word_count: int = 0
    created_at: str = ""

    class Config:
        from_attributes = True

    @field_validator('title', 'content', 'author', mode='before')
    @classmethod
    def convert_none_to_string(cls, v):
        return v if v is not None else ""

    @field_validator('created_at', mode='before')
    @classmethod
    def convert_datetime_to_string(cls, v):
        if v is None:
            return ""
        if isinstance(v, datetime):
            return v.isoformat()
        return str(v)

    @model_validator(mode='after')
    def compute_derived_fields(self) -> 'ArticleResponse':
        # Compute word count
        if self.content:
            self.word_count = len(self.content.split())
        else:
            self.word_count = 0
        
        # Deduce source from url
        if self.url:
            from urllib.parse import urlparse
            try:
                parsed = urlparse(self.url)
                self.source = parsed.netloc.replace("www.", "")
            except:
                self.source = ""
        
        # Compute summary from content
        if self.content and not self.summary:
            clean_content = self.content.replace("\n", " ").strip()
            if len(clean_content) > 150:
                self.summary = clean_content[:147] + "..."
            else:
                self.summary = clean_content
        return self

# --- TTS SCHEMAS ---
class TTSGenerateRequest(BaseModel):
    article_id: str
    text: str

class AudioFileResponse(BaseModel):
    id: str
    article_id: str
    file_path: str
    provider: str
    created_at: datetime

    class Config:
        from_attributes = True

# --- BOOKMARK SCHEMAS ---
class BookmarkCreate(BaseModel):
    user_id: str
    article_id: str

class BookmarkResponse(BaseModel):
    id: str
    user_id: str
    article_id: str
    is_archived: bool
    created_at: datetime
    article: Optional[ArticleResponse] = None

    class Config:
        from_attributes = True

# --- RESPONSE WRAPPERS ---
class ArticleListResponse(BaseModel):
    articles: List[ArticleResponse]