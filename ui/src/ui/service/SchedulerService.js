import axios from 'axios'



export default class SchedulerService {
  store: any

  constructor() {
  }

  getServiceName() {
    return 'SchedulerService'
  }

  setStore(store:any) {
    this.store = store
  }

  saveTriggers(triggers: Array<PlainTrigger>) :Promise<'OK'> {
    return Promise.resolve('OK')
  }
}
