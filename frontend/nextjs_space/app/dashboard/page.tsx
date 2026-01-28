'use client';

import { useEffect, useState } from 'react';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { Navbar } from '@/components/navbar';
import { StatsCard } from '@/components/stats-card';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { CustomPieChart } from '@/components/pie-chart';
import { CreditCard, TrendingUp, DollarSign, Receipt } from 'lucide-react';
import { balanceApi, creditCardApi } from '@/lib/services/api';
import { Balance, CreditCard as CreditCardType } from '@/lib/types';
import { Loader2 } from 'lucide-react';

export default function DashboardPage() {
  const { data: session, status } = useSession() || {};
  const router = useRouter();
  const [balance, setBalance] = useState<Balance | null>(null);
  const [cards, setCards] = useState<CreditCardType[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.replace('/login');
    }
  }, [status, router]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        const currentMonth = parseInt(
          new Date().toISOString().slice(0, 7).replace('-', '')
        );
        const [balanceData, cardsData] = await Promise.all([
          balanceApi.getMonthlyBalance(currentMonth),
          creditCardApi.getAll(),
        ]);
        setBalance(balanceData);
        setCards(cardsData);
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      } finally {
        setIsLoading(false);
      }
    };

    if (status === 'authenticated') {
      fetchData();
    }
  }, [status]);

  if (status === 'loading' || isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!session) {
    return null;
  }

  const totalCreditLimit = cards?.reduce?.((sum, card) => sum + (card?.creditLimit ?? 0), 0) ?? 0;
  const totalInvoicesAmount = balance?.totalInvoicesAmount ?? 0;
  const availableCredit = totalCreditLimit - totalInvoicesAmount;

  // Chart data for balance
  const balanceChartData = balance?.cards?.map?.((card) => ({
    name: card?.name ?? 'Cartão',
    value: card?.invoice?.totalAmount ?? 0,
  })) ?? [];

  // Chart data for card usage
  const cardUsageData = cards?.map?.((card) => {
    const cardInvoice = balance?.cards?.find?.(bc => bc?.id === card?.id)?.invoice;
    return {
      name: card?.name ?? 'Cartão',
      value: cardInvoice?.totalAmount ?? 0,
    };
  }) ?? [];

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold">Dashboard</h1>
          <p className="text-muted-foreground mt-1">
            Visão geral das suas finanças
          </p>
        </div>

        {/* Stats Cards */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 mb-8">
          <StatsCard
            title="Total em Faturas"
            value={new Intl.NumberFormat('pt-BR', {
              style: 'currency',
              currency: 'BRL',
            }).format(totalInvoicesAmount)}
            icon={Receipt}
            description="Faturas do mês atual"
          />
          <StatsCard
            title="Limite Total"
            value={new Intl.NumberFormat('pt-BR', {
              style: 'currency',
              currency: 'BRL',
            }).format(totalCreditLimit)}
            icon={CreditCard}
            description="Soma de todos os cartões"
          />
          <StatsCard
            title="Crédito Disponível"
            value={new Intl.NumberFormat('pt-BR', {
              style: 'currency',
              currency: 'BRL',
            }).format(availableCredit)}
            icon={DollarSign}
            description="Limite - Faturas"
          />
          <StatsCard
            title="Cartões Ativos"
            value={cards?.length?.toString?.() ?? '0'}
            icon={TrendingUp}
            description="Cartões em uso"
          />
        </div>

        {/* Charts */}
        <div className="grid gap-6 md:grid-cols-2 mb-8">
          <Card>
            <CardHeader>
              <CardTitle>Distribuição de Gastos</CardTitle>
              <CardDescription>Gastos por cartão de crédito no mês atual</CardDescription>
            </CardHeader>
            <CardContent>
              {balanceChartData?.length > 0 ? (
                <CustomPieChart data={balanceChartData} />
              ) : (
                <div className="h-[300px] flex items-center justify-center text-muted-foreground">
                  Nenhum dado disponível
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Uso de Crédito</CardTitle>
              <CardDescription>Valor utilizado por cartão</CardDescription>
            </CardHeader>
            <CardContent>
              {cardUsageData?.length > 0 ? (
                <CustomPieChart data={cardUsageData} />
              ) : (
                <div className="h-[300px] flex items-center justify-center text-muted-foreground">
                  Nenhum dado disponível
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Recent Cards */}
        <Card>
          <CardHeader>
            <CardTitle>Cartões de Crédito</CardTitle>
            <CardDescription>Seus cartões ativos</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {cards?.length > 0 ? (
                cards?.map?.((card) => {
                  const cardBalance = balance?.cards?.find?.(bc => bc?.id === card?.id);
                  return (
                    <div
                      key={card?.id ?? ''}
                      className="flex items-center justify-between p-4 border rounded-lg hover:bg-accent transition-colors"
                    >
                      <div className="flex items-center gap-4">
                        <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                          <CreditCard className="h-5 w-5 text-primary" />
                        </div>
                        <div>
                          <p className="font-medium">{card?.name ?? 'Cartão'}</p>
                          <p className="text-sm text-muted-foreground">{card?.brand ?? 'Bandeira'}</p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="font-medium">
                          {new Intl.NumberFormat('pt-BR', {
                            style: 'currency',
                            currency: 'BRL',
                          }).format(cardBalance?.invoice?.totalAmount ?? 0)}
                        </p>
                        <p className="text-sm text-muted-foreground">
                          Limite: {new Intl.NumberFormat('pt-BR', {
                            style: 'currency',
                            currency: 'BRL',
                          }).format(card?.creditLimit ?? 0)}
                        </p>
                      </div>
                    </div>
                  );
                }) ?? null
              ) : (
                <div className="text-center py-8 text-muted-foreground">
                  Nenhum cartão cadastrado
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </main>
    </div>
  );
}
