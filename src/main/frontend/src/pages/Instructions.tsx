import React from 'react';
import ExternalLayout from '../components/ExternalLayout';
import { ArrowForward } from '@material-ui/icons';
import placeholder from '../img/placeholder.png';

interface Props{};
interface State{};


class Instructions extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
    }

    render() {

        const content = {
            float: 'left',
            width: '80%',
            paddingLeft: '4%',
            paddingRight: '4%',
        } as React.CSSProperties;

        const sidebar = {
            height: '100vh',
            width: '20%',
            position: 'fixed',
            zIndex: '1',
            top: '0',
            right: '0',
            backgroundColor: '#DCDCDC',
            overflowX: 'hidden',
            paddingTop: '20px',
            paddingLeft: '20px',
        } as unknown as React.CSSProperties;

        return (
            <>
                <div style={sidebar}>
                    <h6>Quick links</h6>

                    <ul>
                        <li>
                            <a href="#one" style={{color: 'black'}}>
                                Pain</a>
                        </li>
                        <li>
                            <a href="#two" style={{color: 'black'}}>
                                Pain 2</a>
                        </li>
                    </ul>
                </div>
                <div style={content}>
                    <h3 style={{marginTop: '30px', marginBottom: '20px'}}>
                        What can I do with Innexgo Hours?</h3>
                    <p style={{marginBottom: '20px'}}>
                        This is an overview of Innexgo Hours and its features. If you're new to the
                        service, reading through will give you a pretty good idea of what you can do
                        with Innexgo Hours. If you have a specific question, the sidebar should be able
                        to guide you to the right thing! This guide will likely answer your questions,
                        but if your question is not listed here feel free to contact us (Contact method).
                    </p>
                    <hr style={{marginTop: '20px', marginBottom: '20px'}}/>

                    <h5 id="one">Pain</h5>
                    <img src={placeholder} style={{height: '30%', width: '30%',}}/> <br/>
                    <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. In ornare quam viverra orci sagittis eu volutpat. Varius duis at consectetur lorem donec massa sapien faucibus et. Pellentesque pulvinar pellentesque habitant morbi tristique senectus et netus et. Lectus proin nibh nisl condimentum.</p>
                    <hr style={{marginTop: '20px', marginBottom: '20px'}}/>

                    <h5 id="two">Pain 2</h5>
                    <img src={placeholder} style={{height: '30%', width: '30%',}}/> <br/>
                    <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. In ornare quam viverra orci sagittis eu volutpat. Varius duis at consectetur lorem donec massa sapien faucibus et. Pellentesque pulvinar pellentesque habitant morbi tristique senectus et netus et. Lectus proin nibh nisl condimentum.</p>
                    <hr style={{marginTop: '20px', marginBottom: '20px'}}/>
                </div>
            </>
        );
    }
}

export default Instructions;