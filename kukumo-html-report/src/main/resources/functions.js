function switchVisibility(ids,displayValues) {
   for (i in ids) {
       console.log('switch visibility for id ',ids[i]);
       let e = document.getElementById(ids[i]);
       if (e) {
         if( e.style.display === 'none' ) {
           e.style.display = displayValues[i];
         } else {
           e.style.display = 'none';
         }
       }
   }
}