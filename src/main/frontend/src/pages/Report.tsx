//copied from tg's reports layout

import React from 'react'
import DashboardLayout from '../components/DashboardLayout';
import {Container, Col, Row} from 'react-bootstrap';

function Report(props: AuthenticatedComponentProps) {

    return(
        <DashboardLayout name={props.apiKey.user.name} logoutCallback={() => props.setApiKey(null)}>
            <Container fluid>
                <Row>
                    <Col sm={8} style={{marginTop: "-1.5rem"}}> /**/ </Col>
                    <Col sm={4}> /**/ </Col>
                </Row>
            </Container>
        </DashboardLayout>
    );
};
