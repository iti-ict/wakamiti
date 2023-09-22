<template>
  <Layout>
    <h1>
      {{ $page.doc.title }}
    </h1>
     <div class="markdown" v-html="$page.doc.content" @load="$mount" />
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
import https from "https";
import JSZip from "jszip";

export default {
  metaInfo() {
    return {
      title: this.$page.doc.title,
      meta: [
        { key: 'description', name: 'description', content: this.$page.doc.description }
      ]
    }
  },
  methods: {
    download: async () => {
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
    }
  },
  mounted () {
    document.querySelectorAll('a').forEach(el => {
      const href = el.getAttribute('href');
      if (href.startsWith('javascript:')) {
        el.addEventListener('click', e => {
          e.preventDefault();
          eval(href.replace('javascript:', ''));
        })
        el.removeAttribute('target')
      }
    });
    document.querySelectorAll('.remark-code-clipboard').forEach(el => {
      const btn = el.querySelector('button')
      const code = el.querySelector('pre.hidden').textContent
      btn.addEventListener('click', () => {
        navigator.clipboard.writeText(`${code}`)
        btn.querySelector('.clipboard-copy-icon').addClass('hidden');
        btn.querySelector('.clipboard-check-icon').removeClass('hidden');

        setTimeout(() => {
          btn.querySelector('.clipboard-copy-icon').removeClass('hidden');
          btn.querySelector('.clipboard-check-icon').addClass('hidden');
        }, 3000);
      })
    })
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


/* tabs */
.remark-code-tabs {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min-content, 1px));
  grid-template-rows: min-content auto;
  width: 100%;
  min-height: 10px;

  & > label {
    display: flex;
    cursor: pointer;
    white-space: nowrap;
    border-bottom: .1rem solid #0000;
    padding: 10px;

    & > [type=radio] {
      appearance: none;
      margin: 0;
    }

  }

  & > :not(label) {
    grid-column-start: 1;
    grid-column-end: -1;
    grid-row-start: 2;
    grid-row-end: 3;
  }

  & > label:has(:checked) {
    color: $brandPrimary;
    border-bottom-color: $brandPrimary;
  }

  & > label:not(:has(:checked)) + * {
    display: none;
  }
}

.remark-code-tabs-x {
  width: 0;
  height: 0;
  visibility: hidden;
}

/* clipboard */
.remark-code-clipboard {
  position: relative;

  &:hover button {
    display: inline-grid;
  }

  & button {
    position: absolute;
    top: 0;
    right: 0;
    margin: 5px;
    padding: 5px;
    background: transparent;
    border-radius: 5px;
    fill: currentColor;
    opacity: .4;
    cursor: pointer;
    display: none;
  }
  & button:hover {
    opacity: .8;
  }


  .dark & button {
    border: solid 1px #{$textBright}22
  }

  .bright & button {
    border: solid 1px #{$textDark}22
  }

  .dark & button:hover {
    border: solid 1px #{$textBright}55
  }

  .bright & button:hover {
    border: solid 1px #{$textDark}55
  }

  .dark & button:active {
    border: solid 1px #{$textBright}
  }

  .bright & button:active {
    border: solid 1px #{$textDark}
  }
}
</style>
