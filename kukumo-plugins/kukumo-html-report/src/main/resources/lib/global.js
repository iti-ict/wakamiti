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


window.onload = function () {

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

    for (let elem of document.querySelectorAll('.nav-menu--control input')) {
        const disabledClass = 'nav-menu--disabled';
        const toggle = function () {
            const menu = document.querySelector('.nav-menu--section > ul');

            for (let scenario of menu.querySelectorAll('li[data-target="' + elem.id + '"]')) {
                const href = scenario.querySelector('a').getAttribute('href');
                const id = document.querySelector(href);

                if (elem.checked) {
                    toggleOff(scenario, disabledClass);
                    toggleOff(id, 'hidden');
                } else {
                    scenario.className += ' ' + disabledClass;
                    id.className += ' hidden';
                }
            }

            for (let feature of menu.children) {
                const href = feature.querySelector('a').getAttribute('href');
                const id = document.querySelector(href);

                const scenarios = feature.querySelectorAll('ul.nav-menu--sub > li');
                const disabled = feature.querySelectorAll('ul.nav-menu--sub > li.' + disabledClass);
                if (scenarios.length > 0) {
                    if (scenarios.length === disabled.length) {
                        feature.className += ' ' + disabledClass;
                        id.className += ' hidden';
                    } else {
                        toggleOff(feature, disabledClass);
                        toggleOff(id, 'hidden');
                    }
                } else {
                    const href = feature.querySelector('a').getAttribute('href');
                    const id = document.querySelector(href);

                    if (elem.checked) {
                        toggleOff(feature, disabledClass);
                        toggleOff(id, 'hidden');
                    } else {
                        feature.className += ' ' + disabledClass;
                        id.className += ' hidden';
                    }
                }
            }

            elem.setAttribute('aria-label', 'Toggle status: ' + (this.checked ? 'on' : 'off'));
        };
        elem.onclick = toggle;
        toggle();
    }

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
    hljs.highlightAll();
}