import React from 'react';
import { Form, Modal, Button } from 'react-bootstrap';
import axios from 'axios';


function OpenAccount(props) {
    const [type, setType] = React.useState("Checking");

    let show = props.show;
    let handleClose = props.handleClose;

    const handleSubmit = async () => {
        let res = await axios.put(process.env.REACT_APP_API_URL + "/openAccount", {'type': type}, {withCredentials: true});

        if(res.status === 200){
            console.log("Account successfully opened!");
        } else{
            console.log("There was a problem creating the account!");
        }

        console.log(res);
    }

    const handleFormChange = (event) => {
        setType(event.target.value);
    }

    return(
        <Modal show={show} onHide={() => handleClose()}>
            <Modal.Dialog>
                <Modal.Header closeButton>
                    <Modal.Title>Open Account</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form>
                        <Form.Group controlId="Type">
                            <Form.Label>Account Type:</Form.Label>
                            <Form.Check onClick={handleFormChange} value="Checking" name="group1" type="radio" label="Checking" />
                            <Form.Check onClick={handleFormChange} value="Savings" name="group1" type="radio" label="Savings" />
                        </Form.Group>
                    </Form>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="primary" onClick={handleSubmit}>Submit</Button>
                    <Button variant="secondary" onClick={() => handleClose()}>Close</Button>
                </Modal.Footer>
            </Modal.Dialog>
        </Modal>
    );
}

export default OpenAccount;