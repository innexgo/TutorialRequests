import React from 'react';
import ExternalLayout from '../components/ExternalLayout';

interface Props{};
interface State{};


class Instructions extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
    }

    render() {

        const half = {
            float: 'left',
            width: '50%',
        } as React.CSSProperties;

        return (
            <>
                <ExternalLayout fixed={true} transparentTop={true}>
                    <h3 style={{textAlign: 'center'}}>What can I do with Innexgo Hours?</h3>
                    <p style={{textAlign: 'center', margin: '7px',}}>
                        This page is a simple guide to using Innexgo Hours to track your tutorial appointments,
                        create ones with students, track attendance data, and more. You can also jump to a 
                        specific topic using the links below. If you have a question that isn't answered here, 
                        feel free to send us an email at sample@email.com!
                    </p>

                    


                </ExternalLayout>

            </>
        );
    }
}

export default Instructions;