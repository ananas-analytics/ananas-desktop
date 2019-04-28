import moment from 'moment'
import VariableService from '../service/VariableService'

test('variable service - date format', ()=>{
  let service = new VariableService()

  let expressions = service.findExpressions('/home/csv/{date|dateFormat(YYYYMMDD)}')
  expect(expressions.length).toBe(1)

  let result = expressions[0].eval(moment('20190101'))
  expect(result).toBe('20190101')

  // test - in the format 
  expressions = service.findExpressions('/home/csv/{date|dateFormat(YYYY-MM-DD)}')
  expect(expressions.length).toBe(1)
  result = expressions[0].eval(moment('20190101'))
  expect(result).toBe('2019-01-01')
})
