export interface TagDto {
    id: number;
    name: string;
}

export interface DocumentDto {
	id: number;
	title: string;
	content: string;
	createdDate: string;
	storagePath: string;
	status: string;
	summary: string;
    fileSize: number;
	tags?: TagDto[];
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
        const errorBody = await response.text();
        console.error("Failed to upload. Server response:", errorBody);
		
        throw new Error(`Upload failed: ${response.status} ${response.statusText}`);
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

export async function getDownloadUrl(id: number): Promise<string> {
    const response = await fetch(`${API_BASE}/${id}/download-url`);
    if (!response.ok) {
        throw new Error('Failed to get download URL');
    }
    const data = await response.json();
    return data.url;
}

export async function searchDocuments(query: string): Promise<DocumentDto[]> {
    const response = await fetch(`${API_BASE}/search?query=${encodeURIComponent(query)}`);
    if (!response.ok) {
        throw new Error('Failed to search documents');
    }
    return await response.json();
}

export async function addTag(docId: number, tagName: string): Promise<DocumentDto> {
    const response = await fetch(`${API_BASE}/${docId}/tags`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: tagName })
    });
    if (!response.ok) throw new Error('Failed to add tag');
    return await response.json();
}

export async function removeTag(docId: number, tagId: number): Promise<DocumentDto> {
    const response = await fetch(`${API_BASE}/${docId}/tags/${tagId}`, {
        method: 'DELETE'
    });
    if (!response.ok) throw new Error('Failed to remove tag');
    return await response.json();
}