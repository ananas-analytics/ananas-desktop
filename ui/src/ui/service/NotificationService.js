// @flow

// import type { Dispatch } from '../model/flowtypes'

export default class NotificationService {

  getServiceName() {
    return 'NotificationService'
  }

  notify(title: string, options: {}) {
    // $FlowFixMe
    let myNotification = new Notification(title, options)
    myNotification.onClick = () => {console.log('click notification')}
  }
}
