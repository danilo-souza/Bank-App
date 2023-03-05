import './App.css';

import NavBar from "./components/NavBar";
import {Routes, Route} from 'react-router-dom';

import React from 'react';

import 'bootstrap/dist/css/bootstrap.min.css';

import CreateAccount from "./pages/createAccount"
import Home from './pages/home';
import Login from './pages/login';
import Accounts from './pages/Accounts';
import Account from './pages/Account';

function App() {
  const [username, setUsername] = React.useState("");

  const backgroundStyle = {
    parent: {
      backgroundImage:'url(./assets/logo.png)',
      backgroundSize: 'contain',
      objectFit: 'cover',
      backgroundRepeat: 'no-repeat',
      backgroundPosition: "center",
      minHeight: '90vh',
      position: 'relative'
    },
    overlay: {
      position: 'absolute',
      top: '0',
      bottom: '0',
      left: '0',
      right: '0',
      backgroundColor: 'rgba(255, 255, 255, 0.8)',
      zIndex: '0'
    },
    content: {
      position: 'relative',
      zIndex: '1'
    }
  }

  return (
    <div className="App">
        <NavBar className="navbar"/>
        <main>
          <div className="parent" style={backgroundStyle.parent}>
          <div className="overlay" style={backgroundStyle.overlay} />
            <div className="content" style={backgroundStyle.content}>
              <br />
              <Routes>
                <Route index element={<Home />} />
                <Route path="/" element={<Home />} />
                <Route path="/CreateAccount" element={<CreateAccount />} />
                <Route path="/Login" element={<Login username={username} setUsername={setUsername} />} />
                <Route path="/Accounts" element={<Accounts />} />
                <Route path="/Accounts/Checking" element={<Account type="Checking" />} />
                <Route path="/Accounts/Savings" element={<Account type="Savings" />} />
              </Routes>
            </div>
          </div>
        </main>
    </div>
  );
}

export default App;
