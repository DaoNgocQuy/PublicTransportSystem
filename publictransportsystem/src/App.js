import { BrowserRouter, Route, Routes } from "react-router-dom"
import Header from "./component/layouts/Header"
import Footer from "./component/layouts/Footer"
import Home from "./component/Home"
import Register from "./component/Register"
import MapLeaflet from "./component/MapLeaflet"
import React from 'react';
import 'react-toastify/dist/ReactToastify.css';
import { useLocation } from "react-router-dom";
import 'bootstrap/dist/css/bootstrap.min.css';

const AppContent = () => {
  const location = useLocation();
  const isAuthPage = location.pathname === "/register";

  return (
    <>
      {!isAuthPage && <Header />}
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/map"
          element={
            <div className="App">
              <h1>Bản đồ OpenStreetMap với Leaflet</h1>
              <MapLeaflet />
            </div>
          }
        />
        <Route path="/register" element={<Register />} />
      </Routes>
      {!isAuthPage && <Footer />}
    </>
  );
};

const App = () => {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  );
};

export default App