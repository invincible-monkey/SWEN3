export interface DocumentDto {
	id: number;
	title: string;
	content: string;
	createdDate: string;
	storagePath: string;
}

const API_BASE = '/api/documents';

export async function getDocuments(): Promise<DocumentDto[]> {
	const response = await fetch(API_BASE);
	if (!response.ok) {
		throw new Error('Failed to fetch documents');
	}
	return await response.json();
}

export async function uploadDocument(title: string, file: File): Promise<DocumentDto> {
	const formData = new FormData();
	formData.append('title', title);
	formData.append('file', file);

	const response = await fetch(API_BASE, {
		method: 'POST',
		body: formData
	});

	if (!response.ok) {
		const errorText = await response.text();
		throw new Error(`Failed to upload document: ${errorText}`);
	}
	return await response.json();
}

export async function getDocumentById(id: number): Promise<DocumentDto> {
	const response = await fetch(`${API_BASE}/${id}`);
	if (!response.ok) {
		throw new Error(`Failed to fetch document with id ${id}`);
	}
	return await response.json();
}

export async function updateDocument(id: number, title: string, content: string): Promise<DocumentDto> {
	const response = await fetch(`${API_BASE}/${id}`, {
		method: 'PUT',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({ title, content })
	});
	if (!response.ok) {
		throw new Error('Failed to update document');
	}
	return await response.json();
}

export async function deleteDocument(id: number): Promise<void> {
	const response = await fetch(`${API_BASE}/${id}`, {
		method: 'DELETE'
	});
	if (!response.ok) {
		throw new Error('Failed to delete document');
	}
}