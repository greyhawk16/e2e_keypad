import React, { useEffect, useState } from 'react';
import axios from "axios";

const App: React.FC = () => {
    const [imageSrc, setImageSrc] = useState<string | null>(null);
    const [keypadInfo, setKeypadInfo] = useState<any>(null);
    const [userInput, setUserInput] = useState<string>('');
    const [clickedPositions, setClickedPositions] = useState<string[]>([]);
    const baseURL = process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080';
    const apiEndpointShowKeypad = `${baseURL}/api/show_keypad`;
    const apiEndpointGetKeypadInfo = `${baseURL}/api/get_public_key`;

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

    const handleImageClick = (event: React.MouseEvent<HTMLImageElement>) => {
        const img = event.currentTarget;
        const rect = img.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;
        const row = Math.floor(y / (rect.height / 3));
        const col = Math.floor(x / (rect.width / 4));
        const positionString = `(${row}, ${col})`;

        setClickedPositions(prevPositions => {
            const newPositions = [...prevPositions, positionString];
            if (newPositions.length === 6) {
                const clickedValues = newPositions.map(pos => keypadInfo[pos]);
                alert(`${JSON.stringify(clickedValues)}`);
            }
            return newPositions;
        });
    };

    return (
        <div className="App">
            <h1>Demo Project</h1>
            {imageSrc ? <img src={imageSrc} alt="Rendered Keypad" onClick={handleImageClick} /> : <p>Loading image...</p>}
            {keypadInfo && <pre>{JSON.stringify(keypadInfo, null, 2)}</pre>}
        </div>
    );
}

export default App;