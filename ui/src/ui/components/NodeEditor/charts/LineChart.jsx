import React from 'react'
import PropTypes from 'prop-types'
import palette from 'google-palette'

import './chart.scss'

import { Grid, GridRows } from '@vx/grid'
import { Group } from '@vx/group'
import { curveBasis } from '@vx/curve'
import { GradientOrangeRed } from '@vx/gradient'
import { genDateValue } from '@vx/mock-data'
import { AxisLeft, AxisRight, AxisBottom } from '@vx/axis'
import { AreaClosed, LinePath, Line, Bar } from '@vx/shape'
import { LegendOrdinal, Ordinal } from '@vx/legend'
import { scaleTime, scaleLinear, scaleOrdinal, scaleBand, scaleLog, scalePower } from '@vx/scale'
import { withTooltip, Tooltip } from '@vx/tooltip'
import { withParentSize } from '@vx/responsive'
import { localPoint } from '@vx/event'
import { extent, max, min, bisector } from 'd3-array'

const LEFT_AXIS_GAP = 20

const FONT_FAMILY = 'Work Sans, Roboto'

function calculateMargin(data, x, ys) {
  let margin = {
    top: 30,
    left: 100,
    right: 100,
    bottom: 100,
  }

  return margin
}

function getScaleX(xMax, data, accessor, fieldType) {
  switch(fieldType) {
    case 'DATETIME':
      return scaleTime({
        range: [0, xMax],
        domain: extent(data, accessor),
      })
    case 'STRING':
    default:
      return scaleBand({
        range: [0, xMax],
        domain: data.map(accessor),
      })
  } 
}

function getScaleY(yMax, data, accessors, scaleType) {
  let yDomainMin = accessors.map(y => min(data, y))
  let yDomainMax = accessors.map(y => max(data, y))
  switch(scaleType) {
    case 'Log':
      return scaleLog({
        range: [yMax, 0],
        domain: [min(yDomainMin), max(yDomainMax)],
        nice: true
      })
    case 'Power':
      return scalePower({
        range: [yMax, 0],
        domain: [min(yDomainMin), max(yDomainMax)],
        nice: true
      })
    case 'Linear':
    default:
      return scaleLinear({
        range: [yMax, 0],
        domain: [min(yDomainMin), max(yDomainMax) * 1.1],
        nice: true
      })
  }
}

function renderLine(data, x, ys, xScale, yScale, margin, colorPalette) {
  let colors = getColorPalette(ys.length)
  return (<Group top={margin.top} left={margin.left + LEFT_AXIS_GAP}>
    {
      ys.map((y, i) => (
        <LinePath
          key={i}
          data={data}
          x={d=>xScale(x(d))}
          y={d=>yScale(y(d))}
          stroke={`${colors[i]}`}
          strokeWidth={3}
        />
      ))
    }
  </Group>)
}

function getColorPalette(n) {
  return palette('tol', n).map(color => `#${color}`)
}


function handleTooltip({ event, data, x, ys, xScale, yScale, margin, showTooltip }) {
  const point= localPoint(event)
  let eachBand = xScale.step()
  let index = Math.round(((point.x - margin.left - 20 ) / eachBand))
  let value = xScale.domain()[index]
  let d = data[index]
  let top = point.y
  let left = xScale(value)  + xScale.bandwidth()

  showTooltip({
    tooltipData: d,
    tooltipLeft: left,
    tooltipTop: top,
  })
}

function renderTooltipData( data, tooltipTop, tooltipLeft, measures, yLabel, colorPalette ) {
  if (!data) {
    return null
  }

  let items = measures.map((measure, index) => {
    return (<li className="bx--data-tooltip-list-item bx--data-tooltip__multiple" style={{borderLeft: `4px solid ${colorPalette[index]}`}}>
      <span className="bx--data-tooltip-list-item__label">{measure.name}</span>
      <span className="bx--data-tooltip-list-item__data">{data[measure.name]}</span>
    </li>)
  }) 
  
  return (
    <Tooltip
      top={tooltipTop}
      left={tooltipLeft}
    >
      <div className="bx--tooltip bx--data-tooltip bx--tooltip--shown" data-floating-menu-direction="top">
        <p class="bx--data-tooltip__label">{yLabel}</p>
        <ul className="bx--data-tooltip-list bx--data-tooltip-list--single">
          {items}
        </ul>
      </div>
    </Tooltip>
  )
}

const LineChart = ({ type, data, // mandatory
  parentWidth, parentHeight, // comes from withParentSize, do NOT pass these properties from Component
  width, height, margin, // optional 
  dimension, measures = [], // optional, both dimension, and measures are type field
  title, xLabel, yLabel, yScaleType = 'Linear', // optional, default null
  getNumTicksForHeight, getNumTicksForWidth, // optional
  colorPalette = [],

  tooltipOpen,
  tooltipLeft,
  tooltipTop,
  tooltipData,
  hideTooltip, 
  showTooltip,

}) => {
  if (!data || !dimension || !measures || measures.length === 0) {
    return <div className="linechart-placeholder"></div>
  }


  let w = width ? width : parentWidth
  let h = height ? height : parentHeight -35 // minus the title height
  let x = d => d.x 
  let ys = []

  let dimensionType = dimension ? dimension.type : 'STRING'
  if (dimension && dimension.name !== '') {
    x = d => d[dimension.name]
  }
  if (Array.isArray(measures)) {
    ys = measures.map(measure => {
      return d => d[measure.name]
    })
  } else {
    measures = []
  }

  // calculate margin
  
  if (!margin) margin = calculateMargin(data, x, ys)

  let xMax = w - margin.left - margin.right
  let yMax = h - margin.top - margin.bottom
  let xScale = getScaleX(xMax, data, x, dimensionType)
  let yScale = getScaleY(yMax, data, ys, yScaleType )


  let legendColor = scaleOrdinal({
    domain: measures.map(measure => measure.name),
    range: getColorPalette(measures.length),
  })

  let palette = getColorPalette(measures.length)
  console.log(w, h, margin, xMax, yMax)
  return (
    <div style={{width: '100%', height: '100%', position: 'absolute'}}>
      <h2 className="bx--graph-header">{title}</h2>
      <div className="bx--graph-container">
        <svg width={w} height={h} >
          <GradientOrangeRed id="linear" vertical={false} fromOpacity={0.8} toOpacity={0.3} />
          <GridRows
            top={margin.top}
            left={margin.left + 20}
            scale={yScale}
            width={xMax}
            stroke="#8897a2"
            strokeWidth='1'
            strokeDasharray="5 3"
            numTicks={getNumTicksForHeight(h)}
          />
          {renderLine(data, x, ys, xScale, yScale, margin, legendColor)}
          
          <Bar
            x={0}
            y={0}
            width={w}
            height={h}
            fill="transparent"
            rx={14}
            data={data}
            onTouchStart={data => event =>
              handleTooltip({
                event,
                data,
                x,
                ys,
                xScale,
                yScale,
                margin,
                showTooltip,
              })}
            onTouchMove={data => event =>
              handleTooltip({
                event,
                data,
                x,
                ys,
                xScale,
                yScale,
                margin,
                showTooltip,
              })}
            onMouseMove={data => event =>
              handleTooltip({
                event,
                data,
                x,
                ys,
                xScale,
                yScale,
                margin,
                showTooltip,
              })}
            onMouseLeave={data => event => hideTooltip()}
          />
          <Group left={margin.left} >
            <AxisLeft
              top={margin.top}
              left={0}
              scale={yScale}
              hideZero={false}
              hideAxisLine
              numTicks={getNumTicksForHeight(h)}
              label={yLabel}
              labelOffset={margin.left - 20}
              labelProps={{
                fill: '#5a6872', textAnchor: 'middle', fontSize: 12,
                fontWeight: 600,
                fontFamily: FONT_FAMILY,
              }}
              stroke="#8897a2"
              tickStroke="#8897a2"
              tickLabelProps={(value, index) => ({
                fill: '#8897a2',
                textAnchor: 'end',
                fontSize: 12,
                fontFamily: FONT_FAMILY,
                dx: '-0.25em',
                dy: '0.25em'
              })}
              tickComponent={({ formattedValue, ...tickProps }) => (
                <text {...tickProps}>{formattedValue}</text>
              )}
            />
            <AxisBottom
              top={h - margin.bottom}
              left={LEFT_AXIS_GAP}
              scale={xScale}
              numTicks={getNumTicksForWidth(w)}
              hideTicks
              label={xLabel}
              labelOffset={margin.bottom - 40}
              labelProps={{
                fill: '#5a6872',
                textAnchor: 'middle',
                fontSize: 12,
                fontWeight: 600,
                fontFamily: FONT_FAMILY,
              }}
              stroke="#8897a2"
              strokeWidth={1}
              tickStroke="#8897a2"
              tickLabelProps={(value, index) => ({
                fill: '#8897a2',
                textAnchor: 'end',
                fontSize: 12,
                fontFamily: FONT_FAMILY,
                dx: '0em',
                dy: '0em',
              })}
              tickComponent={({ formattedValue, ...tickProps }) => {
                return(
                  <text transform={`translate(${-LEFT_AXIS_GAP}, 0) rotate(-45, ${tickProps.x}, ${5})`} {...tickProps}>{formattedValue}</text>
                )
              }}
            >
            </AxisBottom>
          </Group>
        </svg>
      </div>
      <div className="linechart-legend">
        <LegendOrdinal
          direction="column"
          itemDirection="row"
          shapeMargin="0"
          labelMargin="0 0 0 4px"
          itemMargin="0 5px"
          scale={legendColor}
          shape="rect"
          fill={({ datum }) => legendColor(datum)}
          labelFormat={label => `${label.toUpperCase()}`}
          onClick={data => event => {
            //alert(`clicked: ${JSON.stringify(data)}`)
          }}
          onMouseOver={data => event => {
            console.log(
              `mouse over: ${data.text}`,
              `index: ${data.index}`,
            )
          }}
        />
      </div>

      {tooltipOpen && renderTooltipData(tooltipData, tooltipTop, tooltipLeft, measures, yLabel, palette)}

    </div>
  )
}


LineChart.propTypes = {
  width: PropTypes.number,
  height: PropTypes.number,
  data: PropTypes.array,
  
  getNumTicksForHeight: PropTypes.func, 
  getNumTicksForWidth: PropTypes.func,
}

LineChart.defaultProps = {
  data: [],
  margin: null,

  getNumTicksForHeight: height => {
    if (height <= 300) return 3
    if (300 < height && height <= 600) return 5
    return 10
  },
  getNumTicksForWidth: width => {
    if (width <= 300) return 2
    if (300 < width && width <= 400) return 5
    return 10
  }
}

export default withParentSize(withTooltip(LineChart))
