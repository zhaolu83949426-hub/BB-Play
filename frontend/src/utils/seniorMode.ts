const SENIOR_MODE_STORAGE_KEY = 'front-senior-mode';
const SENIOR_MODE_CLASS = 'senior-mode';
const FRONT_ROUTE_CLASS = 'front-route';

function getRootElement() {
  return document.documentElement;
}

export function getSeniorMode() {
  return localStorage.getItem(SENIOR_MODE_STORAGE_KEY) === '1';
}

export function applySeniorModeClass(enabled: boolean) {
  getRootElement().classList.toggle(SENIOR_MODE_CLASS, enabled);
}

export function setSeniorMode(enabled: boolean) {
  localStorage.setItem(SENIOR_MODE_STORAGE_KEY, enabled ? '1' : '0');
  applySeniorModeClass(enabled);
}

export function applyFrontRouteClass(path: string) {
  const isFrontRoute = !path.startsWith('/admin') && path !== '/login' && path !== '/register';
  getRootElement().classList.toggle(FRONT_ROUTE_CLASS, isFrontRoute);
}
