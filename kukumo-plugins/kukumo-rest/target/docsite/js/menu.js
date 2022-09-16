function expandOrCollapse(event,expandable) {
    let ul = expandable.getElementsByTagName('ul')[0];
    ul.classList.toggle('visible');
    ul.classList.toggle('hidden');
    expandable.classList.toggle('collapsed');
    expandable.classList.toggle('expanded');
    event.stopPropagation();
}

function expandOrCollapseExpandedMenu(event,expandable) {
    let ul = expandable.getElementsByTagName('ul')[0];
    let hiddenBefore = ul.classList.contains('hidden');
    collapseAllMenus(expandable);
    if (hiddenBefore) {
        ul.classList.toggle('visible');
        ul.classList.toggle('hidden');
        expandable.classList.toggle('collapsed');
        expandable.classList.toggle('expanded');
    }
    event.stopPropagation();
}


function collapseAllMenus(expandable) {
    let mainUl = window.expandedMenu.childNodes[0];
    for (let mainIl of mainUl.childNodes) {
      for (let mainIlChild of mainIl.childNodes) {
        if (mainIlChild.tagName === 'UL') {
          mainIlChild.classList.remove('visible');
          mainIlChild.classList.add('hidden');
          mainIl.classList.remove('expanded');
          mainIl.classList.add('collapsed');
          break;
        }
      }
    }
}



function showOrHideMenu(event,button) {
    button.classList.toggle('selected');
    let menu = window.burgerMenu;
    menu.classList.toggle('visible');
    menu.classList.toggle('hidden');
}

function showOrHideToc(event,button) {
    button.classList.toggle('selected');
    let toc = document.getElementsByTagName('aside')[0];
    toc.classList.toggle('visible');
    toc.classList.toggle('hidden');
}


function hideTocIfVisible(event,emitter) {
    let toc = document.getElementsByTagName('aside')[0];
    if (toc.classList.contains('visible')) {
        toc.classList.toggle('visible');
        toc.classList.toggle('hidden');
    }
}


function resizeExpandedMenu(elements) {
  let reducedLayout = window.getComputedStyle(window.document.body).getPropertyValue('--reduced-layout').trim();
  if (reducedLayout === 'true') {
    window.expandedMenu.style.display = 'none';
    window.menuButton.classList.add('visible');
    window.menuButton.classList.remove('hidden');
    return;
  }
  let isDisplayed = (window.expandedMenu.style.display != 'none');
  if (isDisplayed) {
    let hasOverflow = window.expandedMenu.scrollWidth > window.expandedMenu.clientWidth;
    if (hasOverflow) {
        window.expandedMenu.style.display = 'none';
        window.menuButton.classList.add('visible');
        window.menuButton.classList.remove('hidden')
    } else {
        window.menuButton.classList.remove('visible');
        window.menuButton.classList.add('hidden')
    }
  } else {
    window.expandedMenu.style.visibility = 'hidden';
    window.expandedMenu.style.display = 'block';
    let hasOverflow = window.expandedMenu.scrollWidth > window.expandedMenu.clientWidth;
    if (hasOverflow) {
       window.expandedMenu.style.display = 'none';
       window.menuButton.classList.add('visible');
       window.menuButton.classList.remove('hidden')
    } else {
       window.expandedMenu.style.visibility = 'visible';
       window.menuButton.classList.remove('visible');
       window.menuButton.classList.add('hidden');
       window.burgerMenu.classList.remove('visible');
       window.burgerMenu.classList.add('hidden')
    }
  }

}



window.addEventListener('load', ()=> {
  mermaid.initialize({ startOnLoad: true });
  window.expandedMenu = document.getElementsByClassName('expanded-menu')[0];
  window.menuButton = document.getElementsByClassName('menu-button')[0];
  window.burgerMenu = document.getElementsByClassName('burger-menu')[0];
  new ResizeObserver(resizeExpandedMenu).observe(window.document.body);
});




