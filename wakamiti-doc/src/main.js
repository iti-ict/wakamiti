// This is the main.js file. Import global CSS and scripts here.
// The Client API can be used here. Learn more: gridsome.org/docs/client-api

import DefaultLayout from '~/layouts/Default.vue'
import '~/assets/scss/globals.scss'
import '~/assets/scss/asciinema-player.css'
import Vuex from 'vuex'
import JSZip from "jszip";
import https from "https";
import {pathPrefix} from "../gridsome.config";
require('typeface-source-sans-pro')

// const absolutePath = process.env.CI_PAGES_URL
// const pathPrefix = new URL(absolutePath).pathname
export default function (Vue, { router, head, isClient, appOptions }) {
  Vue.use(Vuex)

  // Set default layout as a global component
  Vue.component('Layout', DefaultLayout)

  // Add attributes to HTML tag
  head.htmlAttrs = { lang: 'es' }

  // head.link.push({
  //   rel: 'manifest',
  //   href: url('/manifest.json')
  // })

  head.meta.push({
    name: 'theme-color',
    content: '#10c186'
  })

  head.meta.push({
    name: 'google-site-verification',
    content: process.env.GSV_META
  })

  head.meta.push({
    name: 'apple-mobile-web-app-status-bar-style',
    content: 'default'
  })

  head.script.push({ src: pathPrefix + '/asciinema-player.min.js' })

  // State
  appOptions.store = new Vuex.Store({
    state: {
      sidebarOpen: false
    },
    mutations: {
      toggleSidebar(state) {
        state.sidebarOpen = !state.sidebarOpen
      },
      closeSidebar(state) {
        state.sidebarOpen = false
      },
      openSidebar(state) {
        state.sidebarOpen = true
      }
    }
  })
}
