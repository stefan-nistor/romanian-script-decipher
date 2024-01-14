import uvicorn
from fastapi import FastAPI, UploadFile, File, APIRouter
from dotenv import load_dotenv

load_dotenv()

from models.TranskribusAPI import TranskribusAPI
from models.Manuscript import ManuscriptUploading
from PIL import Image, ImageDraw, ImageFont
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

    if not response["errors"]:
        return {"message": response["message"], "filename": file.filename, "errors": False, "jobId": response["jobId"],
                "docId": response["docId"]}
    else:
        return {"message": response["message"], "filename": file.filename, "errors": True}


@prefix_router.get("/upload_manuscript/{job_id}")
async def get_upload_manuscript_job_status(job_id: int):
    response = ManuscriptUploading.get_manuscript_status(job_id, transkribusAPI.session_id)
    return response


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
@prefix_router.post("/ocr", status_code=200)
async def apply_ocr_on_document(filename: str, document_id: str):
    manuscript_uploading = ManuscriptUploading(name=filename,
                                               collection_id=__COLLECTION_ID__)
    manuscript_uploading.get_key_document(document_id=document_id, session_id=transkribusAPI.session_id,
                                          collection_id=__COLLECTION_ID__, version_of_document=1)
    ocr_text = manuscript_uploading.apply_ocr_document(document_id=document_id, session_id=transkribusAPI.session_id,
                                                       collection_id=__COLLECTION_ID__, model_id=__HTR_MODEL_ID__)

    # img = Image.new("RGB", (595, 842), color="white")
    # d = ImageDraw.Draw(img)
    # unicode_font = ImageFont.truetype("./assets/GentiumPlus-Regular.ttf", 18)
    # d.text((10, 10), text=ocr_text, font=unicode_font, fill="black")
    # img.save("test4.jpg", "JPEG")

    return {"ocr": ocr_text}


# TODO build process NLP
@prefix_router.post("/nlp/", status_code=201)
async def apply_nlp_on_document(filename: str, document_id: int):
    manuscript_uploading = ManuscriptUploading(name=filename,
                                               collection_id=__COLLECTION_ID__)
    manuscript_uploading.get_key_document(session_id=transkribusAPI.session_id,
                                          document_id=document_id,
                                          collection_id=__COLLECTION_ID__,
                                          version_of_document=1
                                          )
    nlp = manuscript_uploading.apply_nlp_document(document_id=document_id, session_id=transkribusAPI.session_id,
                                                  collection_id=__COLLECTION_ID__,
                                                  model_id=__NLP_MODEL_ID__)

    return {"text": nlp}


app.include_router(prefix_router)

if __name__ == "__main__":
    uvicorn.run("main:app", host="127.0.0.1", port=8001, reload=True)
