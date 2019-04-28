import axios from 'axios'

export default class AxiosService {
  constructor() {
    this.handlers = []
  }

  send(options) {
    axios(options)
      .then(res => {
        let data = res.data
        this.handlers.forEach(handler=>{
          handler({data})
        })
      })
      .catch(err => {
        this.handlers.forEach(handler=>{
          handler({data: {code: 500, message: err.message}})
        })
      })
  }

  onMessage(handler) {
    this.handlers.push(handler)
  }
}
