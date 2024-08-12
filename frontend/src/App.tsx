import React, { useEffect, useState } from 'react';
import axios from "axios";
// import api from './api';

const App: React.FC = () => {
    const [imageSrc, setImageSrc] = useState<string | null>(null);
    const [keypadInfo, setKeypadInfo] = useState<any>(null);
    const baseURL = process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080';
    const apiEndpointShowKeypad = `${baseURL}/api/show_keypad`;
    const apiEndpointGetKeypadInfo = `${baseURL}/api/get_kaypad_info`;

    useEffect(() => {
        axios.get(apiEndpointShowKeypad, { responseType: 'arraybuffer' })
            .then(response => {
                if (response.status === 200) {
                    const base64String = btoa(
                        new Uint8Array(response.data)
                            .reduce((data, byte) => data + String.fromCharCode(byte), '')
                    );
                    const imageSrc = `data:image/png;base64,${base64String}`;
                    setImageSrc(imageSrc);

                    // Call the second API after successfully fetching the image
                    return axios.get(apiEndpointGetKeypadInfo);
                } else {
                    throw new Error("Failed to fetch image");
                }
            })
            .then(response => {
                if (response.status === 200) {
                    setKeypadInfo(response.data);
                    console.log("Keypad Info:", response.data); // Print the received result
                }
            })
            .catch(error => {
                console.error("There was an error!", error);
            });
    }, []);

    return (
        <div className="App">
            <h1>Demo Project</h1>
            {imageSrc ? <img src={imageSrc} alt="Rendered Keypad" /> : <p>Loading image...</p>}
            {keypadInfo && <pre>{JSON.stringify(keypadInfo, null, 2)}</pre>}
        </div>
    );
}

export default App;