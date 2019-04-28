import React from 'react'

import { Anchor } from 'grommet/components/Anchor'
import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'

import { Home, FormNext } from 'grommet-icons'

const ViewPath = ({
  user,
  path,
  onChangePath,
}) => {
  return (
    <Box align='center' direction='row' >
			<Box align='center' direction='row' height='18px' onClick={()=>onChangePath({})} 
				style={{cursor: 'pointer'}}> 
				<Home size='18px' color='brand'/>
			</Box>
      {
        path.project && path.project.name ? (
          <Box align='center' direction='row'>
            &nbsp;<FormNext size='small'/>&nbsp;
            <Anchor onClick={()=>onChangePath({project: path.project, app: { id: 0, name: 'Analysis Board' }})}><Text size='small'>{path.project.name}</Text></Anchor>
          </Box>) : null
      }
      {
        path.app && path.app.name ? (
          <Box align='center' direction='row'>
            &nbsp;<FormNext size='small'/>&nbsp;
            <Anchor onClick={()=>onChangePath({project: path.project, app: { id: path.app.id, name: path.app.name }})}><Text size='small'>{path.app.name}</Text></Anchor>
          </Box>) : null
      }
      {
        path.app && path.app.view ? (
          <Box align='center' direction='row'>
            &nbsp;<FormNext size='small'/>&nbsp;
            <Text size='small' color='brand' style={{fontWeight: '600'}}>Configure {path.app.view.name}</Text>
          </Box>) : null
      }
    </Box>
  )
}

export default ViewPath
