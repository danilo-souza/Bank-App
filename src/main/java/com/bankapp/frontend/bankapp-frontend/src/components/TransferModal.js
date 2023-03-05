import React from 'react';

import axios from 'axios';

import {Modal, Form, Button} from 'react-bootstrap';

function TransferModal(props) {
    const [recepient, setRecepient] = React.useState("");
    const [recepientType, setRecepientType] = React.useState("Checking");
    const [amount, setAmount] = React.useState(0);

    let show = props.show;
    let handleClose = props.handleClose;

    let account = props.account;
    let accountNumber = account["accountNumber"];
    let setNewBalance = props.setNewBalance;
    let accountType = props.accountType;

    const handleFormChange = (event) => {
        if(event.target.name == "amount"){
            setAmount(event.target.value);
        }
        else if(event.target.name == "recepient"){
            setRecepient(event.target.value);
        } 

        console.log(event);
    }

    const handleRecepientTypeChange  = (event) => {
        setRecepientType(event.target.value);
    }

    const handleSubmit = async () => {
        let res = await axios.post(process.env.REACT_APP_API_URL + '/transfer/' + amount, {
            'accountNumber1' : accountNumber,
            'accountNumber2' : recepient,
            'recepientType' : recepientType,
            'senderType' : accountType
        }, {withCredentials: true});

        if(res.status === 200){
            console.log("Transfer successfully completed!");
        } else{
            console.log("There was a problem with the transfer!");
        }

        setNewBalance(res.data);
    }

    return(
        <Modal show={show} onHide={() => handleClose()}>
            <Modal.Dialog>
                <Modal.Header closeButton>
                    <Modal.Title>transfer</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form>
                        <Form.Group controlId="Recepient">
                            <Form.Label>Recepient:</Form.Label>
                            <Form.Control required onChange={handleFormChange} type="text"
                                name="recepient" placeholder="Enter recepient account number" />
                        </Form.Group>
                        <Form.Group controlId="RecepientType" className="mb-3 mt-3">
                            <Form.Check onClick={handleRecepientTypeChange} inline default value="Checking" name="group1" type="radio" label="Checking" />
                            <Form.Check onClick={handleRecepientTypeChange} inline value="Savings" name="group1" type="radio" label="Savings" />
                        </Form.Group>
                        <Form.Group controlId="Amount">
                            <Form.Label>Amount:</Form.Label>
                            <Form.Control required onChange={handleFormChange} type="number" 
                                name="amount" placeholder="Enter amount" />
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

export default TransferModal;