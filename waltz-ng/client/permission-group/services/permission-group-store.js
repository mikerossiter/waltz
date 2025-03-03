/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */


function store($http, baseApiUrl) {
    const baseUrl = `${baseApiUrl}/permission-group`;

    const findByEntity = (ref) => $http
        .get(`${baseUrl}/entity-ref/${ref.kind}/${ref.id}`)
        .then(d => d.data);

    const findForEntityRefAndSubjectKind = (ref, subjectKind) => $http
        .get(`${baseUrl}/entity-ref/${ref.kind}/${ref.id}/subject-kind/${subjectKind}`)
        .then(d => d.data);

    return {
        findByEntity,
        findForEntityRefAndSubjectKind
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


const serviceName = "PermissionGroupStore";

export default {
    serviceName,
    store
};

export const PermissionGroupStore_API = {
    findByEntity: {
        serviceName,
        serviceFnName: "findByEntity",
        description: "findByEntity [ref]"
    },
    findForEntityRefAndSubjectKind: {
        serviceName,
        serviceFnName: "findForEntityRefAndSubjectKind",
        description: "findForEntityRefAndSubjectKind [ref, subjectKind]"
    }
};