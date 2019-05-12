import React from 'react'

import styled from 'styled-components'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'
import { Stack } from 'grommet/components/Stack'

import { Duplicate, Edit, Trash } from 'grommet-icons'

const IconBox = styled(Box)`
  &:hover {
    cursor: pointer;
  }
`

const EngineItem = ({ 
  name, type='Flink', description, scope='runtime', doc, properties = {}, selected,
  onEdit, onDuplicate, onDelete,
}) => {
  return (<Box direction='column' fill
    border={ selected ? {color:'brandLight'} : null}
  >
    <Stack fill>
      <Box fill pad='small' >
        <Box direction='row' justify='center' margin={{top: 'small'}} >
          <Text color='brand' size='xsmall' truncate weight={900} >{name.toUpperCase()}</Text>
        </Box>
        <Box direction='row' justify='center' margin={{top: 'small'}} >
          <Text color='brand' size='xsmall' truncate weight={600} >SCOPE: {scope.toUpperCase()}</Text>
        </Box>
        <Box direction='column' flex margin={{top: 'small'}} overflow='hidden'>
          <Text size='xsmall' >{type.toUpperCase()}. {description}</Text>
        </Box>
      </Box>
      {
        (scope !== 'runtime' && selected) ? (<Box align='center' background={{opacity: 'strong', color: 'brand'}} 
          direction='row' justify='center' fill>
          <Box direction='row' gap='medium'> 
            <IconBox onClick={()=>onEdit()}>
              <Edit size='medium'/> 
            </IconBox>
            {/*
            <IconBox onClick={()=>onDuplicate()}>
              <Duplicate size='medium'/> 
            </IconBox>
            */}
            <IconBox onClick={()=>onDelete()}>
              <Trash size='medium'/>
            </IconBox>
          </Box>
        </Box>) : null
      }
      </Stack>
  </Box>)
}

export default EngineItem
