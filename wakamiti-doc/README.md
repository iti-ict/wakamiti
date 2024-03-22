# wakamiti-doc
Wakamiti Documentation
 
### Test source
```
docker run --rm -it -p "8080:8080" -e "CI_PAGES_URL=http://localhost:8080/" -v "%cd%:/app" -w /app node:12 bash -c "npm i && npm install http-server -g && npm run build && npx http-server --cors -p8080 dist"
```

> NOTA: \
> Si está trabajando sobre un disco externo es posible que no funcione