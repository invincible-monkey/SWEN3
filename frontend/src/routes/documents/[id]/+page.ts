import { getDocumentById } from '$lib/api';
import type { PageLoad } from './$types';

export const load: PageLoad = async ({ params }) => {
	const id = Number(params.id);
	try {
		const document = await getDocumentById(id);
		return {
			document // This will be available as the 'data' prop in the component
		};
	} catch (error) {
		return {
			status: 500,
			error: 'Could not load document'
		};
	}
};