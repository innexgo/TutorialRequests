import * as React from "react";
import {Table} from 'react-bootstrap';

function Students(props: AuthenticatedComponentProps) {
  return (
    <Table striped bordered hover responsive>
      <thead>
        <tr>
          <th>08/10/20</th>
          <th>08/11/20</th>
          <th>08/12/20</th>
          <th>08/13/20</th>
          <th>08/14/20</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>1</td>
      <td>Mark</td>
      <td>Otto</td>
      <td>@mdo</td>
    </tr>
    <tr>
      <td>2</td>
      <td>Jacob</td>
      <td>Thornton</td>
      <td>@fat</td>
    </tr>
    <tr>
      <td>3</td>
      <td colSpan="2">Larry the Bird</td>
      <td>@twitter</td>
    </tr>
  </tbody>
</Table>
);

}

export default Students;
