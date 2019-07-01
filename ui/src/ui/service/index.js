// @flow

export interface Service {
  getServiceName(): string,
  setStore(store:any): void
}

export { default as JobService } from './JobService'
export { default as ModelService } from './ModelService'
export { default as VariableService } from './VariableService'
export { default as ExecutionService } from './ExecutionService'
export { default as NotificationService } from './NotificationService'
export { default as MetadataService } from './MetadataService'
export { default as SchedulerService } from './SchedulerService'
export { default as SettingService } from './SettingService'

export default class ServiceRegistry {
  registry = {}

  register(name: string, service: Service) :void {
    this.registry[name] = service
  }

  getService(name: string) :?Service {
    return this.registry[name]
  }
}
