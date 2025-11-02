import { getDocumentById, getDownloadUrl } from '$lib/api';
import type { PageLoad } from './$types';

export const load: PageLoad = async ({ params }) => {
    const id = Number(params.id);
    try {
        // Fetch document and download URL in parallel
        const [document, downloadUrl] = await Promise.all([
            getDocumentById(id),
            getDownloadUrl(id)
        ]);

        return {
            document,
            downloadUrl
        };
    } catch (error) {
        return {
            status: 500,
            error: 'Could not load document'
        };
    }
};