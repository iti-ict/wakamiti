/**
 * The current filtered pages.
 *
 * @type {*[]}
 */
let cp = [];

/**
 * Replace some values to display correctly in HTML.
 *
 * @param {string} s The string value.
 * @returns {string | undefined}
 */
function toHTML(s) {
    return s?.toLowerCase().replace('_', '-');
}

/**
 * Flattens a nested array to a single depth level.
 *
 * @param {Array} c - The array to be flattened.
 * @returns {Array} - The flattened array.
 */
function flatten(c) {
    return c.map((it) => it.t !== 'STEP' && it.t !== 'VIRTUAL_STEP' && it.c ? flatten(it.c) : it)
        .flat(Infinity);
}

/**
 * Sets the given value as the current page.
 *
 * @param {number} n The current page number
 */
function setPage(n) {
    document.getElementById('current-page').value = n;
    // console.log('Page set to ' + n);
}

/**
 * Gets the current page number.
 *
 * @returns {number}
 */
function getPage() {
    return parseInt(document.getElementById('current-page').value);
}

/**
 * Gets the current selected features per page number.
 *
 * @returns {number}
 */
function getPageSize() {
    return parseInt(document.getElementById('numElements').value);
}

/**
 * Removes the hash part from the URL.
 */
function resetUrl() {
    if (window.location.href.includes('#')) {
        window.history.pushState({}, document.title, window.location.href.split('#')[0]);
    }
    $('html,body').scrollTop(0);
}

/**
 * Renders the content of mustache templates.
 */
function render() {
    // clean the mustache panels
    for (let e of document.getElementsByClassName('mustache')) {
        $(e).find('li:not(.loader):not(.empty)').remove();
        $(e).find('.empty').hide(50);
    }
    const data = filtered();
    // send data to mustache panels
    for (let e of document.getElementsByClassName('mustache')) {
        if (!e.id) e.id = Math.random().toString().replace("0.", "");
        const id = e.getAttribute("data-template");
        const func = e.getAttribute("data-prev");
        const aux = func ? this[func](data) : data;
        window.worker.postMessage({uuid: e.id, id, value: {c: aux}});
    }
}

/**
 * Sets the pages according to the applied filters and selected settings.
 */
function makePages() {
    const d = filtered();
    const ps = getPageSize();
    // console.log('Page size: ' + pageSize);
    cp = Array.from({length: Math.ceil(d.length / ps)}, (v, i) => {
        const f = ps * i;
        return d.slice(f, f + ps);
    });
}

/**
 * Returns the current page.
 *
 * @param data Unused parameter
 * @returns {*}
 */
function page(data) {
    return cp[getPage() - 1]
}

/**
 * Generates the paging elements from the given data and the paging
 * configuration.
 *
 * @param data {*[]} The data
 * @returns {[{current: number, total: number, pages: string[]}]}
 */
function pages(data) {
    const p = getPage();
    const t = Math.ceil(data.length / getPageSize());
    const s = Math.min(t, 7);
    const h = Math.ceil(s * .5);
    // console.log('page: ' + p);
    // console.log('total: ' + t);
    // console.log('size: ' + s);
    // console.log('half: ' + h);
    let pages = Array.from({length: s}, (v, i) => {
        if (p < h) {
            return 1 + i;
        } else if (p > (t - h)) {
            return t - s + 1 + i;
        } else {
            return p - h + 1 + i;
        }
    });
    if (pages.length < t) {
        if (pages[0] > 1) {
            pages[0] = '...';
        }
        if (pages.at(-1) < t) {
            pages[pages.length - 1] = '...';
        }
    }
    // console.log("Pages: " + pages);
    return [{current: p, total: t, pages}];
}

/**
 * Gets the checked status list.
 *
 * @returns {string[]}
 */
function statuses() {
    const a = [];
    for (let e of document.querySelectorAll('.nav-menu--control input')) {
        if (e.checked) a.push(e.value);
    }
    return a;
}

/**
 * Applies the filters set to the result data.
 *
 * @returns {*[]}
 */
function filtered() {
    // filter by results
    let aux = JSON.parse(data).c;
    if (JSON.parse(data).tr) {
        aux = aux.reduce((rf, f) => {
            f.c = f.c.reduce((rs, sc) => {
                if (sc.t === 'AGGREGATOR') {
                    sc.c = sc.c.filter((s) => {
                        return statuses().includes(s.r)
                    });
                    if (sc.c.length > 0) rs.push(sc);
                } else if (statuses().includes(sc.r)) {
                    rs.push(sc);
                }
                return rs;
            }, []);
            if (f.c.length > 0) rf.push(f);
            return rf;
        }, []);
    }

    // filter by text
    const text = document.getElementById("search-input").value.toLowerCase();
    if (text) {
        aux = aux.reduce((rf, f) => {
            f.c = f.c.reduce((rs, sc) => {
                if (sc.t === "AGGREGATOR") {
                    sc.c = sc.c.filter((s) => {
                        return s.n.toLowerCase().includes(text)
                            || s.g?.some((it) => ('@' + it).toLowerCase().includes(text))
                            || s.l?.some((it) => it.toLowerCase().includes(text));
                    });
                    if (sc.c.length > 0) rs.push(sc);
                } else if (sc.n.toLowerCase().includes(text)
                    || sc.g?.some((it) => ('@' + it).toLowerCase().includes(text))
                    || sc.l?.some((it) => it.toLowerCase().includes(text))
                ) {
                    rs.push(sc);
                }
                return rs;
            }, []);
            if (f.c.length > 0) rf.push(f);
            return rf;
        }, []);
    }

    return aux;
}

/**
 * Sets the page on which the given feature or scenario id is located as the current page.
 *
 * @param {string} id The feature or scenario id.
 */
function searchPage(id) {
    // console.log('Searching id ' + id);
    // console.log('Current pages: ' + JSON.stringify(cp));
    const i = cp.findIndex((p) => {
        return p.some((f) => {
            return f.i === id || f.c?.some((sc) => {
                if (sc.t === "AGGREGATOR") {
                    return sc.c?.some((s) => s.i === id);
                } else {
                    return sc.i === id;
                }
            });
        })
    });
    if (i === -1) {
        throw new Error('Id \'' + id + '\' not found');
    }
    if ((i + 1) !== getPage()) {
        $('.loader').show(50);
        setPage(i + 1);
    }
}

/**
 * Toggles a class on a specific element within a group of elements.
 *
 * @param {HTMLElement} e The HTML element to toggle the class on.
 * @param {string} c The class name to be toggled.
 */
function toggleGroup(e, c) {
    let p = e.parentElement;
    while (p.nodeName.toLowerCase() !== 'li') {
        p = p.parentElement;
    }
    const on = $(e).hasClass(c);
    for (let t of p.getElementsByClassName('toggle')) {
        $(t).removeClass(c);
    }
    if (!on) {
        $(e).toggleClass(c);
    }
}

/**
 * Retrieves the mustache templates.
 */
function load() {
    // Mustache
    window.templates = {};
    for (let e of document.querySelectorAll('script[type="x-tmpl-mustache"]')) {
        window.templates[e.id] = e.innerHTML;
        // console.log(`Template '${elem.id}' loaded`)
    }
    makePages();

    if (window.location.href.includes('#')) {
        searchPage(getUrlId());
    }
    render();
}

function getUrlId() {
    const url = window.location.href;
    return url.substring(url.indexOf('#') + 1);
}

/**
 * Create buttons.
 */
function buttons() {
    for (let e of document.getElementsByClassName('toggle')) {
        e.onclick = function () {
            $(this).toggleClass('on');
            return false;
        }
    }

    document.addEventListener('click', (e) => {
        if (!e.composedPath().includes(document.querySelector('nav.collapsable'))
                && !e.composedPath().includes(document.querySelector('.menu-button'))) {
            $('.menu-button.on').removeClass('on');
        }
    });

    $('nav.collapsable a').on('click', function() {
        $('.menu-button.on').removeClass('on');
    });

    for (let e of document.getElementsByClassName('toggle-group')) {
        e.onclick = function () {
            toggleGroup(this, 'on');
            return false;
        }
    }

    $('input[type="checkbox"],select').change(function(e) {
        $('.loader').show(50);
    });
    $(document).on('change', 'input[type="checkbox"]', function(e){
        e.stopImmediatePropagation();
        setPage(1);
        makePages();
        render();
    });
    $(document).on('change', 'select', function(e){
        e.stopImmediatePropagation();
        resetUrl();
        $('.simple-pagination button').remove();
        setPage(1);
        makePages();
        render();
    });

    $('input[type="search"]').on('input', function(e) {
        $('.loader').show(50);
    });
    $(document).on('input', 'input[type="search"]', function(e){
        e.stopImmediatePropagation();
        resetUrl();
        setPage(1);
        makePages();
        render();
    });

    $('.simple-pagination button').on('click', function(e){
        $('.loader').show(50);
    });
    $(document).on('click', '.simple-pagination button', function(e){
        e.stopImmediatePropagation();
        resetUrl();
        setPage($(this).attr('data-page'));
        render();
    });

    $('nav a').on('click', function(e) {
        e.stopImmediatePropagation();
        const id = $(this).attr('href').replace('#', '').toString();
        searchPage(id);
        render();
    });

    // hljs.highlightAll();
}

function getCssVar(name) {
    return getComputedStyle(document.documentElement).getPropertyValue(name);
}

// worker

/**
 * Generate template content.
 *
 * @param {MessageEvent} e The message event
 */
function generateContent(e) {
    const frag = new DocumentFragment();
    const el = document.getElementById(e.data.uuid);  // mustache element
    const container = document.createElement("div");

    if (e.data.value.c?.length > 0) {
        e.data.value.c.forEach((c) => {
            container.innerHTML = Mustache.render(window.templates[e.data.id], Object.assign(c || {}, {
                isAggregator: function(){ return this.t === 'AGGREGATOR' },
                toHTML: function () { return toHTML(this) },
                toView: function () {
                    return toHTML(this).split('-')
                        .map((word) => { return word[0].toUpperCase() + word.substring(1) })
                        .join(" ")
                },
                isPassed: function () { return !['ERROR', 'FAILED', 'UNDEFINED'].includes(this.r) },
                hasChildren: function(){ return this.c?.length > 0 },
                hasTags: function(){ return this.g?.length > 0 },
                cResults: function(){
                    return Object.entries(this.tr)
                        .map((it)=>{ return {key: it[0], value: it[1]} })
                },
                sum: function(){
                    return Object.values(this.tr).reduce((r, v) =>  r + v, 0)
                },
                cFResults: function(){
                    return Object.entries(flatten(this.c)
                        .reduce((r, it) => { (r[it.r] = (r[it.r] || [])).push(it); return r }, {}))
                        .map((it)=> { return {key: it[0], value: it[1].length} })
                },
                hasDoc: function(){ return !!this.m || !!this.p || !!this.d },
                hasDataTable: function(){ return !!this.d },
                getHeader: function(){ return this.d[0] },
                getBody: function(){ return this.d.slice(1) },
                count: function(){ return flatten(this.c).length },
                isNum: function(){ return !isNaN(this) },
                isFirst: function(){ return this.current === 1 },
                isLast: function() { return this.current === this.total },
                prev: function(){ return this.current - 1 },
                next: function(){ return this.current + 1 },
                isCurrent: function() {
                    return this == parseInt(document.getElementById('current-page').value)
                }
            }), window.templates);
            frag.appendChild(container.firstElementChild);
        });
        $(el).find('li:not(.loader):not(.empty),button').remove();
        $(frag).hide(0);
        el.appendChild(frag);
        buttons();
        $(el).find('button').show(0);
        $(el).find('.loader').hide(50);
        $(el).find('.empty').hide(50);
        $(el).find('li:not(.loader):not(.empty)').show(50);
        const url = window.location.href;
        if (url.includes('#')) {
            const id = getUrlId();
            $(`li:has(#${id})`).find('.suite--header .test--header-btn').addClass('on');
            $(`#${id}`).find('.test--header-btn').addClass('on');
            document.getElementById(url.substring(url.indexOf('#') + 1))?.scrollIntoView();
            const adjust = parseInt(getCssVar('--navbar-height'))+55;
            window.scrollBy(0, -adjust); // Adjust scrolling with a negative value here
        }
    } else {
        $(el).find('li:not(.loader):not(.empty),button').remove();
        $(el).find('.loader').hide(50);
        $(el).find('.empty').show(50);
    }

}

/**
 * Creates worker.
 */
function newWorker() {
    const blob = new Blob([document.querySelector('#worker').textContent], { type: "text/javascript" });
    window.worker = new Worker((window.URL || window.webkitURL).createObjectURL(blob));
    let evs = {};
    window.worker.addEventListener('message', function(e) {
        if (!e.data.uuid) {
            // console.log("Call " + JSON.stringify(e.data));
            return;
        }
        // console.log("Call elem " + e.data.uuid);
        if (!evs.hasOwnProperty(e.data.uuid)) {
            evs[e.data.uuid] = [];
        }
        evs[e.data.uuid].push(() => generateContent(e));
        setTimeout(function() {
            for (const [key, value] of Object.entries(evs)) {
                if (evs[key].length > 0) {
                    evs[key] = evs[key].slice(-1);
                    evs[key].pop().call();
                }
            }
        }, 100*e.data.value.c?.length);
    }, false);
}

$(function () {
    newWorker();
    Mustache.tags = ['<%', '%>'];
    load();
});
