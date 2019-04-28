import React from 'react'
import PropTypes from 'prop-types'

import { Box } from 'grommet/components/Box'

import { Sidebar } from 'grommet-icons'

import ViewPath from './ViewPath'

const AppToolBar = ({
  user, path, contextSideBarExpanded,
  onToggleContextSideBar, onChangePath }) => {
  return (
    <Box direction='row'
      background='linear-gradient(102.77deg, #EDEDED, #F8F8F8)'
      basis='xxsmall'
      pad='small' >
      <Box alignSelf='center' flex >
        <ViewPath user={user} path={path} onChangePath={onChangePath}/>
      </Box>
      <Box alignSefl='center' onClick={() => onToggleContextSideBar()}>
        <Sidebar color={ contextSideBarExpanded ? 'brand' : 'light-6' }
          style={{cursor: 'pointer'}}
        />
      </Box>
    </Box>
  )
}

AppToolBar.propTypes = {
  user: PropTypes.object,
  path: PropTypes.object,
  contextSideBarExpanded: PropTypes.bool,

  onToggleContextSideBar: PropTypes.func,
  onChangePath: PropTypes.func,
}

AppToolBar.defaultProps = {
  user: {},
  path: { project: {}, app: {} },
  contextSideBarExpanded: false,

  onToggleContextSideBar: () => {},
  onChangePath: path=>{},
}

export default AppToolBar
