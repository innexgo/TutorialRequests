import React from 'react';
import { Link } from 'react-router-dom';
import { SvgIconComponent, ExitToApp, Menu } from '@material-ui/icons';

// Bootstrap CSS & Js
import '../style/dashboard.scss';
import 'bootstrap/dist/js/bootstrap.js'
import 'popper.js/dist/popper.js'

const iconStyle = {
  width: "2rem",
  height: "2rem",
};

const DashboardLayoutContext = React.createContext<boolean>(true)

interface SidebarEntryProps {
  label: string,
  icon: SvgIconComponent,
  href: string,
}

const SidebarEntry: React.FunctionComponent<SidebarEntryProps> = props => {
  const style = {
    color: "#fff"
  }
  const Icon = props.icon;
  if (React.useContext(DashboardLayoutContext)) {
    // collapsed
    return <Link style={style} className="nav-item nav-link" to={props.href}>
      <Icon style={iconStyle} />
    </Link>
  } else {
    // not collapsed
    return <Link style={style} className="nav-item nav-link" to={props.href}>
      <Icon style={iconStyle} /> {props.label}
    </Link>
  }
}

const Body: React.StatelessComponent = props => <> {props.children} </>

interface DashboardLayoutComposition {
  SidebarEntry: React.FunctionComponent<SidebarEntryProps>
  Body: React.StatelessComponent
}

interface DashboardLayoutProps {
  name: string
  logoutCallback: () => void
}

const DashboardLayout: React.FunctionComponent<React.PropsWithChildren<DashboardLayoutProps>> & DashboardLayoutComposition =
  props => {
    let [collapsed, setCollapsed] = React.useState<boolean>(true);

    const widthrem = collapsed ? 4 : 15;

    const sidebarStyle = {
      height: "100%",
      width: `${widthrem}rem`,
      position: "fixed" as const,
      top: 0,
      left: 0,
      overflowX: "hidden" as const,
      overflowY: "hidden" as const,
      margin: "0%"
    };

    const sidebarBottom = {
      position: 'absolute' as const,
      bottom: 0,
      right: 0,
      left: 0,
    };

    let sidebarChildren: React.ReactElement[] = [];
    let nonSidebarChildren: React.ReactNode[] = [];

    React.Children.forEach(props.children, child => {
      if (React.isValidElement(child)) {
        if (child.type === SidebarEntry) {
          sidebarChildren.push(child);
        } else if (child.type === Body) {
          nonSidebarChildren.push(child);
        }
      }
    });

    return (
      <DashboardLayoutContext.Provider value={collapsed}>
        <div>
          <nav className="bg-dark text-light" style={sidebarStyle}>
            <div>
              <button type="button" className="btn btn-lg text-light float-right" onClick={_ => setCollapsed(!collapsed)}>
                <Menu style={iconStyle} />
              </button>
            </div>
            {
              collapsed
                ? ""
                : <div className="nav-item nav-link mx-auto my-3">
                  <h6>{props.name}</h6>
                </div>
            }
            {sidebarChildren}
            <div style={sidebarBottom}>
              <button
                style={{ color: "#fff" }}
                type="button"
                className="btn nav-item nav-link"
                onClick={() => props.logoutCallback()}
              >
                <ExitToApp style={iconStyle} /> {collapsed ? "" : "Log Out"}
              </button>
            </div>
          </nav>
          <div style={{ marginLeft: `${widthrem}rem` }}>
            {nonSidebarChildren}
          </div>
        </div>
      </DashboardLayoutContext.Provider>
    )
  }

DashboardLayout.SidebarEntry = SidebarEntry;
DashboardLayout.Body = Body;

export default DashboardLayout;
