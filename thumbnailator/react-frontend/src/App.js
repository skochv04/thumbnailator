import React, { useState } from "react";
import "./App.css";
import { useNavigate } from "react-router-dom";

export default function App() {
  const [files, setFiles] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [size, setSize] = useState("middle");
  const navigate = useNavigate();

  const handleFileChange = (event) => {
    const selectedFiles = event.target.files;
    setFiles([...selectedFiles]);
  };

  const handleFileUpload = () => {
    if (files.length === 0) {
      alert("Please select files before uploading.");
      return;
    }

    setIsLoading(true);

    const formData = new FormData();
    files.forEach((file) => {
      formData.append("files", file);
    });

    fetch("http://localhost:8080/api/batches", {
      method: "POST",
      body: formData,
    })
      .then((response) => {
        if (response.ok) {
          return response.json();
        }
        throw new Error(`HTTP error! Status: ${response.status}`);
      })
      .then((data) => {
        if (data.invalidFiles.length > 0) {
          alert(
              `${data.numberOfProcessedImages} files processed successfully. ${
                  data.invalidFiles.length
              } files could not be processed. Please check the file extensions of these files: ${data.invalidFiles.join(
                  ", "
              )}`
          );
        }
        setFiles([]);
        setIsLoading(false);
        navigate(`/thumbnails/${data.batchId}`, { state: { size } });
      })
      .catch((error) => {
        console.error("Error occurred:", error);
        setIsLoading(false);
      });
  };

  return (
      <div className="App">
        <div className="App-content">
          <div className="size-selector">
            <label>
              <div className="size-label">
                Select size:
              </div>
              <select value={size} onChange={(e) => setSize(e.target.value)}>
                <option value="small">Small</option>
                <option value="middle">Middle</option>
                <option value="big">Big</option>
              </select>
            </label>
          </div>
          <div className="file-upload-container">
            <input type="file" multiple onChange={handleFileChange} />
            <button onClick={handleFileUpload} disabled={isLoading}>
              Upload Files
            </button>
          </div>
          {isLoading && (
              <p className="loading-message">
                Loading started. Please, wait a second...
              </p>
          )}
        </div>
      </div>
  );
}
