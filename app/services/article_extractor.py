import urllib.request
import trafilatura
from typing import Dict, Any, Optional

class ArticleExtractorService:
    @staticmethod
    def extract_from_url(url: str) -> Optional[Dict[str, Any]]:
        try:
            print(f"\n--- [START] Extracting URL: {url} ---")
            headers = {
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36'
            }
            
            req = urllib.request.Request(url, headers=headers)
            
            print("→ Fetching raw HTML via urllib...")
            with urllib.request.urlopen(req, timeout=10) as response:
                raw_bytes = response.read()
                print(f"Successfully downloaded {len(raw_bytes)} bytes.")

            if not raw_bytes:
                print("HTML content received was completely empty.")
                return None
            
            # Decode bytes to a clean string format for reliable local parsing
            html_content = raw_bytes.decode("utf-8", errors="ignore")
            
            print("→ Attempting primary bare_extraction...")
            result = trafilatura.bare_extraction(
                html_content, 
                output_format='json', 
                include_comments=False
            )
            
            # Fallback check: If bare_extraction completely missed the core content text block
            if not result or not result.get("text"):
                print("Primary bare_extraction returned no body text. Running fallback extract()...")
                fallback_text = trafilatura.extract(html_content, include_comments=False)
                
                if fallback_text:
                    print("Fallback extraction successful!")
                    return {
                        "title": trafilatura.extract_metadata(html_content).title or "Untitled Article",
                        "content": fallback_text,
                        "author": trafilatura.extract_metadata(html_content).author,
                        "date": trafilatura.extract_metadata(html_content).date
                    }
            
            if result and result.get("text"):
                print("Primary extraction successful!")
                return {
                    "title": result.get("title"),
                    "content": result.get("text"),
                    "author": result.get("author"),
                    "date": result.get("date")
                }
            
            print("All parsing options failed to isolate readable body text content.")
            return None
            
        except Exception as e:
            print(f"SYSTEM ERROR during extraction logic: {str(e)}")
            import traceback
            traceback.print_exc()
            return None