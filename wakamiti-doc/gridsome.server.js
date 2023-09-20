// Server API makes it possible to hook into various parts of Gridsome
// on server-side and add custom data to the GraphQL data layer.
// Learn more: https://gridsome.org/docs/server-api

// Changes here require a server restart.
// To restart press CTRL + C in terminal and run `gridsome develop`

module.exports = function (api) {

    api.loadSource(store => {
        // Use the Data Store API here: https://gridsome.org/docs/data-store-api

       // store.addMetadata('prefix', api.config.pathPrefix)

        const i18n = api.config.plugins.find(p => p.use === 'gridsome-plugin-i18n');
        if (i18n) {
            store.addMetadata('defaultLocale', i18n.options.defaultLocale);
            store.addMetadata('locales', i18n.options.locales);
            i18n.options.locales.forEach(locale => {
                const settings = require(`./data/settings_${locale}.json`);
                for (const item of settings.sidebar) {
                    store.addCollection({typeName: 'Menu'}).addNode({
                        section: item.section,
                        topics: item.topics,
                        locale
                    })
                }
            });
        }
    })

    api.createManagedPages(async ({createPage, removePageByPath, graphql}) => {
        // query your data source to retrieve pages
        const response = await graphql(`
          query {
            allDoc {
              edges {
                node {
                  id
                  path
                }
              }
            }
          }
        `)


        if (response.errors) {
            throw response.errors[0]
        }

        // generate pages from query response
        response.data.allDoc.edges.map(edge => edge.node).forEach((page) => {
            const locale = page.path.includes('/en/') ? 'en' : 'es';

            removePageByPath(page.path)
            createPage({
                path: page.path,
                component: './src/templates/Doc.vue',
                context: {
                    id: page.id,
                    path: page.path,
                    locale
                },
                route: {
                    meta: {
                        locale
                    }
                }
            })
        })
    });

}
