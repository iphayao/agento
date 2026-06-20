from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    # Spring Boot callback
    agento_api_url: str = "http://localhost:8080/api"
    agento_api_key: str = ""

    # AI provider
    ai_provider: str = "openai"
    ai_api_key: str = ""
    ai_base_url: str = "https://api.openai.com/v1"
    ai_model: str = "gpt-4o-mini"
    ai_temperature: float = 0.7
    ai_max_tokens: int = 2000

    # Worker
    worker_port: int = 8001
    log_level: str = "INFO"


settings = Settings()
