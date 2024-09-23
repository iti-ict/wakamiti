
/**
 * Gets the css color of the given error classifier.
 *
 * @param {number} i The error index
 * @returns {string}
 */
function getErrorColor(i){
    return getCssVar('--error-classifier' + i);
}

/**
 * Gets the css color of the given result type.
 *
 * @param {string} t The result type
 * @returns {string}
 */
function getColor(t) {
    t = '--' + t.toLowerCase().replaceAll(' ', '-') + '-color';
    return getCssVar(t);
}

/**
 * Create charts.
 */
function charts() {
    for (let ch of document.getElementsByClassName('chart')) {
        const r = JSON.parse(ch.getAttribute("data-result"));
        const ls = Object.keys(r).map(k => k.replaceAll("_", " "));
        const c = Object.values(r);
        const bg = [];

        for (let l of ls) {
            bg.push(getColor(l));
        }

        newChart(ch, ls, c, bg);
    }

    for (let ch of document.getElementsByClassName('chart-error')) {
        const r = JSON.parse(ch.getAttribute("data-result"));
        const ls = Object.keys(r).map(k => k.replaceAll("_", " "));
        const c = Object.values(r);
        const bg = [];

        for (let i = 0; i < ls.length; i++) {
            bg.push(getErrorColor(i));
        }

        newChart(ch, ls, c, bg);
    }
}

/**
 * Generate chart.
 *
 * @param {HTMLElement} e The html element
 * @param l The labels
 * @param {*} d The data
 * @param {string[]} bg The background colors
 */
function newChart(e, l, d, bg) {
    new Chart(e, {
        type: 'doughnut',
        data: {
            labels: l,
            datasets: [
                {
                    data: d,
                    backgroundColor: bg,
                    hoverOffset: 4,
                    borderWidth: [0, 0, 0, 0],
                }
            ]
        },
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
                    container: e.parentElement,
                },
                legend: {
                    display: false,
                }
            }
        },
        plugins: [{
            beforeDraw: function (chart, a, b) {
                let w = chart.width,
                    h = chart.height,
                    ctx = chart.ctx;

                ctx.restore();
                let fs = (h / 100).toFixed(2);
                ctx.font = fs + "em sans-serif";
                ctx.textBaseline = "middle";

                let t = d.filter((label, i) => chart.getDataVisibility(i)).reduce((a, b) => a + b, 0).toString(),
                    tX = Math.round((w - ctx.measureText(t).width) / 2),
                    tY = h / 2;

                ctx.fillText(t, tX, tY);
                ctx.save();
            },
        },{
            id: 'htmlLegend',
            afterUpdate(chart, args, op) {
                if (op.container.querySelector('ul')) {
                    op.container.querySelector('ul').remove();
                }

                const le = document.createElement('ul');
                le.className = 'chart-legend';

                // Reuse the built-in legendItems generator
                const its = chart.options.plugins.legend.labels.generateLabels(chart);

                its.forEach(it => {
                    const li = document.createElement('li');
                    li.onclick = () => {
                        chart.toggleDataVisibility(it.index);
                        chart.update();
                    };

                    // Color box
                    const s = document.createElement('span');
                    s.style.background = it.fillStyle;
                    s.style.borderColor = it.strokeStyle;
                    s.style.padding = '8px';
                    s.style.borderWidth = it.lineWidth + 'px';

                    // Text
                    const txt = document.createElement('p');
                    txt.style.textDecoration = it.hidden ? 'line-through' : '';
                    txt.appendChild(document.createTextNode(it.text));

                    li.appendChild(s);
                    li.appendChild(txt);
                    le.appendChild(li);
                });
                op.container.appendChild(le);
            }
        }]
    });
}

$(function () {
    charts();
});