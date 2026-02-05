'use client';

import { useEffect, useState, useCallback, useMemo } from 'react';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';

import { Navbar } from '@/components/navbar';
import { StatsCard } from '@/components/stats-card';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { CustomPieChart } from '@/components/pie-chart';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

import { CreditCard, TrendingUp, DollarSign, Loader2, Calendar, PieChart } from 'lucide-react';

import { balanceApi, creditCardApi, invoiceApi, analyticsApi } from '@/lib/services/api';
import { 
  Balance, 
  CreditCard as CreditCardType, 
  STATUS_LABELS, 
  InvoiceStatus,
  ExpensesByCategory,
  CATEGORY_LABELS 
} from '@/lib/types';

import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';

export default function DashboardPage() {
  const { data: session, status } = useSession() || {};
  const router = useRouter();

  const [balance, setBalance] = useState<Balance | null>(null);
  const [cards, setCards] = useState<CreditCardType[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Estados para analytics
  const [monthlyExpenses, setMonthlyExpenses] = useState<ExpensesByCategory | null>(null);
  const [totalExpenses, setTotalExpenses] = useState<ExpensesByCategory | null>(null);

  const [availableMonths, setAvailableMonths] = useState<number[]>([]);
  const [isMonthsLoading, setIsMonthsLoading] = useState(true);

  const [selectedMonth, setSelectedMonth] = useState<string>(() =>
    new Date().toISOString().slice(0, 7).replace('-', '')
  );

  const currentMonthInt = useMemo(() => {
    return parseInt(new Date().toISOString().slice(0, 7).replace('-', ''));
  }, []);

  const monthOptions = useCallback(() => {
    return (availableMonths ?? []).map((yyyymm) => {
      const year = Math.floor(yyyymm / 100);
      const monthIndex = (yyyymm % 100) - 1;
      const date = new Date(year, monthIndex, 1);

      return {
        value: String(yyyymm),
        label: format(date, 'MMMM yyyy', { locale: ptBR }),
      };
    });
  }, [availableMonths]);

  const selectedMonthLabel = useMemo(() => {
    const opt = monthOptions().find((m) => m.value === selectedMonth);
    return opt?.label ?? '';
  }, [monthOptions, selectedMonth]);

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.replace('/login');
    }
  }, [status, router]);

  useEffect(() => {
    const fetchMonths = async () => {
      try {
        setIsMonthsLoading(true);

        const months = await invoiceApi.getReferenceMonths();
        const normalized = (months ?? []).filter((m) => typeof m === 'number');
        setAvailableMonths(normalized);

        if (normalized.includes(currentMonthInt)) {
          setSelectedMonth(String(currentMonthInt));
        } else if (normalized.length > 0) {
          setSelectedMonth(String(normalized[0]));
        }
      } catch (error) {
        console.error('Error fetching available months:', error);
      } finally {
        setIsMonthsLoading(false);
      }
    };

    if (status === 'authenticated') {
      fetchMonths();
    }
  }, [status, currentMonthInt]);

  const fetchData = useCallback(async () => {
    try {
      setIsLoading(true);

      const monthInt = parseInt(selectedMonth);

      const [balanceData, cardsData, monthlyExpensesData, totalExpensesData] = await Promise.all([
        balanceApi.getMonthlyBalance(monthInt),
        creditCardApi.getAll(),
        analyticsApi.getExpensesByCategory(monthInt),
        analyticsApi.getTotalExpensesByCategory(),
      ]);

      setBalance(balanceData);
      setCards(cardsData);
      setMonthlyExpenses(monthlyExpensesData);
      setTotalExpenses(totalExpensesData);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setIsLoading(false);
    }
  }, [selectedMonth]);

  useEffect(() => {
    if (status !== 'authenticated') return;

    if ((availableMonths?.length ?? 0) === 0) {
      setBalance(null);
      setCards([]);
      setMonthlyExpenses(null);
      setTotalExpenses(null);
      setIsLoading(false);
      return;
    }

    if (selectedMonth) {
      fetchData();
    }
  }, [status, availableMonths, selectedMonth, fetchData]);

  if (status === 'loading' || isMonthsLoading || isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!session) return null;

  const totalCreditLimit =
    cards?.reduce?.((sum, card) => sum + (card?.creditLimit ?? 0), 0) ?? 0;

  const totalInvoicesAmount = balance?.totalInvoicesAmount ?? 0;
  const totalAmount = balance?.totalCreditCardsAmount ?? 0;
  const availableCredit = totalCreditLimit - totalAmount;

  const balanceChartData =
    balance?.cards
      ?.map?.((card) => ({
        name: card?.name ?? 'Cartão',
        value: card?.invoice?.totalAmount ?? 0,
      }))
      ?.filter((item) => item.value > 0) ?? [];

  // Dados para gráfico de gastos mensais por categoria
  const monthlyExpensesChartData =
    monthlyExpenses?.categories
      ?.map((cat) => ({
        name: CATEGORY_LABELS[cat.category] || cat.categoryName,
        value: cat.monthlyAmount,
      }))
      ?.filter((item) => item.value > 0) ?? [];

  // Dados para gráfico de gastos totais por categoria
  const totalExpensesChartData =
    totalExpenses?.categories
      ?.map((cat) => ({
        name: CATEGORY_LABELS[cat.category] || cat.categoryName,
        value: cat.totalAccumulatedAmount,
      }))
      ?.filter((item) => item.value > 0) ?? [];

  const getStatusColor = (invoiceStatus: InvoiceStatus) => {
    const colors: Record<InvoiceStatus, string> = {
      [InvoiceStatus.OPEN]:
        'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
      [InvoiceStatus.CLOSED]:
        'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
      [InvoiceStatus.PAID]:
        'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
      [InvoiceStatus.OVERDUE]:
        'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
    };
    return colors[invoiceStatus];
  };

  const getMainInvoiceStatus = (): InvoiceStatus | null => {
    const invoices = balance?.cards?.map((c) => c.invoice).filter(Boolean) ?? [];
    if (invoices.length === 0) return null;

    if (invoices.some((inv) => inv?.status === InvoiceStatus.OVERDUE))
      return InvoiceStatus.OVERDUE;
    if (invoices.some((inv) => inv?.status === InvoiceStatus.CLOSED))
      return InvoiceStatus.CLOSED;
    if (invoices.some((inv) => inv?.status === InvoiceStatus.OPEN))
      return InvoiceStatus.OPEN;
    return InvoiceStatus.PAID;
  };

  const mainStatus = getMainInvoiceStatus();

  return (
    <div className="min-h-screen bg-background">
      <Navbar />

      <main className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
        {/* Header com Seletor */}
        <div className="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4">
          <div>
            <h1 className="text-3xl font-bold">Dashboard</h1>
            <p className="text-muted-foreground mt-1">Visão geral das suas finanças</p>
          </div>

          <div className="flex items-center gap-2 min-w-[200px]">
            <Calendar className="h-4 w-4 text-muted-foreground" />
            <Select value={selectedMonth} onValueChange={setSelectedMonth}>
              <SelectTrigger className="w-full md:w-[220px]">
                <SelectValue placeholder="Selecione o mês" />
              </SelectTrigger>
              <SelectContent>
                {monthOptions().length > 0 ? (
                  monthOptions().map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label.charAt(0).toUpperCase() + option.label.slice(1)}
                    </SelectItem>
                  ))
                ) : (
                  <div className="px-3 py-2 text-sm text-muted-foreground">
                    Nenhuma fatura encontrada
                  </div>
                )}
              </SelectContent>
            </Select>
          </div>
        </div>

        {/* Stats Cards */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 mb-8">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Fatura do mês</CardTitle>
              {mainStatus && (
                <span
                  className={`px-2 py-1 rounded-full text-[10px] font-bold uppercase ${getStatusColor(
                    mainStatus
                  )}`}
                >
                  {STATUS_LABELS[mainStatus]}
                </span>
              )}
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {new Intl.NumberFormat('pt-BR', {
                  style: 'currency',
                  currency: 'BRL',
                }).format(totalInvoicesAmount)}
              </div>
              <p className="text-xs text-muted-foreground mt-1">
                {selectedMonthLabel ? `Referente a ${selectedMonthLabel}` : 'Referente ao mês selecionado'}
              </p>
            </CardContent>
          </Card>

          <StatsCard
            title="Gasto Total em Aberto"
            value={new Intl.NumberFormat('pt-BR', {
              style: 'currency',
              currency: 'BRL',
            }).format(totalAmount)}
            icon={CreditCard}
            description="Soma de todos os gastos com cartões"
          />

          <StatsCard
            title="Crédito Disponível"
            value={new Intl.NumberFormat('pt-BR', {
              style: 'currency',
              currency: 'BRL',
            }).format(availableCredit)}
            icon={DollarSign}
            description="Limite - Faturas do mês"
          />

          <StatsCard
            title="Cartões Ativos"
            value={cards?.length?.toString() ?? '0'}
            icon={TrendingUp}
            description="Cartões cadastrados"
          />
        </div>

        {/* Charts - Primeira Linha */}
        <div className="grid gap-6 md:grid-cols-2 mb-8">
          <Card>
            <CardHeader>
              <CardTitle>Distribuição de Gastos</CardTitle>
              <CardDescription>Gastos por cartão no período selecionado</CardDescription>
            </CardHeader>
            <CardContent>
              {balanceChartData.length > 0 ? (
                <CustomPieChart data={balanceChartData} />
              ) : (
                <div className="h-[300px] flex items-center justify-center text-muted-foreground border-2 border-dashed rounded-lg">
                  Nenhum gasto registrado neste mês
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Resumo de Utilização</CardTitle>
              <CardDescription>Percentual de uso do limite total</CardDescription>
            </CardHeader>
            <CardContent className="flex items-center justify-center">
              <CustomPieChart
                data={[
                  { name: 'Usado', value: totalAmount },
                  { name: 'Disponível', value: availableCredit > 0 ? availableCredit : 0 },
                ]}
              />
            </CardContent>
          </Card>
        </div>

        {/* Charts - Segunda Linha (Gastos por Categoria) */}
        <div className="grid gap-6 md:grid-cols-2 mb-8">
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <PieChart className="h-5 w-5 text-primary" />
                <CardTitle>Gastos por Categoria - Mensal</CardTitle>
              </div>
              <CardDescription>
                Distribuição de gastos por categoria{' '}
                {selectedMonthLabel ? `em ${selectedMonthLabel}` : 'no mês selecionado'}
              </CardDescription>
            </CardHeader>
            <CardContent>
              {monthlyExpensesChartData.length > 0 ? (
                <CustomPieChart data={monthlyExpensesChartData} />
              ) : (
                <div className="h-[300px] flex items-center justify-center text-muted-foreground border-2 border-dashed rounded-lg">
                  Nenhum gasto por categoria neste mês
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <PieChart className="h-5 w-5 text-primary" />
                <CardTitle>Gastos por Categoria - Total</CardTitle>
              </div>
              <CardDescription>
                Distribuição total acumulada de gastos por categoria
              </CardDescription>
            </CardHeader>
            <CardContent>
              {totalExpensesChartData.length > 0 ? (
                <CustomPieChart data={totalExpensesChartData} />
              ) : (
                <div className="h-[300px] flex items-center justify-center text-muted-foreground border-2 border-dashed rounded-lg">
                  Nenhum gasto por categoria registrado
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Lista de Cartões Detalhada */}
        <Card>
          <CardHeader>
            <CardTitle>Status dos Cartões</CardTitle>
            <CardDescription>
              Detalhamento por cartão{selectedMonthLabel ? ` em ${selectedMonthLabel}` : ''}
            </CardDescription>
          </CardHeader>

          <CardContent>
            <div className="space-y-4">
              {cards?.length > 0 ? (
                cards.map((card) => {
                  const cardBalance = balance?.cards?.find((bc) => bc.id === card.id);
                  const spent = cardBalance?.usageLimit ?? 0;
                  const percent = card.creditLimit > 0 ? (spent / card.creditLimit) * 100 : 0;

                  return (
                    <div key={card.id} className="p-4 border rounded-lg">
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-3">
                          <div className="h-8 w-8 rounded bg-primary/10 flex items-center justify-center">
                            <CreditCard className="h-4 w-4 text-primary" />
                          </div>
                          <span className="font-medium">{card.name}</span>
                        </div>

                        <span className="font-bold">
                          {new Intl.NumberFormat('pt-BR', {
                            style: 'currency',
                            currency: 'BRL',
                          }).format(spent)}
                        </span>
                      </div>

                      <div className="w-full bg-secondary h-2 rounded-full overflow-hidden">
                        <div
                          className={`h-full transition-all ${percent > 90 ? 'bg-destructive' : 'bg-primary'}`}
                          style={{ width: `${Math.min(percent, 100)}%` }}
                        />
                      </div>

                      <div className="flex justify-between mt-1 text-[10px] text-muted-foreground uppercase font-bold">
                        <span>{percent.toFixed(1)}% utilizado</span>
                        <span>
                          Limite:{' '}
                          {new Intl.NumberFormat('pt-BR', {
                            style: 'currency',
                            currency: 'BRL',
                          }).format(card.creditLimit)}
                        </span>
                      </div>
                    </div>
                  );
                })
              ) : (
                <div className="text-center py-8 text-muted-foreground">Nenhum cartão encontrado</div>
              )}
            </div>
          </CardContent>
        </Card>
      </main>
    </div>
  );
}