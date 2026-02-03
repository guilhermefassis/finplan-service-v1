'use client';

import { useEffect, useState, useCallback } from 'react';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { Navbar } from '@/components/navbar';
import { StatsCard } from '@/components/stats-card';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { CustomPieChart } from '@/components/pie-chart';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { CreditCard, TrendingUp, DollarSign, Receipt, Loader2, Calendar } from 'lucide-react';
import { balanceApi, creditCardApi } from '@/lib/services/api';
import { Balance, CreditCard as CreditCardType } from '@/lib/types';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';

export default function DashboardPage() {
  const { data: session, status } = useSession() || {};
  const router = useRouter();
  
  // Estados
  const [balance, setBalance] = useState<Balance | null>(null);
  const [cards, setCards] = useState<CreditCardType[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  
  // Estado do Mês Selecionado (Default: Mês Atual no formato YYYYMM)
  const [selectedMonth, setSelectedMonth] = useState<string>(
    new Date().toISOString().slice(0, 7).replace('-', '')
  );

  // Gerador de opções de meses (6 meses atrás até 6 meses à frente)
  const monthOptions = useCallback(() => {
    const options = [];
    const currentDate = new Date();
    for (let i = -6; i <= 6; i++) {
      const date = new Date(currentDate.getFullYear(), currentDate.getMonth() + i, 1);
      const value = date.toISOString().slice(0, 7).replace('-', '');
      const label = format(date, 'MMMM yyyy', { locale: ptBR });
      options.push({ value, label });
    }
    return options;
  }, []);

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.replace('/login');
    }
  }, [status, router]);

  // Função de busca de dados
  const fetchData = useCallback(async () => {
    try {
      setIsLoading(true);
      const monthInt = parseInt(selectedMonth);
      
      const [balanceData, cardsData] = await Promise.all([
        balanceApi.getMonthlyBalance(monthInt),
        creditCardApi.getAll(),
      ]);
      
      setBalance(balanceData);
      setCards(cardsData);
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setIsLoading(false);
    }
  }, [selectedMonth]);

  useEffect(() => {
    if (status === 'authenticated') {
      fetchData();
    }
  }, [status, fetchData]);

  if (status === 'loading' || isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!session) return null;

  // Cálculos
  const totalCreditLimit = cards?.reduce?.((sum, card) => sum + (card?.creditLimit ?? 0), 0) ?? 0;
  const totalInvoicesAmount = balance?.totalInvoicesAmount ?? 0;
  const totalAmount = balance?.totalCreditCardsAmount ?? 0;
  const availableCredit = totalCreditLimit - totalAmount;

  const balanceChartData = balance?.cards?.map?.((card) => ({
    name: card?.name ?? 'Cartão',
    value: card?.invoice?.totalAmount ?? 0,
  })).filter(item => item.value > 0) ?? [];

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
        
        {/* Header com Seletor */}
        <div className="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4">
          <div>
            <h1 className="text-3xl font-bold">Dashboard</h1>
            <p className="text-muted-foreground mt-1">
              Visão geral das suas finanças
            </p>
          </div>

          <div className="flex items-center gap-2 min-w-[200px]">
            <Calendar className="h-4 w-4 text-muted-foreground" />
            <Select value={selectedMonth} onValueChange={setSelectedMonth}>
              <SelectTrigger className="w-full md:w-[200px]">
                <SelectValue placeholder="Selecione o mês" />
              </SelectTrigger>
              <SelectContent>
                {monthOptions().map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label.charAt(0).toUpperCase() + option.label.slice(1)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>

        {/* Stats Cards */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 mb-8">
          <StatsCard
            title="Total em Faturas"
            value={new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(totalInvoicesAmount)}
            icon={Receipt}
            description={`Referente a ${monthOptions().find(m => m.value === selectedMonth)?.label}`}
          />
          <StatsCard
            title="Limite Total"
            value={new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(totalCreditLimit)}
            icon={CreditCard}
            description="Soma de todos os cartões"
          />
          <StatsCard
            title="Crédito Disponível"
            value={new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(availableCredit)}
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

        {/* Charts */}
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
               {/* Aqui você pode manter o PieChart ou criar um gráfico de barras de uso */}
               <CustomPieChart data={[
                 { name: 'Usado', value: totalAmount },
                 { name: 'Disponível', value: availableCredit > 0 ? availableCredit : 0 }
               ]} />
            </CardContent>
          </Card>
        </div>

        {/* Lista de Cartões Detalhada */}
        <Card>
          <CardHeader>
            <CardTitle>Status dos Cartões</CardTitle>
            <CardDescription>Detalhamento por cartão em {monthOptions().find(m => m.value === selectedMonth)?.label}</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {cards?.length > 0 ? (
                cards.map((card) => {
                  const cardBalance = balance?.cards?.find(bc => bc.id === card.id);
                  const spent = cardBalance?.invoice?.totalAmount ?? 0;
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
                          {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(spent)}
                        </span>
                      </div>
                      {/* Barra de progresso simples */}
                      <div className="w-full bg-secondary h-2 rounded-full overflow-hidden">
                        <div 
                          className={`h-full transition-all ${percent > 90 ? 'bg-destructive' : 'bg-primary'}`}
                          style={{ width: `${Math.min(percent, 100)}%` }}
                        />
                      </div>
                      <div className="flex justify-between mt-1 text-[10px] text-muted-foreground uppercase font-bold">
                        <span>{percent.toFixed(1)}% utilizado</span>
                        <span>Limite: {new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(card.creditLimit)}</span>
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