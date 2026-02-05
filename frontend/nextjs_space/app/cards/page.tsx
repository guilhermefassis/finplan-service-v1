'use client';

import { useEffect, useState } from 'react';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Navbar } from '@/components/navbar';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Plus, CreditCard as CreditCardIcon, Edit, Trash2, Loader2 } from 'lucide-react';
import { creditCardApi } from '@/lib/services/api';
import { CreditCard } from '@/lib/types';
import { useToast } from '@/hooks/use-toast';

export default function CardsPage() {
  const { data: session, status } = useSession() || {};
  const router = useRouter();
  const { toast } = useToast();
  const [cards, setCards] = useState<CreditCard[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.replace('/login');
    }
  }, [status, router]);

  useEffect(() => {
    const fetchCards = async () => {
      try {
        setIsLoading(true);
        const data = await creditCardApi.getAll();
        setCards(data);
      } catch (error) {
        console.error('Error fetching cards:', error);
        toast({
          title: 'Erro',
          description: 'Não foi possível carregar os cartões',
          variant: 'destructive',
        });
      } finally {
        setIsLoading(false);
      }
    };

    if (status === 'authenticated') {
      fetchCards();
    }
  }, [status, toast]);

  const handleDelete = async (id: string) => {
    if (!confirm('Tem certeza que deseja desativar este cartão?')) {
      return;
    }

    try {
      await creditCardApi.disable(id);
      setCards(cards?.filter?.(c => c?.id !== id) ?? []);
      toast({
        title: 'Sucesso',
        description: 'Cartão desativado com sucesso',
      });
    } catch (error) {
      console.error('Error disabling card:', error);
      toast({
        title: 'Erro',
        description: 'Não foi possível desativar o cartão',
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
            <h1 className="text-3xl font-bold">Cartões de Crédito</h1>
            <p className="text-muted-foreground mt-1">Gerencie seus cartões</p>
          </div>
          <Link href="/cards/new">
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              Novo Cartão
            </Button>
          </Link>
        </div>

        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {cards?.length > 0 ? (
            cards?.map?.((card) => (
              <Card key={card?.id ?? ''} className="hover:shadow-lg transition-shadow">
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <div className="flex items-center gap-3">
                      <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                        <CreditCardIcon className="h-5 w-5 text-primary" />
                      </div>
                      <div>
                        <CardTitle className="text-lg">{card?.name ?? 'Cartão'}</CardTitle>
                        <CardDescription>{card?.brand ?? 'Bandeira'}</CardDescription>
                      </div>
                    </div>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="space-y-2 mb-4">
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Limite:</span>
                      <span className="font-medium">
                        {new Intl.NumberFormat('pt-BR', {
                          style: 'currency',
                          currency: 'BRL',
                        }).format(card?.creditLimit ?? 0)}
                      </span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Fechamento:</span>
                      <span className="font-medium">Dia {card?.closingDay ?? 0}</span>
                    </div>
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Vencimento:</span>
                      <span className="font-medium">Dia {card?.dueDay ?? 0}</span>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Link href={`/cards/${card?.id}/edit`} className="flex-1">
                      <Button variant="outline" className="w-full" size="sm">
                        <Edit className="mr-2 h-4 w-4" />
                        Editar
                      </Button>
                    </Link>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleDelete(card?.id ?? '')}
                      className="text-destructive hover:text-destructive"
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </CardContent>
              </Card>
            )) ?? null
          ) : (
            <Card className="col-span-full">
              <CardContent className="flex flex-col items-center justify-center py-12">
                <CreditCardIcon className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground mb-4">Nenhum cartão cadastrado</p>
                <Link href="/cards/new">
                  <Button>
                    <Plus className="mr-2 h-4 w-4" />
                    Adicionar primeiro cartão
                  </Button>
                </Link>
              </CardContent>
            </Card>
          )}
        </div>
      </main>
    </div>
  );
}
