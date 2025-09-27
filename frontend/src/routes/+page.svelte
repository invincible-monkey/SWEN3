<script lang="ts">
    import { onMount } from 'svelte';
    import type { DocumentDto } from '$lib/api';
    import { getDocuments } from '$lib/api';

    let documents = $state<DocumentDto[]>([]);
    let error = $state<string | null>(null);

    onMount(async () => {
        try {
            documents = await getDocuments();
        } catch (e: any) {
            error = e.message;
        }
    });
</script>

<main class="container">
    <h1>Paperless</h1>

    <hr />

    <h2>Documents</h2>
    {#if error}
        <p class="error">Error loading documents: {error}</p>
    {:else if documents.length === 0}
        <p>No documents found. Upload one to get started!</p>
    {:else}
        <ul>
            {#each documents as doc}
                <li>
                    <a href="/documents/{doc.id}">{doc.title}</a>
                    <span>- Created on: {new Date(doc.createdDate).toLocaleDateString()}</span>
                </li>
            {/each}
        </ul>
    {/if}
</main>

<style>
    .container {
        max-width: 800px;
        margin: 2rem auto;
        font-family: sans-serif;
    }
    .error {
        color: red;
    }
</style>