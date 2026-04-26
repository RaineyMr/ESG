import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly baseUrl: string;

  constructor() {
    this.baseUrl = environment.apiUrl;
  }

  getApiUrl(endpoint: string = ''): string {
    return `${this.baseUrl}${endpoint}`;
  }

  getHeaders(): { [key: string]: string } {
    return {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'X-Requested-With': 'XMLHttpRequest'
    };
  }

  // Helper method to handle CORS and API communication
  handleApiError(error: any): string {
    if (error.status === 0) {
      return 'Unable to connect to the backend server. Please check if the backend is running.';
    } else if (error.status === 401) {
      return 'Unauthorized access. Please check your credentials.';
    } else if (error.status === 403) {
      return 'Access forbidden. You do not have permission to access this resource.';
    } else if (error.status === 404) {
      return 'Resource not found. The requested endpoint does not exist.';
    } else if (error.status >= 500) {
      return 'Server error. Please try again later.';
    } else {
      return error.message || 'An unexpected error occurred.';
    }
  }

  // Utility method for API calls with proper error handling
  async makeRequest(url: string, options: RequestInit = {}): Promise<Response> {
    const defaultOptions: RequestInit = {
      headers: this.getHeaders(),
      mode: 'cors',
      credentials: 'omit'
    };

    const mergedOptions = { ...defaultOptions, ...options };

    try {
      const response = await fetch(url, mergedOptions);
      return response;
    } catch (error) {
      console.error('API Request failed:', error);
      throw new Error(this.handleApiError(error));
    }
  }
}
