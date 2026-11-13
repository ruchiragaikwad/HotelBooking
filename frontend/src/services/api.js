// Central place for backend requests. Hotel and booking calls will be added later.
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export { apiBaseUrl };
