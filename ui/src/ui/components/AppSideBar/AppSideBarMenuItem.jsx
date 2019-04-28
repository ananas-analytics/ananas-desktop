import React, { Component } from 'react'
import PropTypes from 'prop-types'
import styled from 'styled-components'

import { Box } from 'grommet/components/Box'

const MenuItem = styled(Box)`
  cursor: pointer; 

  :hover {
    background: ${props => props.theme.global.colors.brandDark } 
  }
`

function handleClick(active, onClick) {
  if (!active) {
    onClick()
  }
}

const AppSideBarMenuItem = ({ active, expand, children,
  onClick }) => {
  return (
    <MenuItem
      align={ expand ? 'start' : 'center' }
      background={ active ? 'brandDark' : 'brand'}
      pad={expand ? 'small' : { vertical: '14px', horizontal: 'small' }} 
      onClick={() => handleClick(active, onClick)}
    >
      {children}
    </MenuItem>
  )
}

AppSideBarMenuItem.propTypes = {
  active: PropTypes.bool,
  expand: PropTypes.bool,

  onClick: PropTypes.func,
}

AppSideBarMenuItem.defaultProps = {
  active: false,
  expand: false,

  onClick: () => {},
}

export default AppSideBarMenuItem
