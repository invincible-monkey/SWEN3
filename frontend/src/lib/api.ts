export interface DocumentDto {
    id: number;
    title: string;
    content: string;
    createdDate: string;
}

const API_BASE = '/api/documents';

export async function getDocuments(): Promise<DocumentDto[]> {
    const response = await fetch(API_BASE);
    if (!response.ok) {
        throw new Error('Failed to fetch documents');
    }
    return await response.json();
}