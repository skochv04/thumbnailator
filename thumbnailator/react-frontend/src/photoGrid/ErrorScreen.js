import React from "react";

export default function ErrorScreen() {
    return (
        <div
            style={{
                position: "fixed",
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                backgroundColor: "rgba(0, 0, 0, 0.9)",
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                zIndex: 1000,
            }}
        >
            <div className="loading-spinner"></div>
            <p
                style={{
                    color: "#ff4444",
                    fontSize: "1.2rem",
                    marginTop: "20px",
                }}
            >
                Connection lost. Trying to reconnect...
            </p>
        </div>
    );
}
