import { 
  User, 
  UserUpdateDTO, 
  CreditCard, 
  CreateCreditCardDTO, 
  UpdateCreditCardDTO,
  Transaction,
  CreateTransactionDTO,
  Invoice,
  PayInvoiceDTO,
  Balance,
  InvoiceStatus,
  ExpensesByCategory,
  TransactionCategory
} from '../types';
import { 
  mockUsers, 
  mockCreditCards, 
  mockTransactions, 
  mockInvoices,
  mockBalance 
} from '../mock-data';
import { getSession } from 'next-auth/react';

// Configuration
const DATA_MODE = process.env.DATA_MODE || 'mock';
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/finplan/api/v1';


const delay = (ms: number = 0) => new Promise(resolve => setTimeout(resolve, ms));


const getAuthToken = async (): Promise<string | null> => {
  if (typeof window === 'undefined') return null;
  const session = await getSession();
  if ((session as any)?.error === 'RefreshAccessTokenError') {
    window.location.href = '/login';
    return null;
  }
  
  return (session as any)?.supabaseAccessToken ?? null;
};

const apiCall = async (endpoint: string, options: RequestInit = {}, retry = true): Promise<any> => {
  const token = await getAuthToken();
  
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.headers || {}),
  };

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (response.status === 401 && retry) {
    console.log('401 detected, forcing session refresh...');
  
    await getSession();
    return apiCall(endpoint, options, false);
  }

  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`);
  }

  return response.json();
};

export const userApi = {
  async getMe(): Promise<User> {
    await delay();
    return apiCall('/users/me');
  },

  async updateUser(data: UserUpdateDTO): Promise<User> {
    await delay();
   
      return apiCall('/users', {
        method: 'PUT',
        body: JSON.stringify(data),
      });
  },
};



// ============== CREDIT CARD API ==============

export const creditCardApi = {
  async getAll(): Promise<CreditCard[]> {
    await delay();
      return apiCall('/credit-cards');
  },

  async getById(id: string): Promise<CreditCard> {
    await delay();
    
    if (DATA_MODE === 'api') {
      return apiCall(`/credit-cards/${id}`);
    }
    
    // Mock implementation
    const card = mockCreditCards.find(c => c.id === id);
    if (!card) throw new Error('Card not found');
    return card;
  },

  async create(data: CreateCreditCardDTO): Promise<CreditCard> {
    await delay();
    
    if (DATA_MODE === 'api') {
      return apiCall('/credit-cards', {
        method: 'POST',
        body: JSON.stringify(data),
      });
    }
    
    // Mock implementation
    const newCard: CreditCard = {
      id: `660e8400-e29b-41d4-a716-${Date.now()}`,
      userId: mockUsers[0].id,
      ...data,
      active: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    mockCreditCards.push(newCard);
    return newCard;
  },

  async update(id: string, data: UpdateCreditCardDTO): Promise<CreditCard> {
    await delay();
    
    if (DATA_MODE === 'api') {
      return apiCall(`/credit-cards/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data),
      });
    }
    
    // Mock implementation
    const cardIndex = mockCreditCards.findIndex(c => c.id === id);
    if (cardIndex === -1) throw new Error('Card not found');
    
    const updatedCard = {
      ...mockCreditCards[cardIndex],
      ...data,
      updatedAt: new Date().toISOString(),
    };
    mockCreditCards[cardIndex] = updatedCard;
    return updatedCard;
  },

  async disable(id: string): Promise<void> {
    await delay();
    
    if (DATA_MODE === 'api') {
      await apiCall(`/credit-cards/${id}/disable`, { method: 'DELETE' });
      return;
    }
    
    // Mock implementation
    const cardIndex = mockCreditCards.findIndex(c => c.id === id);
    if (cardIndex !== -1) {
      mockCreditCards[cardIndex].active = false;
      mockCreditCards[cardIndex].updatedAt = new Date().toISOString();
    }
  },

  async activate(id: string): Promise<void> {
    await delay();
    
    if (DATA_MODE === 'api') {
      await apiCall(`/credit-cards/${id}/activate`, { method: 'POST' });
      return;
    }
    
    // Mock implementation
    const cardIndex = mockCreditCards.findIndex(c => c.id === id);
    if (cardIndex !== -1) {
      mockCreditCards[cardIndex].active = true;
      mockCreditCards[cardIndex].updatedAt = new Date().toISOString();
    }
  },
};

// ============== TRANSACTION API ==============

export const transactionApi = {
  async getAll(cardId: string): Promise<Transaction[]> {
    await delay();
    
    if (DATA_MODE === 'api') {
      return apiCall(`/credit-cards/${cardId}/transactions`);
    }
    
    // Mock implementation
    return mockTransactions.filter(t => t.creditCardId === cardId);
  },

  async create(cardId: string, data: CreateTransactionDTO): Promise<Transaction> {
    await delay();
    
    if (DATA_MODE === 'api') {
      return apiCall(`/credit-cards/${cardId}/transactions`, {
        method: 'POST',
        body: JSON.stringify(data),
      });
    }
    
    // Mock implementation
    const totalPurchaseAmount = data.installments && data.totalInstallments 
      ? data.amount * data.totalInstallments 
      : data.amount;
      
    const newTransaction: Transaction = {
      id: `770e8400-e29b-41d4-a716-${Date.now()}`,
      groupId: `990e8400-e29b-41d4-a716-${Date.now()}`,
      invoiceId: mockInvoices.find(i => i.creditCardId === cardId && i.status === InvoiceStatus.OPEN)?.id,
      ...data,
      totalPurchaseAmount,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };
    mockTransactions.push(newTransaction);
    return newTransaction;
  },

  async delete(cardId: string, groupId: string): Promise<void> {
    await delay();
    
    if (DATA_MODE === 'api') {
      await apiCall(`/credit-cards/${cardId}/transactions/${groupId}`, { method: 'DELETE' });
      return;
    }
    
    // Mock implementation
    const index = mockTransactions.findIndex(t => t.groupId === groupId && t.creditCardId === cardId);
    if (index !== -1) {
      mockTransactions.splice(index, 1);
    }
  },
};

// ============== INVOICE API ==============

export const invoiceApi = {

  getReferenceMonths: async (): Promise<number[]> => {
    return apiCall('/invoices/reference-months');
  },

   getReferenceMonthsByCard: async (cardId: string): Promise<number[]> => {
    return apiCall(`/credit-cards/${cardId}/invoices/reference-months`); 
  },

  async getByCard(cardId: string, referenceDate?: number, includeTransactions: boolean = false): Promise<Invoice[]> {
    await delay();
    
    if (DATA_MODE === 'api') {
      const params = new URLSearchParams();
      if (referenceDate) params.append('referenceDate', String(referenceDate));
      params.append('includeTransactions', String(includeTransactions));
      return apiCall(`/credit-cards/${cardId}/invoices?${params.toString()}`);
    }
    
    // Mock implementation
    let invoices = mockInvoices.filter(i => i.creditCardId === cardId);
    if (referenceDate) {
      invoices = invoices.filter(i => i.referenceMonth === referenceDate);
    }
    return invoices;
  },

  async getById(cardId: string, invoiceId: string, includeTransactions: boolean = true): Promise<Invoice> {
    await delay();
    
    if (DATA_MODE === 'api') {
      const params = new URLSearchParams();
      params.append('includeTransactions', String(includeTransactions));
      return apiCall(`/credit-cards/${cardId}/invoices/${invoiceId}?${params.toString()}`);
    }
    
    // Mock implementation
    const invoice = mockInvoices.find(i => i.id === invoiceId && i.creditCardId === cardId);
    if (!invoice) throw new Error('Invoice not found');
    return invoice;
  },

  async pay(cardId: string, invoiceId: string, data: PayInvoiceDTO): Promise<Invoice> {
    await delay();
    
    if (DATA_MODE === 'api') {
      return apiCall(`/credit-cards/${cardId}/invoices/${invoiceId}/pay`, {
        method: 'POST',
        body: JSON.stringify(data),
      });
    }
    
    // Mock implementation
    const invoiceIndex = mockInvoices.findIndex(i => i.id === invoiceId && i.creditCardId === cardId);
    if (invoiceIndex === -1) throw new Error('Invoice not found');
    
    const updatedInvoice = {
      ...mockInvoices[invoiceIndex],
      status: InvoiceStatus.PAID,
      paymentDate: data.paymentDate,
      updatedAt: new Date().toISOString(),
    };
    mockInvoices[invoiceIndex] = updatedInvoice;
    return updatedInvoice;
  },
};

// ============== BALANCE API ==============

export const balanceApi = {
  async getMonthlyBalance(referenceDate: number): Promise<Balance> {
    await delay();
    
    if (DATA_MODE === 'api') {
      return apiCall(`/balances/cards?referenceDate=${referenceDate}`);
    }
    
    // Mock implementation
    return mockBalance;
  },
};

// Adicione ao final do arquivo api.ts

// ============== ANALYTICS API ==============

export const analyticsApi = {
  async getExpensesByCategory(referenceMonth?: number): Promise<ExpensesByCategory> {
    await delay();
    
    if (DATA_MODE === 'api') {
      const params = referenceMonth ? `?referenceMonth=${referenceMonth}` : '';
      return apiCall(`/balances/analytics/expenses/by-category${params}`);
    }
    
    // Mock implementation
    return {
      referenceMonth: referenceMonth || 0,
      currency: 'BRL',
      totalAmount: 1850.00,
      categories: [
        {
          category: 'TRAVEL' as TransactionCategory,
          categoryName: 'TRAVEL',
          monthlyAmount: 1500.00,
          totalAccumulatedAmount: 1500.00,
          percentageInMonth: 81.08,
          transactionCount: 10,
        },
        {
          category: 'FOOD_AND_DINING' as TransactionCategory,
          categoryName: 'FOOD_AND_DINING',
          monthlyAmount: 350.00,
          totalAccumulatedAmount: 350.00,
          percentageInMonth: 18.92,
          transactionCount: 1,
        },
      ],
    };
  },

  async getTotalExpensesByCategory(): Promise<ExpensesByCategory> {
    return this.getExpensesByCategory();
  },
};