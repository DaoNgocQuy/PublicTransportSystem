import React, { lazy, Suspense } from 'react';
import Spinner from 'react-bootstrap/Spinner';

const Loading = () => (
  <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '70vh' }}>
    <Spinner animation="border" variant="dark" />
    <span className="ms-2">Đang tải...</span>
  </div>
);

// HOC (Higher Order Component) để thêm lazy loading cho bất kỳ component nào
export const withLazy = (importFunc) => {
  const LazyComponent = lazy(importFunc);
  
  return (props) => (
    <Suspense fallback={<Loading />}>
      <LazyComponent {...props} />
    </Suspense>
  );
};

//Chính
export const LazyHome = withLazy(() => import('./components/Home'));
export const LazyLogin = withLazy(() => import('./components/User/Login'));
export const LazyRegister = withLazy(() => import('./components/User/Register'));
export const LazyReset = withLazy(() => import('./components/User/Reset'));
export const LazyUserInfo = withLazy(() => import('./components/User/userInfo'));
//Pages
export const LazyTrafficMapPage = withLazy(() => import('./pages/TrafficMapPage'));
export const LazyTrafficAdminPage = withLazy(() => import('./pages/TrafficAdminPage'));
