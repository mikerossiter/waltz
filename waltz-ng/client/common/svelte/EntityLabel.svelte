<script>
    import EntityIcon from "./EntityIcon.svelte";

    export let ref;
    export let showIcon = true;

    const nameMap = {
        SERVER: "hostname",
        DATABASE: "databaseName"
    };

    $: name = _.get(ref, [nameMap[ref?.kind] || "name"], "unknown");

</script>

{#if showIcon}
    <EntityIcon kind={ref?.kind}/>
{/if}
<span class:removed={ref?.entityLifecycleStatus === 'REMOVED' || ref?.isRemoved}>
    {name}
</span>

<style>

    .removed {
        color: #999;
        text-decoration: line-through;
    }

</style>
