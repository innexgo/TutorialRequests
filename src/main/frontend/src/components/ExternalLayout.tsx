import React from 'react';

import { fetchApi } from '../utils/utils'

import { Async } from 'react-async';

// Bootstrap CSS & JS
import '../style/external.scss'
import 'bootstrap/dist/js/bootstrap.js'
import 'popper.js/dist/popper.js'

import Footer from './Footer';
import ExternalHeader from './ExternalHeader';

interface ExternalLayoutProps {
    fixed: boolean;
    transparentTop: boolean;
}

class ExternalLayout extends React.Component<ExternalLayoutProps> {
  render() {

    return (
      <>
        <Async promise={fetchApi('misc/info/school/')}>
          <Async.Pending>
            <ExternalHeader title="Innexgo Hours" fixed={this.props.fixed} transparentTop={this.props.transparentTop} />
          </Async.Pending>
          <Async.Rejected>
            <ExternalHeader title="Innexgo Hours" fixed={this.props.fixed} transparentTop={this.props.transparentTop} />
          </Async.Rejected>
          <Async.Fulfilled<SchoolInfo>>
            {schoolInfo => <ExternalHeader title={`Innexgo Hours: ${schoolInfo.name}`} fixed={this.props.fixed} transparentTop={this.props.transparentTop} />}
          </Async.Fulfilled>
        </Async>
        {this.props.children}
        <Footer />
      </>
    )
  }
}

export default ExternalLayout;
