import sources from './metadata/nodes/sources.json'
import transforms from './metadata/nodes/transforms.json'
import destinations from './metadata/nodes/destinations.json'
import visualizations from './metadata/nodes/visualizations.json'

export function getNodeTypes() {
  return [ 
    ... sources,
    ... transforms,
    ... destinations,
    ... visualizations,
  ]

  /*
  return [
    {
      "name": "CSV",
      "description": "Connect data from a CSV file",
      "icon": "images/csv.svg",
      "type": "Source",
      "step": {
        "type": "connector",
        "config": {
          "subtype": "file",
          "format": "csv"
        },
      },
      "options": {
        "maxIncoming": 0,
        "maxOutgoing": -1
      }
    },
    {
      "name": "SQL",
      "description": "Transform your data with SQL",
      "icon": "images/sql.svg",
      "type": "Transform",
      "step": {
        "type": "transformer",
        "config": {
          "subtype": "sql"
        }
      },
      "options": {
        "maxIncoming": 1,
        "maxOutgoing": -1
      }
    },
    {
      "name": "JOIN",
      "description": "Join two data",
      "icon": "images/up-arrow.svg",
      "type": "Transform",
      "step": {
        "type": "transformer",
        "config": {
          "subtype": "join",
        }
      },
      "options": {
        "maxIncoming": 2,
        "maxOutgoing": -1
      }
    },
    {
      "name": "CONCAT",
      "description": "Concatenate two data",
      "icon": "images/row.svg",
      "type": "Transform",
      "step": {
        "type": "transformer",
        "config": {
          "subtype": "concat",
        }
      },
      "options": {
        "maxIncoming": 2,
        "maxOutgoing": -1
      }
    }

  ]
  */
}

