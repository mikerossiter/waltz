<script>

    import Grid from "../../../../common/svelte/Grid.svelte";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {surveyTemplateStore} from "../../../../svelte-stores/survey-template-store";
    import {surveyQuestionStore} from "../../../../svelte-stores/survey-question-store";
    import _ from "lodash";

    export let onSelect = () => console.log("Selecting involvement kind");
    export let selectionFilter = () => true;

    let selectedTemplate = null;

    $: templatesCall = surveyTemplateStore.findAll();

    $: templates = _
        .chain($templatesCall?.data)
        .filter(selectionFilter)
        .orderBy(d => d.name)
        .value();

    $: questionsCall = selectedTemplate && surveyQuestionStore.findQuestionsForTemplate(selectedTemplate?.id)
    $: questions = $questionsCall?.data || [];

    $: rowData = _
        .chain(questions)
        .map(d => Object.assign(
            {},
            d,
            {
                columnEntityId: d.id,
                columnEntityKind: d.kind,
                entityFieldReference: null,
                columnName: d.questionText,
                displayName: null
            }))
        .filter(selectionFilter)
        .value()

    const columnDefs = [
        {field: "questionText", name: "Question", width: "40%"},
        {field: "label", name: "Label", width: "40%"},
        {field: "fieldType", name: "Type", width: "20%"},
    ];

    const templateColumnDefs = [
        {field: "name", name: "Survey Name", width: "40%"},
        {field: "description", name: "Description", width: "60%", maxLength: 300},
    ];

    function selectTemplate(template) {
        selectedTemplate = template;
    }

    function clearSelectedTemplate() {
        selectedTemplate = null;
    }

</script>

{#if selectedTemplate}
    <div class="help-block small">
        <Icon name="info-circle"/>Select a question from the list below, you can filter the list using the search bar or
        <button on:click={clearSelectedTemplate}
                class="btn-skinny">
            choose a different template
        </button>.
    </div>
    <p>Questions for template: <strong>{selectedTemplate.name}</strong></p>
    <Grid {columnDefs}
          {rowData}
          onSelectRow={onSelect}/>
{:else}
    <div class="help-block small">
        <Icon name="info-circle"/>Select a template from the list below, you can filter the list using the search bar.
    </div>
    <br>
    <Grid columnDefs={templateColumnDefs}
          rowData={templates}
          onSelectRow={selectTemplate}/>
{/if}
