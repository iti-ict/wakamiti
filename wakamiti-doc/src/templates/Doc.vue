<template>
  <Layout>
    <h1>
      {{ $page.doc.title }}
    </h1>
     <div class="markdown" v-html="$page.doc.content" />
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
    download: () => {
      const zip = new JSZip();
      const base = 'https://raw.githubusercontent.com/iti-ict/wakamiti/main/examples/tutorial';
      ['application-wakamiti.properties', 'docker-compose.yml'].forEach(file => {
        https.get(`${base}/${file}`, (response) => {
          let data = '';
          response.on('data', (chunk) => data += chunk);
          response.on('end', function () {
            zip.file(file, data);
          });
        }).on('error', function (e) {
          console.log(e.message);
        });

      });
      const a = document.createElement('a');
      a.href = "data:application/zip;base64," + zip.generate();
      a.download = 'tutorial.zip';
      a.click();
      return false;
    }
  },
  mounted () {
    document.querySelectorAll('a').forEach(el => {
      const href = el.getAttribute('href');
      if (href.startsWith('javascript:')) {
        el.addEventListener('click', e => {
          e.preventDefault();
          eval('this.' + href.replace('javascript:', ''));
        })
        el.setAttribute('href', '#')
        el.removeAttribute('target')
      }
    });
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
