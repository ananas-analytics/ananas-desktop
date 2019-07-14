import React from 'react'
import styled from 'styled-components'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'

import { Logout, Upgrade } from 'grommet-icons'

const MenuItem = styled(Box)`
  &:hover {
    background: ${props=>props.theme.global.colors.brand};
  }
  background: ${props=>props.theme.global.colors.brandLight};
  color: ${props=>props.theme.global.colors['light-1']};
  cursor: pointer;
`

export default ({onLogout, onCheckUpdate}) => {
  return (<Box direction='column' elevation='small' width='200px'>
    <MenuItem direction='row' align='center'
      pad='small'
      onClick={()=>{onCheckUpdate()}}>
      <Box width='32px' ><Upgrade color='light-1' size='26px' /></Box>
      <Box flex direction='row' justify='center'>
        <Text size='small'>Check Update</Text>
      </Box>
    </MenuItem>

    {/*
    <MenuItem direction='row' align='center'
      pad='small'
      onClick={()=>{onLogout()}}>
      <Box width='32px' ><Logout color='light-1' size='26px' /></Box>
      <Box flex direction='row' justify='center'>
        <Text size='small'>Sign out</Text>
      </Box>
    </MenuItem>
    */}
  </Box>)
}
