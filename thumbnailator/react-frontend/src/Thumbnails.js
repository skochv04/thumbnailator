import React, {useEffect} from 'react';
import './App.css';
import PhotoGrid from "./photoGrid/PhotoGrid";
import {useParams} from "react-router-dom";

export default function Thumbnails() {
    return (
        <div className="App">
            <div className="App-content">
                <PhotoGrid />
            </div>
        </div>
    );
}