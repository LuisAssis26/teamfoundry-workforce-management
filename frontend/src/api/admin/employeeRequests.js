import { API_URL } from '../config/config.js';

const BASE_URL = `${API_URL}/api/employee-requests`;

export const employeeRequestsAPI = {
  /**
   * Busca todas as requisições de funcionários
   */
  getAll: async () => {
    try {
      const response = await fetch(`${BASE_URL}`, {
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
      const response = await fetch(`${BASE_URL}/company/${companyId}`, {
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
      const response = await fetch(`${BASE_URL}/${id}`, {
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
