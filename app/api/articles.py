from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app import models, schemas
from app.services.article_extractor import ArticleExtractorService

router = APIRouter(prefix="/api/articles", tags=["Articles"])

@router.post("/fetch", response_model=schemas.ArticleResponse)
def fetch_article(payload: schemas.ArticleExtractRequest, db: Session = Depends(get_db)):
    url_str = str(payload.url)
    print(f"\n--- [API ROUTE] Inbound request for URL: {url_str} ---")
    
    existing_article = db.query(models.Article).filter(models.Article.url == url_str).first()
    if existing_article:
        print("✔ Article found in database cache. Returning existing record.")
        return existing_article

    extracted_data = ArticleExtractorService.extract_from_url(url_str)
    
    # Trace the dictionary structure returned from the service
    print(f"→ Extracted Data received in route: {extracted_data}")

    if not extracted_data or not extracted_data.get("content"):
        print("❌ Route blocking: Extracted content is empty or None.")
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, 
            detail="Failed to extract readable content from the provided URL."
        )

    try:
        print("→ Instantiating SQLAlchemy database model...")
        new_article = models.Article(
            url=url_str,
            title=extracted_data.get("title"),
            content=extracted_data.get("content"),
            author=extracted_data.get("author")
        )
        
        print("→ Saving record to database session...")
        db.add(new_article)
        db.commit()
        db.refresh(new_article)
        
        print(f"✔ Database persistence complete. Assigned ID: {new_article.id}")
        return new_article
        
    except Exception as route_err:
        print(f"💥 DATABASE/SERIALIZATION CRASH inside route: {str(route_err)}")
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(route_err))

@router.get("", response_model=schemas.ArticleListResponse)
def get_articles(db: Session = Depends(get_db)):
    articles = db.query(models.Article).order_by(models.Article.created_at.desc()).all()
    return {"articles": articles}

@router.get("/{id}", response_model=schemas.ArticleResponse)
def get_article(id: str, db: Session = Depends(get_db)):
    article = db.query(models.Article).filter(models.Article.id == id).first()
    if not article:
        raise HTTPException(status_code=404, detail="Article not found")
    return article

@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_article(id: str, db: Session = Depends(get_db)):
    article = db.query(models.Article).filter(models.Article.id == id).first()
    if not article:
        raise HTTPException(status_code=404, detail="Article not found")
    db.delete(article)
    db.commit()
    return