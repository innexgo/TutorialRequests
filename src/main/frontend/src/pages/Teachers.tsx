import React from 'react';
import DashboardLayout from '../components/DashboardLayout';
import {Table, Form, Button} from 'react-bootstrap'

function Dashboard(props: AuthenticatedComponentProps) {
    return (
      <DashboardLayout {...props} >
        <Table responsive>
          <thead>
            <tr>
              <th>Monday</th>
              <th>Tuesday</th>
              <th>Wednesday</th>
              <th>Thursday</th>
              <th>Friday</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>Accepted:</td>
              <td>Accepted:</td>
              <td>Accepted:</td>
              <td>Accepted:</td>
              <td>Accepted:</td>
            </tr>
            <tr><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></tr>
            <tr><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></tr>
            <tr><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></tr>
            <tr><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></tr>
            <tr><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></tr>
          </tbody>
        </Table>
        <Button variant="success">Accept</Button>
        <Button variant="danger">Reject</Button>
      </DashboardLayout >
    )
  }

  export default Dashboard;
