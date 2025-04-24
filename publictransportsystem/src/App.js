import { BrowserRouter, Route, Routes } from "react-router-dom"
import Header from "./component/layouts/Header"
import Footer from "./component/layouts/Footer"
import Home from "./component/Home"
import Register from "./component/Register"
import MapLeaflet from "./component/MapLeaflet"
import React from 'react';

const App = () => {
  return (

    <BrowserRouter>
      <Header />
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
      <Footer />
    </BrowserRouter>
  )
}

export default App