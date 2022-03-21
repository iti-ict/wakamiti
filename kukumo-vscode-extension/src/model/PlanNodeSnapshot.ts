/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
export class PlanNodeSnapshot {

    executionID: string | undefined;
    snapshotInstant: string | undefined;
    nodeType: string | undefined;
    id: string | undefined;
    name: string | undefined;
    keyword: string | undefined;
    language: string | undefined;
    source: string | undefined;
    displayName: string | undefined;
    description: string[] | undefined;
    tags: string[] | undefined;
    properties: { [key: string]: string } | undefined;
    startInstant: string | undefined;
    finishInstant: string | undefined;
    duration: number | undefined;
    document: string | undefined;
    documentType: string | undefined;
    dataTable: string[][] | undefined;
    errorMessage: string | undefined;
    errorTrace: string | undefined;
    result: string | undefined;
    testCaseResults: { [result: string]: number } | undefined;
    children: PlanNodeSnapshot[] | undefined;

}