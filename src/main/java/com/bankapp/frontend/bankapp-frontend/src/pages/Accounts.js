import React, { useEffect } from 'react';
import { Card, Container, Row, Button, Table } from 'react-bootstrap';
import axios from 'axios';
import {useNavigate} from 'react-router-dom';

import OpenAccount from '../components/OpenAccountModal';
import DepositWithdrawModal from '../components/DepositWithdrawModal';
import TransferModal from '../components/TransferModal';

function Accounts(props) {
    const [accounts, setAccounts] = React.useState({
        Checking: {
            accountNumber: null,
            balance: null
        },
        Savings: {
            accountNumber: null,
            balance: null
        }
    });

    const [logs, setLogs] = React.useState({
        list: [{
            timestamp: 0,
            accountnUmber: "",
            description: ""
        }]
    });

    const [show, setShow] = React.useState({
        openAccount: false,
        deposit: false,
        withdraw: false,
        transfer: false
    });

    const [selectedAccount, setSelectedAccount] = React.useState("Checking");

    useEffect(() => {
        const getAccount = async () =>{
            let res = await axios.get(process.env.REACT_APP_API_URL + "/accounts", {withCredentials: true});
            
            if(res.status === 200){
                console.log("Data retrieved");
            } else{
                return;
            }

            let data = res.data;
            let checkingNumber = data.accountNumbers.Checking;
            let savingsNumber = data.accountNumbers.Savings;
            let balance = data.accounts;

            setAccounts({
                Checking: {
                    accountNumber: checkingNumber,
                    balance: balance[checkingNumber]
                },
                Savings: {
                    accountNumber: savingsNumber,
                    balance: balance[savingsNumber]
                }
            })
        }

        const getLogs = async () => {
            let res = await axios.get(process.env.REACT_APP_API_URL + "/logs", {withCredentials: true});

            if(res.status === 200){
                console.log("Logs retrieved");
            } else{
                return;
            }

            let data = res.data;
            let list = [];

            Object.keys(data).map(key => {
                let aNumber = Object.keys(data[key])[1];
                list.push({
                    timestamp: data[key]._id.date,
                    accountNumber: aNumber,
                    description: data[key][aNumber]
                });
            });

            setLogs(list);
        }

        getAccount().catch(console.error);
        getLogs().catch(console.error);
    }, []);

    const username = props.username;

    const handleModalOpen = (event) => {
        console.log("Opening Modal!");

        if(event.type != null){
            setSelectedAccount(event.type);
        }

        setShow(event);
    }

    const handleClose = () => {
        console.log("Closing Modal!");

        setShow({
            openAccount: false,
            deposit:false,
            withdraw:false,
            transfer:false
        });
    }

    let navigate = useNavigate();
    const handleNavigate = (route) =>{
        navigate("/Accounts/" + route);
    }

    const handleChangeBalance = (balance) => {
        accounts[selectedAccount]["balance"] = balance;
        setAccounts(accounts);
    }

    const hasAccounts = () => {
        let dynamicCards = [];

        if(accounts["Checking"]["accountNumber"] != null){
            dynamicCards.push(<Card border="primary" className="me-5" key="checking">
                    <Card.Header onClick={() => handleNavigate("Checking")} style={{cursor: "pointer"}}  as="h4">Checking</Card.Header>
                    <Card.Title className="mt-3">Account Number: {accounts["Checking"]["accountNumber"]}</Card.Title>
                    <Card.Body style={{color: "green"}}>
                        <Row style={{fontSize: "25px"}} className="justify-content-center mb-3">
                            ${accounts["Checking"]["balance"]}
                        </Row>
                    </Card.Body>
                    <Card.Footer>
                        <Row md="auto" className="justify-content-center">
                            <Button onClick={() => handleModalOpen({openAccount: false,
                                deposit: true,
                                withdraw: false,
                                transfer: false,
                                type: "Checking"})}
                                className="me-2" style={{zIndex: 1}} variant="success">Deposit</Button>

                            <Button onClick={() => handleModalOpen({openAccount: false,
                                deposit: false,
                                withdraw: true,
                                transfer: false,
                                type: "Checking"})}
                                className="me-2" variant="danger">Withdraw</Button>

                            <Button onClick={() => handleModalOpen({openAccount: false,
                                deposit: false,
                                withdraw: false,
                                transfer: true,
                                type: "Checking"})}
                                className="me-2" variant="dark">Transfer</Button>
                        </Row>
                    </Card.Footer>
                </Card>);
        }
        if(accounts["Savings"]["accountNumber"] != null){
            dynamicCards.push(<Card border="primary" className="me-5" key="savings">
                    <Card.Header onClick={() => handleNavigate("Savings")} style={{cursor: "pointer"}}  as="h4">Savings</Card.Header>
                    <Card.Title className="mt-3">Account Number: {accounts["Savings"]["accountNumber"]}</Card.Title>
                    <Card.Body style={{color: "green"}}>
                        <Row style={{fontSize: "25px"}} className="justify-content-center mb-3">
                            ${accounts["Savings"]["balance"]}
                        </Row>
                    </Card.Body>
                    <Card.Footer>
                        <Row md="auto" className="justify-content-center">
                            <Button onClick={() => handleModalOpen({openAccount: false,
                                deposit: true,
                                withdraw: false,
                                transfer: false,
                                type: "Savings"})}
                                className="me-2" style={{zIndex: 1}} variant="success">Deposit</Button>

                            <Button onClick={() => handleModalOpen({openAccount: false,
                                deposit: false,
                                withdraw: true,
                                transfer: false,
                                type: "Savings"})}
                                className="me-2" variant="danger">Withdraw</Button>

                            <Button onClick={() => handleModalOpen({openAccount: false,
                                deposit: false,
                                withdraw: false,
                                transfer: true,
                                type: "Savings"})}
                                className="me-2" variant="dark">Transfer</Button>
                        </Row>
                    </Card.Footer>
                </Card>);
        }
        if(accounts["Checking"]["accountNumber"] == null || accounts["Savings"]["accountNumber"] == null){
            dynamicCards.push(<Card border="success" key="openAccount" onClick={() => handleModalOpen({openAccount: true,
                deposit: false,
                withdraw: false,
                transfer: false
            })} style={{cursor: "pointer"}}><Card.Body>click</Card.Body></Card>);
        }

        return dynamicCards
    }

    const getValue = (input) =>{
        input = input.split(" ");

        let out = "";

        if(input[1] === "Deposited"){
            out += "+";
        }
        else{
            out += "-";
        }

        out += input[0];

        return out;
    }

    return(
        <Container fluid>
            <Row md="auto" className="justify-content-left mb-3">
                <h2>Your Accounts:</h2>
            </Row>
            <Row md="auto" className="justify-content-left ms-3 mb-3">
                {hasAccounts()}
            </Row>
            <Row md="auto" className="justify-content-left">
                <h2>Transaction History:</h2>
            </Row>
            <Row md="auto" className="justify-content-left">
                <Table responsive striped variant="dark" bordered>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Account Number</th>
                            <th>Description</th>
                            <th>Timestamp</th>
                            <th>Value</th>
                        </tr>
                    </thead>
                    <tbody>
                        {Array.from({length: logs.length}).map((_, index) => (
                            <tr>
                                <td>{index+1}</td>
                                <td>{logs[index]['accountNumber']}</td>
                                <td>{logs[index]['description']}</td>
                                <td>{logs[index]['timestamp']}</td>
                                <td>{getValue(logs[index]['description'])}</td>
                            </tr>
                        ))}
                    </tbody>
                </Table>
            </Row>

            <OpenAccount show={show["openAccount"]} handleClose={handleClose}/>
            <DepositWithdrawModal show={show["deposit"]} handleClose={handleClose} 
                account={accounts[selectedAccount]} setNewBalance={handleChangeBalance} transactionType="deposit" />
            <DepositWithdrawModal show={show["withdraw"]} handleClose={handleClose}
                account={accounts[selectedAccount]} setNewBalance={handleChangeBalance} transactionType="withdraw" />
            <TransferModal show={show["transfer"]} handleClose={handleClose}
                account={accounts[selectedAccount]} setNewBalance={handleChangeBalance} accountType={selectedAccount} />
        </Container>
    );
}

export default Accounts;