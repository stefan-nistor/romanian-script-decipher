import requests
import os
import xml.etree.ElementTree as ET
from lxml import etree

parser = etree.XMLParser(recover=True)

TMP_MANUSCRIPTS_DIR = "uploads"


# TODO better logging, model view and processes
class ManuscriptUploading:
    name: str
    path: str
    collection_id: int
    upload_id: int
    job_id: int
    doc_id: int
    key: str
    transcripted_key: str

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
            self.doc_id = int(response.text.split("docId>")[1].replace("</", ""))
            return {"message": f"Start uploading manuscript, with job id: {self.job_id}", "errors": False,
                    "jobId": self.job_id}
        else:
            print(response.status_code)
            return {"message": "A problem occur when trying to upload the manuscript", "errors": True}

    def get_key_document(self, collection_id, session_id, version_of_document):
        # TODO put dynamically the doc id
        url = f"https://transkribus.eu/TrpServer/rest/collections/{collection_id}/1737884/{version_of_document}"
        response = requests.get(url, cookies={"JSESSIONID": session_id})

        # TODO improve the code logic here
        self.key = response.text.rsplit("<key>", maxsplit=2)[1].split("</key")[0]

    def apply_ocr_document(self, collection_id, session_id, model_id):
        url = f"https://transkribus.eu/TrpServer/rest/pylaia/{collection_id}/{model_id}/recognition?id=1737884&pages=1&writeKwsIndex=false&doStructures=&clearLines=false&doWordSeg=true&allowConcurrentExecution=false&keepOriginalLinePolygons=false&useExistingLinePolygons=false"
        requests.post(url, cookies={"JSESSIONID": session_id})

        url_get_translated_xml = f"https://files.transkribus.eu/Get?id={self.key}"
        response_req = requests.get(url_get_translated_xml, cookies={"JSESSIONID": session_id})

        final_text = ''
        root = ET.fromstring(response_req.text)
        for child in root:
            if "Page" in child.tag:
                for pageChild in child:
                    if "TextRegion" in pageChild.tag:
                        textRegion = pageChild
                        textRegionChildren = list(textRegion)
                        for trKid in textRegionChildren:
                            if "TextLine" in trKid.tag:
                                for element in trKid:
                                    if "TextEquiv" in element.tag:
                                        for teKid in element:
                                            if "Unicode" in teKid.tag:
                                                text = teKid.text
                                                print(text)
                                                final_text += text
                                final_text += '\n'

        return final_text

    def get_already_ocr_document(self, session_id):
        url_get_translated_xml = f"https://files.transkribus.eu/Get?id={self.key}"
        print(f"url called {url_get_translated_xml}")
        response_req = requests.get(url_get_translated_xml, cookies={"JSESSIONID": session_id})
        print(f'the text is  {response_req.content}')
        # img = Image.open(BytesIO(response_req.content))
        # output_path = "models\\tmp\\image.png"  # Replace with the desired output path
        # img.save(output_path)
        # # creator = root.find('.//{http://schema.primaresearch.org/PAGE/gts/pagecontent/2013-07-15}Creator').text
        # # print("Creator:", creator)
        print(f'req status is {response_req.text}')
        return response_req.text

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
