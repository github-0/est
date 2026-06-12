"""
Reads Firebase credentials from project files (single source of truth).
Import this module instead of hardcoding credentials in maintenance scripts.
"""

import json
from pathlib import Path

_HERE = Path(__file__).parent

def _read_google_services():
    google_services = _HERE.parent / "app/google-services.json"
    return json.loads(google_services.read_text())

def _read_project_id():
    return _read_google_services()["project_info"]["project_id"]

PROJECT_ID        = _read_project_id()
SERVICE_ACCOUNT_KEY = _HERE / "service_account.json"
