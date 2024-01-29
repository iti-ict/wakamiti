<template>
  <div class="logoo">
    <transition>
      <div v-if="color == 'dark'">
        <g-image src="~/assets/img/generalitat-dark.svg" alt="Generalitat Valenciana"/>
        <g-image src="~/assets/img/ivace-dark.svg" alt="IVACE" />
        <g-image src="~/assets/img/ITI-logo-dark.svg" alt="ITI" />
      </div>
      <div v-if="color == 'bright'">
        <g-image src="~/assets/img/generalitat-bright.svg" alt="Generalitat Valenciana"/>
        <g-image src="~/assets/img/ivace-bright.svg" alt="IVACE" />
        <g-image src="~/assets/img/ITI-logo-bright.svg" alt="ITI" />
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  props: {
    color: {
      type: String,
      required: true
    }
  }
}
</script>

<style lang="scss" scoped>
.logoo {
  height: 50px;
  display: block;
  transition: color .15s ease-in-out;
  color: $textBright;
  position: relative;
  perspective: 200px;

  div {
    height: 50px;
    position: absolute;
    top: 0;
    left: 0;
  }

  img {
    height: 100%;
    width: auto;
  }

}

.theme-enter-active, .theme-leave-active {
  transition: opacity .15s ease-in-out;
}
.theme-enter, .theme-leave-to /* .fade-leave-active below version 2.1.8 */ {
  opacity: 0;
}
</style>