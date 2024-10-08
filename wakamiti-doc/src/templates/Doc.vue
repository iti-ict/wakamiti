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
<static-query>
query {
  metadata {
    prefix
  }
}
</static-query>

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
  computed: {
    content() {
      return this.$page.doc.content
    },
  },
  methods: {
    changed() {
      document.querySelectorAll('.remark-asciinema').forEach(it => {
        const url = it.getAttribute('data-url')
        const opts = JSON.parse(it.getAttribute('data-opts'));
        try {
          if (it.querySelectorAll('.ap-wrapper').length === 0)
            AsciinemaPlayer.create(`${this.$static.metadata.prefix}/${url}`, it, opts);
        } catch (e) {}
      })
    },
  },
  mounted() {

    window.downloadTutorial = async () => {
      const zip = new JSZip();
      const base = 'https://raw.githubusercontent.com/iti-ict/wakamiti/main/examples/tutorial';
      const files = ['application-wakamiti.properties', 'docker-compose.yml', 'readme.md'];
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
      const code = btn.parentElement.querySelector('pre.hidden').textContent
      navigator.clipboard.writeText(code)
      btn.querySelector('.clipboard-copy-icon').classList.add('hidden');
      btn.querySelector('.clipboard-check-icon').classList.remove('hidden');

      setTimeout(() => {
        btn.querySelector('.clipboard-copy-icon').classList.remove('hidden');
        btn.querySelector('.clipboard-check-icon').classList.add('hidden');
      }, 3000);
    }

    const prefix = this.$static.metadata.prefix;

    const script = document.createElement('script')
    script.setAttribute('src', prefix + '/asciinema-player.min.js');
    script.onload = this.changed
    document.head.appendChild(script);

    document.querySelectorAll("#app").forEach(element => {
      element.addEventListener('DOMSubtreeModified', this.changed, false)
    })
  }
}
</script>


<style lang="scss" scoped>
/deep/ > p {
  opacity: .8;
}

/deep/ > h2, /deep/ > h3, /deep/ > h4, /deep/ > h5, /deep/ > h6 {
  margin-top: 40px;
}

/deep/ > h2 {
  padding-bottom: 15px;
  border-bottom: 1px solid;

  &.bright {
    border-color: $textBright;
  }

  &.dark {
    border-color: $textDark;
  }
}

/deep/ > :not(h2) + h3 {
  padding-top: 25px;
  border-top: 1px solid;

  &.bright {
    border-color: $textBright;
  }

  &.dark {
    border-color: $textDark;
  }
}

/deep/ > p > img {
    max-width: 100%;
  }

.markdown {
  padding-bottom: 50px;
  text-align: justify;
}
</style>
