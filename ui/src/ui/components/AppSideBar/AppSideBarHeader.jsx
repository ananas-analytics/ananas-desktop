import React, { Component } from 'react'
import PropTypes from 'prop-types'

import { Box } from 'grommet/components/Box'
import { Image } from 'grommet/components/Image'
import { Text } from 'grommet/components/Text'

const AppSideBarHeader = ({ user, expand }) => {
  return (
    <Box background='brand'
      direction='row' height='48px' pad='small'>
      {
      expand ? (
      <Box alignSelf='center' direction='row' justify='start'>
        <Box>
          <Image src='images/logo-white.png' fit='contain' style={{height: '26px'}}/>
        </Box>
        <Box alignSelf='center' pad={{left: 'xsmall'}}>
          <Text>{user.name}</Text>
        </Box>
      </Box>
      ) : (
      <Box alignSelf='center' >
        <Image src='images/logo-white.png' fit='contain' style={{height: '26px'}}/>
      </Box>
      )
      }
    </Box>
  )
}

AppSideBarHeader.propTypes = {
  expand: PropTypes.bool,
}

AppSideBarHeader.defaultProps = {
  expand: true,
}

export default AppSideBarHeader
