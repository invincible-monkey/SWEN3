<script lang="ts">
    import { goto } from '$app/navigation';
    import { updateDocument, deleteDocument, addTag, removeTag } from '$lib/api';
    import type { PageData } from './$types';
    import * as Button from '$lib/components/ui/button/index.js';
    import * as Input from '$lib/components/ui/input/index.js';
    import * as Card from '$lib/components/ui/card/index.js';
    import * as Label from '$lib/components/ui/label/index.js';
    import { Badge } from '$lib/components/ui/badge/index.js';

    let { data }: { data: PageData } = $props();
    let doc = $state(data.document);

    let isEditing = $state(false);
    let editableTitle = $state(doc?.title ?? '');
    let error = $state<string | null>(null);
    let newTagName = $state('');

    function formatFileSize(bytes: number) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    async function handleDelete() {
        if (!doc) return;
        if (confirm('Are you sure you want to delete this document?')) {
            try {
                await deleteDocument(doc.id);
                goto('/');
            } catch (e: any) {
                error = e.message;
            }
        }
    }

    async function handleUpdate(event: SubmitEvent) {
        event.preventDefault();
        if (!doc) return;
        try {
            doc = await updateDocument(
                doc.id, 
                editableTitle, 
                doc.content ?? '' 
            );
            isEditing = false;
        } catch (e: any) {
            error = e.message;
        }
    }

    async function handleAddTag() {
        if (!newTagName.trim() || !doc) return;
        try {
            doc = await addTag(doc.id, newTagName);
            newTagName = '';
        } catch (e: any) {
            error = e.message;
        }
    }

    async function handleRemoveTag(tagId: number) {
        if (!doc) return;
        try {
            doc = await removeTag(doc.id, tagId);
        } catch (e: any) {
            error = e.message;
        }
    }
</script>

<main class="container mx-auto p-4 md:p-8">
    <Button.Root href="/" variant="outline" class="mb-8">&larr; Back to Dashboard</Button.Root>

    {#if doc}
        <Card.Root>
            <Card.Header>
                {#if isEditing}
                    <form onsubmit={handleUpdate} class="space-y-4">
                        <Label.Root for="title-edit">Edit Title</Label.Root>
                        <Input.Root id="title-edit" bind:value={editableTitle} class="text-2xl font-bold" />
                        <div class="flex gap-2">
                            <Button.Root type="button" variant="outline" onclick={() => (isEditing = false)}>
                                Cancel
                            </Button.Root>
                            <Button.Root type="submit">Save Title</Button.Root>
                        </div>
                    </form>
                {:else}
                    <Card.Title class="text-2xl">{doc.title}</Card.Title>
                    <Card.Description>
                        Created: {new Date(doc.createdDate).toLocaleDateString()}
                        <br />
                        Size: {formatFileSize(doc.fileSize)}
                    </Card.Description>
                {/if}
            </Card.Header>
            <Card.Content class="space-y-6">
                
                <div>
                    <h3 class="font-semibold mb-2">Tags</h3>
                    <div class="flex flex-wrap gap-2 mb-2">
                        {#if doc.tags && doc.tags.length > 0}
                            {#each doc.tags as tag}
                                <Badge variant="secondary" class="flex items-center gap-1 pr-1 pl-2.5 py-1">
                                    #{tag.name}
                                    <button 
                                        class="ml-1 hover:bg-destructive/20 hover:text-destructive rounded-full p-0.5 transition-colors"
                                        onclick={() => handleRemoveTag(tag.id)}
                                        title="Remove tag"
                                        aria-label="Remove tag"
                                    >
                                        <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M5 12h14"/></svg>
                                    </button>
                                </Badge>
                            {/each}
                        {:else}
                            <span class="text-sm text-muted-foreground">No tags yet.</span>
                        {/if}
                    </div>
                    
                    <div class="flex gap-2 max-w-sm items-center">
                        <Input.Root 
                            placeholder="Add a tag..." 
                            bind:value={newTagName} 
                            onkeydown={(e) => e.key === 'Enter' && handleAddTag()}
                            class="h-8"
                        />
                        <Button.Root variant="outline" size="sm" onclick={handleAddTag} class="h-8 px-3">
                            +
                        </Button.Root>
                    </div>
                </div>

                <div>
                    <h3 class="font-semibold mb-2">Summary</h3>
                    <p class="p-4 bg-muted rounded-md min-h-48 whitespace-pre-wrap">
                        {#if doc.status === 'PROCESSING'}
                            <em>Generating summary... please refresh in a moment.</em>
                        {:else if doc.status === 'FAILED'}
                            <em class="text-destructive">Failed to generate summary.</em>
                        {:else}
                            {doc.summary ?? 'No summary generated.'}
                        {/if}
                    </p>
                </div>
            </Card.Content>
            <Card.Footer class="flex justify-between">
                <div class="flex gap-2">
                    <Button.Root variant="destructive" onclick={handleDelete}>Delete Document</Button.Root>
                    <Button.Root onclick={() => (isEditing = true)} disabled={isEditing}>Edit Title</Button.Root>
                </div>
                <Button.Root href={data.downloadUrl} download>Download Original</Button.Root>
            </Card.Footer>
        </Card.Root>
    {:else}
        <p class="text-destructive">{data.error ?? 'Document not found.'}</p>
    {/if}

    {#if error}
        <p class="text-destructive mt-4">{error}</p>
    {/if}
</main>