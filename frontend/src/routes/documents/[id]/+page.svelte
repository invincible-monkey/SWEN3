<script lang="ts">
	import { goto } from '$app/navigation';
	import { updateDocument, deleteDocument } from '$lib/api';
	import type { PageData } from './$types';
	import * as Button from '$lib/components/ui/button/index.js';
	import * as Input from '$lib/components/ui/input/index.js';
	import * as Textarea from '$lib/components/ui/textarea/index.js';
	import * as Card from '$lib/components/ui/card/index.js';

	let { data }: { data: PageData } = $props();

	let isEditing = $state(false);
	let editableTitle = $state(data.document?.title ?? '');
	let editableContent = $state(data.document?.content ?? '');
	let error = $state<string | null>(null);

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
			data.document = await updateDocument(data.document.id, editableTitle, editableContent);
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
					<Card.Title class="text-2xl">Edit Document</Card.Title>
				{:else}
					<Card.Title class="text-2xl">{data.document.title}</Card.Title>
					<Card.Description>
						Created on: {new Date(data.document.createdDate).toLocaleDateString()}
					</Card.Description>
				{/if}
			</Card.Header>
			<Card.Content>
				{#if isEditing}
					<form onsubmit={handleUpdate} class="space-y-4">
						<div>
							<Input.Root bind:value={editableTitle} class="text-2xl font-bold" />
						</div>
						<div>
							<Textarea.Root
								bind:value={editableContent}
								rows={15}
								placeholder="Document content..."
							/>
						</div>
						<div class="flex gap-2">
							<Button.Root type="button" variant="outline" onclick={() => (isEditing = false)}>
								Cancel
							</Button.Root>
							<Button.Root type="submit">Save Changes</Button.Root>
						</div>
					</form>
				{:else}
					<div>
						<h3 class="font-semibold mb-2">Content</h3>
						<p class="p-4 bg-muted rounded-md min-h-48 whitespace-pre-wrap">
							{data.document.content ?? 'No content extracted yet.'}
						</p>
					</div>
				{/if}
			</Card.Content>
			<Card.Footer class="flex gap-2">
				{#if !isEditing}
					<Button.Root variant="destructive" onclick={handleDelete}>Delete</Button.Root>
					<Button.Root onclick={() => (isEditing = true)}>Edit</Button.Root>
				{/if}
			</Card.Footer>
		</Card.Root>
	{:else}
		<p class="text-destructive">{data.error ?? 'Document not found.'}</p>
	{/if}

	{#if error}
		<p class="text-destructive mt-4">{error}</p>
	{/if}
</main>