// TODO: support multiple themes
import merge from 'deepmerge'
import { v1 } from 'grommet-theme-v1'
import { hpe } from 'grommet-theme-hpe'
import { hp } from 'grommet-theme-hp'
import { aruba } from 'grommet-theme-aruba'
import twenty from './twenty'
import peach from './peach'
import moodyBlue from './moody-blue'

const theme = {
  global: {
    colors: {
      // brand: '#228BE6',
      brandDark: '#632CCF',
      brandLight: '#986FE6',
      border: {
        light: '#DADADA',
        dark: '#999999'
      },
      node: '#F8F8F8',
      'node-text': '#999',
      'node-border': '#7D4CDB',
      'node-border-highlight': '#00873D',
      tag: 'accent-1',
      label: '#999',
    },
    font: {
      family: 'Work Sans, Roboto',
      size: '1rem',
      height: '20px',
    },
  },
}

export function getTheme(name) {
  switch(name) {
    case 'v1':
      return merge(v1, theme)
    case 'twenty':
      return twenty
    case 'peach':
      return peach
    case 'moody-blue':
      return moodyBlue
    case 'hpe':
      return hpe
    case 'hp':
      return hp
    case 'aruba':
      return aruba
    case 'default':
      default:
      return theme
  }
}
