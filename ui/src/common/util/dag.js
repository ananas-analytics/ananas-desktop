// @flow

const dagre = require('dagre')

import type { PlainStep, PlainConnection } from '../model/flowtypes'

function calculateLayout(steps: Array<PlainStep>, connections: Array<PlainConnection>) {
  let g = new dagre.graphlib.Graph()

  g.setGraph({
    rankdir: 'LR'
  })
  g.setDefaultEdgeLabel(function() { return {} })

  steps.forEach(step => {
    g.setNode(step.id, { 
      id: step.id,
      metadataId: step.metadataId, 
      width: 100,
      height: 100,
    })
  })

  connections.forEach(connection => {
    g.setEdge(connection.source, connection.target)
  })

  dagre.layout(g)

  return g.nodes().map(v => g.node(v))
}


module.exports = {
  calculateLayout,
}
