import React from 'react';
import {Container, Navbar, Nav, NavDropdown} from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

function NavBar(){
    let navigate = useNavigate();

    const nav = (path) => {
        navigate(path);
    }

    return(
        <Navbar collapseOnSelect expand="md" bg="primary" variant="dark">
            <Container fluid>
                <Navbar.Brand href="/">
                    <img
                        src="/assets/logo.png"
                        width="30"
                        height="30"
                        className="d-inline-block align-top mx-2"
                        alt="React Bootstrap logo"
                    />
                    BankApp
                </Navbar.Brand>
                <Navbar.Toggle aria-controls="responsive-navbar-nav"/>
                <Navbar.Collapse className="justify-content-end" id="responsive-navbar-nav">
                    <Nav>
                        <NavDropdown className="mx-5" title="Accounts" id="collapsible-nav-dropdown" align="end">
                            <NavDropdown.Item onClick={() => nav("/Accounts/Checking")}>Checking</NavDropdown.Item>
                            <NavDropdown.Item onClick={() => nav("/Accounts/Savings")}>Savings</NavDropdown.Item>
                            <NavDropdown.Divider />
                            <NavDropdown.Item onClick={() => nav("/Accounts")}>All Accounts</NavDropdown.Item>
                        </NavDropdown>
                        <NavDropdown className="mx-5" title="Login" id="collapsible-nav-dropdown" align="end">
                            <NavDropdown.Item onClick={() => nav("/Login")}>Login</NavDropdown.Item>
                            <NavDropdown.Item onClick={() => nav("/CreateAccount")}>Create Account</NavDropdown.Item>
                        </NavDropdown>
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}

export default NavBar;