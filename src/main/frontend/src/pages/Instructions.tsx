import React from 'react';
import ExternalLayout from '../components/ExternalLayout';
import { ArrowForward } from '@material-ui/icons';

import sample2 from '../img/sample2.mov';
import sample3 from '../img/sample3.mov';

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
                        <li><a href="#one" style={{color: 'black'}}>Create appointment</a></li>
                        <li><a href="#two" style={{color: 'black'}}>Adjust duration</a></li>
                        <li><a href="#three" style={{color: 'black'}}>Day/week view</a></li>
                    </ul>
                </div>
                <div style={content}>
                    <h3 style={{marginTop: '30px', marginBottom: '20px'}}>
                        Innexgo Hours</h3>
                    <p style={{marginBottom: '20px'}}>
                        Innexgo Hours is a service that helps teachers and administrators give and track
                        office hours in a simple application. This guide walks users through the features that
                        Hours provides and how to use them. If you're new to using Hours, reading through this
                        guide will teach you the basics right away.
                    </p>
                    <hr id="one" style={{marginTop: '20px', marginBottom: '20px'}}/>
                    <h5 style={{marginBottom: '20px'}}>Create office hour appointment</h5>
                    <video width='80%' height='60%' autoPlay loop style={{marginBottom: '20px'}}>
                        <source src={sample2} type="video/mp4" />
                        Your browser does not support the video tag.
                    </video><br/>
                    <p>Click on the calendar to create an office hour appointment. Appointments will automatically
                        be set to the 30minute block of time clicked on the calendar.
                    </p>
                    
                    <hr id="two" style={{marginTop: '20px', marginBottom: '20px'}}/>
                    <h5 style={{marginBottom: '20px'}}>Change duration of appointment</h5>
                    <video width='80%' height='60%' autoPlay loop style={{marginBottom: '20px'}}>
                        <source src={sample3} type="video/mp4" />
                        Your browser does not support the video tag.
                    </video><br/>
                    <p>Click on the start time in the calendar, then continue holding down and drag up and down
                        to adjust the duration of the office hour appointment. (A popup to choose the other details
                        will appear once you stop holding down).
                    </p>
                    
                    <hr id="three" style={{marginTop: '20px', marginBottom: '20px'}}/>
                    <h5 style={{marginBottom: '20px'}}>Calendar day/week view</h5>
                    <video width='80%' height='60%' autoPlay loop style={{marginBottom: '20px'}}>
                        <source type="video/mp4" />
                        Your browser does not support the video tag.
                    </video><br/>
                    <p>Use the 'day/week' buttons to change the calendar view. Use the front &amp;
                        back arrows to see previous and future weeks. (Note: you will not be able
                        to create, edit, or mark attendance on past appointments.) 
                    </p>

                </div>
            </>
        );
    }
}

export default Instructions;