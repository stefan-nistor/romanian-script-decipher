import base64

from pydantic import BaseModel
import requests
from utils.utils_variables import __URL__, __COLLECTION_ID__

import codecs


# TODO better logging, model view and processes


class ManuscriptUploading:
    name: str
    path: str
    collection_id: int
    upload_id: int

    def __init__(self, name, path, collection_id):
        self.name = name
        self.path = path
        self.collection_id = collection_id

    def upload_manuscript(self, session_id):
        upload_url = f"https://transkribus.eu/TrpServer/rest/uploads?collId={self.collection_id}"
        print(f"collection id is {self.collection_id}")
        data = {
            "md": {
                "title": "BCUTimișoara",
                "author": "",
                "genre": "",
                "writer": ""
            },
            "pageList": {"pages": [
                {
                    "fileName": self.name,
                    "pageNr": 1
                }
            ]}
        }

        response = requests.post(upload_url, cookies={"JSESSIONID": session_id}, json=data)
        if response.status_code == 201 or response.status_code == 200:
            print(response.text.split("uploadId>")[1].replace("</", ""))
            if response.status_code == 201:
                upload_id = response.json()['uploadId']
            else:
                upload_id = int(response.text.split("uploadId>")[1].replace("</", ""))
            print(f"upload_id is {upload_id}")
            self.upload_id = upload_id
            files = {'img': (open(self.path, 'rb'))}
            print("inainte")
            response = requests.put(url='https://transkribus.eu/TrpServer/rest/uploads/{}'.format(upload_id),
                                    files=files, cookies={"JSESSIONID": session_id})
            print("dupa")
            return response.text
        else:
            print(response.status_code)
            return response.text

    def get_manuscript(self):
        pass
