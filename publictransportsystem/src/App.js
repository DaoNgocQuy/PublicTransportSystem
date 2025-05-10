import { BrowserRouter, Route, Routes, Navigate, useLocation } from "react-router-dom"
import { useReducer } from "react";
import Header from "./components/layouts/Header"
import Home from "./components/Home"
import Register from "./components/Register"
import Login from "./components/Login"
import MapLeaflet from "./components/Map/MapLeaflet"
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import cookie from "react-cookies";
import { UserContext, UserDispatchContext } from "./configs/MyContexts";

const ProtectedRoute = ({ children }) => {
  const isAuthenticated = sessionStorage.getItem('isLoggedIn') === 'true';

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

const userReducer = (currentState, action) => {
  switch (action.type) {
    case "login":
      return action.payload;
    case "logout":
      cookie.remove("token", { path: '/' });
      cookie.remove("user", { path: '/' });
      sessionStorage.removeItem("isLoggedIn");
      sessionStorage.removeItem("user");
      return null;
    default:
      return currentState;
  }
};

const AppContent = () => {
  const initialUser = cookie.load("user") || JSON.parse(sessionStorage.getItem("user") || "null");
  const [user, dispatch] = useReducer(userReducer, initialUser);
  const location = useLocation();

  // Kiểm tra xem đang ở trang đăng nhập hay đăng ký
  const isAuthPage = location.pathname === '/login' || location.pathname === '/register';

  return (
    <>
      {!isAuthPage && <Header />}
      <UserContext.Provider value={user}>
        <UserDispatchContext.Provider value={dispatch}>
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
        </UserDispatchContext.Provider>
      </UserContext.Provider>

      <ToastContainer
        position="top-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop
        closeOnClick
        rtl={false}
        pauseOnFocusLoss={false} 
        draggable
        pauseOnHover={false}     
        limit={3}               
      />

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