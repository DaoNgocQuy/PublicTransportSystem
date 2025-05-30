import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Navbar, Container, Nav, NavDropdown } from 'react-bootstrap';
import { UserDispatchContext } from '../../configs/MyContexts';
import './Header.css';

const Header = () => {
  const navigate = useNavigate();
  const dispatch = useContext(UserDispatchContext);

  const currentUser = JSON.parse(sessionStorage.getItem('user'));

  // Kiểm tra xem người dùng có phải là admin hay không
  const isAdmin = currentUser &&
    ((currentUser.roles && Array.isArray(currentUser.roles) && currentUser.roles.includes('ADMIN')) ||
      (currentUser.role === 'ADMIN'));

  const handleLogout = () => {
    // Kiểm tra dispatch có phải là function không
    if (typeof dispatch === 'function') {
      dispatch({ type: "logout" });
    } else {
      // Nếu dispatch không có sẵn, thực hiện đăng xuất thủ công
      sessionStorage.removeItem('user');
      sessionStorage.removeItem('isLoggedIn');
      // Các hành động đăng xuất khác nếu cần
    }

    navigate('/login');
  };

  return (
    <Navbar bg="dark" variant="dark" expand="lg">
      <Container>
        <Navbar.Brand as={Link} to="/">Public Transport System</Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/">Trang chủ</Nav.Link>
            <Nav.Link as={Link} to="/map">Bản đồ</Nav.Link>
            <Nav.Link as={Link} to="/traffic">Tình trạng giao thông</Nav.Link>

            {/* Chỉ hiển thị menu quản lý giao thông nếu là admin */}
            {isAdmin && (
              <Nav.Link as={Link} to="/traffic-admin">Quản lý giao thông</Nav.Link>
            )}
          </Nav>

          {currentUser ? (
            <Nav>
              <NavDropdown
                title={
                  <span>
                    {currentUser.avatarUrl ? (
                      <img
                        src={currentUser.avatarUrl}
                        alt="Profile"
                        style={{ width: '30px', height: '30px', borderRadius: '50%', marginRight: '8px' }}
                      />
                    ) : (
                      <span className="me-2">👤</span>
                    )}
                    {currentUser.fullName || currentUser.username}
                  </span>
                }
                id="basic-nav-dropdown"
              >
                <NavDropdown.Item as={Link} to="/profile">
                  <i className="bi bi-person-circle me-2"></i>
                  Thông tin cá nhân
                </NavDropdown.Item>
                <NavDropdown.Divider />
                <NavDropdown.Item onClick={handleLogout}>
                  <i className="bi bi-box-arrow-right me-2"></i>
                  Đăng xuất
                </NavDropdown.Item>
              </NavDropdown>
            </Nav>
          ) : (
            <Nav>
              <Nav.Link as={Link} to="/login">
                <i className="bi bi-box-arrow-in-right me-1"></i> Đăng nhập
              </Nav.Link>
              <Nav.Link as={Link} to="/register">
                <i className="bi bi-person-plus me-1"></i> Đăng ký
              </Nav.Link>
            </Nav>
          )}
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Header;