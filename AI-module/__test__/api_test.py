from fastapi.testclient import TestClient

from main import app

client = TestClient(app)

def test_login():
    response = client.get("/api/v1/third-party/login")
    assert response.status_code == 200
    assert response.json() == {"message": "Login was made successfully!", "errors": False}