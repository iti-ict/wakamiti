<template>
    <aside class="sidebar" :class="{'sidebar--open' : this.$store.state.sidebarOpen}">
      <div class="scroll">
        <nav>
          <ul>
            <li class="section" v-for="{ node } in filterEdges($static.menu.edges)" :key="node.id">
              <h3 class="section-title">{{node.section}}</h3>
              <ul>
                <li v-for="item in node.topics" :key="item.title">
                  <g-link class="topic" :to="'/' + item.slug">{{item.title}}</g-link>
                  <ul v-if="checkAnchors(node, item)" v-for="{ node } in $static.docs.edges" :key="node.id">
                    <li v-for="heading in filterHeadings(node.headings)" :key="heading.value">
                      <g-link class="sub-topic" :to="'/' + item.slug + heading.anchor">{{heading.value}}</g-link>
                    </li>
                  </ul>
                </li>
              </ul>
            </li>
          </ul>
          <GitLink class="git" />
        </nav>
      </div>
    </aside>
</template>

<static-query>
query Menu {
  menu: allMenu(order:ASC) {
    edges {
      node {
        locale
        section
        topics {
          title
          slug,
          headings {
            depth
            value
            anchor
          }
        }
      }
    }
  }
  docs: allDoc {
    edges {
      node {
        slug
        headings {
          depth
          value
          anchor
        }
      }
    }
  }
}
</static-query>

<script>
import GitLink from '~/components/GitLink.vue'
import throttle from 'lodash/throttle'

export default {
  components: {
    GitLink
  },
  watch: {
    '$route' () {
      this.$store.commit('closeSidebar')
    }
  },
  methods: {
    filterHeadings(headings) {
      return headings.filter( h => {
        return h.depth < 3
      })
    },
    filterEdges(edges) {
      return edges.filter( e => e.node.locale == this.$context.locale)
    },
    checkAnchors(node, item) {
      if (node.slug.replaceAll(/^\//g, '') == item.slug.replaceAll(/^\//g, '')) {
        if (item.headings.length > 0) {
          node.headings = item.headings
        }
        return true
      }
    },
    stateFromSize: function() {
      if (window.getComputedStyle(document.body, ':before').content == '"small"') {
        this.$store.commit('closeSidebar')
      } else {
        this.$store.commit('openSidebar')
      }
    },
    sidebarScroll: function() {
      let mainNavLinks = document.querySelectorAll('.topic.active + ul .sub-topic')
      let fromTop = window.scrollY

      mainNavLinks.forEach(link => {
        if (link.hash) {
          let section = document.querySelector(decodeURIComponent(link.hash))
          let allCurrent = document.querySelectorAll('.current'), i

          if (section.offsetTop <= fromTop) {
            for (i = 0; i < allCurrent.length; ++i) {
              allCurrent[i].classList.remove('current')
            }
            link.classList.add('current')
          } else {
            link.classList.remove('current')
          }
        } else {
          const url = document.URL.replaceAll(/#.*/g, '').replaceAll(/\/$/g, '')
          if (url.endsWith(link.getAttribute('href'))) {
            link.classList.add('current');
          }else {
            link.classList.remove('current')
          }
        }

      })
    }
  },
  beforeMount () {
    this.stateFromSize()
  },
  mounted() {
    document.querySelectorAll("#app").forEach(element => {
      element.addEventListener('DOMSubtreeModified', throttle(this.sidebarScroll, 50), false)
    })
    window.addEventListener('scroll', throttle(this.sidebarScroll, 50))
  }
}
</script>

<style lang="scss" scoped>
.sidebar {
  transition: background .15s ease-in-out, transform .15s ease-in-out, border-color .15s linear;
  width: 300px;
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 9;
  will-change: transform;
  transform: translateX(-300px);
  border-right: 1px solid transparent;

  @include respond-above(sm) {
    transform: translateX(0);
  }

  &--open {
    transform: translateX(0);
  }
  
  .bright & {
    background: $sidebarBright;
    border-color: shade($sidebarBright, 10%);
  }

  .dark & {
    background: $sidebarDark;
    border-color: shade($sidebarDark, 40%);
  }

}

.scroll {
  margin-top: 80px;
  padding: 0 30px 30px;
  overflow: auto;
  height: -webkit-fill-available;
  transition: margin-top .15s linear;
  will-change: margin-top;

  @include respond-above(sm) {
    margin-top: 110px;
  }
}

nav {
  position: relative;
  min-height: 100%;
  border: 1px solid transparent;
  padding-bottom: 40px;
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;

  a {
    text-decoration: none;
    padding: 5px 0;
    display: block;

    .dark & {
      color: $textDark;
    }

    .bright & {
      color: $textBright;
    }

    &.topic.active {
      color: $brandPrimary;
    }

  }

  .topic + & {
    padding-left: 5px;
  }

  .topic:not(.active) + & {
    height: 0;
    transition: height .15s;
    will-change: height;
    overflow: hidden;
  }

}

.section {
  margin-bottom: 30px;
}

.section-title {
  text-transform: uppercase;
  font-size: 12px;
  margin-bottom: 20px;
  opacity: .3;
  letter-spacing: .15em;
  font-weight: 700;
}

.topic {
  font-weight: 700;
}

.sub-topic {
  font-size: .875rem;
  position: relative;
  opacity: .8;

  &::after {
    content: '';
    transition: opacity .15s ease-in-out;
    width: 6px;
    height: 6px;
    background: $brandPrimary;
    border-radius: 100%;
    display: block;
    opacity: 0;
    position: absolute;
    top: 13px;
    left: -15px;
  }

  &.current {
    &::after {
      opacity: 1;
    }
  }
}

.git {
  position: absolute;
  bottom: 0;
  left: 0;
}
</style>


