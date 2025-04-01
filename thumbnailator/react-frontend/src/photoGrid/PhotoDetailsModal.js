import "./PhotoDetailsModal.css"
import {useEffect, useState} from "react";

export default function PhotoDetailsModal({setIsOpen, id}) {
    const [originalImageUrl, setOriginalImageUrl] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetch(`http://localhost:8080/api/thumbnails/${id}/original`, {
            method: 'GET',
        })
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error(`HTTP error! Status: ${response.status}`);
            })
            .then((data) => {
                    setOriginalImageUrl(data.imageUrl)
                }
            )
            .catch(error => {
                console.error("Error occurred:", error);
                setError(error);
            });
    }, []);

    return (
        <div className="overlay" onClick={() => setIsOpen(false)}>
            <div className="modal" onClick={(e) => {
                e.stopPropagation()
            }}>
                {originalImageUrl && (
                    <img
                        src={originalImageUrl}
                        alt={`Thumbnail`}
                        style={{width: "100%", height: "100%"}}
                    />
                )}
                {error && (
                    <div className="error">
                        Could not fetch requested photo
                    </div>
                )}
                <div style={{display: "flex", flexDirection: "column", gap: "8px"}}>
                    <a href={originalImageUrl}>Download</a>
                    <button onClick={() => setIsOpen(false)}>Close dialog</button>
                </div>
            </div>
        </div>
    )
}