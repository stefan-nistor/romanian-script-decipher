from locust import HttpUser, task, between


class MyUser(HttpUser):
    wait_time = between(1, 3)

    @task
    def my_task(self):
        self.client.get("/api/v1/third-party/login")

    @task
    def my_task(self):
        self.client.get("/api/v1/third-party/get-collections")
