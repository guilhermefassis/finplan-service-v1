'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Navbar } from '@/components/navbar';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { ArrowLeft, Loader2 } from 'lucide-react';
import { creditCardApi } from '@/lib/services/api';
import { useToast } from '@/hooks/use-toast';
import Link from 'next/link';

export default function NewCardPage() {
  const router = useRouter();
  const { toast } = useToast();
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    brand: '',
    closingDay: '',
    dueDay: '',
    creditLimit: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      await creditCardApi.create({
        name: formData.name,
        brand: formData.brand,
        closingDay: parseInt(formData.closingDay),
        dueDay: parseInt(formData.dueDay),
        creditLimit: parseFloat(formData.creditLimit),
      });

      toast({
        title: 'Sucesso',
        description: 'Cartão criado com sucesso',
      });

      router.push('/cards');
    } catch (error) {
      console.error('Error creating card:', error);
      toast({
        title: 'Erro',
        description: 'Não foi possível criar o cartão',
        variant: 'destructive',
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="mx-auto max-w-2xl px-4 sm:px-6 lg:px-8 py-8">
        <Link href="/cards">
          <Button variant="ghost" className="mb-4">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Voltar
          </Button>
        </Link>

        <Card>
          <CardHeader>
            <CardTitle>Novo Cartão de Crédito</CardTitle>
            <CardDescription>Adicione um novo cartão para gerenciar</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="name">Nome do Cartão</Label>
                <Input
                  id="name"
                  name="name"
                  placeholder="Ex: Cartão Principal"
                  value={formData.name}
                  onChange={handleChange}
                  required
                  disabled={isLoading}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="brand">Bandeira</Label>
                <Input
                  id="brand"
                  name="brand"
                  placeholder="Ex: Visa, Mastercard"
                  value={formData.brand}
                  onChange={handleChange}
                  required
                  disabled={isLoading}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="closingDay">Dia de Fechamento</Label>
                  <Input
                    id="closingDay"
                    name="closingDay"
                    type="number"
                    min="1"
                    max="31"
                    placeholder="10"
                    value={formData.closingDay}
                    onChange={handleChange}
                    required
                    disabled={isLoading}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="dueDay">Dia de Vencimento</Label>
                  <Input
                    id="dueDay"
                    name="dueDay"
                    type="number"
                    min="1"
                    max="31"
                    placeholder="20"
                    value={formData.dueDay}
                    onChange={handleChange}
                    required
                    disabled={isLoading}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="creditLimit">Limite de Crédito</Label>
                <Input
                  id="creditLimit"
                  name="creditLimit"
                  type="number"
                  step="0.01"
                  min="0"
                  placeholder="5000.00"
                  value={formData.creditLimit}
                  onChange={handleChange}
                  required
                  disabled={isLoading}
                />
              </div>

              <div className="flex gap-4 pt-4">
                <Button type="submit" className="flex-1" disabled={isLoading}>
                  {isLoading ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Criando...
                    </>
                  ) : (
                    'Criar Cartão'
                  )}
                </Button>
                <Link href="/cards" className="flex-1">
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
