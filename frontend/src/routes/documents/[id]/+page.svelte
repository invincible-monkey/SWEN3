<script lang="ts">
	import { goto } from '$app/navigation';
	import { updateDocument, deleteDocument } from '$lib/api';
	import type { PageData } from './$types';
	import * as Button from '$lib/components/ui/button/index.js';
	import * as Input from '$lib/components/ui/input/index.js';
	import * as Card from '$lib/components/ui/card/index.js';
	import * as Label from '$lib/components/ui/label/index.js';

	let { data }: { data: PageData } = $props();

	let isEditing = $state(false);
	let editableTitle = $state(data.document?.title ?? '');
	let error = $state<string | null>(null);

	function formatFileSize(bytes: number) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

	async function handleDelete() {
		if (!data.document) return;
		if (confirm('Are you sure you want to delete this document?')) {
			try {
				await deleteDocument(data.document.id);
				goto('/');
			} catch (e: any) {
				error = e.message;
			}
		}
	}

	async function handleUpdate(event: SubmitEvent) {
        event.preventDefault();
        if (!data.document) return;
        try {
            data.document = await updateDocument(
                data.document.id, 
                editableTitle, 
                data.document.content ?? '' 
            );
            isEditing = false;
        } catch (e: any) {
            error = e.message;
        }
    }
</script>

<main class="container mx-auto p-4 md:p-8">
    <Button.Root href="/" variant="outline" class="mb-8">&larr; Back to Dashboard</Button.Root>

    {#if data.document}
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
                    <Card.Title class="text-2xl">{data.document.title}</Card.Title>
                    <Card.Description>
                        Created: {new Date(data.document.createdDate).toLocaleDateString()}
                        <br />
                        Size: {formatFileSize(data.document.fileSize)}
                    </Card.Description>
                {/if}
            </Card.Header>
            <Card.Content>
                <div>
                    <h3 class="font-semibold mb-2">Summary</h3>
                    <p class="p-4 bg-muted rounded-md min-h-48 whitespace-pre-wrap">
                        {#if data.document.status === 'PROCESSING'}
                            <em>Generating summary... please refresh in a moment.</em>
                        {:else if data.document.status === 'FAILED'}
                            <em class="text-destructive">Failed to generate summary.</em>
                        {:else}
                            {data.document.summary ?? 'No summary generated.'}
                        {/if}
                    </p>
                </div>
            </Card.Content>
            <Card.Footer class="flex justify-between">
                <div class="flex gap-2">
                    <Button.Root variant="destructive" onclick={handleDelete}>Delete</Button.Root>
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