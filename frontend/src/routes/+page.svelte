<script lang="ts">
    import { onMount } from 'svelte';
    import type { DocumentDto } from '$lib/api';
    import { getDocuments, uploadDocument, searchDocuments } from '$lib/api'; 
    import * as Card from '$lib/components/ui/card/index.js';
    import { Button, buttonVariants } from '$lib/components/ui/button/index.js';
    import * as Input from '$lib/components/ui/input/index.js';
    import * as Label from '$lib/components/ui/label/index.js';

    let documents = $state<DocumentDto[]>([]);
    let error = $state<string | null>(null);
    let isLoading = $state(true);

    // State for the upload form
    let title = $state('');
    let selectedFile = $state<File | null>(null);
    let isUploading = $state(false);

    // Search State
    let searchQuery = $state('');
    let isSearching = $state(false);

    onMount(async () => {
        try {
            documents = await getDocuments();
        } catch (e: any) {
            error = e.message;
        } finally {
            isLoading = false;
        }
    });

    // Handle Search Logic
    async function handleSearch() {
        isLoading = true;
        isSearching = true;
        error = null;
        try {
            if (!searchQuery.trim()) {
                // If search is empty, reload the full list
                documents = await getDocuments();
            } else {
                // Otherwise, search via Elasticsearch
                documents = await searchDocuments(searchQuery);
            }
        } catch (e: any) {
            error = e.message;
        } finally {
            isLoading = false;
            isSearching = false;
        }
    }

    function onKeyDown(e: KeyboardEvent) {
        if (e.key === 'Enter') handleSearch();
    }

    function handleFileChange(e: Event) {
        const target = e.currentTarget as HTMLInputElement;
        if (target.files && target.files.length > 0) {
            selectedFile = target.files[0];
        }
    }

    async function handleSubmit(event: SubmitEvent) {
        event.preventDefault();
        if (!selectedFile || !title) {
            error = 'Please provide a title and select a file.';
            return;
        }
        isUploading = true;
        error = null;

        try {
            const newDocument = await uploadDocument(title, selectedFile);
            // Add the new document to the list immediately
            documents.push(newDocument);

            // Reset form
            title = '';
            selectedFile = null;
            const fileInput = document.getElementById('file-input') as HTMLInputElement;
            if (fileInput) fileInput.value = '';
        } catch (e: any) {
            error = e.message;
        } finally {
            isUploading = false;
        }
    }
</script>

<main class="container mx-auto p-4 md:p-8">
    <h1 class="text-4xl font-bold mb-8">Paperless</h1>

    <Card.Root class="mb-8">
        <Card.Header>
            <Card.Title>Upload New Document</Card.Title>
        </Card.Header>
        <Card.Content>
            <form onsubmit={handleSubmit} class="space-y-4">
                <div class="grid w-full max-w-sm items-center gap-1.5">
                    <Label.Root for="title">Document Title</Label.Root>
                    <Input.Root
                        id="title"
                        type="text"
                        placeholder="e.g., Invoice from March"
                        bind:value={title}
                        required
                    />
                </div>
                <div class="grid w-full max-w-sm items-center gap-1.5">
                    <Label.Root>PDF File</Label.Root>
                    <div class="flex items-center gap-4">
                        <Input.Root
                            id="file-input"
                            type="file"
                            accept=".pdf"
                            onchange={handleFileChange}
                            required
                            class="sr-only"
                        />
                        <Label.Root for="file-input" class={buttonVariants({ variant: 'outline' })}>
                            Browse...
                        </Label.Root>
                        <span class="text-sm text-muted-foreground">
                            {#if selectedFile}
                                {selectedFile.name}
                            {:else}
                                No file selected
                            {/if}
                        </span>
                    </div>
                </div>
                <Button type="submit" disabled={isUploading}>
                    {#if isUploading}
                        Uploading...
                    {:else}
                        Upload
                    {/if}
                </Button>
            </form>
        </Card.Content>
    </Card.Root>

    <div class="flex items-center justify-between mt-12 mb-4">
        <h2 class="text-2xl font-bold">Documents</h2>
    </div>
    
    <div class="flex gap-2 mb-6 max-w-md">
        <Input.Root 
            placeholder="Search documents..." 
            bind:value={searchQuery} 
            onkeydown={onKeyDown}
        />
        <Button variant="secondary" onclick={handleSearch} disabled={isSearching}>
            {#if isSearching}
                ...
            {:else}
                Search
            {/if}
        </Button>
    </div>

    <Card.Root>
        <Card.Content class="pt-6">
            {#if isLoading}
                <p class="text-muted-foreground">Loading documents...</p>
            {:else if error}
                <p class="text-destructive">{error}</p>
            {:else if documents.length === 0}
                <p class="text-muted-foreground">No documents found.</p>
            {:else}
                <ul class="space-y-2">
                    {#each documents as doc (doc.id)}
                        <li class="border-b border-border last:border-0 py-2">
                            <div class="flex justify-between items-center">
                                <div>
                                    <a href="/documents/{doc.id}" class="text-lg hover:underline font-medium">
                                        {doc.title}
                                    </a>
                                    <p class="text-sm text-muted-foreground">
                                        Created on: {new Date(doc.createdDate).toLocaleDateString()}
                                    </p>
                                </div>
                            </div>
                        </li>
                    {/each}
                </ul>
            {/if}
        </Card.Content>
    </Card.Root>
</main>