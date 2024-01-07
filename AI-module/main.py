import os
import uvicorn
import requests
import xml.etree.ElementTree as ET
from fastapi import FastAPI
from pydantic import BaseModel
from urllib.parse import urlencode

from dotenv import load_dotenv
load_dotenv()

app = FastAPI()


class TranskribusAPI:
    session_id: str
    session_cookie: dict
    user: str
    password: str

    def __init__(self):
        self.user = os.environ.get("TRANSKRIBUS_USER")
        self.password = os.environ.get("TRANSKRIBUS_PASSWORD")
        self.session_cookie = {}

    def post(self, url, data, headers=None):
        if headers is None:
            headers = {'Content-Type': 'application/json'}
        response = requests.post(url, data=urlencode(data), headers=headers,
                                 cookies=self.session_cookie)
        return response

    def get(self, url):
        response = requests.get(url, cookies=self.session_cookie)
        return response

    def login(self):
        api_url = "https://transkribus.eu/TrpServer/rest/auth/login"
        data = {
            "user": self.user,
            "pw": self.password
        }
        headers = {'Content-Type': 'application/x-www-form-urlencoded'}
        response = self.post(api_url, data, headers)
        root = ET.fromstring(response.text)
        self.session_id = root.find("sessionId").text
        self.session_cookie = {"JSESSIONID": self.session_id}
        return {"message": "Login was made successfully!", "errors": False}


transkribusAPI = TranskribusAPI()


# TODO better logging, model view and processes
class Manuscript(BaseModel):
    name: str
    path: str
    process: object
    image_bytes: bytes
    type_of_processing: str


# class Recognition(BaseModel):
#     path_to_file: str
#     methods: classmethod
#     processed_output: dict
#
#
# class OCR(Recognition):
#     pass
#
#
# class HTR(Recognition):
#     pass


class NLP(BaseModel):
    enhance: bool
    raw_output: str
    enhanced_output: str


@app.get("/")
async def index():
    return {"message": "Hello World"}


@app.post("/receive-manuscript-path/", status_code=201)
async def receive_path(path: str):
    return {"path_to_manuscript": path}


# TODO build process OCR
@app.post("/v1/ocr/", status_code=201)
async def process_image_ocr(path_to_manuscript: str, model_ocr):
    return {"text": ""}


# TODO build process HTR
@app.post("/v1/htr/", status_code=201)
async def process_image_htr(path_to_manuscript: str, model_htr):
    return {"text": ""}


# TODO build process NLP
@app.post("/v1/nlp/", status_code=201)
async def process_image_htr(path_to_manuscript: str, model_htr):
    return {"text": ""}


@app.get("/api/v1/third-party/login", status_code=200)
def login_to_transkribus():
    response = transkribusAPI.login()
    return response


@app.get("/api/v1/third-party/get-collections", status_code=200)
def get_user_collections():
    url = "https://transkribus.eu/TrpServer/rest/collections/list"
    response = transkribusAPI.get(url)
    return response.text


if __name__ == "__main__":
    uvicorn.run("main:app", host="127.0.0.1", port=8001, reload=True)
