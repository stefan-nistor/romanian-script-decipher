from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


# TODO better logging, model view and processes
class Manuscript(BaseModel):
    name: str
    path: str
    process: bool
    image_bytes: bytes


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
