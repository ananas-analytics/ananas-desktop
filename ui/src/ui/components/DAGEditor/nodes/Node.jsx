import React from 'react'
import PropTypes from 'prop-types'

import styled from 'styled-components'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'
import { Image } from 'grommet/components/Image'

import Controls from './Controls'

const Node = styled.div`
  height: 100%;
  position: relative;
  width: 100%;
`

const Label = styled(Text)`
  font-weight: 500;
  overflow: visible;
  position: absolute;
  text-align: center;
	bottom: 95px;
  width: 100%;
`

const Content = styled(Box)`
  &:active {
    cursor: grabbing;
  }

  background: ${props => props.theme.global.colors.node};
  cursor: grab;
  position: absolute;
  margin: 10px;
  top: 0px;
  left: 0px;
  right: 0px;
  bottom: 0px;
`

const getBorderColor = (selected, hasIssue) => {
  if (hasIssue) return 'status-error'
  return selected ? 'node-border-highlight' : 'node-border'
}

const NodeTemplate = ({ 
  label, icon, selected, hasIssue, round,
  onClick, onConfigure, onNote, onDuplicate, onDelete 
}) => {
  return (
    <Node>
      <Label color='node-text' size='0.7rem'>{label}</Label>
      <Content elevation='medium'
        border={{
          size: selected ? '2px' : '0px',
          color: getBorderColor(selected, hasIssue),
        }}
        round={round}
        onClick={() => onClick()} 
      >
        <Box pad={ selected ? '18px' : '25px' } fill>
          <Image src={icon} fit='cover' /> 
        </Box>
      </Content>
      {
        selected ? (<Controls 
          onConfigure={()=>onConfigure()}
          onNote={()=>onNote()}
          onDuplicate={()=>onDuplicate()}
          onDelete={()=>onDelete()}
        />) : null
      }
    </Node> 
  )
}

export default NodeTemplate
