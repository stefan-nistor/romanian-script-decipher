from fastapi.testclient import TestClient
from utils.utils_variables import __COLLECTION_ID__

from main import app

client = TestClient(app)
base_url = "/api/v1"


def test_login():
    response = client.get(base_url + "/third-party/login")
    assert response.status_code == 200
    assert response.json() == {"message": "Login was made successfully!", "errors": False}


def test_uploading():
    with open("manuscripts_examples/BCUTimișoara689841.jpg", "rb") as document:
        response = client.post(base_url + "/upload_manuscript",
                               files={"file": ("BCUTimișoara689841.jpg", document, "image/jpeg")})

    assert response.status_code == 201
    assert response.json()["errors"] == False


def test_finished_job_status():
    response = client.get(base_url + "/job_status/7557137")
    assert response.status_code == 200
    assert response.text == '"FINISHED"'


def test_canceled_job_status():
    response = client.get(base_url + "/job_status/7557108")
    assert response.status_code == 200
    assert response.text == '"CANCELED"'


def test_get_user_collections():
    response = client.get(base_url + "/third-party/get-collections")
    assert response.status_code == 200


def test_get_documents_from_collection():
    response = client.get(base_url + f"/third-party/documents/{__COLLECTION_ID__}")
    assert response.status_code == 200


def test_get_document_from_collection():
    document_id = 1736274
    response = client.get(base_url + f"/third-party/documents/{__COLLECTION_ID__}/{document_id}")
    assert response.status_code == 200


# OCR
def test_apply_ocr_to_document():
    filename = "BCUTimișoara689841.jpg"
    document_id = 1751063
    response = client.post(base_url + "/ocr/send_to_recognition",
                           params={"filename": filename, "document_id": document_id})
    assert response.status_code == 201
    assert "text_recognition_job_id" in response.json()

    response_status = client.get(base_url + f"/job_status/{response.json()['text_recognition_job_id']}")
    assert response_status.status_code == 200
    assert response_status.text == '"CREATED"'


def test_apply_nlp_to_document():
    filename = "BCUTimișoara689841.jpg"
    document_id = 1751062
    response = client.post(base_url + "/nlp/send_to_recognition",
                           params={"filename": filename, "document_id": document_id})
    assert response.status_code == 201
    assert "text_recognition_job_id" in response.json()

    response_status = client.get(base_url + f"/job_status/{response.json()['text_recognition_job_id']}")
    assert response_status.status_code == 200
    assert response_status.text == '"CREATED"'