import React from "react";
import { Container, Row } from "react-bootstrap";
import Button from "react-bootstrap/Button";
import { useNavigate } from "react-router-dom";

function Home() {
    let navigate = useNavigate();

    const nav = () => {
        navigate("/Login");
    }

    return(
        <Container fluid>
            <Row>
                <h1>Welcome to BankApp!</h1>
            </Row>
            <br />
            <Row md="auto" className= "justify-content-center">
                <Button size="lg" variant="primary" onClick={() => nav()}>Sign in/Create Account</Button>
            </Row>
        </Container>
    );
}

export default Home;