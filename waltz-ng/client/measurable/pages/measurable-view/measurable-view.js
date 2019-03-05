/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";
import {initialiseData} from "../../../common";

import template from "./measurable-view.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import {toEntityRef} from "../../../common/entity-utils";


const initialState = {
    sections: [],
    availableSections: []
};


function logHistory(measurable, historyStore) {
    return historyStore
        .put(measurable.name,
            "MEASURABLE",
            "main.measurable.view",
            { id: measurable.id });
}


function controller($q,
                    $stateParams,
                    dynamicSectionManager,
                    serviceBroker,
                    historyStore) {

    const id = $stateParams.id;
    const ref = { id, kind: "MEASURABLE" };
    const childrenSelector = { entityReference: ref, scope: "CHILDREN" };

    const vm = initialiseData(this, initialState);
    vm.entityReference = ref;
    vm.selector = childrenSelector;
    vm.scope = childrenSelector.scope;


    // -- BOOT ---

    vm.$onInit = () => {
        vm.availableSections = dynamicSectionManager.findAvailableSectionsForKind("MEASURABLE");
        vm.sections = dynamicSectionManager.findUserSectionsForKind("MEASURABLE");

        serviceBroker
            .loadViewData(CORE_API.MeasurableStore.getById, [ id ])
            .then(r => {
                vm.measurable = r.data;
                vm.entityReference = toEntityRef(vm.measurable);
            })
            .then(() => serviceBroker
                    .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
                    .then(r => vm.measurableCategory = _.find(r.data, { id: vm.measurable.categoryId })))
            .then(() => logHistory(vm.measurable, historyStore));
    };

    // -- DYNAMIC SECTIONS

    vm.addSection = s => vm.sections = dynamicSectionManager.openSection(s, "MEASURABLE");
    vm.removeSection = (section) => vm.sections = dynamicSectionManager.removeSection(section, "MEASURABLE");
}


controller.$inject = [
    "$q",
    "$stateParams",
    "DynamicSectionManager",
    "ServiceBroker",
    "HistoryStore"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};
