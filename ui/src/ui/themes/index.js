// TODO: support multiple themes
import merge from 'deepmerge'
import { v1 } from 'grommet-theme-v1'

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
    case 'default':
      default:
      return theme
  }
}
