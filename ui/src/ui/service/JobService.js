// @flow

import axios from 'axios'
import moment from 'moment'
import type { ID } from '../../common/model/flowtypes.js'

import NotificationService from './NotificationService'

type JobStatus = 'RUNNING' | 'SUBMITTED' | 'DONE' | 'ERROR' | 'FAILED' 

export class Job {
  id: ID
  stepId: ID
  userName: string
  state: JobStatus 
  message: string
  createTime: moment
  updateTime: moment

  token: string

  // eslint-disable-next-line
  pollingJob: ?IntervalID = null

  notificationService: NotificationService

  constructor(id: ID, stepId: ID, userName: string, state: JobStatus, message: string, token: string) {
    this.id = id
    this.stepId = stepId 
    this.userName = userName
    this.state = state
    this.message = message
    this.createTime = moment()
    this.updateTime = this.createTime

    this.token = token
  }

  setNotificationService(notificationService: NotificationService) {
    this.notificationService = notificationService
  }

  setStatus(state: JobStatus, message: string) {
    this.state = state
    this.message = message
    this.updateTime = moment()
  }

  startStatusPolling(apiURL: string, interval: number = 10000) {
    this.endStatusPolling() 

    this.pollingJob = setInterval(() => {
      if (this.state === 'DONE' || this.state === 'ERROR') {
        this.endStatusPolling()
      }

      axios({
        method: 'GET',
        url: `${apiURL}/jobs/${this.id}/poll`,
        headers: {
          'content-type': 'application/json',
          'authorization': this.token,
        }
      })
      .then(res=>{
        if (res.data.code !== 200) {
          throw new Error(res.data.message)
        }
        this.setStatus(res.data.data.state, res.data.data.message)
          
        if (this.state === 'DONE' || this.state === 'ERROR' || this.state === 'FAILED') {
          this.notificationService.notify(`Job ${this.state}`, {
            body: `Job ${this.id} changes state to ${this.state}`
          })
          this.endStatusPolling()
        }
      })
      .catch(err => {

      })
    }, interval)
  }

  endStatusPolling() {
    if (this.pollingJob !== null) {
      clearInterval(this.pollingJob)
      this.pollingJob = null
      console.log('end job state polling for', this.pollingJob)
    }
  }
}

export default class JobService {
  apiURL: string
  notificationService: NotificationService

  jobs: Array<Job> = []

  constructor(apiURL :string, notificationService: NotificationService) {
    this.apiURL = apiURL
    this.notificationService = notificationService
  }

  getServiceName() {
    return 'JobService'
  }

  getJobs() {
    return this.jobs
  }

  newJob(id: ID, stepId: ID, userName: string, token: string) {
    if (this.jobs.find(job => job.id === id)) {
      return
    }
    let job = new Job(id, stepId, userName, 'SUBMITTED', '', token)
    this.jobs.push(job)
    job.setNotificationService(this.notificationService)
    job.startStatusPolling(this.apiURL, 3000)
  }

  getJobsByStepId(stepId: ID, orderBy?: 'createTime' | 'updateTime') : Array<Job> {
    let filtered = this.jobs.filter(job => job.stepId === stepId)
    switch(orderBy) {
      case 'createTime':
        return filtered.sort((a, b) => a.createTime.diff(b.createTime))
      case 'updateTime':
        return filtered.sort((a, b) => a.updateTime.diff(b.updateTime))
      default: 
        return filtered
    }
  }

  getJobsByStatus(state: JobStatus, orderBy?: 'createTime' | 'updateTime'): Array<Job> {
    let filtered = this.jobs.filter(job => job.state === state)
    switch(orderBy) {
      case 'createTime':
        return filtered.sort((a, b) => a.createTime.diff(b.createTime))
      case 'updateTime':
        return filtered.sort((a, b) => a.updateTime.diff(b.updateTime))
      default: 
        return filtered
    }
  }
}
