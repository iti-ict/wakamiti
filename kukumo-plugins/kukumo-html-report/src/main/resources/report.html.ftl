<!doctype html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-EVSTQN3/azprG1Anm3QDgpJLIm9Nao0Yz1ztcQTwFspd3yD65VohhpuuCOmLASjC" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.5.0/font/bootstrap-icons.css">
    <script type="text/javascript" src="https://cdn.jsdelivr.net/npm/chart.js@3.5.1/dist/chart.min.js" ></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.2.0/styles/default.min.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.2.0/highlight.min.js"></script>
    <script>hljs.highlightAll();</script>

    <style type="text/css">
    ${localStyles}
    </style>

    <script>
        function switchVisibility(header) {
            var sibling = header.nextElementSibling;
            if (sibling.style.display === 'none') {
                sibling.style.display = 'block';
                header.classList.replace('collapsed','expanded');
            } else {
                sibling.style.display = 'none';
                header.classList.replace('expanded', 'collapsed');
            }
        }
    </script>


    
    
    <title>Report</title>

</head>

<body>
<div class="container">

    <header class="row">
        <hr/>
        <div class="col row">
            <div class="logo-left col-1"></div>
            <h1 class="col-10 text-center report-title">${plan.name!plan.displayName}</h1>
            <div class="logo-right col-1"></div>
        </div>
        <hr/>
        <div class="row" style="gap: 10px">
            <#if plan.testCaseResults??>
            <#list plan.testCaseResults as resultType, count>
               <div class="col numtag-${resultType} decorated">${count}</div>
            </#list>
            </#if>
        </div>
    </header>


    <content class="row">


        <div class="panel container">
            <div class="panel-header collapsed decorated"  onclick="switchVisibility(this)">
                <strong>Execution details</strong>
            </div>
            <div class="panel-body" style="display: none">
                <div>Execution ID: ${plan.executionID!'unknown'}</div>
                <div>Started at ${plan.startInstant}</div>
                <div>Finished at ${plan.startInstant}</div>
                <div>Duration: ${plan.duration} millisec</div>
            </div>
        </div>

      
<#macro resultChart resultMap identifier title>
        <div class="chart">
            <canvas id="${identifier}"></canvas>
        </div>

        <script>
            function chartConf_${identifier}() {   
                const colors =  {
                    PASSED: '#5CB85C',
                    FAILED: '#FF7B7E',
                    ERROR: '#ff0000',
                    SKIPPED: '#808080',
                    UNDEFINED: '#ffc107'
                };         
                const labels = [ 
                    <#list resultMap as resultType, count>
                    '${resultType}',
                    </#list>
                ];
                const chartColors = [
                    <#list resultMap as resultType, count>
                    colors['${resultType}'],
                    </#list>
                ];
                const data = [
                    <#list resultMap as resultType, count>
                    ${count},
                    </#list>
                ];
                const datasets = [{
                    data: data,
                    backgroundColor: chartColors,
                    hoverOffset: 4
                }];
                return {
                    type: 'doughnut',
                    data: { labels: labels, datasets: datasets },
                    options: { 
                        responsive: true, 
                        maintainAspectRatio: false,
                        plugins: {
                            title: {
                                display: true,
                                text: '${title}',
                                position: 'bottom'
                            },
                            legend: {
                                position: 'right'
                            }
                        }
                    }
                };
            }
            var ${identifier} = new Chart(document.getElementById('${identifier}'), chartConf_${identifier}());
        </script>        
</#macro>

        <div class="charts">
        <#if plan.childrenResults??>
          <@resultChart plan.childrenResults 'chartFeatureResults' 'FEATURE RESULTS' />          
        </#if>

        <#if plan.testCaseResults??>
          <@resultChart plan.testCaseResults 'chartTestCaseResults' 'TEST CASE RESULTS' />          
        </#if>
        </div>

        



        <div class="results container row">

            <#macro nodePanel node depth>
            <div class="col panel nodeType-${node.nodeType}" style="margin-left:${depth*10}px">
            <#-- node with children, shows a collapsible header and a body (hidden by default)  -->
                <#if node.children??>
                <div class="panel-header <#if node.result == "PASSED">collapsed<#else>expanded</#if> decorated"  onclick="switchVisibility(this)">
                    <div class="panel-header-left">
                        <#if node.id??><span class="node-id">${node.id}</span></#if>
                        <#if node.keyword??><span class="node-keyword">${node.keyword}</span></#if>
                        <span class="node-name">${node.name!""}</span>
                    </div>
                    <div class="panel-header-center">
                    </div>
                    <div class="panel-header-right">
                        <#if node.childrenResults??>
                        <#list node.childrenResults as resultType, count>
                        <div class="numtag-${resultType} decorated">${count}</div>
                        </#list>
                        </#if>
                    </div>
                </div>
                <div class="panel-body" <#if node.result == "PASSED">style="display: none"</#if>>

                    <#if depth == 1 && node.description??>
                    <div class="panel-body-description">
                    <#list node.description as line>
                    ${line}
                    </#list>
                    </div>
                    </#if>


                    <#if node.children??>
                    <#list node.children as child>
                    <@nodePanel child depth+1/>            
                    </#list>                
                    </#if>
                </div>
                <#else>
                <#-- node without children shows a plain header  -->
                <div class="panel-header panel-header-plain" >
                    <div class="panel-header-left">
                        <#if node.keyword??><span class="node-keyword">${node.keyword}</span></#if>
                        <span class="node-name">${node.name!""}</span>
                    </div>
                    <div class="panel-header-center">
                    </div>
                    <div class="panel-header-right">
                        <div class="numtag-${node.result} decorated"></div>
                    </div>
                </div>    

                    <#if node.dataTable??>
                    <div class="panel-body">
                      <table class="panel-body-datatable">
                         <thead>
                           <tr>
                           <#list node.dataTable[0] as cell><td>${cell}</td></#list>
                           </tr>
                         </thead>
                         <tbody>
                           <#list node.dataTable as row>
                           <#if row?counter == 1><#continue></#if>
                           <tr>
                           <#list row as cell><td>${cell}</td></#list>
                           </tr>
                           </#list>
                         </tbody>
                      </table>
                      
                    </div>
                    </#if>

                    <#if node.document??>
                    <div class="panel-body">
                      <pre><code class="panel-body-document languaje-${node.documentType!}plaintext">${node.document}</code></pre>
                    </div>
                    </#if>

                </#if>

            </div>
            </#macro>

            <#if plan.children??>
            <#list plan.children as child>
            <@nodePanel child 1 />            
            <hr/>
            </#list>  
            </#if>



        </div>
    </content>

    <br/>
    <br/>

</div>

</body>
</html>