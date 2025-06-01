import { BrowserRouter, Route, Routes, Navigate, useLocation } from "react-router-dom"
import { useReducer, useState, useEffect } from "react";
import Header from "./components/layouts/Header"
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import cookie from "react-cookies";
import './App.css';
import { UserContext, UserDispatchContext, RoleContext } from "./configs/MyContexts";
import { 
  LazyHome, 
  LazyLogin, 
  LazyRegister, 
  LazyReset, 
  LazyUserInfo,
  LazyTrafficMapPage,
  LazyTrafficAdminPage
} from './lazyComponents';


const ProtectedRoute = ({ children }) => {
  const isAuthenticated = sessionStorage.getItem('isLoggedIn') === 'true';

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

// Route bảo vệ cho admin
const AdminRoute = ({ children }) => {
  const isAuthenticated = sessionStorage.getItem('isLoggedIn') === 'true';
  const userStr = sessionStorage.getItem('user');
  const user = userStr ? JSON.parse(userStr) : null;

  // Kiểm tra quyền admin theo cả hai cách
  const isAdmin = user &&
    ((user.roles && Array.isArray(user.roles) && user.roles.includes('ADMIN')) ||
      (user.role === 'ADMIN'));

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!isAdmin) {
    return <Navigate to="/" replace />;
  }

  return children;
};

const userReducer = (currentState, action) => {
  switch (action.type) {
    case "login":
      return action.payload;
    case "logout":
      sessionStorage.removeItem('user');
      sessionStorage.removeItem('isLoggedIn');
      cookie.remove("token", { path: '/' });
      cookie.remove("user", { path: '/' });
      return null;
    default:
      return currentState;
  }
};

const AppContent = () => {
  const initialUser = cookie.load("user") || JSON.parse(sessionStorage.getItem("user") || "null");
  const [user, dispatch] = useReducer(userReducer, initialUser);
  const [userRole, setUserRole] = useState(null);
  const location = useLocation();

  // Kiểm tra vai trò người dùng
  useEffect(() => {
    if (user && user.roles) {
      if (user.roles.includes('ADMIN')) {
        setUserRole('ADMIN');
      } else {
        setUserRole('USER');
      }
    } else {
      setUserRole(null);
    }
  }, [user]);

  // Kiểm tra xem đang ở trang đăng nhập hay đăng ký
  const isAuthPage = location.pathname === '/login' || location.pathname === '/register';

  return (
    <>
      {!isAuthPage && <Header />}
      <UserContext.Provider value={user}>
        <UserDispatchContext.Provider value={dispatch}>
          <RoleContext.Provider value={userRole}>
            <Routes>
              <Route path="/" element={
                <ProtectedRoute>
                  <LazyHome />
                </ProtectedRoute>
              } />
              <Route path="/register" element={<LazyRegister />} />
              <Route path="/login" element={<LazyLogin />} />
              <Route path="/reset-password" element={<LazyReset />} />
              <Route path="/profile" element={
                <ProtectedRoute>
                  <LazyUserInfo />
                </ProtectedRoute>
              } />
              <Route path="/traffic" element={<LazyTrafficMapPage />} />
              <Route path="/traffic-admin" element={
                <AdminRoute>
                  <LazyTrafficAdminPage />
                </AdminRoute>
              } />

              <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
          </RoleContext.Provider>
        </UserDispatchContext.Provider>
      </UserContext.Provider>
      <ToastContainer
        position="top-right"
        autoClose={1500}
        hideProgressBar={false}
        newestOnTop
        closeOnClick
        rtl={false}
        pauseOnFocusLoss={false}
        draggable
        pauseOnHover={false}
        limit={5}
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