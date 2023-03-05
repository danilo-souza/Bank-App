import React from "react";
import { Container, Row, Button, Form } from "react-bootstrap";
import axios from 'axios';
import { useNavigate, Link } from "react-router-dom";

function Login(props) {
    const [customer, setCustomer] = React.useState({
        username: "",
        password: ""
    });

    const username = props.username;
    const setUsername = props.setUsername;

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

        let res = await axios.post(process.env.REACT_APP_API_URL + "/login", customer, {withCredentials: true});

        let resStatus = res.status;

        let form = event.target;

        if(form.checkValidity() === false){
            event.stopPropagation();
            return;
        }

        if(resStatus == 200){
            setUsername(customer["username"]);
        }

        console.log(res);

        navigate("/Accounts");
    }

    return(
        <Container fluid>
            <Row>
                <h1>Login</h1>
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
                        <Form.Control required onChange={handleFormChange}
                        type="password" name="password" placeholder="Enter your password" />
                    </Form.Group>
                    <Button md="auto" type="submit">
                        Submit
                    </Button>
                </Form>
            </Row>
            <Row md="auto" className="justify-content-center">
                <Link to="/CreateAccount">Don't have an account? Create one now!</Link>
            </Row>
        </Container>
    )
}

export default Login;