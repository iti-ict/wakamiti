open module ${package} {

    exports ${package};

    requires es.iti.wakamiti.api;

    provides es.iti.wakamiti.api.extensions.StepContributor with ${package}.${PluginId}StepContributor;
    provides es.iti.wakamiti.api.extensions.ConfigContributor with ${package}.${PluginId}ConfigContributor;

}