# wakamiti-doc
Wakamiti Documentation
 
### Test source
```
docker run --rm -it -e "CI_PAGES_URL=http://localhost:63342/temp/wakamiti-doc/dist" -v "%cd%:/app" -w /app node:12 bash -c "npm i && npm run build"
```