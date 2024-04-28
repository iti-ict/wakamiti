
function toHTML(s) { return s?.toLowerCase().replace('_', '-')}

function flatten(c) {
    return c.map((it) => it.t !== 'STEP' && it.t !== 'VIRTUAL_STEP' && it.c ? flatten(it.c) : it).flat(Infinity)
}


function render(data) {
    for (let elem of document.getElementsByClassName('mustache')) {
        if (!elem.id) elem.id = Math.random().toString().replace("0.", "");
        const id = elem.getAttribute("data-template");
        window.worker.postMessage({uuid: elem.id, id, value: data})
       // $(elem).html(Mustache.render(window.templates[id], Object.assign(data, aux), window.templates));
    }
}


function getPage(arr, pageSize, page) {
    return arr.length <= pageSize ? arr : arr.reduce((r, v, i) => {
        if (i % pageSize === 0) r.push(arr.slice(i, i + pageSize));
        return r;
        }, [])[page]
}


function statuses() {
    const statuses = [];
    for (let elem of document.querySelectorAll('.nav-menu--control input')) {
        if (elem.checked) statuses.push(elem.value);
    }
    return statuses;
}


function filtered() {
    const pageSize = 10;
    const page = 0;

    // filter by results
    const aux = JSON.parse(data).c.reduce((rf, feature) => {
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


    return {c: getPage(aux, pageSize, page)}
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
    console.log("Running load");
    // Mustache
    window.templates = {};
    for (let elem of document.querySelectorAll('script[type="x-tmpl-mustache"]')) {
        window.templates[elem.id] = elem.innerHTML;
        console.log(`Template '${elem.id}' loaded`)
    }

    render(filtered());
}

function buttons() {
    for (let elem of document.getElementsByClassName('toggle')) {
        elem.onclick = function () {
            toggleClass(this, 'on');
            return false;
        }
    }

    for (let elem of document.getElementsByClassName('toggle-group')) {
        elem.onclick = function () {
            toggleGroup(this, 'on');
            return false;
        }
    }

    $('input[type="checkbox"]').change(function(e) {
        $('.loader').show(50);
    });
    $(document).on('change', 'input[type="checkbox"]', function(e){
        e.stopImmediatePropagation();
        render(filtered());
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

function generateContent(e) {
    const frag = new DocumentFragment();
    const el = document.getElementById(e.data.uuid);
    const container = document.createElement("div");
    $(el).find('li:not(.loader)').remove();
    const promises = e.data.value.c.map((c) => {
        return new Promise(resolve => {
            setTimeout(function() {
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
                    count: function(){return flatten(this.c).length;}
                }), window.templates);
                container.innerHTML = content;
                frag.appendChild(container.firstElementChild);
                // el.appendChild(frag);
                resolve();
            }, 100);
        });
    });
    Promise.all(promises).then(() => {
        el.appendChild(frag);
        buttons();
        $(el).find('.loader').hide(50);
        $(el).find('li:not(.loader)').show(50);
    });
}


$(function () {
    var blob = new Blob([document.querySelector('#worker').textContent], { type: "text/javascript" });
    window.worker = new Worker((window.URL || window.webkitURL).createObjectURL(blob));
    window.worker.addEventListener('message', function(e) {
        if (!e.data.uuid) {
            console.log("Call " + JSON.stringify(e.data));
            return;
        }
        console.log("Call elem " + e.data.uuid);
        generateContent(e);
    }, false);

    Mustache.tags = ['<%', '%>'];
    charts();
    load();
});

