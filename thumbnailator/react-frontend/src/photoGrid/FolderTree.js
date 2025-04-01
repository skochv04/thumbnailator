import React, { useState } from 'react';

export default function FolderTree({ folders, currentFolder, onFolderClick, onCreateFolder, onGoBack, onGoToRoot, onDeleteFolder }) {
    const [newFolderName, setNewFolderName] = useState("");

    const handleCreateFolder = () => {
        if (newFolderName.trim() === "") return;
        onCreateFolder(newFolderName);
        setNewFolderName("");
    };

    const isRoot = currentFolder?.path === "/root";

    return (
        <div
            style={{
                width: "20%",
                display: "flex",
                flexDirection: "column",
                borderRight: "1px solid #ccc",
            }}
        >
            <h3>Current Folder: {currentFolder?.folderNameFromPath || "Loading..."}</h3>
            <button
                onClick={onGoBack}
                disabled={isRoot}
                style={{
                    padding: "8px",
                    backgroundColor: isRoot ? "#ccc" : "#007BFF",
                    color: "white",
                    cursor: isRoot ? "not-allowed" : "pointer",
                    marginBottom: "10px",
                }}
                title={isRoot ? "You're already at the root folder" : "Go to the parent folder"}
            >
                Go Back
            </button>
            <button
                onClick={onGoToRoot}
                disabled={isRoot}
                style={{
                    padding: "8px",
                    backgroundColor: "#40826d",
                    color: "white",
                    cursor: isRoot ? "not-allowed" : "pointer",
                    marginBottom: "10px",
                }}
                title="Go to the root folder"
            >
                Go to Root Folder
            </button>
            <div style={{ overflowY: "auto" }}>
                {folders.map((folder) => (
                    <div
                        key={folder.id}
                        style={{
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "space-between",
                            padding: "10px",
                            borderBottom: "1px solid #ddd",
                        }}
                    >
                        <span
                            onClick={() => onFolderClick(folder.path)}
                            style={{
                                cursor: "pointer",
                            }}
                        >
                            {folder.path.split('/').pop()}
                        </span>
                        <button
                            onClick={() => onDeleteFolder(folder.id)}
                            style={{
                                padding: "5px 10px",
                                backgroundColor: "#FF6347",
                                color: "white",
                                cursor: "pointer",
                                border: "none",
                                borderRadius: "5px",
                            }}
                        >
                            Delete
                        </button>
                    </div>
                ))}
            </div>
            <div style={{ marginTop: "20px" }}>
                <input
                    type="text"
                    value={newFolderName}
                    onChange={(e) => setNewFolderName(e.target.value)}
                    placeholder="Enter new folder name"
                    style={{
                        width: "80%",
                        padding: "8px",
                        marginRight: "10px",
                    }}
                />
                <button
                    onClick={handleCreateFolder}
                    style={{
                        padding: "8px",
                        backgroundColor: "#4CAF50",
                        color: "white",
                        cursor: "pointer",
                    }}
                >
                    Create Folder
                </button>
            </div>
        </div>
    );
}
