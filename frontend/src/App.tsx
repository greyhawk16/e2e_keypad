import React, { useEffect, useState } from 'react';
import api from './api';

const App: React.FC = () => {
  const [data, setData] = useState<string[]>([]);

  useEffect(() => {
    api.get('/example')
        .then(response => {
          setData(response.data);
        })
        .catch(error => {
          console.error("There was an error fetching the data!", error);
        });
  }, []);

  return (
      <div className="App">
        <h1>Demo Project</h1>
        <ul>
          {data.map((item, index) => (
              <li key={index}>{item}</li>
          ))}
        </ul>
      </div>
  );
}

export default App;
