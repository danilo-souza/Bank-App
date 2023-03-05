import React from "react";
import { Container, Row, Button, Form } from "react-bootstrap";
import axios from 'axios';
import { useNavigate, Link } from "react-router-dom";

function CreateAccount() {
    const [customer, setCustomer] = React.useState({
        username: "",
        password: "",
        confirmPassword: ""
    });

    const [error, setError] = React.useState("");

    let navigate = useNavigate();

    const handleFormChange = (event) =>{
        let name = event.target.name;
        let value = event.target.value;

        if(customer.hasOwnProperty(name)){
            customer[name] = value;
        }
    }

    const handleFormSubmit = async (event) => {
        if(event) event.preventDefault();
        if(error != "") return;

        let res = await axios.post(process.env.REACT_APP_API_URL + "/createAccount", customer, {withCredentials: true});

        let form = event.target;

        if(form.checkValidity() === false){
            event.stopPropagation();
            return;
        }

        console.log(res);

        navigate("/Login");
    }

    const handleValidation = (event) => {
        let confirm = customer["confirmPassword"];
        let password = customer["password"];

        if(confirm != password || confirm == null){
            setError("Passwords don't match");
        } else{
            setError("");
        }
    }

    return(
        <Container fluid>
            <Row>
                <h1>Create Account</h1>
            </Row>
            <br />
            <Row md="auto" className="justify-content-center mb-2">
                <Form onSubmit={handleFormSubmit}>
                    <Form.Group className="mb-3" controlId="username">
                        <Form.Label>Username</Form.Label>
                        <Form.Control required onChange={handleFormChange} 
                        type="username" name="username" placeholder="Enter your username" />
                    </Form.Group>
                    <Form.Group className="mb-3" controlId="password">
                        <Form.Label>Password</Form.Label>
                        <Form.Control required onChange={event => {handleFormChange(event); handleValidation(event);}}
                        type="password" name="password" placeholder="Enter your password" />
                    </Form.Group>
                    <Form.Group controlId="confirmPassword">
                        <Form.Label>Confirm Password</Form.Label>
                        <Form.Control required onChange={event => {handleFormChange(event); handleValidation(event);}}
                        type="password" name="confirmPassword" placeholder="Confirm password" />
                    </Form.Group>
                    <div className="text-danger mb-3">{error}</div>
                    <Button md="auto" type="submit">
                        Submit
                    </Button>
                </Form>
            </Row>
        </Container>
    );
}

export default CreateAccount;