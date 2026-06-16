let sourceData = [];
let filteredData = [];
let currentPages = [];
let handlersInitialized = false;
let pageIndex = new Map();

const NON_PASSED_RESULTS = new Set(['ERROR', 'FAILED', 'UNDEFINED']);
const TEMPLATE_VIEW_HELPERS = {
    isAggregator: function () { return this.t === 'AGGREGATOR'; },
    toHTML: function () { return toHTML(this); },
    toView: function () {
        return toHTML(this).split('-')
            .map((word) => word[0].toUpperCase() + word.substring(1))
            .join(' ');
    },
    isPassed: function () { return this._isPassed; },
    hasChildren: function () { return this._hasChildren; },
    hasTags: function () { return this._hasTags; },
    cResults: function () { return this._cResults; },
    sum: function () { return this._sum; },
    cFResults: function () { return this._cFResults; },
    hasDoc: function () { return this._hasDoc; },
    hasDataTable: function () { return this._hasDataTable; },
    getHeader: function () { return this.d?.[0] || []; },
    getBody: function () { return this.d?.slice(1) || []; },
    count: function () { return this._count; },
    isNum: function () { return !isNaN(this); },
    isFirst: function () { return this.current === 1; },
    isLast: function () { return this.current === this.total; },
    prev: function () { return this.current - 1; },
    next: function () { return this.current + 1; },
    isCurrent: function () { return this === getPage(); }
};

/**
 * Replace some values to display correctly in HTML.
 *
 * @param {string} s The string value.
 * @returns {string | undefined}
 */
function toHTML(s) {
    return s?.toLowerCase().replace('_', '-');
}

function debounce(fn, wait) {
    let timeoutId;
    return function (...args) {
        clearTimeout(timeoutId);
        timeoutId = window.setTimeout(() => fn.apply(this, args), wait);
    };
}

function looksLikeJson(text) {
    const trimmed = text?.trim();
    if (!trimmed || !['{', '['].includes(trimmed[0])) {
        return false;
    }
    try {
        JSON.parse(trimmed);
        return true;
    } catch (e) {
        return false;
    }
}

function parseXml(text) {
    const trimmed = text?.trim();
    if (!trimmed || !trimmed.startsWith('<')) {
        return null;
    }
    const xml = new DOMParser().parseFromString(trimmed, 'application/xml');
    return xml.querySelector('parsererror') ? null : xml;
}

function looksLikeXml(text) {
    return !!parseXml(text);
}

function canPrettifyResponse(text) {
    return looksLikeJson(text) || looksLikeXml(text);
}

function formatResultSummary(entries) {
    return Object.entries(entries || {}).map((entry) => ({key: entry[0], value: entry[1]}));
}

/**
 * Flattens a nested array to a single depth level.
 *
 * @param {Array} c The array to be flattened.
 * @returns {Array}
 */
function flatten(c) {
    return c.map((it) => it.t !== 'STEP' && it.t !== 'VIRTUAL_STEP' && it.c ? flatten(it.c) : it)
        .flat(Infinity);
}

function buildSearchText(node) {
    return [
        node.n,
        ...(node.g || []).map((tag) => `@${tag}`),
        ...(node.l || [])
    ]
        .filter(Boolean)
        .join('\n')
        .toLowerCase();
}

function decorateNode(node) {
    if (!node) {
        return node;
    }

    node.c?.forEach(decorateNode);

    const flattenedChildren = node.c?.length ? flatten(node.c) : [];
    const resultCounts = flattenedChildren.reduce((result, entry) => {
        if (entry?.r) {
            result[entry.r] = (result[entry.r] || 0) + 1;
        }
        return result;
    }, {});

    node.prettyResponse = canPrettifyResponse(node.response);
    node._searchText = buildSearchText(node);
    node._isPassed = !NON_PASSED_RESULTS.has(node.r);
    node._hasChildren = !!node.c?.length;
    node._hasTags = !!node.g?.length;
    node._cResults = formatResultSummary(node.tr);
    node._sum = Object.values(node.tr || {}).reduce((result, value) => result + value, 0);
    node._cFResults = formatResultSummary(resultCounts);
    node._hasDoc = !!node.m || !!node.p || !!node.d || !!node.response;
    node._hasDataTable = !!node.d;
    node._count = flattenedChildren.length;

    return node;
}

function formatXml(text) {
    const xml = parseXml(text);
    if (!xml) {
        return text;
    }
    const serialized = new XMLSerializer().serializeToString(xml);
    const formatted = serialized.replace(/(>)(<)(\/*)/g, '$1\n$2$3');
    let indent = 0;
    return formatted.split('\n').map((line) => {
        let currentIndent = indent;
        if (line.match(/^<\//)) {
            currentIndent = Math.max(indent - 1, 0);
        }
        const output = `${'  '.repeat(currentIndent)}${line}`;
        if (line.match(/^<[^!?/][^>]*[^/]>/) && !line.includes('</')) {
            indent += 1;
        } else if (line.match(/^<\//)) {
            indent = currentIndent;
        }
        return output;
    }).join('\n');
}

function prettifyResponse(text) {
    if (looksLikeJson(text)) {
        return JSON.stringify(JSON.parse(text), null, 2);
    }
    if (looksLikeXml(text)) {
        return formatXml(text);
    }
    return text;
}

/**
 * Sets the given value as the current page.
 *
 * @param {number|string} n The current page number
 */
function setPage(n) {
    document.getElementById('current-page').value = n;
}

/**
 * Gets the current page number.
 *
 * @returns {number}
 */
function getPage() {
    return parseInt(document.getElementById('current-page').value, 10);
}

/**
 * Gets the current selected features per page number.
 *
 * @returns {number}
 */
function getPageSize() {
    return parseInt(document.getElementById('numElements').value, 10);
}

/**
 * Removes the hash part from the URL.
 *
 * @param {boolean} resetScroll Whether to scroll to the top after clearing the hash.
 */
function resetUrl(resetScroll = true) {
    if (window.location.href.includes('#')) {
        window.history.pushState({}, document.title, window.location.href.split('#')[0]);
    }
    if (resetScroll) {
        $('html,body').scrollTop(0);
    }
}

function buildTemplateView(item) {
    return Object.assign(Object.create(TEMPLATE_VIEW_HELPERS), item || {});
}

function clearRenderedPanel(element) {
    $(element).find('li:not(.loader):not(.empty),button').remove();
    $(element).find('.empty').hide(50);
}

function renderPanel(element, templateId, items) {
    const fragment = new DocumentFragment();
    const container = document.createElement('div');

    if (items?.length > 0) {
        items.forEach((item) => {
            container.innerHTML = Mustache.render(window.templates[templateId], buildTemplateView(item), window.templates);
            fragment.appendChild(container.firstElementChild);
        });
        clearRenderedPanel(element);
        $(fragment).hide(0);
        element.appendChild(fragment);
        $(element).find('button').show(0);
        $(element).find('.loader').hide(50);
        $(element).find('.empty').hide(50);
        $(element).find('li:not(.loader):not(.empty)').show(50);
    } else {
        clearRenderedPanel(element);
        $(element).find('.loader').hide(50);
        $(element).find('.empty').show(50);
    }
}

/**
 * Renders the content of mustache templates.
 */
function render() {
    for (const element of document.getElementsByClassName('mustache')) {
        if (!element.id) {
            element.id = Math.random().toString().replace('0.', '');
        }
        const templateId = element.getAttribute('data-template');
        const projector = element.getAttribute('data-prev');
        const items = projector ? window[projector](filteredData) : filteredData;
        renderPanel(element, templateId, items);
    }

    focusAnchor();
}

function indexNodes(nodes, pageNumber) {
    nodes?.forEach((node) => {
        if (node.i) {
            pageIndex.set(node.i, pageNumber);
        }
        indexNodes(node.c, pageNumber);
    });
}

/**
 * Sets the pages according to the applied filters and selected settings.
 */
function makePages() {
    const pageSize = getPageSize();
    currentPages = Array.from({length: Math.ceil(filteredData.length / pageSize)}, (_, index) => {
        const first = pageSize * index;
        return filteredData.slice(first, first + pageSize);
    });

    pageIndex = new Map();
    currentPages.forEach((pageEntries, index) => indexNodes(pageEntries, index + 1));
}

/**
 * Returns the current page.
 *
 * @returns {*[]}
 */
function page() {
    return currentPages[getPage() - 1] || [];
}

/**
 * Generates the paging elements from the given data and the paging configuration.
 *
 * @param data {*[]} The data
 * @returns {[{current: number, total: number, pages: string[]}]}.
 */
function pages(data) {
    const total = Math.ceil(data.length / getPageSize());
    if (total === 0) {
        return [];
    }
    const current = Math.min(getPage(), total);
    const size = Math.min(total, 7);
    const half = Math.ceil(size * 0.5);
    let result = Array.from({length: size}, (_, index) => {
        if (current < half) {
            return 1 + index;
        }
        if (current > (total - half)) {
            return total - size + 1 + index;
        }
        return current - half + 1 + index;
    });
    if (result.length < total) {
        if (result[0] > 1) {
            result[0] = '...';
        }
        if (result.at(-1) < total) {
            result[result.length - 1] = '...';
        }
    }
    return [{
        current,
        total,
        pages: result.map((value) => ({
            value,
            isNum: !isNaN(value),
            isCurrent: value === current
        }))
    }];
}

/**
 * Gets the checked status list.
 *
 * @returns {Set<string>}
 */
function statuses() {
    const result = new Set();
    for (const element of document.querySelectorAll('.nav-menu--control input')) {
        if (element.checked) {
            result.add(element.value);
        }
    }
    return result;
}

function matchesText(node, text) {
    return node._searchText?.includes(text);
}

function filterScenario(node, allowedStatuses, text) {
    if (node.t === 'AGGREGATOR') {
        const children = (node.c || [])
            .map((child) => filterScenario(child, allowedStatuses, text))
            .filter(Boolean);
        return children.length > 0 ? {...node, c: children} : null;
    }

    if (node.r && !allowedStatuses.has(node.r)) {
        return null;
    }

    if (!text || matchesText(node, text)) {
        return node;
    }

    return null;
}

/**
 * Applies the filters set to the result data.
 *
 * @returns {*[]}
 */
function filtered() {
    const allowedStatuses = statuses();
    const text = document.getElementById('search-input').value.trim().toLowerCase();

    return sourceData.reduce((features, feature) => {
        const children = (feature.c || [])
            .map((scenario) => filterScenario(scenario, allowedStatuses, text))
            .filter(Boolean);

        const featureMatches = !text || matchesText(feature, text);
        if (children.length === 0 && !featureMatches) {
            return features;
        }

        features.push({...feature, c: children});
        return features;
    }, []);
}

/**
 * Sets the page on which the given feature or scenario id is located as the current page.
 *
 * @param {string} id The feature or scenario id.
 */
function searchPage(id) {
    const pageNumber = pageIndex.get(id);

    if (!pageNumber) {
        throw new Error(`Id '${id}' not found`);
    }
    if (pageNumber !== getPage()) {
        $('.loader').show(50);
        setPage(pageNumber);
    }
}

/**
 * Toggles a class on a specific element within a group of elements.
 *
 * @param {HTMLElement} element The HTML element to toggle the class on.
 * @param {string} className The class name to be toggled.
 */
function toggleGroup(element, className) {
    let parent = element.parentElement;
    while (parent.nodeName.toLowerCase() !== 'li') {
        parent = parent.parentElement;
    }
    const alreadyOn = $(element).hasClass(className);
    for (const target of parent.getElementsByClassName('toggle')) {
        $(target).removeClass(className);
    }
    if (!alreadyOn) {
        $(element).toggleClass(className);
    }
}

function getUrlId() {
    const url = window.location.href;
    return url.substring(url.indexOf('#') + 1);
}

function focusAnchor() {
    const url = window.location.href;
    if (!url.includes('#')) {
        return;
    }
    const id = getUrlId();
    $(`li:has(#${id})`).find('.suite--header .test--header-btn').addClass('on');
    $(`#${id}`).find('.test--header-btn').addClass('on');
    document.getElementById(id)?.scrollIntoView();
    const adjust = parseInt(getCssVar('--navbar-height'), 10) + 55;
    window.scrollBy(0, -adjust);
}

function refresh(shouldRender = true) {
    filteredData = filtered();
    makePages();

    const totalPages = currentPages.length;
    if (totalPages === 0) {
        setPage(1);
    } else if (getPage() > totalPages) {
        setPage(totalPages);
    } else if (getPage() < 1 || Number.isNaN(getPage())) {
        setPage(1);
    }

    if (shouldRender) {
        render();
    }
}

/**
 * Retrieves the mustache templates.
 */
function load() {
    window.templates = {};
    for (const element of document.querySelectorAll('script[type="x-tmpl-mustache"]')) {
        window.templates[element.id] = element.innerHTML;
    }

    const embeddedData = document.getElementById('report-data')?.textContent || '{"c":[]}';
    sourceData = (JSON.parse(embeddedData).c || []).map(decorateNode);

    refresh(false);
    if (window.location.href.includes('#')) {
        searchPage(getUrlId());
    }
    render();
}

/**
 * Create buttons.
 */
function buttons() {
    if (handlersInitialized) {
        return;
    }
    handlersInitialized = true;

    const refreshSearch = debounce(() => {
        resetUrl();
        setPage(1);
        refresh();
    }, 200);

    $(document).on('click', '.toggle:not(.toggle-group)', function () {
        $(this).toggleClass('on');
        return false;
    });

    document.addEventListener('click', (event) => {
        if (!event.composedPath().includes(document.querySelector('nav.collapsable'))
                && !event.composedPath().includes(document.querySelector('.menu-button'))) {
            $('.menu-button.on').removeClass('on');
        }
    });

    $(document).on('click', 'nav.collapsable a', function () {
        $('.menu-button.on').removeClass('on');
    });

    $(document).on('click', '.toggle-group', function () {
        toggleGroup(this, 'on');
        return false;
    });

    $(document).on('change', '.nav-menu--control input[type="checkbox"]', function (event) {
        event.stopImmediatePropagation();
        $('.loader').show(50);
        setPage(1);
        refresh();
    });

    $(document).on('change', 'select', function (event) {
        event.stopImmediatePropagation();
        $('.loader').show(50);
        resetUrl(false);
        setPage(1);
        refresh();
    });

    $(document).on('input', 'input[type="search"]', function (event) {
        event.stopImmediatePropagation();
        $('.loader').show(50);
        refreshSearch();
    });

    $(document).on('click', '.simple-pagination button', function (event) {
        event.stopImmediatePropagation();
        $('.loader').show(50);
        resetUrl(false);
        setPage($(this).attr('data-page'));
        render();
    });

    $(document).on('click', '.step--format-button[data-action="toggle-response-format"]', function (event) {
        event.preventDefault();
        event.stopImmediatePropagation();
        const button = this;
        const responseBody = button.closest('.step--body-heading-wrap')?.nextElementSibling;
        const code = responseBody?.querySelector('code.step--response');
        if (!code) {
            return;
        }
        const raw = code.dataset.raw || code.textContent;
        code.dataset.raw = raw;
        if ((button.dataset.mode || 'raw') === 'raw' && canPrettifyResponse(raw)) {
            code.dataset.pretty = code.dataset.pretty || prettifyResponse(raw);
            code.textContent = code.dataset.pretty;
            button.dataset.mode = 'pretty';
            button.classList.add('on');
        } else {
            code.textContent = raw;
            button.dataset.mode = 'raw';
            button.classList.remove('on');
        }
    });

    $(document).on('click', 'nav a', function (event) {
        event.stopImmediatePropagation();
        const id = $(this).attr('href').replace('#', '').toString();
        searchPage(id);
        render();
    });
}

function getCssVar(name) {
    return getComputedStyle(document.documentElement).getPropertyValue(name);
}

$(function () {
    Mustache.tags = ['<%', '%>'];
    buttons();
    load();
});
