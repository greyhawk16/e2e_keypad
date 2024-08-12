import React, { useEffect, useState } from 'react';
import axios from "axios";
// import api from './api';

const App: React.FC = () => {
    const [imageSrc, setImageSrc] = useState<string | null>(null);

    useEffect(() => {
        axios.get('http://localhost:8080/api/example', { responseType: 'arraybuffer' })
            .then(response => {
                if (response.status === 200) {
                    const base64String = btoa(
                        new Uint8Array(response.data)
                            .reduce((data, byte) => data + String.fromCharCode(byte), '')
                    );
                    const imageSrc = `data:image/png;base64,${base64String}`;
                    setImageSrc(imageSrc);
                }
            })
            .catch(error => {
                console.error("There was an error fetching the image!", error);
            });
    }, []);

    return (
        <div className="App">
            <h1>Demo Project</h1>
            {imageSrc ? <img src={imageSrc} alt="Rendered Keypad" /> : <p>Loading image...</p>}
        </div>
    );
}

export default App;