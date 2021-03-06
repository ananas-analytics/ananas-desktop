import React from 'react'

import styled from 'styled-components'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'
import { Stack } from 'grommet/components/Stack'

import { Edit, Trash } from 'grommet-icons'

const IconBox = styled(Box)`
  &:hover {
    cursor: pointer;
  }
`

const VariableItem = ({ 
  name, type='string', description, scope='project', doc, defaultValue, selected,
  onEdit, onDelete,
}) => {
  return (<Box direction='column' fill
    border={ selected ? {color:'brandLight'} : null}
  >
    <Stack fill>
      <Box fill pad='small' >
        <Box direction='row' justify='center' margin={{top: 'small'}} >
          <Text color='brand' size='xsmall' truncate weight={900} >{name}</Text>
        </Box>
        <Box direction='row' justify='center' margin={{top: 'small'}} >
          <Text color='brand' size='xsmall' truncate weight={600} >SCOPE: {scope.toUpperCase()}</Text>
        </Box>
        <Box direction='column' flex margin={{top: 'small'}} overflow='hidden'>
          <Text color='dark-2' size='xsmall' >{type.toUpperCase()}. {description}</Text>
        </Box>
      </Box>
      {
        (scope !== 'runtime' && selected) ? (<Box align='center' background={{opacity: 'strong', color: 'brand'}} 
          direction='row' justify='center' fill>
          <Box direction='row' gap='medium'> 
            <IconBox onClick={()=>onEdit()}>
              <Edit size='medium'/> 
            </IconBox>
            <IconBox onClick={()=>onDelete()}>
              <Trash size='medium'/>
            </IconBox>
          </Box>
        </Box>) : null
      }
      </Stack>
  </Box>)
}

export default VariableItem
