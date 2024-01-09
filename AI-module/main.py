import uvicorn
from fastapi import FastAPI, UploadFile, File, APIRouter
from dotenv import load_dotenv

load_dotenv()

from models.TranskribusAPI import TranskribusAPI
from models.Manuscript import ManuscriptUploading
from utils.utils_variables import __COLLECTION_ID__, __API_BASE_URL__, __HTR_MODEL_ID__, __NLP_MODEL_ID__

app = FastAPI(
    title="AI_Module API",
    version="1.9"
)
prefix_router = APIRouter(prefix=__API_BASE_URL__)

transkribusAPI = TranskribusAPI()
transkribus_base_url = transkribusAPI.base_url


@prefix_router.get("/third-party/login", status_code=200)
def login_to_transkribus():
    response = transkribusAPI.login()
    return response


# ENDPOINTS -> uploading manuscript to transkribus
@prefix_router.post("/upload_manuscript")
async def upload_manuscript(file: UploadFile = File(...)):
    manuscript_uploading = ManuscriptUploading(name=file.filename, collection_id=__COLLECTION_ID__)

    # temporarily save manuscript locally
    manuscript_uploading.save_manuscript(file)
    # upload saved manuscript to transkribus
    response = manuscript_uploading.upload_manuscript(transkribusAPI.session_id)
    # delete saved manuscript
    manuscript_uploading.delete_manuscript()

    return response


@prefix_router.get("/upload_manuscript/{job_id}")
async def get_upload_manuscript_job_status(job_id: int):
    response = ManuscriptUploading.get_manuscript_status(job_id, transkribusAPI.session_id)
    return response


# endpoint de test, in viitor trebuie inlocuit cu @app.post("/ap1/v1/upload_manuscript")
@prefix_router.get("/upload", status_code=200)
async def receive_path():
    manuscript_uploading = ManuscriptUploading(name="BCUTimișoara689841.jpg",
                                               collection_id=__COLLECTION_ID__)
    status = manuscript_uploading.upload_manuscript(transkribusAPI.session_id)
    status2 = manuscript_uploading.get_manuscript_status(session_id=transkribusAPI.session_id,
                                                         job_id=manuscript_uploading.job_id)
    return status2


# ENDPOINTS -> Collections
@prefix_router.get("/third-party/get-collections", status_code=200)
def get_user_collections():
    url = f"{transkribus_base_url}/collections/list"
    response = transkribusAPI.get(url)
    return response.text


@prefix_router.get("/third-party/documents/{collection_id}", status_code=200)
def get_documents_from_collection(collection_id: int):
    url = f"{transkribus_base_url}/collections/{collection_id}/list"
    response = transkribusAPI.get(url)
    json_format_response = response.text.replace('\\"', '"')
    return json_format_response


@prefix_router.get("/third-party/documents/{collection_id}/{document_id}", status_code=200)
def get_document(collection_id: int, document_id: int):
    url = f"{transkribus_base_url}/collections/{collection_id}/{document_id}/fulldoc"
    response = transkribusAPI.get(url)
    return response.json()


# ENDPOINTS -> OCR and NLP
# TODO build process OCR
# @prefix_router.post("/ocr/", status_code=201)
# async def process_image_ocr():
#     #url = f"{transkribus_base_url}/recognition/{__COLLECTION_ID__}/{__HTR_MODEL_ID__}/trhtr"
#     return {"text": ""}


@prefix_router.get("/ocr", status_code=200)
async def test_api():
    manuscript_uploading = ManuscriptUploading(name="BCUTimișoara689841.jpg",
                                               collection_id=__COLLECTION_ID__)
    response = manuscript_uploading.get_key_document(session_id=transkribusAPI.session_id,
                                                     collection_id=__COLLECTION_ID__, version_of_document=1)
    #status = manuscript_uploading.apply_ocr_document(session_id=transkribusAPI.session_id, collection_id= __COLLECTION_ID__,model_id=__HTR_MODEL_ID__)
    status = manuscript_uploading.get_already_ocr_document(transkribusAPI.session_id)
    return status


# TODO build process NLP
@prefix_router.post("/nlp/", status_code=201)
async def process_image_htr(path_to_manuscript: str, model_htr):
    return {"text": ""}


app.include_router(prefix_router)

if __name__ == "__main__":
    uvicorn.run("main:app", host="127.0.0.1", port=8001, reload=True)
