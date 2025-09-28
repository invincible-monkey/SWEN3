<script lang="ts">
	import { goto } from '$app/navigation';
	import { updateDocument, deleteDocument } from '$lib/api';
	import type { PageData } from './$types';

	let { data }: { data: PageData } = $props();

	// Initialize state only if data.document exists
	let isEditing = $state(false);
	let editableTitle = $state(data.document?.title ?? '');
	let editableContent = $state(data.document?.content ?? '');
	let error = $state<string | null>(null);

	async function handleDelete() {
		// Guard clause to ensure doc exists before acting
		if (!data.document) return;

		if (confirm('Are you sure you want to delete this document?')) {
			try {
				await deleteDocument(data.document.id);
				goto('/'); // Navigate back to the homepage
			} catch (e: any) {
				error = e.message;
			}
		}
	}

	async function handleUpdate(event: SubmitEvent) {
		event.preventDefault();
		if (!data.document) return; // Guard clause

		try {
			data.document = await updateDocument(data.document.id, editableTitle, editableContent);
			isEditing = false;
		} catch (e: any) {
			error = e.message;
		}
	}
</script>

<main class="container">
	<a href="/">&larr; Back to Dashboard</a>

	{#if data.document}
		{#if isEditing}
			<form onsubmit={handleUpdate}>
				<input bind:value={editableTitle} />
				<textarea bind:value={editableContent} rows="10"></textarea>
				<div class="actions">
					<button type="button" onclick={() => (isEditing = false)}>Cancel</button>
					<button type="submit">Save</button>
				</div>
			</form>
		{:else}
			<h1>{data.document.title}</h1>
			<p class="content">{data.document.content ?? 'No content extracted yet.'}</p>
			<div class="actions">
				<button onclick={handleDelete}>Delete</button>
				<button onclick={() => (isEditing = true)}>Edit</button>
			</div>
		{/if}
	{:else}
		<p class="error">{data.error ?? 'Document not found.'}</p>
	{/if}

	{#if error}
		<p class="error">{error}</p>
	{/if}
</main>

<style>
	.container { max-width: 800px; margin: 2rem auto; font-family: sans-serif; }
	.actions { margin-top: 1rem; display: flex; gap: 0.5rem; }
    .content { white-space: pre-wrap; background: #f4f4f4; padding: 1rem; border-radius: 4px; }
	input, textarea { width: 100%; padding: 0.5rem; font-size: 1rem; margin-bottom: 1rem; }
    .error { color: red; }
</style>