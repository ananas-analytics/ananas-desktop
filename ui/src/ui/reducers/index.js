import { combineReducers } from 'redux'

import AppSideBar from './AppSideBar'
import AppToolBar from './AppToolBar'
import AnalysisBoard from './AnalysisBoard'
import ExecutionEngine from './ExecutionEngine'
import Message from './Message'
import Settings from './Settings'
import model from './model'

const reducer = combineReducers({
  AppSideBar,
  AppToolBar,
  AnalysisBoard,
  ExecutionEngine, 
	Message,
	Settings,
  model,
})

export default reducer
