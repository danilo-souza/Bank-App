import React from 'react';
import {Container, Col, Row} from 'react-bootstrap';
import {NavLink} from 'react-router-dom';

function NavBar(){
    return(
        <Container fluid>
            <Row>
                <Col>
                    <h1>
                        <NavLink style={{
                            color: "black",
                            fontweight: "bold",
                            textDecoration: "none"
                        }} to="/">BankApp</NavLink>
                    </h1>
                </Col>
            </Row>
        </Container>
    );
}

export default NavBar;