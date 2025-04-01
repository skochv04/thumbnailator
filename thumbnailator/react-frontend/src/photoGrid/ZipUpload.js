import { useState, useRef } from "react";
import "./FileUpload.css";

export default function ZipUpload({ currentFolder, onUpload }) {
    const [file, setFile] = useState(null);
    const fileInputRef = useRef(null);
    const [isLoading, setIsLoading] = useState(false);

    const handleFileChange = (event) => {
        const selectedFile = event.target.files[0]; // Get the first selected file
        if (selectedFile) {
            setFile(selectedFile);
        }
    };

    const handleFileUpload = () => {
        if (!file) {
            alert("Please select a ZIP file before uploading.");
            return;
        }

        const formData = new FormData();
        formData.append("file", file); // Attach the ZIP file

        formData.append("folder", currentFolder?.path || "root");

        setIsLoading(true);
        setFile(null); // Reset file state
        if (fileInputRef.current) {
            fileInputRef.current.value = ""; // Clear input value
        }

        fetch(`http://localhost:8080/api/zip`, {
            method: "POST",
            body: formData,
        })
            .then((response) => {
                if (response.ok) {
                    onUpload();
                    return response.json();
                }
                throw new Error(`HTTP error! Status: ${response.status}`);
            })
            .then((data) => {
                if (data.invalidFiles && data.invalidFiles.length > 0) {
                    alert(
                        `Could not process the file due to an invalid extension. File: ${data.invalidFiles.join(
                            ", "
                        )}`
                    );
                }
                setIsLoading(false);
            })
            .catch((error) => {
                console.error("Error occurred:", error);
                setIsLoading(false);
            });
    };

    return (
        <div className="file-upload-container">
            <h2
                style={{
                    margin: 0,
                }}
            >ZIP</h2>
            <p
                style={{
                    margin: 0,
                }}
            >Choose one zip file.</p>
            <input
                type="file"
                onChange={handleFileChange}
                ref={fileInputRef}
                accept=".zip" // Restrict file types to ZIP files
            />
            <button onClick={handleFileUpload}>Upload ZIP File</button>
            <p className="loading-message">
                {isLoading ? "File is being uploaded. Please, wait a second..." : ""}
            </p>
        </div>
    );
}
