// Payment Frequency Enum
export enum PaymentFrequency {
  MONTHLY = 'MONTHLY',
  BIWEEKLY = 'BIWEEKLY',
  WEEKLY = 'WEEKLY',
  DAILY = 'DAILY',
}

// Transaction Category Enum
export enum TransactionCategory {
  FOOD_AND_DINING = 'FOOD_AND_DINING',
  GROCERIES = 'GROCERIES',
  TRANSPORTATION = 'TRANSPORTATION',
  FUEL = 'FUEL',
  HOUSING = 'HOUSING',
  UTILITIES = 'UTILITIES',
  HEALTH_AND_FITNESS = 'HEALTH_AND_FITNESS',
  EDUCATION = 'EDUCATION',
  ENTERTAINMENT = 'ENTERTAINMENT',
  SHOPPING = 'SHOPPING',
  CLOTHING = 'CLOTHING',
  ELECTRONICS = 'ELECTRONICS',
  TRAVEL = 'TRAVEL',
  SUBSCRIPTIONS = 'SUBSCRIPTIONS',
  STREAMING_SERVICES = 'STREAMING_SERVICES',
  PERSONAL_CARE = 'PERSONAL_CARE',
  PETS = 'PETS',
  GIFTS_AND_DONATIONS = 'GIFTS_AND_DONATIONS',
  INVESTMENTS = 'INVESTMENTS',
  TAXES = 'TAXES',
  OTHERS = 'OTHERS',
}

// Invoice Status Enum
export enum InvoiceStatus {
  OPEN = 'OPEN',
  CLOSED = 'CLOSED',
  PAID = 'PAID',
  OVERDUE = 'OVERDUE',
}

// User Types
export interface User {
  id: string;
  email: string;
  name: string;
  paymentFrequency?: PaymentFrequency;
  paymentDetails?: Record<string, any>;
  createdAt: Date | string;
  updatedAt: Date | string;
}

export interface UserUpdateDTO {
  name?: string;
  email?: string;
  paymentFrequency?: PaymentFrequency;
  paymentDetails?: Record<string, any>;
}

// Credit Card Types
export interface CreditCard {
  id: string;
  userId: string;
  name: string;
  brand: string;
  closingDay: number;
  dueDay: number;
  creditLimit: number;
  active: boolean;
  createdAt: Date | string;
  updatedAt: Date | string;
}

export interface CreateCreditCardDTO {
  name: string;
  brand: string;
  closingDay: number;
  dueDay: number;
  creditLimit: number;
}

export interface UpdateCreditCardDTO {
  name?: string;
  brand?: string;
  closingDay?: number;
  dueDay?: number;
  creditLimit?: number;
}

// Transaction Types
export interface Transaction {
  id: string;
  creditCardId: string;
  invoiceId?: string;
  groupId: string;
  purchaseDate: Date | string;
  description: string;
  category: TransactionCategory;
  amount: number;
  installments: boolean;
  totalInstallments?: number;
  currentInstallment?: number;
  totalPurchaseAmount?: number;
  createdAt: Date | string;
  updatedAt: Date | string;
}

export interface CreateTransactionDTO {
  creditCardId: string;
  purchaseDate: string;
  description: string;
  category: TransactionCategory;
  amount: number;
  installments: boolean;
  totalInstallments?: number;
  currentInstallment?: number;
}

// Invoice Types
export interface Invoice {
  id: string;
  creditCardId: string;
  referenceMonth: number;
  closingDate: Date | string;
  dueDate: Date | string;
  totalAmount: number;
  status: InvoiceStatus;
  paymentDate?: Date | string;
  createdAt: Date | string;
  updatedAt: Date | string;
  transactions?: Transaction[];
}

export interface PayInvoiceDTO {
  paymentDate: string;
}

// Balance Types
export interface BalanceInvoice {
  id: string;
  referenceMonth: number;
  closingDate: Date | string;
  dueDate: Date | string;
  totalAmount: number;
  status: InvoiceStatus;
  paymentDate?: Date | string;
}

export interface BalanceCreditCard {
  id: string;
  name: string;
  brand: string;
  creditLimit: number;
  usageLimit: number,
  availableLimit: number,
  invoice?: BalanceInvoice;
}

export interface Balance {
  referenceMonth: number;
  totalInvoicesAmount: number;
  totalCreditCardsAmount: number,
  cards: BalanceCreditCard[];
}

// Category Labels
export const CATEGORY_LABELS: Record<TransactionCategory, string> = {
  [TransactionCategory.FOOD_AND_DINING]: 'Alimentação e Restaurantes',
  [TransactionCategory.GROCERIES]: 'Supermercado',
  [TransactionCategory.TRANSPORTATION]: 'Transporte',
  [TransactionCategory.FUEL]: 'Combustível',
  [TransactionCategory.HOUSING]: 'Moradia',
  [TransactionCategory.UTILITIES]: 'Contas Básicas',
  [TransactionCategory.HEALTH_AND_FITNESS]: 'Saúde e Fitness',
  [TransactionCategory.EDUCATION]: 'Educação',
  [TransactionCategory.ENTERTAINMENT]: 'Entretenimento',
  [TransactionCategory.SHOPPING]: 'Compras',
  [TransactionCategory.CLOTHING]: 'Vestuário',
  [TransactionCategory.ELECTRONICS]: 'Eletrônicos',
  [TransactionCategory.TRAVEL]: 'Viagens',
  [TransactionCategory.SUBSCRIPTIONS]: 'Assinaturas',
  [TransactionCategory.STREAMING_SERVICES]: 'Streaming',
  [TransactionCategory.PERSONAL_CARE]: 'Cuidados Pessoais',
  [TransactionCategory.PETS]: 'Pets',
  [TransactionCategory.GIFTS_AND_DONATIONS]: 'Presentes e Doações',
  [TransactionCategory.INVESTMENTS]: 'Investimentos',
  [TransactionCategory.TAXES]: 'Impostos',
  [TransactionCategory.OTHERS]: 'Outros',
};

// Status Labels
export const STATUS_LABELS: Record<InvoiceStatus, string> = {
  [InvoiceStatus.OPEN]: 'Aberta',
  [InvoiceStatus.CLOSED]: 'Fechada',
  [InvoiceStatus.PAID]: 'Paga',
  [InvoiceStatus.OVERDUE]: 'Vencida',
};
