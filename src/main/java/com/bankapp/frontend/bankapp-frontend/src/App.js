import logo from './logo.svg';
import './App.css';

import NavBar from "./components/NavBar";
import {Routes, Route, Navigate} from 'react-router-dom';

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          Edit <code>src/App.js</code> and save to reload.
        </p>
        <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        >
          Learn React
        </a>
      </header>

      <NavBar />
      <main>
        <Routes>
        </Routes>
      </main>
    </div>
  );
}

export default App;
