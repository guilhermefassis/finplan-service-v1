'use client';

import { useEffect, useState } from 'react';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Navbar } from '@/components/navbar';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Plus, Receipt, Trash2, Loader2 } from 'lucide-react';
import { transactionApi, creditCardApi } from '@/lib/services/api';
import { Transaction, CreditCard, CATEGORY_LABELS } from '@/lib/types';
import { useToast } from '@/hooks/use-toast';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';

export default function TransactionsPage() {
  const { data: session, status } = useSession() || {};
  const router = useRouter();
  const { toast } = useToast();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [cards, setCards] = useState<CreditCard[]>([]);
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
        const cardsData = await creditCardApi.getAll();
        setCards(cardsData);

        if (cardsData?.length > 0) {
          const allTransactions = await Promise.all(
            cardsData?.map?.((card) => transactionApi.getAll(card?.id ?? '')) ?? []
          );
          setTransactions(allTransactions?.flat?.() ?? []);
        }
      } catch (error) {
        console.error('Error fetching transactions:', error);
        toast({
          title: 'Erro',
          description: 'Não foi possível carregar as transações',
          variant: 'destructive',
        });
      } finally {
        setIsLoading(false);
      }
    };

    if (status === 'authenticated') {
      fetchData();
    }
  }, [status, toast]);

  const handleDelete = async (cardId: string, groupId: string) => {
    if (!confirm('Tem certeza que deseja excluir esta transação?')) {
      return;
    }

    try {
      await transactionApi.delete(cardId, groupId);
      setTransactions(transactions?.filter?.(t => t?.groupId !== groupId) ?? []);
      toast({
        title: 'Sucesso',
        description: 'Transação excluída com sucesso',
      });
    } catch (error) {
      console.error('Error deleting transaction:', error);
      toast({
        title: 'Erro',
        description: 'Não foi possível excluir a transação',
        variant: 'destructive',
      });
    }
  };

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

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold">Transações</h1>
            <p className="text-muted-foreground mt-1">Histórico de gastos</p>
          </div>
          <Link href="/transactions/new">
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              Nova Transação
            </Button>
          </Link>
        </div>

        {transactions?.length > 0 ? (
          <div className="space-y-4">
            {transactions?.map?.((transaction) => {
              const card = cards?.find?.(c => c?.id === transaction?.creditCardId);
              return (
                <Card key={transaction?.id ?? ''} className="hover:shadow-md transition-shadow">
                  <CardContent className="p-6">
                    <div className="flex items-start justify-between">
                      <div className="flex items-start gap-4">
                        <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                          <Receipt className="h-5 w-5 text-primary" />
                        </div>
                        <div>
                          <p className="font-medium">{transaction?.description ?? 'Transação'}</p>
                          <p className="text-sm text-muted-foreground">
                            {CATEGORY_LABELS[transaction?.category] ?? transaction?.category ?? 'Categoria'}
                          </p>
                          <p className="text-xs text-muted-foreground mt-1">
                            {card?.name ?? 'Cartão'} •{' '}
                            {transaction?.purchaseDate
                              ? format(new Date(transaction.purchaseDate), 'PPP', { locale: ptBR })
                              : 'Data não disponível'}
                          </p>
                          {transaction?.installments && (
                            <p className="text-xs text-muted-foreground">
                              Parcela {transaction?.currentInstallment ?? 1} de {transaction?.totalInstallments ?? 1}
                            </p>
                          )}
                        </div>
                      </div>
                      <div className="flex items-center gap-4">
                        <div className="text-right">
                          <p className="font-bold text-lg">
                            {new Intl.NumberFormat('pt-BR', {
                              style: 'currency',
                              currency: 'BRL',
                            }).format(transaction?.amount ?? 0)}
                          </p>
                          {transaction?.installments && (
                            <p className="text-xs text-muted-foreground">
                              Total:{' '}
                              {new Intl.NumberFormat('pt-BR', {
                                style: 'currency',
                                currency: 'BRL',
                              }).format(transaction?.totalPurchaseAmount ?? 0)}
                            </p>
                          )}
                        </div>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() =>
                            handleDelete(transaction?.creditCardId ?? '', transaction?.groupId ?? '')
                          }
                          className="text-destructive hover:text-destructive"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            }) ?? null}
          </div>
        ) : (
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-12">
              <Receipt className="h-12 w-12 text-muted-foreground mb-4" />
              <p className="text-muted-foreground mb-4">Nenhuma transação encontrada</p>
              <Link href="/transactions/new">
                <Button>
                  <Plus className="mr-2 h-4 w-4" />
                  Adicionar primeira transação
                </Button>
              </Link>
            </CardContent>
          </Card>
        )}
      </main>
    </div>
  );
}
