import React from 'react';
import { OverlayTrigger, Card } from 'react-bootstrap';
import { Help } from '@material-ui/icons';

interface UtilityWrapperProps {
  title: string
  children: [React.ReactElement, React.ReactElement]
}

function UtilityWrapper(props: UtilityWrapperProps) {
  return <Card>
    <Card.Body>
      <div className="d-flex justify-content-between">
        <Card.Title >{props.title}</Card.Title>
        <OverlayTrigger
          overlay={props.children[0]}
          placement="auto"
        >
          <button type="button" className="btn btn-sm">
            <Help />
          </button>
        </OverlayTrigger>
      </div>
      {props.children[1]}
    </Card.Body>
  </Card>
}

export default UtilityWrapper;
