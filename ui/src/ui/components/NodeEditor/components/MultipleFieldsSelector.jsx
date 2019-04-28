// @flow

import * as React from 'react'

import { Box } from 'grommet/components/Box'
import { Button } from 'grommet/components/Button'
import { Select } from 'grommet/components/Select'
import { Text } from 'grommet/components/Text'

import { FormClose } from 'grommet-icons'

import type { NodeEditorContext } from '../../../../common/model/flowtypes.js'

type Props = {
  label: string,
  configKey?: string, // if defined, the value of the config with this key will be used to identify the step
  typeFilter: Array<string>, // filter the field by type
  maxSelections: number, // max selection
  context: NodeEditorContext, 
  value: Array<string>,
  onChange: (Array<string>)=>void,
}

type State = {
  selected: Array<string>
}

/**
 * By default MultipleFieldsSelector select the fields from the current step's first upstream dataframe
 * If the configKey property is set, it's value will be used as the stepid.
 */
export default class MultipleFieldsSelector extends React.Component<Props, State> {
  static defaultProps = {
    typeFilter: [],
    maxSelections: 0
  }
  state = { selected: this.props.value || [] }
  step = this.props.context.step
  options: Array<string> = []
  constructor(props: Props) {
    super(props)
  }

  submitChange(selected: Array<string>) {
    this.props.onChange(selected)
  }

  onRemoveField = (field:string) => {
    const { selected } = this.state
    const nextSelected = [...selected]
    nextSelected.splice(nextSelected.indexOf(this.options.indexOf(field)), 1)
    this.setState({ selected: nextSelected })
    this.submitChange(nextSelected)
  };

  renderField = (field:string) => (
    <Button
      key={`field_tag_${field}`}
      href='#'
      onClick={(event:SyntheticEvent<Button>) => {
        event.preventDefault()
        event.stopPropagation()
        this.onRemoveField(field)
      }}
      onFocus={event => event.stopPropagation()}
    >
      <Box
        align='center'
        direction='row'
        gap='xsmall'
        pad={{ vertical: 'xsmall', horizontal: 'small' }}
        margin='xsmall'
        background='accent-1'
        round='large'
      >
        <Text size='small' color='white'>
          {field}
        </Text>
        <Box background='white' round='full' margin={{ left: 'xsmall' }}>
          <FormClose
            color='accent-1'
            size='small'
            style={{ width: '12px', height: '12px' }}
          />
        </Box>
      </Box>
    </Button>
  );

  renderOption = (option:string, index:number, options:Array<string>, state:any) => (
    <Box pad='small' background={state.active ? 'active' : undefined}>
      {option}
    </Box>
  );

  render() {
    let step = null
    if (this.props.configKey) {
      let steps = this.props.context.project.steps
      step = steps[this.props.context.step.config[this.props.configKey]]
    } else {
      // get upstream
      let steps = this.props.context.project.dag.connections.filter(connection => connection.target === this.step.id)
        .map(connection => {
          return this.props.context.project.steps[connection.source] 
        })
      if (steps.length > 0) {
        step = steps[0]
      }
    }
    if (step && step.dataframe) {
      this.options = step.dataframe.schema.fields.filter(field=>{
        if (this.props.typeFilter.length === 0) {
          return true
        }
        return this.props.typeFilter.indexOf(field.type) >= 0
      }).map(field => field.name)
    } else {
      this.options = []
    }

    const { selected } = this.state

    return (
      <Box margin={{vertical: 'small'}} flex={false} fill>
        { typeof this.props.label === 'string' ? <Text size='small' margin={{bottom: 'xsmall'}}>{this.props.label}</Text> : null }
        <Select
          closeOnChange={false}
          multiple
          value={
            <Box wrap direction='row' width='small'>
              {selected && selected.length ? (
                selected.map((v, index) => this.renderField(v))
              ) : (
                <Box
                  pad={{ vertical: 'xsmall', horizontal: 'small' }}
                  margin='xsmall'
                >
                  Select Fields
                </Box>
              )}
            </Box>
          }
          options={this.options}
          disabled={selected}
          onChange={(e) => {
            if (this.props.maxSelections > 0 && selected.length === this.props.maxSelections) {
              return
            }
            let newSelected = [ ... selected, ... e.value ]
            this.setState({ selected: newSelected })
            this.submitChange(newSelected)
          }}
        >
          {this.renderOption}
        </Select>
      </Box>
    )
  }
}
