import React, { Component } from 'react'
import PropTypes from 'prop-types'

import 'jsplumb'
import panzoom from 'panzoom'
import ObjectID from 'bson-objectid'

import { DropTarget } from 'react-dnd'

import { Source, Transform, Destination, Visualization } from './nodes'

import { Graph, Grid } from './DAGEditorStyle'

import { Box } from 'grommet/components/Box'
import { Button } from 'grommet/components/Button'

import { Vulnerability } from 'grommet-icons'


const ZOOM_TO_FIT_MARGIN = 25 // must be the multiply of 25
const MIN_ZOOM = 0.5
const MAX_ZOOM = 1.5

const CONNECTION_COLOR = '#CCCCCC'

class DAGEditor extends Component {
  static propTypes = {
    nodes: PropTypes.array, // initial nodes
    connections: PropTypes.array, // initial connections
    x: PropTypes.number, // initial x position
    y: PropTypes.number,  // initial y position
    zoom: PropTypes.number, // initial zoom

    onSelectionChange: PropTypes.func,
    onChange: PropTypes.func,
    onConfigureNode: PropTypes.func,
  }

  static defaultProps = {
    nodes: [],
    connections: [],
    x: 0,
    y: 0,
    zoom: 1,

    onSelectionChange: (node)=>{},
    onChange: (nodes, connections, operation) => {},
    onConfigureNode: node=>{},
  }

  /**
   * Constructor
   */
  constructor(props) {
    super(props)

    this.containerElem = null
    this.panzoomInstance = null 
    this.jsPlumbInstance = null

    this.transform = { x: 0, y: 0, scale: this.props.zoom }

    this.state = {
      nodes: this.props.nodes,
      connections: this.props.connections,
      selectedNodeId: null,
    }
  }

  
  componentDidMount() {
    console.log('----------init dag editor----------')

    // init the panzoom
    this.panzoomInstance = panzoom(this.containerElem, {
      maxZoom: MAX_ZOOM,
      minZoom: MIN_ZOOM,
      smoothScroll: false,
      zoomSpeed: 0.025,
    })
    
    this.panzoomInstance.zoomAbs(
      this.props.x, // initial x position
      this.props.y, // initial y position
      this.props.zoom  // initial zoom 
    )

    this.panzoomInstance.on('transform', (e) => {
      this.transform = e.getTransform()
      if (this.jsPlumbInstance) {
        this.jsPlumbInstance.setZoom(this.transform.scale)
      }
    })

    // init jsplumb
    jsPlumb.ready(() => {
      // create jsPlubm instance
      this.jsPlumbInstance = jsPlumb.getInstance({
        Container: this.containerElem, 
        Connector:[ 'Flowchart', {
          cornerRadius: 10,
        } ],
        Endpoint:[ 'Dot', { radius: 5 } ],
        EndpointStyle: { fill: CONNECTION_COLOR },
        PaintStyle:{
          strokeWidth:6,
          stroke:'#567567',
          outlineStroke:'black',
          outlineWidth:1
        },
      })

      // add initial nodes and connections
      this.state.nodes.map(node => {
        this.addNode(node)
      })

      this.state.connections.map(connection => {
        this.addConnection(connection)
      })

      // register event handlers
      this.jsPlumbInstance.bind('connection', info => {
        let existing = this.state.connections.find(c => c.source === info.sourceId && c.target === info.targetId)
        if (existing) {
          return
        }
        let newConnections = [ ... this.state.connections, {source: info.sourceId, target: info.targetId} ] 
        this.changeAndNotify(this.state.nodes, newConnections, {}, {
          type: 'NEW_CONNECTION',
          connection: {source: info.sourceId, target: info.targetId}
        })
      })

      this.jsPlumbInstance.bind('connectionDetached', info => {
        let newConnections = this.state.connections.filter(c => {
          return c.source !== info.sourceId || c.target !== info.targetId
        })
        //this.props.onDeleteConnection({source: info.sourceId, target: info.targetId})
        console.log('connection detached', newConnections)
        this.changeAndNotify(this.state.nodes, newConnections, {}, {
          type: 'DELETE_CONNECTION',
          connection: {source: info.sourceId, target: info.targetId}
        })
      })

      // fit to container
      this.fitToContainer(ZOOM_TO_FIT_MARGIN)
    })
  }

  componentWillUnmount() {
    this.panzoomInstance.dispose()
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    // add new node to jsplumb
    // Note: new node are always appended to the end of the list
    if (prevState.nodes.length < this.state.nodes.length) {
      for (let i = prevState.nodes.length; i < this.state.nodes.length; i++) {
        this.addNode(this.state.nodes[i])
      }
    }
  }

  /** CUSTOMIZED METHODS **/

  changeAndNotify(nodes, connections, others = {}, operation = {}) {
    this.setState({ nodes, connections, ... others })
    this.props.onChange(nodes, connections, operation)
  } 

  fitToContainer(margin = 50) {
    if (this.state.nodes.length <= 0) {
      this.zoomAndMove(1, 0, 0)
      return
    }

    let containerRect = this.containerElem.getBoundingClientRect()
    let { scale } = this.transform

    let minX = 999999 
    let minY = 999999 
    let maxX = -999999
    let maxY = -999999
    this.state.nodes.forEach(n => {
      if (n.x < minX) minX = n.x
      if (n.x > maxX) maxX = n.x
      if (n.y < minY) minY = n.y
      if (n.y > maxY) maxY = n.y
    })

    let widthSpan = (maxX - minX) + 100 + 2 * margin
    let heightSpan = (maxY - minY) + 100 + 2 * margin
    let deltaX = margin - minX
    let deltaY = margin - minY
    let zoomX = containerRect.width / widthSpan / scale
    let zoomY = containerRect.height / heightSpan / scale

    let zoom = zoomX < zoomY ? zoomX : zoomY
    if (zoom > MAX_ZOOM) zoom = MAX_ZOOM
    if (zoom < MIN_ZOOM) zoom = MIN_ZOOM

    let newNodes = this.state.nodes.map(n => {
      return { ... n, ... {
        x: n.x + deltaX,
        y: n.y + deltaY,
      }}
    })

    // apply
    this.changeAndNotify(newNodes, this.state.connections, {}, {
      type: 'UPDATE_POSITION',
    })
    this.zoomAndMove(zoom, 0, 0)
  }

  zoomAndMove(scale, x, y) {
    this.panzoomInstance.zoomAbs(0, 0, scale)
    this.panzoomInstance.moveTo(0, 0)
    // TODO: use an asyc API?
    setTimeout(() => {
      this.jsPlumbInstance.repaintEverything()
    }, 50) // repaint after 50 ms to make sure all nodes are transformed
  }

  addNode(node) {
    if (typeof node.x !== 'number' || typeof node.y !== 'number') {
      let { left, top } = this.containerElem.getBoundingClientRect()
      let { scale } = this.transform
      node.id = ObjectID.generate()
      node.x = (node.clientX - left) / scale
      node.y = (node.clientY - top) / scale
			delete node['clientX']
			delete node['clientY']
      let newNodes = [ ... this.state.nodes, node ]
      this.changeAndNotify( newNodes, this.state.connections, {}, { 
        type: 'NEW_NODE', 
        node: { ... node } 
      })
    }

    this.jsPlumbInstance.draggable(node.id, {
      grid:[25,25],
      drag: () => {},
      stop: (e, ui) => {
        let id = e.el.id
        let pos = { x: e.pos[0], y: e.pos[1] }
        let newNodes = this.state.nodes.map(n => {
          if (n.id === id) {
            return { ... n, ... pos }
          }
          return { ... n }
        })
        this.changeAndNotify(newNodes, this.state.connections, {}, {
          type: 'UPDATE_POSITION',
        })
      }
    })

    if (['Source', 'Transform'].indexOf(node.type) >= 0) {
      this.jsPlumbInstance.addEndpoint(node.id, { anchor: 'RightMiddle' }, {
        connectionsDetachable: false,
        uniqueEndpoint: true,
        endpoint: ['Dot', { radius: 5 }],
        maxConnections: node.metadata.options.maxOutgoing,
        connectorStyle: { stroke: CONNECTION_COLOR, strokeWidth: 3 },
        isSource: true,
        overlays: [
          [ 'Arrow', { location: 1 } ],
        ],
      })
    }

    if (['Transform', 'Destination', 'Visualization'].indexOf(node.type) >= 0) {
      this.jsPlumbInstance.makeTarget(node.id, {
        allowLoopback: false,
        maxConnections: node.metadata.options.maxIncoming, //node.type === 'Transform' ? -1 : 1,
        anchor: [ 'Continuous', { faces: ['left'] } ],
        paintStyle: { fill: CONNECTION_COLOR }
      })
    }
  }

  addConnection(connection) {
    this.jsPlumbInstance.connect({
      source: connection.source,
      target: connection.target,
      endpoint: [ 'Dot', { radius: 5 } ], 
      endpointStyle: { fill: CONNECTION_COLOR },
      paintStyle: { stroke: CONNECTION_COLOR, strokeWidth: 3 },
      anchors: [
        'RightMiddle',
        [ 'Continuous', { faces: ['left'] } ]
      ],
      overlays:[
      ],
    })
  }

  onClickNode(node) {
    this.setState({ selectedNodeId: node.id })
    this.props.onSelectionChange(node)
  }

  onDeleteNode(node) {
    // get connected connections
    /*
    let connections = this.state.connections.filter(connection => {
      return connection.target === node.id || connection.source === node.id
    })
    */

    let nodes = this.state.nodes.filter(n => n.id !== node.id)
    
    if (['Source', 'Transform'].indexOf(node.type) >= 0) {
      this.jsPlumbInstance.selectEndpoints({source: node.id}).delete()
    }
  
    if (['Destination', 'Visualization'].indexOf(node.type) >= 0) {
      this.jsPlumbInstance.selectEndpoints({target: node.id}).delete()
    }

    this.jsPlumbInstance.select({source: node.id}).delete()
    this.jsPlumbInstance.select({target: node.id}).delete()
    // TODO: notify onDeleteConnection here
    
    // update state
    let newConnections = this.state.connections.filter(connection => {
      return connection.target !== node.id && connection.source !== node.id
    })
    let selectedNodeId = this.state.selectedNodeId
    if (this.state.selectedNodeId === node.id) {
      selectedNodeId = null 
      this.props.onSelectionChange(null)
    }
    this.changeAndNotify(nodes, newConnections, { selectedNodeId }, { 
      type: 'DELETE_NODE', 
      node: {...node}
    })
  }

  onDeleteConnection(connections) {
    let newConnections = this.state.connections.filter(connection => {
      return !connections.find(c => c.source === connection.source && c.target === connection.target)
    })
    connections.forEach(c => {
      this.jsPlumbInstance.select({source: c.source, target: c.target}).delete()
    })
    this.changeAndNotify(this.state.nodes, newConnections, {}, { 
      type: 'DELETE_CONNECTION'
      // TODO: add deleted connections here
    })
  }

  onDuplicateNode(node) {
    let newNode = { ... node }
    newNode.id = ObjectID.generate()
    newNode.x = node.x + 100
    newNode.y = node.y + 100
    let newNodes = [ ... this.state.nodes, newNode ]

    this.changeAndNotify(newNodes, this.state.connections, {}, {
      type: 'DUPLICATE_NODE',
      original: node,
      newNode: newNode,
    })
  } 

  onConfigureNode(node) {
    if (this.props.onConfigureNode) {
      this.props.onConfigureNode(node.id)
    }
  }


  /* RENDER FUNCTIONS */

  renderNode(node) {
    switch(node.type) {
      case 'Source':
        return <Source label={node.label} icon={node.metadata.icon} 
          selected={this.state.selectedNodeId === node.id} 
          onClick={() => this.onClickNode(node)}
          onConfigure={() => this.onConfigureNode(node)}
          onDuplicate={() => this.onDuplicateNode(node)}
          onDelete={() => this.onDeleteNode(node)}
        />
      case 'Transform':
        return <Transform label={node.label} icon={node.metadata.icon}  
          selected={this.state.selectedNodeId === node.id} 
          onClick={() => this.onClickNode(node)}
          onConfigure={() => this.onConfigureNode(node)}
          onDuplicate={() => this.onDuplicateNode(node)}
          onDelete={() => this.onDeleteNode(node)}
        />
      case 'Destination':
        return <Destination label={node.label} icon={node.metadata.icon}  
          selected={this.state.selectedNodeId === node.id} 
          onClick={() => this.onClickNode(node)}
          onConfigure={() => this.onConfigureNode(node)}
          onDuplicate={() => this.onDuplicateNode(node)}
          onDelete={() => this.onDeleteNode(node)}
        />
      case 'Visualization':
        return <Visualization label={node.label} icon={node.metadata.icon} 
          selected={this.state.selectedNodeId === node.id} 
          onClick={() => this.onClickNode(node)}
          onConfigure={() => this.onConfigureNode(node)}
          onDuplicate={() => this.onDuplicateNode(node)}
          onDelete={() => this.onDeleteNode(node)}
        />
    }
  }
  
  renderNodes() {
    return this.state.nodes.map(node => {
      return (<div key={node.id} id={node.id} style={{
          position: 'absolute', 
          left: `${node.x}px`,
          top: `${node.y}px`,
          height: '100px',
          width: '100px',
        }}
      >
        { this.renderNode(node) }
      </div>)
    })
  }

  renderControls() {
    return (
      <Box pad='small' style={{
          bottom: '10px',
          right: '10px',
          position: 'absolute',
        }}>
        <Button icon={<Vulnerability />} 
          hoverIndicator 
          onClick={() => this.fitToContainer(ZOOM_TO_FIT_MARGIN)}
        />
      </Box>
    )
  }

  render() {
    return this.props.connectDropTarget(
      <div id='dag-editor-wrapper' style={{
          backgroundColor: '#F2F2F2',
          display: 'flex',
          flex: '1 1 auto',
          overflow: 'hidden',
          outline: 'none',
          position: 'relative',
        }} > 
        <Graph id='dag-editor' ref={el => this.containerElem = el} >
          <Grid />
          {this.renderNodes()}
        </Graph>
        {this.renderControls()}
      </div>  
    )
  }
}

const dagItemTarget = {
  drop(props, monitor, component) {
    let item = monitor.getItem()
    let { x, y } = monitor.getClientOffset()
    component.addNode({
      id: ObjectID.generate(),
      label: item.name, 
      type: item.type,
      clientX: x, 
      clientY: y,
      metadata: item,
    })
  }
}

function collect(connect, monitor) {
  return {
    connectDropTarget: connect.dropTarget(),
    isOver: monitor.isOver()
  }
}

export default DropTarget('DAGItem', dagItemTarget, collect)(DAGEditor)
