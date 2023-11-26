document.addEventListener("DOMContentLoaded", function () {
  let original_filename = null;

  fetchAndDisplayImage();
  clearTextArea();
});

function fetchAndDisplayImage() {
  fetch("http://localhost:8080/api/annotator", {
    method: "GET",
  })
    .then((response) => {
      if (response.ok) {
        const contentType = response.headers.get("Content-Type");
        const contentDisposition = response.headers.get("Content-Disposition");

        // Log the headers or use them as needed
        original_filename = contentDisposition.match(/filename="([^"]+)"/)[1];
        return response.blob();
      }
      throw new Error("Network response was not ok.");
    })
    .then((blob) => {
      const imageUrl = URL.createObjectURL(blob);
      const imageElement = document.createElement("img");
      imageElement.src = imageUrl;
      document.getElementById("image-container").appendChild(imageElement);
    })
    .catch((error) => {
      console.error("Error:", error);
    });
}

function clearTextArea() {
  // Assuming your textarea has an ID of 'textarea'
  document.getElementById("text-input").value = "";
}

function showPopup() {
  var popup = document.getElementById("savingPopup");
  var overlay = document.getElementById("overlay");
  popup.style.display = "block";
}

function closePopup(popupId) {
  document.getElementById(popupId).style.display = "none";

  if (popupId != "savingPopup") {
    location.reload();
  }
}

function saveChanges() {
  var textEntered = document.getElementById("text-input").value;

  fetch("http://localhost:8080/api/annotator", {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: new URLSearchParams({
      originalImageFilename: original_filename,
      decipheredText: textEntered,
    }),
  }).then((response) => {
    showResponsePopup(response.status);
  });
}

function showResponsePopup(responseStatus) {
  closePopup("savingPopup");
  console.log(responseStatus);
  responseStatus === 200
    ? (document.getElementById("successPopup").style.display = "block")
    : (document.getElementById("failurePopup").style.display = "block");
}
