import uvicorn
from fastapi import FastAPI
from dotenv import load_dotenv

from models.TranskribusLogin import TranskribusLogin
from models.Manuscript import ManuscriptUploading
from utils.utils_variables import __COLLECTION_ID__

load_dotenv()
app = FastAPI()
transkribusAPI = TranskribusLogin()


@app.get("/")
async def index():
    return {"message": "Hello World"}


@app.get("/api/v1/third-party/login", status_code=200)
def login_to_transkribus():
    response = transkribusAPI.login()
    return response


@app.get("/api/v1/upload", status_code=200)
async def receive_path():
    manuscript_uploading = ManuscriptUploading(name="BCUTimișoara689841.jpg",
                                               path="D:\\_ user ecaaa\\Documents\\GitHub\\romanian-script-decipher"
                                                    "\\AI-module\\manuscripts_examples\\BCUTimișoara689841.jpg",
                                               collection_id=__COLLECTION_ID__)
    status = manuscript_uploading.upload_manuscript(transkribusAPI.session_id)
    status2 = manuscript_uploading.get_manuscript_status(transkribusAPI.session_id)
    return status2



@app.get("/api/v1/third-party/get-collections", status_code=200)
def get_user_collections():
    url = "https://transkribus.eu/TrpServer/rest/collections/list"
    response = transkribusAPI.get(url)
    return response.text


# TODO build process OCR
@app.post("/v1/ocr/", status_code=201)
async def process_image_ocr(path_to_manuscript: str, model_ocr):
    return {"text": ""}


# TODO build process NLP
@app.post("/v1/nlp/", status_code=201)
async def process_image_htr(path_to_manuscript: str, model_htr):
    return {"text": ""}

#
# if __name__ == "__main__":
#     uvicorn.run("main:app", host="127.0.0.1", port=8001, reload=True)
