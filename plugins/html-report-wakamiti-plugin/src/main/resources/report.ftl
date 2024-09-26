<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta content="IE=edge" http-equiv="X-UA-Compatible"/>
    <meta content="width=device-width, initial-scale=1" name="viewport">

    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons+Outlined" rel="stylesheet"/>
    <style>${globalStyle}</style>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.7.0/highlight.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/mustache.js/4.1.0/mustache.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
    <#if plan.testCaseResults?has_content>
        <script src="https://cdn.jsdelivr.net/npm/chart.js@3.5.1/dist/chart.min.js"></script>
    </#if>
    <script id="worker" type="javascript/worker">self.onmessage=function(e){postMessage(e.data);};</script>
    <script>const data='{"c":${data}}';</script>
    <script>${globalScript};</script>
    <#if plan.testCaseResults?has_content>
        <script>${chartsScript};</script>
    </#if>

    <title>Report</title>

</head>
<body>
<div id="report">

    <div class="navbar">
        <div class="report-info">
            <button class="menu-button toggle" title="Menu" type="button"><span></span></button>
            <h1 class="report-title"
                title="${title!plan.name!plan.displayName}">${title!plan.name!plan.displayName}</h1>
        </div>
        <span id="logo"></span>
    </div>

    <div class="details container" id="details">
        <#if plan.testCaseResults?has_content>
            <ul class="test--list details--list">
                <li class="details--item">
                    <section class="test--component summary--component">
                        <header class="test--header">
                            <button class="test--header-btn" type="button">
                                <h3 class="test--title">Feature Results</h3>
                                <hr/>
                            </button>
                        </header>
                        <div class="test--body details--body">
                            <canvas class="chart"
                                    data-result='{<#list plan.childrenResults as k, v>"${k}":${v?c}<#sep>,</#list>}'>
                            </canvas>
                        </div>
                    </section>
                </li>
                <li class="details--item">
                    <section class="test--component summary--component">
                        <header class="test--header">
                            <button class="test--header-btn" type="button">
                                <h3 class="test--title">Scenario Results</h3>
                                <hr/>
                            </button>
                        </header>
                        <div class="test--body details--body">
                            <canvas class="chart"
                                    data-result='{<#list plan.testCaseResults as k, v>"${k}":${v?c}<#sep>,</#list>}'>
                            </canvas>
                        </div>
                    </section>
                </li>
                <#if plan.errorClassifiers?has_content>
                    <li class="details--item">
                        <section class="test--component summary--component">
                            <header class="test--header">
                                <button class="test--header-btn" type="button">
                                    <h3 class="test--title">Errors By Type</h3>
                                    <hr/>
                                </button>
                            </header>
                            <div class="test--body details--body">
                                <canvas class="chart-error"
                                        data-result='{<#list plan.errorClassifiers as k, v>"${k}":${v?c}<#sep>,</#list>}'>
                                </canvas>
                            </div>
                        </section>
                    </li>
                </#if>
            </ul>
        </#if>
        <ul class="test--list details--list">
            <#if plan.testCaseResults?has_content>
                <li class="details--item">
                    <section class="test--component summary--component">
                        <header class="test--header">
                            <button class="test--header-btn" type="button">
                                <h3 class="test--title">Run info</h3>
                                <hr/>
                            </button>
                        </header>
                        <div class="test--body details--body">
                            <ul>
                                <#if project??>
                                    <li>
                                        <label>Project name</label>
                                        <span>${project}</span>
                                    </li>
                                </#if>
                                <li>
                                    <label>Wakamiti version</label>
                                    <span>${version!"?.?.?"}</span>
                                </li>
                                <li>
                                    <label>Execution start</label>
                                    <span>${plan.startInstant?datetime.xs?string.medium}</span>
                                </li>
                                <li>
                                    <label>Execution end</label>
                                    <span>${plan.finishInstant?datetime.xs?string.medium}</span>
                                </li>
                                <li>
                                    <label>Total duration</label>
                                    <span>${(plan.duration!0)?string.@duration}</span>
                                </li>
                            </ul>
                        </div>
                    </section>
                </li>
            </#if>
            <#if extra_info?has_content>
                <li class="details--item">
                    <section class="test--component summary--component">
                        <header class="test--header">
                            <button class="test--header-btn" type="button">
                                <h3 class="test--title">Extra info</h3>
                                <hr/>
                            </button>
                        </header>
                        <div class="test--body details--body">
                            <ul>
                                <#assign keys = extra_info?keys>
                                <#list keys as key>
                                    <li>
                                        <label>${key}</label>
                                        <span>${extra_info[key]}</span>
                                    </li>
                                </#list>
                            </ul>
                        </div>
                    </section>
                </li>
            </#if>
        </ul>

        <div class="pagination">
            <div class="page-size">
                <label for="numElements">Features per page</label>
                <select name="numElements" id="numElements">
                    <option value="1">1</option>
                    <option value="5" selected>5</option>
                    <option value="10">10</option>
                    <option value="20">20</option>
                </select>
            </div>

            <div class="simple-pagination mustache" data-template="pagination" data-prev="pages">
            </div>
            <input type="hidden" name="current" id="current-page" value="1" />
        </div>
        <ul class="suite--list mustache" data-template="feature" data-prev="page">
            <li style="display: flex; justify-content: center" class="loader">Loading...</li>
            <li style="display: flex; justify-content: center" class="empty">No data</li>
        </ul>

        <script id="pagination" type="x-tmpl-mustache">
            <div>
            <button class="simple-pagination__button" type="button" <%^isFirst%>data-page="1"<%/isFirst%><%#isFirst%>disabled<%/isFirst%>>
                <i class="material-icons">first_page</i>
            </button>
            <button class="simple-pagination__button" type="button" <%^isFirst%>data-page="<%prev%>"<%/isFirst%><%#isFirst%>disabled<%/isFirst%>>
                <i class="material-icons">chevron_left</i>
            </button>
            <%#pages%>
                <%>pages%>
            <%/pages%>
            <button class="simple-pagination__button" type="button" <%^isLast%>data-page="<%next%>"<%/isLast%><%#isLast%>disabled<%/isLast%>>
                <i class="material-icons">chevron_right</i>
            </button>
            <button class="simple-pagination__button" type="button" <%^isLast%>data-page="<%total%>"<%/isLast%><%#isLast%>disabled<%/isLast%>>
                <i class="material-icons">last_page</i>
            </button>
            </div>
        </script>
        <script id="pages" type="x-tmpl-mustache">
            <button class="simple-pagination__button <%#isCurrent%>selected<%/isCurrent%>" type="button"
            <%#isNum%>data-page="<%.%>"<%/isNum%><%^isNum%>disabled<%/isNum%><%#isCurrent%>disabled<%/isCurrent%>>
                <%.%>
            </button>
        </script>
        <script id="feature" type="x-tmpl-mustache">
            <li id="<%i%>">
                <section class="suite--component">
                    <header class="suite--header">
                        <button class="test--header-btn toggle toggle-group <%^isPassed%>on<%/isPassed%> <%^hasChildren%>disabled<%/hasChildren%>" type="button">
                            <h2 class="suite--title">
                                <span><strong class="keyword"><%k%>:</strong> <%n%></span>
                                <%#hasChildren%><i class="material-icons md-18">expand_more</i><%/hasChildren%>
                            </h2>
                            <%#l%>
                                <h6 class="test--comment"><%.%></h6>
                            <%/l%>
            <#if plan.testCaseResults?has_content>
                            <ul class="test-summary--component">
                                <li class="test-summary--item" title="Result: <%#r%><%toView%><%/r%>">
                                    <i class="material-icons md-18 icon--<%#r%><%toHTML%><%/r%>"></i>
                                </li>
                                <li class="test-summary--item" title="Duration">
                                    <i class="material-icons md-18">access_time</i>
                                    <span><%w%></span>
                                </li>
                                <li class="test-summary--item" title="Scenarios">
                                    <i class="material-icons md-18">article</i>
                                    <span><%sum%></span>
                                </li>
                                <%#hasChildren%>
                                <li class="test-summary--item">
                                    <ul class="test-summary--results">
                                        <%#cResults%>
                                        <li class="test-summary--item" title="<%#key%><%toView%><%/key%>">
                                                <i class="material-icons md-18 icon--<%#key%><%toHTML%><%/key%>"></i>
                                                <span><%value%></span>
                                        </li>
                                        <%/cResults%>
                                    </ul>
                                </li>
                                <%/hasChildren%>
                            </ul>
                            </#if>
            </button>
        </header>
        <%#c%>
        <div class="suite--body collapsable">
            <ul class="test--list">
                <%#isAggregator%>
                <%#c%><%>scenario%><%/c%>
                <%/isAggregator%>
                <%^isAggregator%><%>scenario%><%/isAggregator%>
            </ul>
        </div>
        <%/c%>
    </section>
</li>
        </script>
        <script id="step" type="x-tmpl-mustache">
            <li class="step--component">
                <header class="step--header">
                    <button class="step--header-btn toggle <%^isPassed%>on<%/isPassed%>" type="button">
            <#if plan.testCaseResults?has_content>
                        <i class="material-icons md-18 icon--<%#r%><%toHTML%><%/r%>" title="Result: <%#r%><%toView%><%/r%>"></i>
                        </#if>
            <h5 class="step--title" title="<%n%>">
                <span><strong class="keyword"><%k%></strong> <%n%></span>
            </h5>
            <div class="step--info">
                <%#m%><i class="material-icons-outlined md-18" title="<%.%>">feedback</i><%/m%>
            <#if plan.testCaseResults?has_content>
                            <span title="Duration"><%w%></span>
                            <i class="material-icons md-18 step--duration-icon" title="Duration">access_time</i>
                            </#if>
            </div>
        </button>
    </header>
    <%#hasDoc%>
        <div class="step--body-wrap collapsable">
            <%#p%>
            <div class="step--body">
                <pre class="step--code-snippet <%#o%>language-<%.%><%/o%> hljs"><code><%p%></code></pre>
            </div>
            <%/p%>
            <%#hasDataTable%>
            <div class="step--body ">
                <table class="panel-body-datatable step--code-snippet hljs">
                    <thead>
                        <tr><%#getHeader%><td><%.%></td><%/getHeader%></tr>
                    </thead>
                    <tbody>
                        <%#getBody%>
                        <tr><%#.%><td><%.%></td><%/.%></tr>
                        <%/getBody%>
                    </tbody>
                </table>
            </div>
            <%/hasDataTable%>
            <%#m%>
            <div class="step--body">
                <pre class="step--code-snippet text hljs"><code class="step--<#if plan.testCaseResults?has_content><%#r%><%toHTML%><%/r%>-</#if>message"><%#e%><%e%><%/e%><%^e%><%m%><%/e%></code></pre>
                        </div>
                        <%/m%>
                    </div>
                <%/hasDoc%>
            </li>
        </script>
        <script id="scenario" type="x-tmpl-mustache">
            <li id="<%i%>">
                <section class="test--component">
                    <header class="test--header">
                        <button class="test--header-btn toggle toggle-group <%^isPassed%>on<%/isPassed%> <%^hasChildren%>disabled<%/hasChildren%>" type="button">
                            <%#hasTags%>
                            <h5 class="test--title">
                                <span class="test--tags"><%#g%>@<%.%> <%/g%></span>
                                <%#hasChildren%><i class="material-icons md-18">expand_more</i><%/hasChildren%>
                            </h5>
                            <%/hasTags%>
                            <h3 class="test--title">
                                <span><strong class="keyword"><%k%>:</strong> <%n%></span>
                                <%^hasTags%>
                                <%#hasChildren%><i class="material-icons md-18">expand_more</i><%/hasChildren%>
                                <%/hasTags%>
                            </h3>
                            <%#l%>
                            <h6 class="test--comment"><%.%></h6>
                            <%/l%>
                            <ul class="test-summary--component">
            <#if plan.testCaseResults?has_content>
                                <li class="test-summary--item" title="Result: <%#r%><%toView%><%/r%>">
                                    <i class="material-icons md-18 icon--<%#r%><%toHTML%><%/r%>"></i>
                                </li>
                                <li class="test-summary--item" title="Duration">
                                    <i class="material-icons md-18">access_time</i>
                                    <span><%w%></span>
                                </li>
                                </#if>
            <li class="test-summary--item" title="Steps">
                <i class="material-icons md-18">fact_check</i>
                <span><%count%></span>
            </li>
            <#if plan.testCaseResults?has_content>
                                <%#hasChildren%>
                                <li class="test-summary--item">
                                    <ul class="test-summary--results">
                                        <%#cFResults%>
                                        <li class="test-summary--item" title="<%#key%><%toView%><%/key%>">
                                                <i class="material-icons md-18 icon--<%#key%><%toHTML%><%/key%>"></i>
                                                <span><%value%></span>
                                        </li>
                                        <%/cFResults%>
                                    </ul>
                                </li>
                                <%/hasChildren%>
                                </#if>
            </ul>
        </button>
    </header>
    <%#hasChildren%>
    <div class="test--body collapsable">
        <ul class="step--list">
            <%#c%>
            <%#hasChildren%>
            <li>
                <section class="test--component">
                    <header class="test--header">
                        <button class="test--header-btn toggle toggle-group <%^isPassed%>on<%/isPassed%>" type="button">
                            <h5 class="test--title">
                                <span><strong class="keyword"><%k%>:</strong> <%n%></span>
                                <i class="material-icons md-18">expand_more</i>
                            </h5>
                        </button>
                    </header>
                    <div class="test--body collapsable">
                        <ul class="step--list">
                            <%#c%>
                            <%>step%>
                            <%/c%>
                        </ul>
                    </div>
                </section>
            </li>
            <%/hasChildren%>
            <%^hasChildren%><%>step%><%/hasChildren%>
            <%/c%>
        </ul>
    </div>
    <%/hasChildren%>
</section>
</li>
        </script>
    </div>

    <footer>
        <div class="container">
            <p>©2023 Made with <span class="heart">♥</span> by <a href="https://www.iti.es/" rel="noopener noreferrer"
                                                                  target="_blank">ITI</a>
                • <a href="https://github.com/iti-ict/wakamiti/tree/main/wakamiti-plugins/wakamiti-html-report"
                     rel="noopener noreferrer" target="_blank">wakamiti-html-report</a> v${plugin_version!"?.?.?"}</p>
        </div>
    </footer>


    <nav class="nav-menu--menu collapsable">
        <#if plan.testCaseResults?has_content>
            <#assign results = plan.testCaseResults?keys?map(k -> k.toString())>
            <div class="nav-menu--section">
                <div class="nav-menu--control<#if results?seq_contains("PASSED")><#else> toggle-switch--disabled</#if>">
                    <i class="material-icons icon--passed"></i>
                    <label for="passed-toggle">Show Passed
                        <input id="passed-toggle" type="checkbox" <#if results?seq_contains("PASSED")>checked<#else>disabled</#if> value="PASSED">
                        <span class="toggle-switch--toggle"></span>
                    </label>
                </div>
                <div class="nav-menu--control<#if results?seq_contains("NOT_IMPLEMENTED")><#else> toggle-switch--disabled</#if>">
                    <i class="material-icons icon--not-implemented"></i>
                    <label for="not-implemented-toggle">Show Not Implemented
                        <input id="not-implemented-toggle" type="checkbox" <#if results?seq_contains("NOT_IMPLEMENTED")>checked<#else>disabled</#if> value="NOT_IMPLEMENTED">
                        <span class="toggle-switch--toggle"></span>
                    </label>
                </div>
                <div class="nav-menu--control<#if results?seq_contains("SKIPPED")><#else> toggle-switch--disabled</#if>">
                    <i class="material-icons icon--skipped"></i>
                    <label for="skipped-toggle">Show Skipped
                        <input id="skipped-toggle" type="checkbox" <#if results?seq_contains("SKIPPED")>checked<#else>disabled</#if> value="SKIPPED">
                        <span class="toggle-switch--toggle"></span>
                    </label>
                </div>
                <div class="nav-menu--control<#if results?seq_contains("UNDEFINED")><#else> toggle-switch--disabled</#if>">
                    <i class="material-icons icon--undefined"></i>
                    <label for="undefined-toggle">Show Undefined
                        <input id="undefined-toggle" type="checkbox" <#if results?seq_contains("UNDEFINED")>checked<#else>disabled</#if> value="UNDEFINED">
                        <span class="toggle-switch--toggle"></span>
                    </label>
                </div>
                <div class="nav-menu--control<#if results?seq_contains("FAILED")><#else> toggle-switch--disabled</#if>">
                    <i class="material-icons icon--failed"></i>
                    <label for="failed-toggle">Show Failed
                        <input id="failed-toggle" type="checkbox" <#if results?seq_contains("FAILED")>checked<#else>disabled</#if> value="FAILED">
                        <span class="toggle-switch--toggle"></span>
                    </label>
                </div>
                <div class="nav-menu--control<#if results?seq_contains("ERROR")><#else> toggle-switch--disabled</#if>">
                    <i class="material-icons icon--error"></i>
                    <label for="error-toggle">Show Error
                        <input id="error-toggle" type="checkbox" <#if results?seq_contains("ERROR")>checked<#else>disabled</#if> value="ERROR">
                        <span class="toggle-switch--toggle"></span>
                    </label>
                </div>
            </div>
        </#if>
        <div class="nav-menu--section">
            <label for="search-input">
                <input id="search-input" type="search" name="search-input" placeholder="Search text" autocomplete="off" />
            </label>
        </div>

        <div class="nav-menu--section">
            <ul class="mustache" data-template="feature_menu">
                <li class="loader">Loading...</li>
                <li class="empty">No data</li>
            </ul>
            <script id="feature_menu" class="menu" type="x-tmpl-mustache">
                <li>
                    <%>a_menu%>
                    <div>
                        <ul class="nav-menu--sub">
                            <%>aggregator_menu%>
                        </ul>
                    </div>
                </li>
            </script>
            <script id="aggregator_menu" type="x-tmpl-mustache">
                <%#c%>
                    <%#isAggregator%><%>aggregator_menu%><%/isAggregator%>
                    <%^isAggregator%><%>scenario_menu%><%/isAggregator%>
                <%/c%>
            </script>
            <script id="scenario_menu" type="x-tmpl-mustache">
                <li data-target="<#if plan.testCaseResults?has_content><%#r%><%toHTML%><%/r%>-</#if>toggle">
                    <%>a_menu%>
                </li>
            </script>
            <script id="a_menu" type="x-tmpl-mustache">
                <a href="#<%i%>">
                    <span class="list-style<#if plan.testCaseResults?has_content> icon--<%#r%><%toHTML%><%/r%></#if>"></span>
                    <span title="<%n%>"><%n%></span>
                </a>
            </script>
        </div>
    </nav>
</div>
</body>
</html>