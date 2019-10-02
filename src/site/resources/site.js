$(document).ready(function () {
    $(".source").addClass("prettyprint");
    prettyPrint();
    var linkAbout = $('a').filter(function(index) { return $(this).text() === "About"; });
    var projectInformationMenu = $('a').filter(function(index) { return $(this).text() === "Project Information"; });
    linkAbout.hide();
    if (!projectInformationMenu.parent().hasClass('active')) {
        projectInformationMenu.parent().children('ul').hide();
        projectInformationMenu.children('span').removeClass('icon-chevron-down').addClass('icon-chevron-right');
    }
    $('#banner').hide();
});