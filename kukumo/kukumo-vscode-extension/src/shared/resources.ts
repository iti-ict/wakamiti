import * as path from 'path';

export namespace images {

    export const passed = (filename: string) => path.join(filename, '..', '..', '..', 'resources', 'passed.svg');
    export const error = (filename: string) => path.join(filename, '..', '..', '..', 'resources', 'error.svg');
    export const pending = (filename: string) => path.join(filename, '..', '..', '..', 'resources', 'pending.svg');
}
