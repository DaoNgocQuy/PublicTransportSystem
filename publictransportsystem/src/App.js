import { BrowserRouter, Route, Routes, Navigate } from "react-router-dom"
import Header from "./components/layouts/Header"
import Footer from "./components/layouts/Footer"
import Home from "./components/Home"
import Register from "./components/Register"
import Login from "./components/Login"
import MapLeaflet from "./components/Map/MapLeaflet"
import React from 'react';
import 'react-toastify/dist/ReactToastify.css';
import { useLocation } from "react-router-dom";
import 'bootstrap/dist/css/bootstrap.min.css';

// Protected Route component
const ProtectedRoute = ({ children }) => {
  const isAuthenticated = localStorage.getItem('isLoggedIn') === 'true';

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

const AppContent = () => {
  const location = useLocation();
  const isAuthPage = location.pathname === "/register" || location.pathname === "/login";

  return (
    <>
      {!isAuthPage && <Header />}
      <Routes>
        <Route path="/" element={
          <ProtectedRoute>
            <Home />
          </ProtectedRoute>
        } />
        <Route path="/map" element={
          <ProtectedRoute>
            <div className="App">
              <h1>Bản đồ OpenStreetMap với Leaflet</h1>
              <MapLeaflet />
            </div>
          </ProtectedRoute>
        } />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
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

export default App;