// @flow

import React, { Component } from 'react'


import { Box } from 'grommet/components/Box'

import type { PlainExtension } from '../../../common/model/flowtypes'

type Props = {
  projectId: string,
  extensions: {[string]: PlainExtension}
}

class Extensions extends Component<Props> {
  render() {
    return <Box>{JSON.stringify(this.props.extensions, null, 4)}</Box>
  }
}

export default Extensions
