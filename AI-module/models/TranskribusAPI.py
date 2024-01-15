import requests
import os
import xml.etree.ElementTree as ET
from urllib.parse import urlencode
from utils.utils_variables import __URL__


class TranskribusAPI:
    session_id: str
    session_cookie: dict
    base_url: str
    user: str
    password: str

    def __init__(self):
        self.user = os.environ.get("TRANSKRIBUS_USER")
        self.password = os.environ.get("TRANSKRIBUS_PASSWORD")
        self.session_cookie = {}
        self.base_url = __URL__

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
        api_url ="https://transkribus.eu/TrpServer/rest/auth/login"
        data = {
            "user": self.user,
            "pw": self.password
        }
        headers = {'Content-Type': 'application/x-www-form-urlencoded'}

        response = self.post(api_url, data, headers)

        root = ET.fromstring(response.text)
        self.session_id = root.find("sessionId").text
        print("Session ID: ", self.session_id)
        self.session_cookie = {"JSESSIONID": self.session_id}

        return {"message": "Login was made successfully!", "errors": False}
