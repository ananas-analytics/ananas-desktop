import React from 'react'
import PropTypes from 'prop-types'

import { Box } from 'grommet/components/Box'

import { FormPrevious, FormNext } from 'grommet-icons'

const AppSideBarFooter = ({ expand, onToggleExpand }) => {
  return (
    <Box
      align='end'
      pad='small' 
    >
      <Box border onClick={() => onToggleExpand() }>
        { expand ? (<FormPrevious />) : (<FormNext />)} 
      </Box>
    </Box>
  )
}

AppSideBarFooter.propTypes = {
  expand: PropTypes.bool,

  onToggleExpand: PropTypes.func,
}

AppSideBarFooter.defaultTypes = {
  expand: true,

  onToggleExpand: () => {},
}

export default AppSideBarFooter
