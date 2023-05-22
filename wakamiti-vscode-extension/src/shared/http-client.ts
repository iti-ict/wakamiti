/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import * as http from 'http';



var authorization: string | undefined = undefined;

export function setAuthorization(newAuthorization: string | undefined) {
    authorization = newAuthorization;
}


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
        let headers: http.OutgoingHttpHeaders = { };
        if (authorization) {
            headers['Authorization'] = authorization;
        }
        const request = http.request(
            {
                method: method,
                host: host,
                port: port,
                path: `/${path}`,
                headers: headers
            },
            response => {
                response.on('data', chunk => { data += chunk } );
                response.on('end', () => { 
                    const json = parse(response.headers['content-type'], data);
                    console.log('HTTP RESPONSE:\n', json, data);
                    resolve(json);                   
                 });
                response.on('error', error => {
                    console.error(error);
                    reject(error);
                 });
            }
        );
        if (body) {
            if (typeof body === 'string') {
                request.write(body);
            } else {
                request.write(JSON.stringify(body));
            }
        }
        request.end();        
    });
}


function parse(contentType: string | undefined, data: string): any {
    if (contentType?.startsWith('application/json')) {
        return JSON.parse(data);
    } else {
        return data;
    }
}