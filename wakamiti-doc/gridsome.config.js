// This is where project configuration and plugin options are located. 
// Learn more: https://gridsome.org/docs/config

// Changes here require a server restart.
// To restart press CTRL + C in terminal and run `gridsome develop`
const path = require('path')
const toc = require('mdast-util-toc')


function addStyleResource (rule) {
  rule.use('style-resource')
    .loader('style-resources-loader')
    .options({
      patterns: [
        path.resolve(__dirname, './src/assets/scss/config/*.scss')
      ],
    })
}

const absolutePath = process.env.CI_PAGES_URL
const pathPrefix = new URL(absolutePath).pathname
const siteUrl = absolutePath.replace(pathPrefix, '')

const defaultLocale = 'es';

module.exports = {
  siteName: 'Wakamiti',
  siteUrl,
  pathPrefix,
  icon: './src/assets/img/logo.svg',
  templates: {
    Doc: node => node.slug
  },
  plugins: [
    {
      use: '@gridsome/source-filesystem',
      options: {
        path: '**/*.md',
        baseDir: 'docs',
        typeName: 'Doc',
        remark: {
          plugins: [
            () => {
              return (node) => {
                const result = toc(node, Object.assign({},
                    { heading: "Table of content|Tabla de contenido", maxDepth: 3}
                ))
                if (result.endIndex === null || result.index === null || result.index === -1 || !result.map) {
                  return
                }
                result.map.data = { id: 'toc', htmlAttributes: {id: 'toc'}, hProperties: {id: 'toc'} }
                node.children = [
                  ...node.children.slice(0, result.index-1),
                  result.map,
                  ...node.children.slice(result.endIndex-1)
                ]
              }
            },
            '@gridsome/remark-prismjs',
            ["@mgalbis/remark-prefix-links", { pathPrefix }]
          ]
        }
      }
    },
    {
      use: "gridsome-plugin-i18n",
      options: {
        locales: [ 'es', 'en' ],
        fallbackLocale: 'es',          // fallback language
        defaultLocale,                 // default language
        enablePathRewrite: false,      // rewrite path with locale prefix, default: true
        rewriteDefaultLanguage: false, // rewrite default locale, default: true
        enablePathGeneration: false,
        routes: {
          es: [
            {
              path: '/',
              component: './src/pages/Index.vue'
            }
          ],
          en: [
            {
              path: '/en/',
              component: './src/pages/Index.vue'
            }
          ]
        },
        messages: {
          'es': require('./src/assets/locales/es.json'),
          'en': require('./src/assets/locales/en.json'),
        }
      }
    },
    {
      use: '@gridsome/plugin-google-analytics',
      options: {
        id: (process.env.GA_ID ? process.env.GA_ID : 'XX-999999999-9')
      }
    },
    {
      use: '@gridsome/plugin-sitemap',
      options: {
        cacheTime: 600000
      }
    }
  ],
  chainWebpack: config => {
    const types = ['vue-modules', 'vue', 'normal-modules', 'normal']
    types.forEach(type => addStyleResource(config.module.rule('scss').oneOf(type)))
  }
}