import React from 'react';
import DashboardLayout from '../components/DashboardLayout';
import {Table, Form, Button} from 'react-bootstrap'

function Teachers(props: AuthenticatedComponentProps) {
    return (
      <DashboardLayout {...props} >
        <Table striped bordered size="sm">
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
            <tr>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
            </tr>
            <tr>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
            </tr>
            <tr>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
            </tr>
            <tr>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
            </tr>
            <tr>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
              <td><Form.Group controlId="formBasicCheckbox"><Form.Check type="checkbox" label="Check me out" /></Form.Group></td>
            </tr>
          </tbody>
        </Table>
        <Button variant="success" size="sm" block style={{marginBottom: '1rem'}}>Accept</Button>
        <Button variant="danger" size="sm" block>Reject</Button>
      </DashboardLayout >
    )
  }

  export default Teachers;
