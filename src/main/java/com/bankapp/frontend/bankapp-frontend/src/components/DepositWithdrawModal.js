import React from 'react';
import { Modal, Form, Button } from 'react-bootstrap';

import axios from 'axios';

function DepositWithdrawModal(props){
    const [amount, setAmount] = React.useState(0);

    let transactionType = props.transactionType;
    let show = props.show;
    let handleClose = props.handleClose;

    let account = props.account;
    let accountNumber = account["accountNumber"];
    let setNewBalance = props.setNewBalance;

    const handleFormChange = (event) => {
        let amount = event.target.value;

        setAmount(amount);
    }

    const handleSubmit = async () => {
        let res = await axios.post(process.env.REACT_APP_API_URL + "/" + transactionType + "/" + amount, 
            {"number": accountNumber}, {withCredentials: true});

        if(res.status === 200){
            console.log("Transaction completed!");
        } else{
            console.log("There was a problem with the transaction!");
        }

        setNewBalance(res.data);
        handleClose();
    }

    return(
        <Modal show={show} onHide={() => handleClose()}>
            <Modal.Dialog>
                <Modal.Header closeButton>
                    <Modal.Title>{transactionType}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form>
                        <Form.Group controlId="Amount">
                            <Form.Label>Amount:</Form.Label>
                            <Form.Control required onChange={handleFormChange} type="number" 
                                placeholder="Enter amount" />
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

export default DepositWithdrawModal;