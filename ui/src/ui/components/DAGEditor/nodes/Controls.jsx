import React from 'react'
import PropTypes from 'prop-types'

import styled from 'styled-components'

import { Box } from 'grommet/components/Box'

import { Configure, Duplicate, Note, Trash } from 'grommet-icons'


const Controls = styled(Box)`
  position: absolute;
  top: 100px;
  width: 100%;
`

const NodeControls = ({ 
  onConfigure, onNote, onDuplicate, onDelete
}) => {
  return (
    <Controls direction='row' gap='small' justify='center'>
      <Box style={{cursor:'pointer'}}><Configure size='small' onClick={()=>onConfigure()} /></Box>
      {/*<Box style={{cursor:'pointer'}}><Note size='small' onClick={()=>onNote()} /></Box>*/}
      <Box style={{cursor:'pointer'}}><Duplicate size='small' onClick={()=>onDuplicate()} /></Box>
      <Box style={{cursor:'pointer'}}><Trash size='small' onClick={()=>onDelete()} /></Box> 
    </Controls>
  )
}

NodeControls.propTypes = {
  onConfigure: PropTypes.func,
  onNode: PropTypes.func,
  onDuplicate: PropTypes.func,
  onDelete: PropTypes.func,
}

NodeControls.defaultProps = {
  onConfigure: ()=>{},
  onNote: ()=>{},
  onDuplicate: ()=>{},
  onDelete: ()=>{},
}

export default NodeControls
