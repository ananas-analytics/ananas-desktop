import React, { Component } from 'react'
import PropTypes from 'prop-types'

import { DragSource } from 'react-dnd'

import { Box } from 'grommet/components/Box'
import { Text } from 'grommet/components/Text'
import { Image as GrommetImage } from 'grommet/components/Image'

import NodeType from './models/NodeType'

const dagItemSource = {
  beginDrag(props) {
    return { ... props.value }
  }
}

function collect(connect, monitor) {
  return {
    connectDragSource: connect.dragSource(),
    connectDragPreview: connect.dragPreview(),
    isDragging: monitor.isDragging()
  }
}

// value, connectDragSource, connectDragPreview, isDragging 
class DAGItem extends Component {
  imgBox = null

  componentDidMount() {
    const img = new Image(40, 40)
    img.src = this.props.value.icon
    img.onload = () => this.props.connectDragPreview(img)

    this.imgBox.appendChild(img)
  }

  render() {
    if (this.props.value === null) {
      return null
    }

    let opacity = this.props.isDragging ? 0.3 : 1

    return this.props.connectDragSource(
      <div style={{ opacity, }}>
        <Box align='center'
          border={{
            side: 'bottom',
            color: 'border',
            size: 'xsmall',
          }} direction='row' height='80px' pad={{ horizontal: 'small' }} >
          <Box ref={ref=> this.imgBox = ref} height='50px' width='50px' pad='xsmall' margin={{right:'small'}}>
            { /* <GrommetImage src={this.props.value.icon} fit='cover' /> */ }
          </Box>
          <Box direction='column' flex>
            <Text size='small'>{this.props.value.name}</Text>
            <Text margin={{top:'xsmall'}}size='xsmall' >{this.props.value.description}</Text>
          </Box>
        </Box>
      </div>
    )
  }
}

DAGItem.propTypes = {
  value: PropTypes.instanceOf(NodeType)
}

DAGItem.defaultProps = {
  value: null
}

export default DragSource('DAGItem', dagItemSource, collect)(DAGItem)
