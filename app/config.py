import os
from pydantic_settings import BaseSettings

# Manually read the .env file if it exists at the root level
env_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), ".env")
if os.path.exists(env_path):
    with open(env_path, "r") as f:
        for line in f:
            if line.strip() and not line.startswith("#"):
                key, val = line.strip().split("=", 1)
                os.environ[key] = val

class Settings(BaseSettings):
    # It will now definitely pull from os.environ["DATABASE_URL"]
    DATABASE_URL: str = "sqlite:///./articleflow.db"
    ENVIRONMENT: str = "development"
    DEBUG: bool = True
    TTS_PROVIDER: str = "google"

settings = Settings()

# Debug log to verify what string is actually active
print(f"\nACTIVE CONNECTION STRING: {settings.DATABASE_URL}\n")
