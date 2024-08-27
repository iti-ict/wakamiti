
let currentPages = [];

function toHTML(s) { return s?.toLowerCase().replace('_', '-')}

function flatten(c) {
    return c.map((it) => it.t !== 'STEP' && it.t !== 'VIRTUAL_STEP' && it.c ? flatten(it.c) : it).flat(Infinity)
}

function setPage(n) {
    document.getElementById('current-page').value = n;
    // console.log('Page set to ' + n);
}

function getPage() {
    return parseInt(document.getElementById('current-page').value);
}

function getPageSize() {
    return parseInt(document.getElementById('numElements').value);
}

function resetUrl() {
    if (window.location.href.includes('#')) {
        window.history.pushState({}, document.title, window.location.href.split('#')[0]);
    }
}

function render() {
    for (let elem of document.getElementsByClassName('mustache')) {
        $(elem).find('li:not(.loader):not(.empty)').remove();
        $(elem).find('.empty').hide(50);
    }
    const data = filtered();
    for (let elem of document.getElementsByClassName('mustache')) {
        if (!elem.id) elem.id = Math.random().toString().replace("0.", "");
        const id = elem.getAttribute("data-template");
        const func = elem.getAttribute("data-prev");
        const aux = func ? this[func](data) : data;
        window.worker.postMessage({uuid: elem.id, id, value: {c: aux}});
    }
}

function makePages() {
    const data = filtered();
    const pageSize = getPageSize();
    // console.log('Page size: ' + pageSize);
    const total = Math.ceil(data.length / pageSize);
    currentPages = Array.from({length: total}, (v, index) => {
        const init = pageSize * index;
        return data.slice(init, init + pageSize);
    });
}

function page(data) {
    return currentPages[getPage() - 1]
}

function pages(data) {
    const page = getPage() - 1;
    const pageSize = getPageSize();
    const total = Math.ceil(data.length / pageSize);
    const size = Math.min(total, 7);
    const half = Math.floor(size * .5);

    let pages = Array.from({length: size}, (v, index) => {
        if (page < half) {
            return 1 + index;
        } else if (page > (total - half)) {
            return total - size + 1 + index;
        } else {
            return page - half + 1 + index;
        }
    });
    if (pages.length < total) {
        if (pages[0] > 1) {
            pages[0] = '...';
        }
        if (pages.at(-1) < total) {
            pages[pages.length - 1] = '...';
        }
    }
    // console.log("Pages: " + pages);
    return [{current: page + 1, total, pages}];
}

function statuses() {
    const statuses = [];
    for (let elem of document.querySelectorAll('.nav-menu--control input')) {
        if (elem.checked) statuses.push(elem.value);
    }
    return statuses;
}


function filtered() {
    // filter by results
    let aux = JSON.parse(data).c.reduce((rf, feature) => {
        feature.c = feature.c.reduce((rs, scenario) => {
            if (scenario.t === 'AGGREGATOR') {
                scenario.c = scenario.c.filter((s) => {return statuses().includes(s.r)});
                if (scenario.c.length > 0) rs.push(scenario);
            } else if (statuses().includes(scenario.r)) {
                rs.push(scenario);
            }
            return rs;
        }, []);
        if (feature.c.length > 0) rf.push(feature);
        return rf;
    }, []);

    // filter by text
    const text = document.getElementById("search-input").value.toLowerCase();
    if (text) {
        aux = aux.reduce((rf, feature) => {
            feature.c = feature.c.reduce((rs, scenario) => {
                if (scenario.t === "AGGREGATOR") {
                    scenario.c = scenario.c.filter((s) => {
                        return s.n.toLowerCase().includes(text)
                            || s.g?.some((it) => ('@' + it).toLowerCase().includes(text))
                            || s.l?.some((it) => it.toLowerCase().includes(text));
                    });
                    if (scenario.c.length > 0) rs.push(scenario);
                } else if (scenario.n.toLowerCase().includes(text)
                    || scenario.g?.some((it) => ('@' + it).toLowerCase().includes(text))
                    || scenario.l?.some((it) => it.toLowerCase().includes(text))
                ) {
                    rs.push(scenario);
                }
                return rs;
            }, []);
            if (feature.c.length > 0) rf.push(feature);
            return rf;
        }, []);
    }

    return aux;
}

function searchPage(id) {
    // console.log('Searching id ' + id);
    // console.log('Current pages: ' + JSON.stringify(currentPages));
    const index = currentPages.findIndex((page) => {
        return page.some((feature) => {
            return feature.i === id || feature.c?.some((scenario) => {
                if (scenario.t === "AGGREGATOR") {
                    return scenario.c?.some((s) => s.i === id);
                } else {
                    return scenario.i === id;
                }
            });
        })
    });
    if (index === -1) {
        throw new Error('Id \'' + id + '\' not found');
    }
    if ((index + 1) !== getPage()) {
        $('.loader').show(50);
        setPage(index + 1);
        render();
    }
}

function hasClass(elem, className) {
    return new RegExp(' ' + className + ' ').test(' ' + elem.className + ' ');
}


function toggleOff(elem, className) {
    let newClass = ' ' + elem.className.replace(/[\t\r\n]/g, " ") + ' ';
    while (newClass.indexOf(" " + className + " ") >= 0) {
        newClass = newClass.replace(" " + className + " ", " ");
    }
    elem.className = newClass.replace(/^\s+|\s+$/g, '');
}


function toggleClass(elem, className) {
    if (hasClass(elem, className)) {
        toggleOff(elem, className);
    } else {
        elem.className += ' ' + className;
    }
}


function toggleGroup(elem, className) {
    let parent = elem.parentElement;
    while (parent.nodeName.toLowerCase() !== 'li') {
        parent = parent.parentElement;
    }

    const on = hasClass(elem, className);

    for (let toggle of parent.getElementsByClassName('toggle')) {
        toggleOff(toggle, className);
    }

    if (!on) {
        toggleClass(elem, className);
    }
}


function getColor(name) {
    name = '--' + name.toLowerCase().replaceAll(' ', '-') + '-color';
    return getComputedStyle(document.documentElement).getPropertyValue(name);
}

function getErrorColor(index){
    name = '--error-classifier' + index;
    return getComputedStyle(document.documentElement).getPropertyValue(name);

}
function newChart(elem, labels, data, backgroundColor) {

    const datasets = [{
        data,
        backgroundColor,
        hoverOffset: 4,
        borderWidth: [0, 0, 0, 0],
    }];
    new Chart(elem, {
        type: 'doughnut',
        data: {labels, datasets},
        options: {
            responsive: false,
            maintainAspectRatio: true,
            layout: {
                padding: 10
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.dataset.data[context.dataIndex];
                        }
                    }
                },
                htmlLegend: {
                    container: elem.parentElement,
                },
                legend: {
                    display: false,
                }
            }
        },
        plugins: [{
            beforeDraw: function (chart, a, b) {
                let width = chart.width,
                    height = chart.height,
                    ctx = chart.ctx;

                ctx.restore();
                let fontSize = (height / 100).toFixed(2);
                ctx.font = fontSize + "em sans-serif";
                ctx.textBaseline = "middle";

                let text = data.filter((label, i) => chart.getDataVisibility(i)).reduce((a, b) => a + b, 0).toString(),
                    textX = Math.round((width - ctx.measureText(text).width) / 2),
                    textY = height / 2;

                ctx.fillText(text, textX, textY);
                ctx.save();
            },
        },{
            id: 'htmlLegend',
            afterUpdate(chart, args, options) {
                if (options.container.querySelector('ul')) {
                    options.container.querySelector('ul').remove();
                }

                const legend = document.createElement('ul');
                legend.className = 'chart-legend';

                // Reuse the built-in legendItems generator
                const items = chart.options.plugins.legend.labels.generateLabels(chart);

                items.forEach(item => {
                    const li = document.createElement('li');
                    li.onclick = () => {
                        chart.toggleDataVisibility(item.index);
                        chart.update();
                    };

                    // Color box
                    const boxSpan = document.createElement('span');
                    boxSpan.style.background = item.fillStyle;
                    boxSpan.style.borderColor = item.strokeStyle;
                    boxSpan.style.padding = '8px';
                    boxSpan.style.borderWidth = item.lineWidth + 'px';

                    // Text
                    const textContainer = document.createElement('p');
                    textContainer.style.textDecoration = item.hidden ? 'line-through' : '';

                    const text = document.createTextNode(item.text);
                    textContainer.appendChild(text);

                    li.appendChild(boxSpan);
                    li.appendChild(textContainer);
                    legend.appendChild(li);
                });
                options.container.appendChild(legend);
            }
        }]
    });
}


function load() {
    // Mustache
    window.templates = {};
    for (let elem of document.querySelectorAll('script[type="x-tmpl-mustache"]')) {
        window.templates[elem.id] = elem.innerHTML;
        // console.log(`Template '${elem.id}' loaded`)
    }
    makePages();
    const url = window.location.href;
    if (url.includes('#')) {
        searchPage(url.substring(url.indexOf('#') + 1));
    } else {
        render();
    }
}

function buttons() {
    for (let elem of document.getElementsByClassName('toggle')) {
        elem.onclick = function () {
            toggleClass(this, 'on');
            return false;
        }
    }

    document.addEventListener('click', (event) => {
        if (!event.composedPath().includes(document.querySelector('nav.collapsable'))
                && !event.composedPath().includes(document.querySelector('.menu-button'))) {
            $('.menu-button.on').removeClass('on');
        }
    });

    $('nav.collapsable a').on('click', function() {
        $('.menu-button.on').removeClass('on');
    });

    for (let elem of document.getElementsByClassName('toggle-group')) {
        elem.onclick = function () {
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
        searchPage($(this).attr('href').replace('#', '').toString());
    });

    hljs.highlightAll();
}

function charts() {
    const charts = document.getElementsByClassName('chart');

    for (let chart of charts) {
        const result = JSON.parse(chart.getAttribute("data-result"));
        const labels = Object.keys(result).map(k => k.replaceAll("_", " "));
        const counts = Object.values(result);
        const backgroundColor = [];

        for (let label of labels) {
            backgroundColor.push(getColor(label));
        }

        newChart(chart, labels, counts, backgroundColor);
    }

    const chartsError = document.getElementsByClassName('chart-error');

    for (let chart of chartsError) {
        const result = JSON.parse(chart.getAttribute("data-result"));
        const labels = Object.keys(result).map(k => k.replaceAll("_", " "));
        const counts = Object.values(result);
        const backgroundColor = [];

        for (var i = 0; i < labels.length; i++) {
            backgroundColor.push(getErrorColor(i));
        }

        newChart(chart, labels, counts, backgroundColor);
    }
}

// worker

function generateContent(e) {
    const frag = new DocumentFragment();
    const el = document.getElementById(e.data.uuid);  // mustache element
    const container = document.createElement("div");

    if (e.data.value.c.length > 0) {
        e.data.value.c.forEach((c) => {
            const content = Mustache.render(window.templates[e.data.id], Object.assign(c || {}, {
                isAggregator: function(){return this.t === 'AGGREGATOR'},
                toHTML: function () {return toHTML(this);},
                toView: function () {return toHTML(this).split('-')
                    .map((word) => {return word[0].toUpperCase() + word.substring(1);}).join(" ");},
                isPassed: function () {return !['ERROR', 'FAILED', 'UNDEFINED'].includes(this.r)},
                hasChildren: function(){return this.c?.length > 0},
                hasTags: function(){return this.g?.length > 0},
                cResults: function(){return Object.entries(this.tr)
                    .map((it)=>{return {key: it[0], value: it[1]}});},
                sum: function(){return Object.values(this.tr)
                    .reduce((r, v) => {return r + v;}, 0)},
                cFResults: function(){return Object.entries(flatten(this.c)
                    .reduce((r, it) => {(r[it.r] = (r[it.r] || [])).push(it); return r;}, {}))
                    .map((it)=>{return {key: it[0], value: it[1].length}});},
                hasDoc: function(){return !!this.m || !!this.p || !!this.d;},
                hasDataTable: function(){return !!this.d},
                getHeader: function(){return this.d[0];},
                getBody: function(){return this.d.slice(1);},
                count: function(){return flatten(this.c).length;},
                isNum: function(){return !isNaN(this)},
                isFirst: function(){return this.current === 1;},
                isLast: function() {return this.current === this.total},
                prev: function(){return this.current - 1},
                next: function(){return this.current + 1},
                isCurrent: function() {return this == parseInt(document.getElementById('current-page').value)}
            }), window.templates);
            container.innerHTML = content;
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
            document.getElementById(url.substring(url.indexOf('#') + 1))?.scrollIntoView();
        }
    } else {
        $(el).find('li:not(.loader):not(.empty),button').remove();
        $(el).find('.loader').hide(50);
        $(el).find('.empty').show(50);
    }

}

function newWorker() {
    var blob = new Blob([document.querySelector('#worker').textContent], { type: "text/javascript" });
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
    charts();
    load();
});

