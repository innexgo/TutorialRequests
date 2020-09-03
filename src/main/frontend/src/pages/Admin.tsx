import React from 'react'
import { Form} from 'react-bootstrap';
import DashboardLayout from '../components/DashboardLayout';


function Admin(props: AuthenticatedComponentProps) {
  const formStyle = {
    padding: '0% 3%',
    textAlign: 'center' as const,
  };

  const headerStyle = {
    marginTop: '2%',
    textAlign: 'center' as const,
  };

  return (
    <DashboardLayout name={props.apiKey.user.name} logoutCallback={() => props.setApiKey(null)} >
      <h1 style={headerStyle}>Upload data </h1>
      <Form style={formStyle}>

      <Form.File 
    id="csv-data"
    label="Please upload a csv file with your data."
    custom
  />
      </Form>
    </DashboardLayout>
  );
}


export default Admin;
