import React from 'react'
import NodeTemplate from './Node'

const Source = props => <NodeTemplate round='50%' {...props} />
const Transform = props => <NodeTemplate round='10%' {...props} />
const Destination = props => <NodeTemplate round='50%' {...props} />
const Visualization = props => <NodeTemplate round='35%' {...props} />


export { Source, Transform, Destination, Visualization }


