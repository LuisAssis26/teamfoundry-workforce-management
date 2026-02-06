import { API_URL } from '../config/config.js';

const BASE_URL = API_URL ? `${API_URL}/api/employee-requests` : null;

function ensureBaseUrl() {
  if (!BASE_URL) {
    throw new Error('VITE_API_BASE_URL (or VITE_API_URL) is not set.');
  }
  return BASE_URL;
}

export const employeeRequestsAPI = {
  /**
   * Busca todas as requisições de funcionários
   */
  getAll: async () => {
    try {
      const baseUrl = ensureBaseUrl();
      const response = await fetch(`${baseUrl}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      });
      if (!response.ok) throw new Error('Erro ao buscar requisições');
      return await response.json();
    } catch (error) {
      console.error('Erro ao buscar requisições:', error);
      throw error;
    }
  },

  /**
   * Busca requisições por empresa
   */
  getByCompany: async (companyId) => {
    try {
      const baseUrl = ensureBaseUrl();
      const response = await fetch(`${baseUrl}/company/${companyId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      });
      if (!response.ok) throw new Error('Erro ao buscar requisições da empresa');
      return await response.json();
    } catch (error) {
      console.error('Erro ao buscar requisições da empresa:', error);
      throw error;
    }
  },

  /**
   * Busca uma requisição específica por ID
   */
  getById: async (id) => {
    try {
      const baseUrl = ensureBaseUrl();
      const response = await fetch(`${baseUrl}/${id}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      });
      if (!response.ok) throw new Error('Erro ao buscar requisição');
      return await response.json();
    } catch (error) {
      console.error('Erro ao buscar requisição:', error);
      throw error;
    }
  },
};
