import React, { useCallback, useEffect, useRef, useState } from "react";
import {useLocation, useNavigate} from "react-router-dom";
import PhotoCard from "./PhotoCard";
import LoadingCard from "./LoadingCard";
import FileUpload from "./FileUpload";
import ZipUpload from "./ZipUpload"
import FolderTree from "./FolderTree";
import "./PhotoGrid.css";
import Pagination from "./Pagination";
import ErrorScreen from "./ErrorScreen";

export default function PhotoGrid() {
    const PAGE_SIZE = 24;
    const location = useLocation();
    const navigate = useNavigate();
    const searchParams = new URLSearchParams(location.search)
    const [size, setSize] = useState(searchParams.get("size") || "middle");
    const [currentFolder, setCurrentFolder] = useState(searchParams.get("path") || null);
    const [thumbnails, setThumbnails] = useState([]);
    const [folders, setFolders] = useState([]);
    const eventSourceRef = useRef(null);
    const [error, setError] = useState(null);
    const [totalNumberOfImages, setTotalNumberOfImages] = useState(0);
    const [totalNumberOfImagesPerPage, setTotalNumberOfImagesPerPage] = useState(0);
    const [numberOfProcessedImagesPerPage, setNumberOfProcessedImagesPerPage] = useState(0);
    const [selectedPhotos, setSelectedPhotos] = useState([]);
    const imagesBeingProcessed = totalNumberOfImagesPerPage - numberOfProcessedImagesPerPage;
    const page = Number(searchParams.get("page")) || 0;


    const updatePageInURL = (newPage) => {
        searchParams.set("page", newPage);
        navigate(`${location.pathname}?${searchParams.toString()}`);
    };

    useEffect(() => {
        updatePageInURL(0);
    }, [currentFolder])

    const addEventSource = useCallback(() => {
        if (!currentFolder || !currentFolder.path) {
            console.warn("No folder selected. Skipping SSE connection.");
            return;
        }

        if (eventSourceRef.current) {
            eventSourceRef.current.close();
            eventSourceRef.current = null;
        }

        setThumbnails([]);

        eventSourceRef.current = new EventSource(
            `http://localhost:8080/api/thumbnails?path=${encodeURIComponent(currentFolder.path)}&size=${size}&page=${page}`
        );


        eventSourceRef.current.onopen = () => {
            console.log("SSE connection established");
            setError(null);
        };

        eventSourceRef.current.onmessage = (event) => {
            const parsedData = JSON.parse(event.data);
            setThumbnails((prev) => [
                ...prev,
                { url: parsedData.imageUrl, id: parsedData.id },
            ]);
        };

        eventSourceRef.current.onerror = (error) => {
            console.error("Error in SSE connection:", error);
            eventSourceRef.current.close();
            eventSourceRef.current = null;
            setError(error);
        };
    }, [size, currentFolder, page]);

    const handleFolderClick = (folderPath) => {
        if (currentFolder?.path === folderPath) {
            console.log("Folder already selected:", folderPath);
            return;
        }

        fetch(`http://localhost:8080/api/folders?path=${encodeURIComponent(folderPath)}`, {
            method: "GET",
        })
            .then((response) => response.json())
            .then((data) => {
                setCurrentFolder(data);
                setThumbnails([]);
                fetchFolders(data.path);
            })
            .catch((error) => {
                console.error("Error selecting folder:", error);
                alert("Error selecting folder")
            });
    };

    const fetchFolders = useCallback((path) => {
        fetch(`http://localhost:8080/api/folders/subfolders?path=${encodeURIComponent(path)}`, {
            method: "GET",
        })
            .then((response) => response.json())
            .then((data) => {
                setFolders(data);
            })
            .catch((error) => {
                console.error("Error fetching folders:", error);
                alert("Error fetching folders")
            });
    }, []);

    const handleDeselectPhoto = (id) => {
        setSelectedPhotos((prevSelectedPhotos) => {
            const photo = prevSelectedPhotos.find((photo) => photo.id === id);
            if (!photo) return prevSelectedPhotos;

            if (currentFolder && currentFolder.path !== photo.folderPath) {
                fetch(`http://localhost:8080/api/thumbnails/${id}/move`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ newFolderPath: currentFolder.path }),
                })
                    .then((response) => {
                        if (!response.ok) {
                            throw new Error("Failed to move photo");
                        }
                        console.log(`Photo ${id} moved to ${currentFolder.path}`);
                        addEventSource();
                    })
                    .catch((error) => {
                        console.error("Error moving photo:", error);
                    });
            }

            return prevSelectedPhotos.filter((photo) => photo.id !== id);
        });
    };

    const handleDeletePhoto = (id) => {
        const confirmDelete = window.confirm("Are you sure you want to delete this photo?");
        if (confirmDelete) {
            fetch(`http://localhost:8080/api/thumbnails/${id}/delete`, {
                method: "DELETE",
            })
                .then((response) => {
                    if (!response.ok) {
                        throw new Error("Failed to delete photo");
                    }
                    setThumbnails((prevThumbnails) => prevThumbnails.filter((photo) => photo.id !== id));
                    setSelectedPhotos((prevSelectedPhotos) => prevSelectedPhotos.filter((photo) => photo.id !== id));
                    console.log(`Photo ${id} deleted successfully.`);
                })
                .catch((error) => {
                    console.error("Error deleting photo:", error);
                });
        }
    };

    const handleGoBack = () => {
        if (!currentFolder || currentFolder.path === "root") return;
        const parentPath = currentFolder.path.slice(0, currentFolder.path.lastIndexOf("/"));
        const updatedPath = parentPath === "" ? "/root" : parentPath;
        handleFolderClick(updatedPath);
    };

    const handleGoToRoot = () => {
        handleFolderClick("root");
    };

    const handleSelectPhoto = (id, url) => {
        setSelectedPhotos((prevSelectedPhotos) => {
            const alreadySelected = prevSelectedPhotos.some((photo) => photo.id === id);
            if (alreadySelected) {
                return prevSelectedPhotos.filter((photo) => photo.id !== id);
            }
            return [...prevSelectedPhotos, { id, url, folderPath: currentFolder.path }];
        });
    };

    const mergedThumbnails = [
        ...thumbnails,
        ...selectedPhotos.filter(
            (selected) => !thumbnails.some((thumb) => thumb.id === selected.id)
        ),
    ];

    const handleDeleteFolder = async (folderId) => {
        const confirmDelete = window.confirm(
            "Are you sure you want to delete this folder and all its images?"
        );
        if (!confirmDelete) return;

        try {
            const deleteFolderResponse = await fetch(
                `http://localhost:8080/api/folders/delete/${folderId}`,
                { method: "DELETE" }
            );

            if (!deleteFolderResponse.ok) {
                throw new Error("Failed to delete folder.");
            }
            console.log(`Folder ${folderId} deleted successfully.`);

            // update UI (???)
            setFolders((prevFolders) =>
                prevFolders.filter((folder) => folder.id !== folderId)
            );
        } catch (error) {
            console.error("Error deleting folder and its images:", error);
            alert("Error deleting folder and its images")
        }
    };


    useEffect(() => {
        const rootPath = "root";
        fetch(`http://localhost:8080/api/folders?path=${encodeURIComponent(rootPath)}`, {
            method: "GET",
        })
            .then((response) => response.json())
            .then((data) => {
                setCurrentFolder(data);
                fetchFolders(data.path);
            })
            .catch((error) => console.error(error));
    }, [fetchFolders]);

    useEffect(() => {
        addEventSource();
    }, [size, currentFolder, page]);

    useEffect(() => {
        if (error) {
            const retryTimer = setTimeout(() => {
                console.log('Retrying connection...');
                addEventSource();
            }, 3000);

            return () => clearTimeout(retryTimer);
        }
    }, [error, addEventSource]);

    useEffect(() => {
        if (!currentFolder) {
            return;
        }

        fetch(`http://localhost:8080/api/thumbnails/details?path=${encodeURIComponent(currentFolder.path)}&size=${size}&page=${page}`, {
            method: "GET",
        })
            .then((response) => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error(`HTTP error! Status: ${response.status}`);
            })
            .then((data) => {
                setTotalNumberOfImages(data.totalNumberOfImages);
                setTotalNumberOfImagesPerPage(data.totalNumberOfImagesPerPage)
                setNumberOfProcessedImagesPerPage(data.numberOfProcessedImagesPerPage);
            })
            .catch((error) => {
                console.error("Error occurred:", error);
            });
    }, [currentFolder, size, page, thumbnails]);

    const handleCreateFolder = (folderName) => {
        const parentPath = currentFolder ? currentFolder.path : '/root';
        fetch(`http://localhost:8080/api/folders/create?name=${folderName}&parentPath=${parentPath}`, {
            method: 'POST',
        })
            .then(response => response.json())
            .then(data => {
                console.log("Folder created successfully:", data);
                setFolders((prevFolders) => [...prevFolders, data]);
            })
            .catch(error => {
                console.error("Error creating folder:", error);
                alert(`Error creating folder`)
            });
    };

    const getGridColumns = () => {
        switch (size) {
            case "small":
                return "repeat(8, 1fr)";
            case "middle":
                return "repeat(6, 1fr)";
            case "big":
                return "repeat(4, 1fr)";
            default:
                return "repeat(6, 1fr)";
        }
    };

    return (
        <div
            style={{
                width: "100%",
                height: "100%",
                display: "flex",
                gap: "20px",
            }}
        >
            <FolderTree
                folders={folders}
                currentFolder={currentFolder}
                onFolderClick={handleFolderClick}
                onCreateFolder={handleCreateFolder}
                onGoBack={handleGoBack}
                onGoToRoot={handleGoToRoot}
                onDeleteFolder={handleDeleteFolder}
            />
            <div
                style={{
                    width: "80%",
                    display: "flex",
                    flexDirection: "column",
                }}
            >
                <div className="size-selector">
                    <label>
                        <div className="size-label">Select size:</div>
                        <select value={size} onChange={(e) => setSize(e.target.value)}>
                            <option value="small">Small</option>
                            <option value="middle">Middle</option>
                            <option value="big">Big</option>
                        </select>
                    </label>
                </div>
                <div
                    style={{
                        width: "100%",
                        height: "100%",
                        display: "flex",
                        justifyContent: "space-evenly"
                    }}
                >
                    <FileUpload currentFolder={currentFolder} onUpload={() => addEventSource()}/>
                    <ZipUpload currentFolder={currentFolder} onUpload={() => addEventSource()}/>
                </div>
                {error && <ErrorScreen />}

                <div style={{  fontSize: "1rem", color: "#FFFFFF" }}>
                    <p>To move images to another folder, just click "Cut" and navigate to the target folder and then click "Paste".</p>
                </div>

                <div
                    style={{
                        display: "grid",
                        gridTemplateColumns: getGridColumns(),
                        gap: "20px",
                        width: "100%",
                    }}
                >
                    {mergedThumbnails.map(({url, id}) => (
                        <PhotoCard
                            photoUrl={url}
                            key={id}
                            id={id}
                            onSelectPhoto={() => {
                                const alreadySelected = selectedPhotos.some((photo) => photo.id === id);
                                if (alreadySelected) {
                                    handleDeselectPhoto(id, url);
                                } else {
                                    handleSelectPhoto(id, url);
                                }
                            }}
                            isSelected={selectedPhotos.some((photo) => photo.id === id)}
                            onDelete={handleDeletePhoto}
                        />
                    ))}
                    {Array.from({length: imagesBeingProcessed}).map((_, index) => (
                        <LoadingCard key={index}/>
                    ))}
                </div>
                <Pagination totalPages={Math.ceil(totalNumberOfImages / PAGE_SIZE)} />
            </div>
        </div>
    );
}
