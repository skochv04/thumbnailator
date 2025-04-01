import { useState } from "react";
import PhotoDetailsModal from "./PhotoDetailsModal";
import "./PhotoCard.css";

export default function PhotoCard({ photoUrl, id, onSelectPhoto, isSelected, onDelete }) {
    const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);

    const handleSelect = () => {
        onSelectPhoto(id);
    };

    const handleImageClick = () => {
        setIsDetailsModalOpen(true);
    };

    const handleDelete = () => {
        onDelete(id);
    };

    return (
        <>
            <div
                style={{
                    display: "flex",
                    flexDirection: "column",
                    gap: "8px",
                    backgroundColor: isSelected ? "rgba(255, 255, 255, 0.2)" : "transparent",
                    transition: "background-color 0.3s ease",
                    padding: "16px",
                    borderRadius: "8px",
                    boxSizing: "border-box",
                }}
            >
                <img
                    src={photoUrl}
                    alt={`Thumbnail`}
                    style={{
                        width: "100%",
                        aspectRatio: 4 / 3,
                        marginBottom: "8px",
                        cursor: "pointer",
                    }}
                    onClick={handleImageClick}
                />
                <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
                    <a
                        href={photoUrl}
                        style={{
                            backgroundColor: "#00009c",
                            color: "white",
                            padding: "8px 16px",
                            borderRadius: "4px",
                            textDecoration: "none",
                            display: "inline-block",
                            textAlign: "center",
                            cursor: "pointer",
                            transition: "background-color 0.3s ease",
                        }}
                    >
                        Download
                    </a>
                    <button
                        onClick={handleSelect}
                        style={{
                            backgroundColor: isSelected ? "#ff4444" : "#4CAF50",
                            color: "white",
                            border: "none",
                            padding: "8px 16px",
                            borderRadius: "4px",
                            cursor: "pointer",
                            transition: "background-color 0.3s ease",
                        }}
                    >
                        {isSelected ? "Paste" : "Cut"}
                    </button>
                    {/* Delete button */}
                    <button
                        onClick={handleDelete}
                        style={{
                            backgroundColor: "#ff4444",
                            color: "white",
                            border: "none",
                            padding: "8px 16px",
                            borderRadius: "4px",
                            cursor: "pointer",
                            transition: "background-color 0.3s ease",
                        }}
                    >
                        Delete
                    </button>
                </div>
            </div>
            {isDetailsModalOpen && (
                <PhotoDetailsModal
                    isOpen={isDetailsModalOpen}
                    setIsOpen={setIsDetailsModalOpen}
                    id={id}
                />
            )}
        </>
    );
}
