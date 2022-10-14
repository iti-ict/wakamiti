<template>
  <button id="languageSwitch" @click="toggleLanguage()" aria-label="Switch language between spanish and english">
    <transition name="theme">
      <globe-icon v-if="$context.locale == 'es'" class="globe" />
    </transition>
    <transition name="theme">
      <globe-icon v-if="$context.locale == 'en'" class="globe" />
    </transition>
    {{ text[$context.locale] }}
  </button>
</template>

<static-query>
query {
  metadata {
    locales
    defaultLocale
  }
}
</static-query>

<script>
import { GlobeIcon } from 'vue-feather-icons'

export default {
  components: {
    GlobeIcon
  },
  data() {
    return {
      text: {
        es: 'EN',
        en: 'ES'
      }
    }
  },
  methods: {
    getLanguage: function() {
      let locale = this.$static.metadata.locales.filter(locale => locale != this.defaultLocale)
          .find(locale => this.$route.path.includes(`/${locale}/`))
      return locale ? locale : this.$static.metadata.defaultLocale;
    },
    toggleLanguage: function() {
      const language = this.$context.locale == 'es' ? 'en' : 'es';

      if (language != this.getLanguage()) {
        this.$router.push({
          path: this.$tp(this.$route.path, language, true)
        })
        this.$emit('theme-change')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
button {
  background: none;
  border: 0;
  padding: 0 10px;
  transition: color .15s ease-in-out;
  cursor: pointer;
  width: 70px;
  height: 48px;
  position: relative;
  text-align: right;

  &:focus {
    outline: none;
  }

  .dark & {
    color: $textDark;
  }

  .bright & {
    color: $textBright;
  }
}

svg {
  position: absolute;
  top: 12px;
  left: 12px;
}

.theme-enter-active, .theme-leave-active {
  transition: transform .25s ease-in-out, opacity .25s ease-in-out;
}
.theme-enter, .theme-leave-to /* .fade-leave-active below version 2.1.8 */ {
  transform: translateY(20px) scale(.5);
  opacity: 0;
}
</style>


