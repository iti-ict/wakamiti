import * as http from 'http';



export function post<T>(host: string, port: number, path: string, body: any | null = null): Promise<T> {
    return send<T>('POST', host, port, path, body);
}

export function get<T>(host: string, port: number, path: string): Promise<T> {
    return send<T>('GET', host, port, path);
}
 

function send<T>(
    method: string, 
    host: string, 
    port: number, 
    path: string, 
    body: any | null = null
): Promise<T> {
  
    return new Promise((resolve, reject) => {
        console.log(`${method} ${host}:${port}/${path}`);
        let data = '';
        const request = http.request(
            {
                method: method,
                host: host,
                port: port,
                path: `/${path}`
            },
            response => {
                response.on('data', chunk => { data += chunk } );
                response.on('end', () => { 
                    const json = JSON.parse(data);
                    console.log('response', json, data);
                    resolve(json);                   
                 });
                response.on('error', error => {
                    console.error(error);
                    reject(error);
                 });
            }
        );
        if (body) {
            if (typeof body == 'string') {
                request.write(body);
            } else {
                request.write(JSON.stringify(body))
            }
        }
        request.end();        
    });
}
        
