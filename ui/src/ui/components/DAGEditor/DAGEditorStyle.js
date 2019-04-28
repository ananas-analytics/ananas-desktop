import styled from 'styled-components'

const gridColor = 'rgba(120, 120, 120, .05)'
const Grid = styled.div`
  &:active {
    cursor: grabbing;
  }

  background-color: transparent;
  background-image: linear-gradient(0deg, transparent 24%, ${gridColor} 25%, ${gridColor} 26%, transparent 27%, transparent 74%, ${gridColor} 75%, ${gridColor} 76%, transparent 77%, transparent), linear-gradient(90deg, transparent 24%, ${gridColor} 25%, ${gridColor} 26%, transparent 27%, transparent 74%, ${gridColor} 75%, ${gridColor} 76%, transparent 77%, transparent);
  background-size:50px 50px;

  cursor: grab;

  left: -20013.5px;
  height: 100000px;
  outline: none;
  position: absolute;
  top: -20012.5px;
  width: 100000px;
`

const Graph = styled.div`
  position: relative;
  width: 100%;
`

export { Graph, Grid }
