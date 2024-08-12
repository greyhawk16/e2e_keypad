import React, { useEffect, useState } from 'react';
import axios from "axios";
// import api from './api';


let NumHashMap: any = null;

const App: React.FC = () => {
    const [imageSrc, setImageSrc] = useState<string | null>(null);
    const [keypadInfo, setKeypadInfo] = useState<any>(null);
    const [userInput, setUserInput] = useState<string>('');
    const baseURL = process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080';
    const apiEndpointShowKeypad = `${baseURL}/api/show_keypad`;
    const apiEndpointGetKeypadInfo = `${baseURL}/api/get_kaypad_secret_key`;

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
                    NumHashMap = response.data; // Store result in global variable
                    console.log("Keypad Info:", response.data); // Print the received result
                }
            })
            .catch(error => {
                console.error("There was an error!", error);
            });
    }, []);

    const handleKeyPress = (event: React.KeyboardEvent) => {
        const { key } = event;

        // Check if the key is a digit
        if (/^\d$/.test(key)) {
            if (userInput.length < 6) {
                setUserInput(prevInput => prevInput + key);
            }
        } else if (key === 'Backspace') {
            setUserInput(prevInput => prevInput.slice(0, -1));
        }
    };

    const showAlert = () => {
        const mappedValues = userInput.split('').map(digit => NumHashMap[digit]);
        alert(`User Input: ${userInput}\nMapped Values: ${mappedValues.join(', ')}`);
    };

    return (
        <div className="App" tabIndex={0} onKeyDown={handleKeyPress}>
            <h1>Demo Project</h1>
            {imageSrc ? <img src={imageSrc} alt="Rendered Keypad" /> : <p>Loading image...</p>}
            {NumHashMap && <pre>{JSON.stringify(NumHashMap, null, 2)}</pre>}
            <h2>Enter 6-digit Code</h2>
            <p>{userInput}</p>
            <button onClick={showAlert}>Show User Input</button>
        </div>
    );
}

export default App;