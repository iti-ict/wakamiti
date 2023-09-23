// This is the main.js file. Import global CSS and scripts here.
// The Client API can be used here. Learn more: gridsome.org/docs/client-api

import DefaultLayout from '~/layouts/Default.vue'
import '~/assets/scss/globals.scss'
import Vuex from 'vuex'
import JSZip from "jszip";
import https from "https";
require('typeface-source-sans-pro')

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

  Vue.prototype.$downloadTutorial = async () => {
    const zip = new JSZip();
    const base = 'https://raw.githubusercontent.com/iti-ict/wakamiti/main/examples/tutorial';
    const files = ['application-wakamiti.properties', 'docker-compose.yml'];
    let count = 0;
    files.forEach(file => {
      https.get(`${base}/${file}`, (response) => {
        let data = '';
        response.on('data', (chunk) => data += chunk);
        response.on('end', function () {
          zip.file(file, data);
          count++;
        });
      }).on('error', function (e) {
        console.log(e.message);
      });
    });

    const until = (predFn) => {
      const poll = (done) => (predFn() ? done() : setTimeout(() => poll(done), 500));
      return new Promise(poll);
    };

    await until(() => count === files.length);

    zip.generateAsync({type: "base64"}).then(function (content) {
      const a = document.createElement('a');
      a.href = "data:application/zip;base64," + content;
      a.download = 'tutorial.zip';
      a.click();
    });
    return false;
  };

  Vue.prototype.$copyToClipboard = (btn) => {
    const code = btn.parent().querySelector('pre.hidden').textContent
    navigator.clipboard.writeText(code)
    btn.querySelector('.clipboard-copy-icon').classList.add('hidden');
    btn.querySelector('.clipboard-check-icon').classList.remove('hidden');

    setTimeout(() => {
      btn.querySelector('.clipboard-copy-icon').classList.remove('hidden');
      btn.querySelector('.clipboard-check-icon').classList.add('hidden');
    }, 3000);
  }
}
