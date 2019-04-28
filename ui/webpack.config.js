const path = require('path')
const webpack = require('webpack')
const FlowWebpackPlugin = require('flow-webpack-plugin')

const pack = require('./package.json')

const version = pack.version

let env = process.env.NODE_ENV
let production = process.env.NODE_ENV === 'production'
console.log(`- env: ${env}; is production? ${production}`)

let datumaniaEnv = process.env.DATUMANIA_BUILD_ENV || 'local'
console.log(`- build env: ${datumaniaEnv}`)

module.exports = {
  entry: './src/ui/index.js',
  target: 'electron-renderer',
  mode: env,
  module: {
    rules: [
      {
        test: /\.(ejs)$/,
        use: [
          {
            loader: 'ejs-compiled-loader',
          },
        ]
      },
      {
        test: /\.(js|jsx)$/,
        exclude: /(node_modules|bower_components)/,
        loader: 'babel-loader',
        options: {
          presets: ['@babel/preset-env', '@babel/preset-flow'],
          plugins: [
            '@babel/plugin-proposal-class-properties',
            '@babel/plugin-proposal-object-rest-spread',
            '@babel/plugin-syntax-dynamic-import'
          ]
        }
      },
      {
        test: /\.(css|scss)$/,
        use: [
          {
            loader: 'style-loader'
          },
          {
            loader: 'css-loader', options: {
              importLoaders: 1,
            }
          },
          {
            loader: 'sass-loader', options: {
            }
          }
        ]
      },
      {
        test: /\.(gif|png|jpe?g|svg)$/i,
        use: [
          {
            loader: 'file-loader',
            options: {
              publicPath: '/',
              name: 'images/[name].[ext]',
            }
          },
        ],
      }
    ]
  },
  resolve: { extensions: ['*', '.js', '.jsx'] },
  output: {
    path: path.resolve(__dirname, 'dist/'),
    publicPath: '/dist/',
    filename: 'bundle.js'
  },
  devServer: {
    contentBase: path.join(__dirname, 'public/'),
    port: 3000,
    publicPath: 'http://localhost:3000/dist/',
    hotOnly: true
  },
  plugins: [
    new FlowWebpackPlugin(),
    new webpack.HotModuleReplacementPlugin()
  ]
}
