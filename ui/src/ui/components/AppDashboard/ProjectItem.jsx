const { shell } = require('electron')

import React from 'react'

import styled from 'styled-components'
import { Box } from 'grommet/components/Box'
import { Stack } from 'grommet/components/Stack'
import { Text } from 'grommet/components/Text'

import { Edit,FolderOpen, SettingsOption, Trash } from 'grommet-icons'

export const Item = styled(Box)`
  &:hover {
    background: ${props => props.theme.global.colors['light-2'] };
    cursor: pointer;
  }

  background: ${props => props.theme.global.colors['light-1'] };
  margin: 12px;

  height: 10rem;
  width: 100%;

  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
`

export default ({id, name, path, description, selected, onClick, onEdit, onConfig, onDelete, getProjectPath}) => {
  return (
    <Item align='center' direction='column' justify='start'
      onClick={(e)=>{
        e.stopPropagation()
        onClick()
      }}>
      <Stack fill>
        <Box direction='column' fill>
          <Box align='center' direction='row' justify='center' margin='medium' >
            <Text color='brandLight' weight={900}>{name}</Text>
          </Box>
          <Box direction='row' flex margin={{
            bottom: 'small',
            left: 'medium', right: 'medium',
          }}>
            <Box height='5rem' overflow='hidden'>
              <Text color='dark-2' size='1rem' >{description}</Text>
            </Box>
          </Box>
        </Box>

        {
          selected ? (<Box align='center' background={{
            opacity:'strong',
            color:'brand'
          }} direction='row' justify='center' gap='medium' fill>
            <Box onClick={onEdit}><Edit /></Box>
            <Box onClick={onConfig}><SettingsOption /></Box>
            <Box onClick={() => {
              if (path) {
                shell.openItem(path)
                return
              } 
              
              getProjectPath(id)
                .then(p => {
                  shell.openItem(p)
                })
                .catch(err => {
                  // TODO: handle error here
                  console.log(err.message)
                })
              }}><FolderOpen /></Box>
            <Box onClick={onDelete}><Trash /></Box>
          </Box>) : null
        }
      </Stack>
    </Item>
  )
}

//export default ProjectItem
