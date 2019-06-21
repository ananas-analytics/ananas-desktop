import React from 'react'

import styled from 'styled-components'
import moment from 'moment'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'
import { Stack } from 'grommet/components/Stack'

import { Duplicate, Edit, Trash, StatusGoodSmall } from 'grommet-icons'

const IconBox = styled(Box)`
  &:hover {
    cursor: pointer;
  }
`

const TriggerItem = ({ 
  name, type='once', description, startTimestamp, enabled, selected,
  onEdit, onDuplicate, onDelete,
}) => {
  return (<Box direction='column' fill
    border={ selected ? {color:'brandLight'} : null}
  >
    <Stack fill>
      <Box fill pad='small' >
        <Box align='center' direction='row' flex={false} justify='center' margin={{top: 'small'}} >
          <Text color='brand' size='xsmall' truncate weight={900} margin={{right: 'xsmall'}} >{name.toUpperCase()} </Text>
          <StatusGoodSmall size='small' color={enabled? 'status-ok' : 'status-disabled'}/>
        </Box>
        <Box direction='column' flex={false} margin={{top: 'small'}} overflow='hidden'>
          <Text color='brand' size='xsmall' >Start: {moment(startTimestamp).format('lll')}</Text>
        </Box>
        <Box direction='column' fill margin={{top: 'small'}} overflow='hidden'>
          <Text size='xsmall' >{type.toUpperCase()}. {description}</Text>
        </Box>
      </Box>
      { selected ? 
        (<Box align='center' background={{opacity: 'strong', color: 'brand'}} 
          direction='row' justify='center' fill>
          <Box direction='row' gap='medium'> 
            <IconBox onClick={()=>onEdit()}>
              <Edit size='medium'/> 
            </IconBox>
            <IconBox onClick={()=>onDuplicate()}>
              <Duplicate size='medium'/> 
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

export default TriggerItem
