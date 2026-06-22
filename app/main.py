from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.database import engine, Base
from app.api import articles, tts, bookmarks

# Auto-create database tables on application initialization
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="ArticleFlow API",
    description="Backend pipeline engine handling extraction, transformation, and speech rendering operations.",
    version="1.0.0"
)

# Apply permissive CORS middleware rules for Android Emulators & local web components
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Bind individual feature routers to core app engine instance
app.include_router(articles.router)
app.include_router(tts.router)
app.include_router(bookmarks.router)

@app.get("/health", tags=["System"])
def system_health_check():
    return {"status": "operational", "database": "connected"}