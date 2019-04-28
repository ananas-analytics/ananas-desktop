// @flow
const { ipcRenderer } = require('electron')

export function ipc(name:string, ... args:any) {
  return new Promise<any>((resolve, reject) => {
    ipcRenderer.once(`${name}-result`, (event, res) => {
      if (res.code === 200) {
        resolve(res.data)
      } else {
        reject(new Error(res.message))
      }
    })
    ipcRenderer.send(name, ... args)
  })
}
