import React from 'react'
import PropTypes from 'prop-types'

import { Box } from 'grommet/components/Box'

import AppSideBarHeader from './AppSideBarHeader'
import AppSideBarMenu from './AppSideBarMenu'
import AppSideBarFooter from './AppSideBarFooter'

const AppSideBar = ({ user, activeMenu, expand, onClickMenu, onToggleExpand }) => {
  return (
    <Box
      background='brand'
      width={ expand ? 'small' : '50px' }
      direction='column' >

      <AppSideBarHeader user={user} expand={expand} />

      <AppSideBarMenu
        expand={expand}
        activeIndex={activeMenu}
        onClickMenu={onClickMenu} />

      <AppSideBarFooter expand={expand}
        onToggleExpand={onToggleExpand} />
    </Box>
  )
}

AppSideBar.propTypes = {
  activeMenu: PropTypes.number,
  expand: PropTypes.bool,

  onClickMenu: PropTypes.func,
  onToggleExpand: PropTypes.func,
}

AppSideBar.defaultProps = {
  activeMenu: 0,
  expand: true,

  onClickMenu: () => {},
  onToggleExpand: () => {},
}

export default AppSideBar
