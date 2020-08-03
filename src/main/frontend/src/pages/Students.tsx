import React from 'react';
import {Table, Dropdown, DropdownButton, Form} from 'react-bootstrap'

export default function Students(){

  return (
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
        <Form>
        <tr>
          <td>
            <DropdownButton title="Choose Teacher">
              <Dropdown.Item as="button">Ms. Ng</Dropdown.Item>
              <Dropdown.Item as="button">Ms. Cornejo</Dropdown.Item>
              <Dropdown.Item as="button">Ms.Dimas</Dropdown.Item>
            </DropdownButton>
          </td>
          <td>
            <DropdownButton title="Choose Teacher">
              <Dropdown.Item as="button">Ms. Ng</Dropdown.Item>
              <Dropdown.Item as="button">Ms. Cornejo</Dropdown.Item>
              <Dropdown.Item as="button">Ms.Dimas</Dropdown.Item>
            </DropdownButton>
          </td>
          <td>
            <DropdownButton title="Choose Teacher">
              <Dropdown.Item as="button">Ms. Ng</Dropdown.Item>
              <Dropdown.Item as="button">Ms. Cornejo</Dropdown.Item>
              <Dropdown.Item as="button">Ms.Dimas</Dropdown.Item>
            </DropdownButton>
          </td>
          <td>
            <DropdownButton title="Choose Teacher">
              <Dropdown.Item as="button">Ms. Ng</Dropdown.Item>
              <Dropdown.Item as="button">Ms. Cornejo</Dropdown.Item>
              <Dropdown.Item as="button">Ms.Dimas</Dropdown.Item>
            </DropdownButton>
          </td>
          <td>
            <DropdownButton title="Choose Teacher">
              <Dropdown.Item as="button">Ms. Ng</Dropdown.Item>
              <Dropdown.Item as="button">Ms. Cornejo</Dropdown.Item>
              <Dropdown.Item as="button">Ms.Dimas</Dropdown.Item>
            </DropdownButton>
          </td>
        </tr>
        <tr>
          <td></td>
          <td>
            <Form.Group controlId="checkbox">
              <Form.Check type="checkbox" label="Mr. Taylor" />
            </Form.Group>
          </td>
          <td>
            <Form.Group controlId="checkbox">
              <Form.Check type="checkbox" label="Jack Chen" />
            </Form.Group>
          </td>
          <td></td>
          <td></td>
        </tr>
        <tr>
          <td></td>
          <td>
            <Form.Group controlId="checkbox">
              <Form.Check type="checkbox" label="Ms. Cole" />
            </Form.Group>
          </td>
          <td></td>
          <td></td>
          <td></td>
        </tr>
      </Form>
      </tbody>
    </Table>
  );
}
