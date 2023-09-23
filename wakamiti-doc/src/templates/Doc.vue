<template>
  <Layout>
    <h1>
      {{ $page.doc.title }}
    </h1>
     <div class="markdown" v-html="content" />
  </Layout>
</template>

<page-query>
query Doc ($path: String!) {
  doc: doc (path: $path) {
    title
    path
    date (format: "D. MMMM YYYY")
    content
  }
}
</page-query>

<script>
import JSZip from "jszip";
import https from "https";

export default {
  metaInfo() {
    return {
      title: this.$page.doc.title,
      meta: [
        { key: 'description', name: 'description', content: this.$page.doc.description }
      ]
    }
  },
  // methods: {
  //   $download: async () => {
  //     const zip = new JSZip();
  //     const base = 'https://raw.githubusercontent.com/iti-ict/wakamiti/main/examples/tutorial';
  //     const files = ['application-wakamiti.properties', 'docker-compose.yml'];
  //     let count = 0;
  //     files.forEach(file => {
  //       https.get(`${base}/${file}`, (response) => {
  //         let data = '';
  //         response.on('data', (chunk) => data += chunk);
  //         response.on('end', function () {
  //           zip.file(file, data);
  //           count++;
  //         });
  //       }).on('error', function (e) {
  //         console.log(e.message);
  //       });
  //     });
  //
  //     const until = (predFn) => {
  //       const poll = (done) => (predFn() ? done() : setTimeout(() => poll(done), 500));
  //       return new Promise(poll);
  //     };
  //
  //     await until(() => count === files.length);
  //
  //     zip.generateAsync({type: "base64"}).then(function (content) {
  //       const a = document.createElement('a');
  //       a.href = "data:application/zip;base64," + content;
  //       a.download = 'tutorial.zip';
  //       a.click();
  //     });
  //     return false;
  //   },
  //   $mount: function() {
  //     document.querySelectorAll('a').forEach(el => {
  //       const href = el.getAttribute('href');
  //       if (href.startsWith('javascript:')) {
  //         el.addEventListener('click', e => {
  //           e.preventDefault();
  //           eval( href.replace('javascript:', ''));
  //         })
  //         el.removeAttribute('target')
  //       }
  //     });
  //     document.querySelectorAll('.remark-code-clipboard').forEach(el => {
  //       el.querySelectorAll('button').forEach(btn => {
  //         const code = el.querySelector('pre.hidden').textContent
  //         btn.addEventListener('click', () => {
  //           navigator.clipboard.writeText(code)
  //           btn.querySelector('.clipboard-copy-icon').classList.add('hidden');
  //           btn.querySelector('.clipboard-check-icon').classList.remove('hidden');
  //
  //           setTimeout(() => {
  //             btn.querySelector('.clipboard-copy-icon').classList.remove('hidden');
  //             btn.querySelector('.clipboard-check-icon').classList.add('hidden');
  //           }, 3000);
  //         })
  //       });
  //     })
  //   }
  // },
  computed: {
    content() {
      return this.$page.doc.content
    },
  },
  mounted() {
    window.downloadTutorial = async () => {
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

    window.copyToClipboard = (btn) => {
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
}
</script>


<style lang="scss" scoped>
/deep/ > p {
  opacity: .8;
}

/deep/ > h2, /deep/ > h3, /deep/ > h4, /deep/ > h5, /deep/ > h6 {
  padding-top: 100px;
  margin-top: -80px;

 /* @include respond-above(md) {
    font-size: 2rem;
  } */
}

/deep/ > p > img {
    max-width: 100%;
  }

.markdown {
  padding-bottom: 50px;
  text-align: justify;
}
</style>
