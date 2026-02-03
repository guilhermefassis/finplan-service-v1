'use client';

import { useEffect, useMemo, useState } from 'react';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { Navbar } from '@/components/navbar';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { FileText, Loader2 } from 'lucide-react';
import { invoiceApi, creditCardApi } from '@/lib/services/api';
import { Invoice, CreditCard, STATUS_LABELS, InvoiceStatus, CATEGORY_LABELS } from '@/lib/types';
import { useToast } from '@/hooks/use-toast';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { DatePicker } from '@/components/date-picker';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';

export default function InvoicesPage() {
  const { data: session, status } = useSession() || {};
  const router = useRouter();
  const { toast } = useToast();

  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [cards, setCards] = useState<CreditCard[]>([]);
  const [selectedCard, setSelectedCard] = useState<string>('');

  // meses disponíveis por cartão (retornados pela API)
  const [availableMonths, setAvailableMonths] = useState<number[]>([]);
  const [selectedMonth, setSelectedMonth] = useState<string>('');

  const [isLoading, setIsLoading] = useState(true);
  const [isMonthsLoading, setIsMonthsLoading] = useState(true);

  const [payDialogOpen, setPayDialogOpen] = useState(false);
  const [selectedInvoice, setSelectedInvoice] = useState<Invoice | null>(null);
  const [paymentDate, setPaymentDate] = useState<Date>(new Date());
  const [isPaying, setIsPaying] = useState(false);

  const currentMonthInt = useMemo(() => {
    return parseInt(new Date().toISOString().slice(0, 7).replace('-', ''));
  }, []);

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.replace('/login');
    }
  }, [status, router]);

  // 1) Carrega cartões e define default selectedCard
  useEffect(() => {
    const fetchCards = async () => {
      try {
        setIsLoading(true);
        const data = await creditCardApi.getAll();
        setCards(data ?? []);
        if (data?.length > 0) {
          setSelectedCard(data[0]?.id ?? '');
        }
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

  // 2) Quando o cartão mudar, busca os referenceMonths disponíveis daquele cartão
  useEffect(() => {
    const fetchMonthsByCard = async () => {
      if (!selectedCard) return;

      try {
        setIsMonthsLoading(true);

        // IMPORTANTE:
        // Esse método precisa existir no invoiceApi (veja snippet abaixo)
        const months = await invoiceApi.getReferenceMonthsByCard(selectedCard); // ex: [202602, 202601...]

        const normalized = (months ?? []).filter((m) => typeof m === 'number');
        setAvailableMonths(normalized);

        // default selectedMonth:
        // - se mês atual existir para esse cartão -> usa ele
        // - senão -> usa o mais recente
        // - senão -> limpa
        if (normalized.includes(currentMonthInt)) {
          setSelectedMonth(String(currentMonthInt));
        } else if (normalized.length > 0) {
          setSelectedMonth(String(normalized[0]));
        } else {
          setSelectedMonth('');
        }
      } catch (error) {
        console.error('Error fetching available months by card:', error);
        setAvailableMonths([]);
        setSelectedMonth('');
        toast({
          title: 'Erro',
          description: 'Não foi possível carregar os meses disponíveis',
          variant: 'destructive',
        });
      } finally {
        setIsMonthsLoading(false);
      }
    };

    if (status === 'authenticated') {
      fetchMonthsByCard();
    }
  }, [status, selectedCard, toast, currentMonthInt]);

  // 3) Quando cartão/mês mudarem, busca as faturas
  useEffect(() => {
    const fetchInvoices = async () => {
      if (!selectedCard || !selectedMonth) {
        setInvoices([]);
        return;
      }

      try {
        setIsLoading(true);
        const data = await invoiceApi.getByCard(selectedCard, parseInt(selectedMonth), true);
        setInvoices(data ?? []);
      } catch (error) {
        console.error('Error fetching invoices:', error);
        toast({
          title: 'Erro',
          description: 'Não foi possível carregar as faturas',
          variant: 'destructive',
        });
      } finally {
        setIsLoading(false);
      }
    };

    if (status === 'authenticated') {
      fetchInvoices();
    }
  }, [status, selectedCard, selectedMonth, toast]);

  const monthOptions = useMemo(() => {
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

  const handlePayInvoice = (invoice: Invoice) => {
    setSelectedInvoice(invoice);
    setPaymentDate(new Date());
    setPayDialogOpen(true);
  };

  const confirmPayment = async () => {
    if (!selectedInvoice || !paymentDate) return;

    setIsPaying(true);
    try {
      const updatedInvoice = await invoiceApi.pay(selectedInvoice.creditCardId, selectedInvoice.id, {
        paymentDate: format(paymentDate, 'yyyy-MM-dd'),
      });

      setInvoices(
        invoices?.map?.((inv) => (inv?.id === updatedInvoice?.id ? updatedInvoice : inv)) ?? []
      );

      toast({
        title: 'Sucesso',
        description: 'Fatura paga com sucesso',
      });

      setPayDialogOpen(false);
    } catch (error) {
      console.error('Error paying invoice:', error);
      toast({
        title: 'Erro',
        description: 'Não foi possível pagar a fatura',
        variant: 'destructive',
      });
    } finally {
      setIsPaying(false);
    }
  };

  if (status === 'loading' || isLoading || isMonthsLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (!session) return null;

  if (cards?.length === 0) {
    return (
      <div className="min-h-screen bg-background">
        <Navbar />
        <main className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-12">
              <FileText className="h-12 w-12 text-muted-foreground mb-4" />
              <p className="text-muted-foreground mb-4">
                Você precisa ter pelo menos um cartão cadastrado para visualizar faturas
              </p>
              <Button onClick={() => router.push('/cards/new')}>Adicionar Cartão</Button>
            </CardContent>
          </Card>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold">Faturas</h1>
          <p className="text-muted-foreground mt-1">Gerencie suas faturas de cartão</p>
        </div>

        <div className="grid gap-4 md:grid-cols-2 mb-6">
          <div className="space-y-2">
            <label className="text-sm font-medium">Cartão</label>
            <Select
              value={selectedCard}
              onValueChange={(value) => {
                setSelectedCard(value);
                // reset rápido pra evitar buscar invoice com mês antigo
                setSelectedMonth('');
                setInvoices([]);
              }}
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
            <label className="text-sm font-medium">Mês de Referência</label>
            <Select value={selectedMonth} onValueChange={setSelectedMonth} disabled={!selectedCard}>
              <SelectTrigger>
                <SelectValue placeholder="Selecione um mês" />
              </SelectTrigger>
              <SelectContent>
                {monthOptions?.length > 0 ? (
                  monthOptions.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label.charAt(0).toUpperCase() + option.label.slice(1)}
                    </SelectItem>
                  ))
                ) : (
                  <div className="px-3 py-2 text-sm text-muted-foreground">
                    Nenhuma fatura encontrada para este cartão
                  </div>
                )}
              </SelectContent>
            </Select>
          </div>
        </div>

        {selectedCard && !selectedMonth && (
          <Card className="mb-6">
            <CardContent className="py-8 text-center text-muted-foreground">
              Selecione um mês para visualizar as faturas.
            </CardContent>
          </Card>
        )}

        {invoices?.length > 0 ? (
          <div className="space-y-4">
            {invoices?.map?.((invoice) => {
              const card = cards?.find?.((c) => (c?.id ?? '') === (invoice?.creditCardId ?? ''));

              const getStatusColor = (statusVal: InvoiceStatus) => {
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
                return colors[statusVal];
              };

              const statusColor = getStatusColor(invoice?.status ?? InvoiceStatus.OPEN);

              return (
                <Card key={invoice?.id ?? ''} className="hover:shadow-md transition-shadow">
                  <CardHeader>
                    <div className="flex items-start justify-between">
                      <div>
                        <CardTitle className="text-lg">Fatura {card?.name ?? 'Cartão'}</CardTitle>
                        <CardDescription>
                          {invoice?.closingDate
                            ? `Fechamento: ${format(new Date(invoice.closingDate), 'PPP', {
                                locale: ptBR,
                              })}`
                            : 'Data não disponível'}
                        </CardDescription>
                      </div>
                      <span className={`px-3 py-1 rounded-full text-xs font-medium ${statusColor}`}>
                        {STATUS_LABELS[invoice?.status ?? InvoiceStatus.OPEN]}
                      </span>
                    </div>
                  </CardHeader>

                  <CardContent>
                    <div className="space-y-4">
                      <div className="flex justify-between items-center">
                        <span className="text-sm text-muted-foreground">Vencimento:</span>
                        <span className="font-medium">
                          {invoice?.dueDate
                            ? format(new Date(invoice.dueDate), 'PPP', { locale: ptBR })
                            : 'Data não disponível'}
                        </span>
                      </div>

                      <div className="flex justify-between items-center">
                        <span className="text-sm text-muted-foreground">Valor Total:</span>
                        <span className="text-2xl font-bold">
                          {new Intl.NumberFormat('pt-BR', {
                            style: 'currency',
                            currency: 'BRL',
                          }).format(invoice?.totalAmount ?? 0)}
                        </span>
                      </div>

                      {invoice?.paymentDate && (
                        <div className="flex justify-between items-center">
                          <span className="text-sm text-muted-foreground">Data de Pagamento:</span>
                          <span className="font-medium">
                            {format(new Date(invoice.paymentDate), 'PPP', { locale: ptBR })}
                          </span>
                        </div>
                      )}

                      {invoice?.transactions && invoice.transactions.length > 0 && (
                        <div className="mt-4">
                          <h4 className="text-sm font-medium mb-3 text-muted-foreground">
                            Transações:
                          </h4>
                          <div className="space-y-3 max-h-60 overflow-y-auto pr-2 scrollbar-hide">
                            {invoice.transactions.map((transaction) => (
                              <div
                                key={transaction?.id ?? ''}
                                className="flex justify-between items-center text-sm border-l-2 border-primary/50 hover:border-primary pl-3 py-1 transition-colors"
                              >
                                <div className="flex flex-col">
                                  <span className="font-medium text-foreground">
                                    {transaction?.description ?? 'Transação'}
                                  </span>
                                  <span className="text-[10px] text-muted-foreground uppercase tracking-wider">
                                    {CATEGORY_LABELS[transaction?.category]}
                                  </span>
                                </div>

                                <div className="flex items-center gap-4 text-right">
                                  {transaction?.installments && (
                                    <span className="text-[11px] font-bold bg-secondary text-secondary-foreground px-2 py-0.5 rounded-full border border-border">
                                      {transaction?.currentInstallment}/{transaction?.totalInstallments}
                                    </span>
                                  )}

                                  <span className="font-semibold text-foreground min-w-[80px]">
                                    {new Intl.NumberFormat('pt-BR', {
                                      style: 'currency',
                                      currency: 'BRL',
                                    }).format(transaction?.amount ?? 0)}
                                  </span>
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      )}

                      {invoice?.status !== InvoiceStatus.PAID && (
                        <Button onClick={() => handlePayInvoice(invoice)} className="w-full mt-4">
                          Pagar Fatura
                        </Button>
                      )}
                    </div>
                  </CardContent>
                </Card>
              );
            }) ?? null}
          </div>
        ) : (
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-12">
              <FileText className="h-12 w-12 text-muted-foreground mb-4" />
              <p className="text-muted-foreground">
                {selectedMonth
                  ? 'Nenhuma fatura encontrada para este período'
                  : 'Selecione um mês para ver as faturas'}
              </p>
            </CardContent>
          </Card>
        )}
      </main>

      <Dialog open={payDialogOpen} onOpenChange={setPayDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Pagar Fatura</DialogTitle>
            <DialogDescription>Confirme a data de pagamento da fatura</DialogDescription>
          </DialogHeader>

          <div className="py-4">
            <div className="space-y-4">
              <div>
                <p className="text-sm text-muted-foreground mb-2">Valor da Fatura:</p>
                <p className="text-2xl font-bold">
                  {new Intl.NumberFormat('pt-BR', {
                    style: 'currency',
                    currency: 'BRL',
                  }).format(selectedInvoice?.totalAmount ?? 0)}
                </p>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium">Data de Pagamento</label>
                <DatePicker
                  date={paymentDate}
                  onSelect={(date) => date && setPaymentDate(date)}
                  placeholder="Selecione a data de pagamento"
                />
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setPayDialogOpen(false)} disabled={isPaying}>
              Cancelar
            </Button>
            <Button onClick={confirmPayment} disabled={isPaying || !paymentDate}>
              {isPaying ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Processando...
                </>
              ) : (
                'Confirmar Pagamento'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}