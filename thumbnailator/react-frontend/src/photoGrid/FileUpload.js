import React, { useState, useRef } from "react";
import "./FileUpload.css";

export default function FileUpload({ currentFolder, onUpload }) {
    const [files, setFiles] = useState([]);
    const fileInputRef = useRef(null);
    const [isLoading, setIsLoading] = useState(false);

    const handleFileChange = (event) => {
        const selectedFiles = event.target.files;
        setFiles([...selectedFiles]);
    };

    const handleFileUpload = () => {
        const formData = new FormData();
        files.forEach((file) => {
            formData.append("files", file);
        });

        formData.append("folder", currentFolder?.path || "root");

        setIsLoading(true);
        setFiles([]);
        if (fileInputRef.current) {
            fileInputRef.current.value = "";
        }

        fetch(`http://localhost:8080/api/images`, {
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
                if (data.invalidFiles.length > 0) {
                    alert(
                        `Could not process some files due to invalid extension. Please check these files: ${data.invalidFiles.join(
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
            >IMAGES</h2>
            <p
                style={{
                    margin: 0,
                }}
            >Choose multiple images.</p>
            <input
                type="file"
                multiple
                onChange={handleFileChange}
                ref={fileInputRef}
            />
            <button onClick={handleFileUpload}>Upload Files</button>
            <p className="loading-message">
                {isLoading ? "Files are being uploaded. Please, wait a second..." : ""}
            </p>
        </div>
    );
}
