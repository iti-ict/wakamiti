<template>
  <Layout :sidebar="false" v-slot="{color}">
    <div class="content">
      <h1>{{ $t('description') }}</h1>
      <nav>
        <!-- To use other icons here, you need to import them in the Shortcut component -->
        <Shortcut v-bind:link="$tp('introduction/getting-started')" v-bind:text="$t('index1')" icon="play-icon" />
        <Shortcut v-bind:link="$tp('plugins')" v-bind:text="$t('index2')" icon="file-text-icon" />
        <Shortcut v-bind:link="$tp('wakamiti/architecture#configuración-global')" v-bind:text="$t('index3')" icon="sliders-icon" />
      </nav>
      <GitLink class="git" size="large" />
      <div class="footer" v-if="color == 'dark'">
        <g-image src="~/assets/img/generalitat-dark.svg" alt="Generalitat Valenciana"/>
        <g-image src="~/assets/img/ivace-dark.svg" alt="IVACE"/>
        <g-image src="~/assets/img/ITI-logo-dark.svg" alt="ITI"/>
      </div>
      <div class="footer" v-if="color == 'bright'">
        <g-image src="~/assets/img/generalitat-bright.svg" alt="Generalitat Valenciana"/>
        <g-image src="~/assets/img/ivace-bright.svg" alt="IVACE"/>
        <g-image src="~/assets/img/ITI-logo-bright.svg" alt="ITI"/>
      </div>
    </div>
  </Layout>
</template>

<static-query>
query {
  metadata {
    siteName
  }
}
</static-query>

<script>
import GitLink from '~/components/GitLink.vue'
import Shortcut from '~/components/Shortcut.vue'

export default {
  components: {
    GitLink,
    Shortcut
  },
  metaInfo() {
    return {
      title: "Wakamiti",
      meta: [
        { key: 'description', name: 'description', content: this.$t('description') }
      ]
    }
  }
}
</script>

<style lang="scss" scoped>
.content {
  display: flex;
  flex-direction: column;
}

h1 {
  text-align: center;
  max-width: 600px;
  margin: 1.5em auto 1.5em;

  @include respond-above(md) {
    max-width: 1000px;
  }
}

h2 {
  font-size: 1.4rem;
  margin: 0;
}

nav {
  display: flex;
  justify-content: space-between;
  flex-direction: column;

  @include respond-above(sm) {
    flex-direction: row;
  }
}

article {
  min-width: 180px;
}

.git {
  margin: 3em 0 0;
  align-self: center;

  @include respond-above(md) {
    margin: 5em 0 0;
  }
}
.footer {
  height: 50px;

}
</style>
