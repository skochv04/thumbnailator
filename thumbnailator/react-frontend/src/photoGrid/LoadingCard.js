import "./LoadingCard.css";
import LoadingDots from "./LoadingDots";

export default function LoadingCard() {
    return (
        <div style={{width: "100%", height: "100%", display: "flex", flexDirection: "column", justifyContent: "space-between", gap: "8px"}}>
            <div style={{width: "100%", aspectRatio: 4/3, display: "flex", justifyContent: "center", alignItems: "center", textAlign: "center", position: "relative"}}>
                <LoadingDots />
            </div>
            <div style={{display: "flex", flexDirection: "column", gap: "8px"}}>
                <button disabled className="disabled">Download</button>
                <button disabled className="disabled">Photo details</button>
            </div>
        </div>
    )
}