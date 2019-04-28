// @flow

import React from 'react'

import { Box } from 'grommet/components/Box'
import { Layer } from 'grommet/components/Layer'
import { Text } from 'grommet/components/Text'

import { StatusGood, StatusCritical, StatusInfo, StatusWarning, FormClose } from 'grommet-icons'

function renderMessage(message: any, onDispose: (id:string)=>void) {
	let status = null
	let bgColor = 'status-unknown'
	switch(message.level) {
		case 'info':
			status = <StatusInfo />
			bgColor= 'status-unknown'
			break
		case 'success':
			status = <StatusGood />
			bgColor= 'status-ok'
			break
		case 'warning': 
			status = <StatusWarning />
			bgColor= 'status-warning'
			break
		case 'danger':
			status = <StatusCritical />
			bgColor= 'status-error'
			break
		default:
			status = <StatusInfo />
	}

	return (
		<Box
				key={message.id}
				align='center'
				background={bgColor}
				elevation='medium'
				direction='row'
				gap='small'
				margin={{ vertical: 'xsmall' }}
				round='small'
				pad={{ vertical: 'xsmall', horizontal: 'small' }}
				width='40%'
			>
				<Box align='center' direction='row' gap='xsmall' fill='horizontal'>
					{ status }
					<Box direction='column' margin={{left: 'xsmall'}}>
						<Text>{ message.title }</Text>
						<Text size='small'>{ message.options.body }</Text>
					</Box>
				</Box>
				<Box onClick={()=>onDispose(message.id)} style={{cursor: 'pointer'}}>
					<FormClose />
				</Box>
		</Box>
	)
}

// $FlowFixMe
export default ({ messages, onDispose }) => {
	return (
		<Layer
			position='bottom-left'
			full='horizontal'
			modal={false}
			responsive={false}
			onEsc={()=>{}}
			plain
		>
			<Box
				align='start'
				pad={{ vertical: 'medium', horizontal: 'small' }}
			>
				{ 
					messages.map(message => renderMessage(message, onDispose)) 
				}		
			</Box>
		</Layer>
	)
}
