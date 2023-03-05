import React from 'react';
import {Container, Row, Table} from 'react-bootstrap';
import axios from 'axios';
import { useEffect } from 'react';

function Account(props) {
    const [logs, setLogs] = React.useState({
        list: [{
            accountNumber: 0,
            description: "",
            timestamp: 0
        }]
    })

    const [account, setAccount] = React.useState({
        accountNumber: "",
        balance: 0
    })

    let type = props.type;

    useEffect(() => {
        let aNumber = '';

        const getAccount = async () =>{
            let res = await axios.get(process.env.REACT_APP_API_URL + "/accounts", {withCredentials: true});
            
            if(res.status === 200){
                console.log("Data retrieved");
            } else{
                return;
            }

            let data = res.data;
            aNumber = data.accountNumbers[type];

            setAccount({
                accountNumber: aNumber,
                balance: data.accounts[aNumber]
                
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
                let accNumber = Object.keys(data[key])[1];
                if(accNumber === aNumber){
                    list.push({
                        timestamp: data[key]._id.date,
                        accountNumber: aNumber,
                        description: data[key][aNumber]
                    });
                }
            });

            setLogs(list);
        }

        getAccount().catch(console.error);
        getLogs().catch(console.error);
    }, [type]);

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
        <Container className="justify-content-left" fluid>
            <Row md="auto">
                <h1>{type}</h1>
            </Row>
            <Row md="auto">
                <h2>Account Number: {account.accountNumber}</h2>
            </Row>
            <Row md="auto">
                <h2>Balance: ${account.balance}</h2>
            </Row>
            <Row md="auto">
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
                            <tr key={index}>
                                <td>{index+1}</td>
                                <td >{logs[index]['accountNumber']}</td>
                                <td>{logs[index]['description']}</td>
                                <td>{logs[index]['timestamp']}</td>
                                <td>{getValue(logs[index]['description'])}</td>
                            </tr>
                        ))}
                    </tbody>
                </Table>
            </Row>
        </Container>
    );
}

export default Account;