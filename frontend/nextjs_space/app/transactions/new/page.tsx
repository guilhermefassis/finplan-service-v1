'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Navbar } from '@/components/navbar';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ArrowLeft, Loader2 } from 'lucide-react';
import { transactionApi, creditCardApi } from '@/lib/services/api';
import { CreditCard, TransactionCategory, CATEGORY_LABELS } from '@/lib/types';
import { useToast } from '@/hooks/use-toast';
import Link from 'next/link';
import { DatePicker } from '@/components/date-picker';
import { format } from 'date-fns';

export default function NewTransactionPage() {
  const router = useRouter();
  const { toast } = useToast();
  const [cards, setCards] = useState<CreditCard[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingCards, setIsLoadingCards] = useState(true);
  const [purchaseDate, setPurchaseDate] = useState<Date>(new Date());
  const [formData, setFormData] = useState({
    creditCardId: '',
    description: '',
    category: '',
    amount: '',
    installments: false,
    totalInstallments: '1',
  });

  useEffect(() => {
    const fetchCards = async () => {
      try {
        setIsLoadingCards(true);
        const data = await creditCardApi.getAll();
        setCards(data);
        if (data?.length > 0) {
          setFormData((prev) => ({ ...prev, creditCardId: data[0]?.id ?? '' }));
        }
      } catch (error) {
        console.error('Error fetching cards:', error);
        toast({
          title: 'Erro',
          description: 'Não foi possível carregar os cartões',
          variant: 'destructive',
        });
      } finally {
        setIsLoadingCards(false);
      }
    };

    fetchCards();
  }, [toast]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      await transactionApi.create(formData.creditCardId, {
        creditCardId: formData.creditCardId,
        purchaseDate: format(purchaseDate, 'yyyy-MM-dd'),
        description: formData.description,
        category: formData.category as TransactionCategory,
        amount: parseFloat(formData.amount),
        installments: formData.installments,
        totalInstallments: formData.installments ? parseInt(formData.totalInstallments) : 1,
        currentInstallment: 1,
      });

      toast({
        title: 'Sucesso',
        description: 'Transação criada com sucesso',
      });

      router.push('/transactions');
    } catch (error) {
      console.error('Error creating transaction:', error);
      toast({
        title: 'Erro',
        description: 'Não foi possível criar a transação',
        variant: 'destructive',
      });
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoadingCards) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (cards?.length === 0) {
    return (
      <div className="min-h-screen bg-background">
        <Navbar />
        <main className="mx-auto max-w-2xl px-4 sm:px-6 lg:px-8 py-8">
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-12">
              <p className="text-muted-foreground mb-4">Você precisa ter pelo menos um cartão cadastrado para criar uma transação</p>
              <Link href="/cards/new">
                <Button>Adicionar Cartão</Button>
              </Link>
            </CardContent>
          </Card>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="mx-auto max-w-2xl px-4 sm:px-6 lg:px-8 py-8">
        <Link href="/transactions">
          <Button variant="ghost" className="mb-4">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Voltar
          </Button>
        </Link>

        <Card>
          <CardHeader>
            <CardTitle>Nova Transação</CardTitle>
            <CardDescription>Registre um novo gasto no cartão</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="creditCardId">Cartão</Label>
                <Select
                  value={formData.creditCardId}
                  onValueChange={(value) => setFormData({ ...formData, creditCardId: value })}
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Selecione um cartão" />
                  </SelectTrigger>
                  <SelectContent>
                    {cards?.map?.((card) => (
                      <SelectItem key={card?.id ?? ''} value={card?.id ?? ''}>
                        {card?.name ?? 'Cartão'} - {card?.brand ?? 'Bandeira'}
                      </SelectItem>
                    )) ?? null}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label>Data da Compra</Label>
                <DatePicker date={purchaseDate} onSelect={(date) => date && setPurchaseDate(date)} />
              </div>

              <div className="space-y-2">
                <Label htmlFor="description">Descrição</Label>
                <Input
                  id="description"
                  name="description"
                  placeholder="Ex: Supermercado"
                  value={formData.description}
                  onChange={handleChange}
                  required
                  disabled={isLoading}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="category">Categoria</Label>
                <Select
                  value={formData.category}
                  onValueChange={(value) => setFormData({ ...formData, category: value })}
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Selecione uma categoria" />
                  </SelectTrigger>
                  <SelectContent>
                    {Object.entries(CATEGORY_LABELS)?.map?.(([key, label]) => (
                      <SelectItem key={key} value={key}>
                        {label}
                      </SelectItem>
                    )) ?? null}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="amount">Valor</Label>
                <Input
                  id="amount"
                  name="amount"
                  type="number"
                  step="0.01"
                  min="0.01"
                  placeholder="100.00"
                  value={formData.amount}
                  onChange={handleChange}
                  required
                  disabled={isLoading}
                />
              </div>

              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <input
                    id="installments"
                    name="installments"
                    type="checkbox"
                    checked={formData.installments}
                    onChange={handleChange}
                    disabled={isLoading}
                    className="h-4 w-4"
                  />
                  <Label htmlFor="installments" className="cursor-pointer">
                    Parcelado
                  </Label>
                </div>
              </div>

              {formData.installments && (
                <div className="space-y-2">
                  <Label htmlFor="totalInstallments">Número de Parcelas</Label>
                  <Input
                    id="totalInstallments"
                    name="totalInstallments"
                    type="number"
                    min="2"
                    max="48"
                    placeholder="12"
                    value={formData.totalInstallments}
                    onChange={handleChange}
                    required
                    disabled={isLoading}
                  />
                </div>
              )}

              <div className="flex gap-4 pt-4">
                <Button type="submit" className="flex-1" disabled={isLoading}>
                  {isLoading ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Criando...
                    </>
                  ) : (
                    'Criar Transação'
                  )}
                </Button>
                <Link href="/transactions" className="flex-1">
                  <Button type="button" variant="outline" className="w-full" disabled={isLoading}>
                    Cancelar
                  </Button>
                </Link>
              </div>
            </form>
          </CardContent>
        </Card>
      </main>
    </div>
  );
}
