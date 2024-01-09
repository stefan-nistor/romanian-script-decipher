import requests
import os

TMP_MANUSCRIPTS_DIR = "uploads"


# TODO better logging, model view and processes
class ManuscriptUploading:
    name: str
    path: str
    collection_id: int
    upload_id: int
    job_id: int

    def __init__(self, name, collection_id):
        self.name = name
        self.collection_id = collection_id
        self.path = os.path.join(TMP_MANUSCRIPTS_DIR, self.name)

    def save_manuscript(self, file):
        manuscript_path = os.path.join(TMP_MANUSCRIPTS_DIR, self.name)
        with open(manuscript_path, "wb") as f:
            f.write(file.file.read())
        self.path = manuscript_path

    def delete_manuscript(self):
        if os.path.exists(self.path):
            os.remove(self.path)
            return True
        return False

    def create_manuscript_request_data(self):
        data = {
            "md": {
                "title": os.path.splitext(self.name)[0],
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
        return data

    def upload_manuscript(self, session_id):
        upload_url = f"https://transkribus.eu/TrpServer/rest/uploads?collId={self.collection_id}"
        print(f"collection id is {self.collection_id}")
        data = self.create_manuscript_request_data()

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
            self.job_id = int(response.text.split("jobId>")[1].replace("</", ""))
            return {"message": f"Start uploading manuscript, with job id: {self.job_id}", "errors": False}
        else:
            print(response.status_code)
            return {"message": "A problem occur when trying to upload the manuscript", "errors": True}

    @staticmethod
    def get_manuscript_status(job_id: int, session_id: str):
        url = f"https://transkribus.eu/TrpServer/rest/jobs/{job_id}"
        print(f"Job id for the manuscript is {job_id}")
        response = requests.get(url, cookies={"JSESSIONID": session_id})
        # while response.json["state"] != 'FINISHED':
        #     export_status = requests.get(f'https://transkribus.eu/TrpServer/rest/jobs/{self.job_id}')
        #     export_status = export_status.json()
        #     time.sleep(10)
        return response.text
