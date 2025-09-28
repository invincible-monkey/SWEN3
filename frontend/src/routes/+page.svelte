<script lang="ts">
	import { onMount } from 'svelte';
	import type { DocumentDto } from '$lib/api';
	import { getDocuments, uploadDocument } from '$lib/api';

	let documents = $state<DocumentDto[]>([]);
	let error = $state<string | null>(null);
	let isLoading = $state(true);

	// State for the upload form
	let title = $state('');
	let selectedFile = $state<File | null>(null);
	let isUploading = $state(false);

	onMount(async () => {
		try {
			documents = await getDocuments();
		} catch (e: any) {
			error = e.message;
		} finally {
			isLoading = false;
		}
	});

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
			documents.push(newDocument);
			
			title = '';
			selectedFile = null;
			const fileInput = document.getElementById('file-input') as HTMLInputElement;
			if(fileInput) fileInput.value = '';

		} catch (e: any) {
			error = e.message;
		} finally {
			isUploading = false;
		}
	}
</script>

<main class="container">
	<h1>Paperless</h1>

	<form onsubmit={handleSubmit} class="upload-form">
		<h2>Upload New Document</h2>
		<div>
			<label for="title">Document Title</label>
			<input id="title" type="text" bind:value={title} required />
		</div>
		<div>
			<label for="file-input">PDF File</label>
			<input id="file-input" type="file" accept=".pdf" onchange={handleFileChange} required />
		</div>
		<button type="submit" disabled={isUploading}>
			{#if isUploading}
				Uploading...
			{:else}
				Upload
			{/if}
		</button>
	</form>

	<hr />

	<h2>Documents</h2>
	{#if isLoading}
		<p>Loading documents...</p>
	{:else if error}
		<p class="error">{error}</p>
	{:else if documents.length === 0}
		<p>No documents found. Upload one to get started!</p>
	{:else}
		<ul>
			{#each documents as doc (doc.id)}
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
		margin-top: 1rem;
	}
	.upload-form {
		display: flex;
		flex-direction: column;
		gap: 1rem;
		padding: 1rem;
		border: 1px solid #ccc;
		border-radius: 8px;
		margin-bottom: 2rem;
	}
	.upload-form div {
		display: flex;
		flex-direction: column;
		gap: 0.5rem;
	}
	button {
		padding: 0.5rem 1rem;
		cursor: pointer;
	}
	button:disabled {
		cursor: not-allowed;
		opacity: 0.6;
	}
</style>