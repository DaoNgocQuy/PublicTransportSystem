import React, { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Navbar, Container, Nav, NavDropdown } from 'react-bootstrap';
import { UserDispatchContext } from '../../configs/MyContexts';

const Header = () => {
  const navigate = useNavigate();
  const dispatch = useContext(UserDispatchContext);

  const currentUser = JSON.parse(sessionStorage.getItem('user'));

  const handleLogout = () => {
    dispatch({ type: "logout" });
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
          </Nav>

          {currentUser && (
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
                <NavDropdown.Item as={Link} to={`/profile/${currentUser.id}`}>
                  Thông tin cá nhân
                </NavDropdown.Item>
                <NavDropdown.Divider />
                <NavDropdown.Item onClick={handleLogout}>
                  Đăng xuất
                </NavDropdown.Item>
              </NavDropdown>
            </Nav>
          )}
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Header;