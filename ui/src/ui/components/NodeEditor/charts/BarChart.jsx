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
import { BarGroup } from '@vx/shape'
import { LegendOrdinal } from '@vx/legend'
import { scaleTime, scaleLinear, scaleOrdinal, scaleBand, scaleLog, scalePower } from '@vx/scale'
import { withTooltip, Tooltip } from '@vx/tooltip'
import { withParentSize } from '@vx/responsive'
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

function getScaleX0(xMax, data, accessor, fieldType) {
  // TODO: format the tick according to field type
  let format = v => '' + v
  switch(fieldType) {
    case 'DATETIME':
      format = date => `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}` 
      break
  }
  return scaleBand({
    rangeRound: [0, xMax],
    domain: data.map(accessor),
    padding: 0.2,
    tickFormat: () => format,
  })
}

function getScaleX1(x0Scale, measures) {
  return scaleBand({
    rangeRound: [0, x0Scale.bandwidth()],
    domain: measures.map(measure => measure.name),
    padding: .1,
  })
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

function getScaleZ(measures, colorPalette) {
  return scaleOrdinal({
    domain: measures.map(measure => measure.name),
    range: colorPalette,
  })
}

function renderBar(data, x, ys, measures, x0Scale, x1Scale, yScale, zScale, yMax, w, h, margin, colorPalette, hideTooltip, showTooltip) {
  let tooltipTimeout
  return (
    <BarGroup
      left={margin.left}
      top={margin.top}
      data={data}
      keys={measures.map(measure=>measure.name)}
      height={yMax}
      x0={x}
      x0Scale={x0Scale}
      x1Scale={x1Scale}
      yScale={yScale}
      color={zScale}
      rx={4}
      onClick={data => event => {
        //alert(`clicked: ${JSON.stringify(data)}`)
      }}
      onMouseLeave={data => event => {
        tooltipTimeout = setTimeout(() => {
          hideTooltip()
        }, 300);
      }}
      onMouseMove={d => event => {
        if (tooltipTimeout) clearTimeout(tooltipTimeout)
        const top =  yScale(d.value)
        const left = x0Scale(x(d.data)) + margin.left + x1Scale(d.key) + x1Scale.bandwidth() / 2 
        showTooltip({
          tooltipData: d,
          tooltipTop: top,
          tooltipLeft: left
        })
      }}

    />
  )
}

function getColorPalette(n) {
  return palette('tol', n).map(color => `#${color}`)
}

const BarChart = ({ type, data, // mandatory
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
    return <div className="chart-placeholder"></div>
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
  if (!colorPalette || colorPalette.length === 0) {
    colorPalette = getColorPalette(measures.length)
  }
  

  let xMax = w - margin.left - margin.right
  let yMax = h - margin.top - margin.bottom
  let x0Scale = getScaleX0(xMax, data, x, dimension.type)
  let x1Scale = getScaleX1(x0Scale, measures)
  let yScale = getScaleY(yMax, data, ys, yScaleType )
  let zScale = getScaleZ(measures, colorPalette)


  let legendColor = scaleOrdinal({
    domain: measures.map(measure => measure.name),
    range: colorPalette,
  })

  console.log(tooltipData)
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

          {renderBar(
            data, x, ys, measures, x0Scale, x1Scale, yScale, zScale, yMax, w, h, margin, colorPalette,
            hideTooltip, showTooltip
          )}
          
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
              scale={x0Scale}
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
      <div className="chart-legend">
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
            //alert(`clicked: ${JSON.stringify(data)}`);
          }}
          onMouseOver={data => event => {
            /*
            console.log(
              `mouse over: ${data.text}`,
              `index: ${data.index}`,
            );
            */
          }}
        />
      </div>
      {tooltipOpen && (
        <Tooltip
          top={tooltipTop}
          left={tooltipLeft}
        >
          {/*
          <div style={{ color: zScale(tooltipData.key) }}>
            <strong>{tooltipData.key}</strong>
          </div>
          <div>{tooltipData.data[tooltipData.key]}</div>
          <div>
            <small>{tooltipData.xFormatted}</small>
          </div>
          */}
          <div className="bx--tooltip bx--data-tooltip bx--tooltip--shown" data-floating-menu-direction="top">
            <ul className="bx--data-tooltip-list bx--data-tooltip-list--single">
              <li className="bx--data-tooltip-list-item bx--data-tooltip__single" style={{borderTop: '4px solid #3b1a40'}}>
                <span className="bx--data-tooltip-list-item__label">{tooltipData.key}</span>
                <span className="bx--data-tooltip-list-item__data">{tooltipData.data[tooltipData.key]}</span>
              </li>
            </ul>
          </div>
        </Tooltip>
      )}
      
    </div>
  )
}


BarChart.propTypes = {
  width: PropTypes.number,
  height: PropTypes.number,
  data: PropTypes.array,
  
  getNumTicksForHeight: PropTypes.func, 
  getNumTicksForWidth: PropTypes.func,
}

BarChart.defaultProps = {
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

export default withParentSize(withTooltip(BarChart))
