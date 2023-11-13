from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


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


@app.post("/v1/ocr/", status_code=201)
async def process_image_ocr(path_to_manuscript: str, model_ocr):
    return {"text": ""}


@app.post("/v1/htr/", status_code=201)
async def process_image_htr(path_to_manuscript: str, model_htr):
    return {"text": ""}
