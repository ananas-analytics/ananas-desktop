class DataModel {
  insertPipeline(template) {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        let pipeline = template
        resolve(pipeline)
      }, 200)
    })
  }
}

export default DataModel

