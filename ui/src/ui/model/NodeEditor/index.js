// @flow

export interface GetDataEventOption {
  getType(): string,
  getDescription(): string
}

export class ExploreOption implements GetDataEventOption {
  type = 'EXPLORE'
  description = 'Explore the data'
  constructor(description?: string) {
    if (description) {
      this.description = description  
    }
  }

  getType() {
    return this.type
  }

  getDescription() {
    return this.description
  }
}

export class TestOption implements GetDataEventOption {
  type = 'TEST'
  description = 'The following data only shows the example output (it is calculate from partial data). Connect a Destination or a Visualization and "RUN" to get the full result'
  constructor(description?: string) {
    if (description) {
      this.description = description  
    } 
  }

  getType() {
    return this.type
  }

  getDescription() {
    return this.description
  }
}

export class JobResultOption implements GetDataEventOption {
  type = 'JOB_RESULT'
  description = 'Explore the data of specific job'
  jobid = null
  constructor(jobid:string, description?:string) {
    if (description) {
      this.description = description  
    }
    this.jobid = jobid
  }

  getType() {
    return this.type
  }

  getDescription() {
    return this.description
  }

  getJobId() {
    return this.jobid
  }
}
