/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
import * as path from 'path';
import { PlanNodeSnapshot } from '../model/PlanNodeSnapshot';

export namespace images {

    export const passed = (filename: string) => path.join(filename, '..', '..', '..', 'resources', 'passed.svg');
    export const error = (filename: string) => path.join(filename, '..', '..', '..', 'resources', 'error.svg');
    export const pending = (filename: string) => path.join(filename, '..', '..', '..', 'resources', 'pending.svg');


    export const plan = (filename: string) => image(filename,'plan');



    export const iconByNodeType = (node: PlanNodeSnapshot, filename: string) => {
        switch (node.nodeType) {
            case 'AGGREGATOR': return image(filename,'aggregator');
            case 'STEP_AGGREGATOR': return image(filename,'step-aggregator');
            case 'TEST_CASE' : return image(filename, 'test-case');
            case 'STEP': return image(filename,'step');
            case 'VIRTUAL_STEP': return image(filename, 'virtual-step');
            default: return undefined;
        }
    };


    export const iconByNodeTypeAndResult = (node: PlanNodeSnapshot | undefined, filename: string) => {
        if (node?.result) {
            switch (node.nodeType) {
                case 'AGGREGATOR': return image(filename,'aggregator', node.result.toLowerCase());
                case 'STEP_AGGREGATOR': return image(filename,'step-aggregator', node.result.toLowerCase());
                case 'TEST_CASE' : return image(filename, 'test-case', node.result.toLowerCase());
                case 'STEP': return image(filename,'step', node.result.toLowerCase());
                case 'VIRTUAL_STEP': return image(filename, 'virtual-step');
                default: return '';
            }
        } else {
            return image(filename, 'pending');
        }
    };


} 

export function image(filename: string, file: string, variant: string | undefined = undefined) {
    if (variant) {
        return {
            light: path.join(filename, '..', '..', '..', 'resources', 'light', `${file}_${variant}.svg`),
            dark:  path.join(filename, '..', '..', '..', 'resources', 'dark', `${file}_${variant}.svg`)
        };
    } else {
        return {
            light: path.join(filename, '..', '..', '..', 'resources', 'light', `${file}.svg`),
            dark:  path.join(filename, '..', '..', '..', 'resources', 'dark', `${file}.svg`)
        };
    }
}