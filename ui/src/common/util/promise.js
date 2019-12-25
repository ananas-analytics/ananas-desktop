// @flow

const toResultObject = (promise) => {
  return promise
    .then(result => ({ success: true, result  }))
    .catch(error => ({ success: false, error  }))
}


function promiseAllWithError(promises: Array<Promise<any>> ) :Promise<any> {
  return Promise.all(promises.map(toResultObject)).then(values => {
    let results = []
    for (let i = 0; i < values.length; i++) {
      if (!values[i].success) {
        results.push(values[i].error)
      } else {
        results.push(values[i].result)
      }
    }
    return results
  })
}

function promiseAllWithoutError(promises: Array<Promise<any>>) :Promise<any> {
  return Promise.all(promises.map(toResultObject)).then(values => {
    let results = []
    for (let i = 0; i < values.length; i++) {
      if (values[i].success) {
        results.push(values[i].result)
      }
    }
    return results
  })
}

module.exports = {
  promiseAllWithError,
  promiseAllWithoutError,
}
