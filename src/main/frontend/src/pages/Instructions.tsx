import React from 'react';
import SimpleLayout from '../components/SimpleLayout';

import sample1 from '../img/sample1.mov';
import sample2 from '../img/sample2.mov';
import sample3 from '../img/sample3.mov';
import sample4 from '../img/sample4.mov';
import sample5 from '../img/sample5.mov';

function Instructions() {

  return (
    <SimpleLayout>
      <div className="px-3 py-3">
        <div>
          <h6>Quick links</h6>
          <ul>
            <li><a href="#one" style={{ color: 'black' }}>Create appointment</a></li>
            <li><a href="#two" style={{ color: 'black' }}>Adjust duration</a></li>
            <li><a href="#three" style={{ color: 'black' }}>Day/week view</a></li>
            <li><a href="#four" style={{ color: 'black' }}>Past/future appointments</a></li>
            <li><a href="#five" style={{ color: 'black' }}>Mark attendance</a></li>
          </ul>
        </div>
        <div>
          <h3 style={{ marginTop: '30px', marginBottom: '20px' }}>
            Innexgo Hours
          </h3>
          <p style={{ marginBottom: '20px' }}>
            Innexgo Hours is a service that helps teachers and administrators give and track
            office hours in a simple application. This guide walks users through the features that
            Hours provides and how to use them. If you're new to using Hours, reading through this
            guide will teach you the basics right away.
          </p>
          <hr id="one" style={{ marginTop: '20px', marginBottom: '20px' }} />
          <h5 style={{ marginBottom: '20px' }}>Create office hour appointment</h5>
          <video width='80%' height='60%' controls style={{ marginBottom: '20px' }}>
            <source src={sample1} type="video/mp4" />
            Your browser does not support the video tag.
          </video>
          <br />
          <p>
            Click on the calendar to create an office hour appointment. A popup will appear
            that allows you to type in a student name and extra details. If not dragged, appointments will
            automatically be set to the 30minute block of time clicked on the calendar.
          </p>

          <hr id="two" style={{ marginTop: '20px', marginBottom: '20px' }} />
          <h5 style={{ marginBottom: '20px' }}>Change duration of appointment</h5>
          <video width='80%' height='60%' controls style={{ marginBottom: '20px' }}>
            <source src={sample2} type="video/mp4" />
                        Your browser does not support the video tag.
          </video>
          <br />
          <p>
            Click on the start time in the calendar, then continue holding down and drag up and down
            to adjust the duration of the office hour appointment. (A popup to choose the other details
            will appear once you stop holding down).
          </p>

          <hr id="three" style={{ marginTop: '20px', marginBottom: '20px' }} />
          <h5 style={{ marginBottom: '20px' }}>Calendar day/week view</h5>
          <video width='80%' height='60%' controls style={{ marginBottom: '20px' }}>
            <source src={sample3} type="video/mp4" />
                        Your browser does not support the video tag.
          </video>
          <br />
          <p>
            Use the day/week buttons to change the calendar view.
          </p>

          <hr id="four" style={{ marginTop: '20px', marginBottom: '20px' }} />
          <h5 style={{ marginBottom: '20px' }}>See past/future appointments</h5>
          <video width='80%' height='60%' controls style={{ marginBottom: '20px' }}>
            <source src={sample4} type="video/mp4" />
            Your browser does not support the video tag.
          </video>
          <br />
          <p>
            Click the back/front arrows to see past/future appointments. To go back to
            the current week or day, click the 'today' button. (Note: no appointments can
            be added to days that have passed.)
          </p>

          <hr id="five" style={{ marginTop: '20px', marginBottom: '20px' }} />
          <h5 style={{ marginBottom: '20px' }}>Mark attendance</h5>
          <video width='80%' height='60%' controls style={{ marginBottom: '20px' }}>
            <source src={sample5} type="video/mp4" />
            Your browser does not support the video tag.
          </video>
          <br />
          <p>
            Use the present, tardy, or late buttons on a past office hour appointment
            to keep track of attendance. (Note: attendance cannot be taken for future
            appointments.) All attendance that is tracked can be obtained in a report.
          </p>

        </div>
      </div>
    </SimpleLayout>
  );
}

export default Instructions;
