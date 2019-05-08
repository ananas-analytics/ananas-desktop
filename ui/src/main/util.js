const path = require('path')
const { app } = require('electron')

/**
 * Get the resource path, the resource is located at [project]/ui/resources folder
 * @param {string} resourcePath - the path of the resource relative to resources folder
 * @returns {string} the full resource path
 */
function getResourcePath(resourcePath) {
  let fullPath = null
  // #if process.env.NODE_ENV === 'production'  
  fullPath = path.join(app.getAppPath(), '..', 'resources', resourcePath)
  // #endif

  // #if process.env.NODE_ENV !== 'production'  
  fullPath = path.join(app.getAppPath(), 'resources', resourcePath)
  // #endif
  
  return fullPath
}

module.exports = {
  getResourcePath,
}
