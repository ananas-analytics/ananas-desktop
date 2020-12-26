const path = require('path')
const webpack = require('webpack')
const nodeExternals = require('webpack-node-externals')


module.exports = {
  entry: './main.js',
  target: 'electron-main',
  externals: nodeExternals(),
  module: {
    noParse: /anyproxy/,
    rules: [
      {
        test: /\.(js)$/,
        exclude: /(node_modules)/,
        use: [
          {
            loader: 'babel-loader',
            options: {
              presets: [ '@babel/env', '@babel/preset-flow' ],
              plugins: [
                '@babel/plugin-proposal-class-properties',
                '@babel/plugin-proposal-object-rest-spread',
                '@babel/plugin-syntax-dynamic-import',
                '@babel/plugin-transform-async-to-generator',
                '@babel/plugin-transform-runtime'
              ]
            }
          },
          {
            loader: 'webpack-conditional-loader'
          }
        ],
      },
    ]
  },
  resolve: { extensions: ['*', '.js'] },
  output: {
    path: path.resolve(__dirname, 'dist/'),
    publicPath: '/dist/',
    filename: 'app.js'
  },
}
