'use client';

import { useEffect, useState } from 'react';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { Navbar } from '@/components/navbar';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Loader2, User as UserIcon } from 'lucide-react';
import { userApi } from '@/lib/services/api';
import { User, PaymentFrequency } from '@/lib/types';
import { useToast } from '@/hooks/use-toast';

export default function ProfilePage() {
  const { data: session, status } = useSession() || {};
  const router = useRouter();
  const { toast } = useToast();
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    paymentFrequency: PaymentFrequency.MONTHLY,
  });

  useEffect(() => {
    if (status === 'unauthenticated') {
      router.replace('/login');
    }
  }, [status, router]);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        setIsLoading(true);
        const data = await userApi.getMe();
        setUser(data);
        setFormData({
          name: data?.name ?? '',
          email: data?.email ?? '',
          paymentFrequency: (data?.paymentFrequency as PaymentFrequency) ?? PaymentFrequency.MONTHLY,
        });
      } catch (error) {
        console.error('Error fetching user:', error);
        toast({
          title: 'Erro',
          description: 'Não foi possível carregar os dados do usuário',
          variant: 'destructive',
        });
      } finally {
        setIsLoading(false);
      }
    };

    if (status === 'authenticated') {
      fetchUser();
    }
  }, [status, toast]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);

    try {
      const updatedUser = await userApi.updateUser({
        name: formData.name,
        email: formData.email,
        paymentFrequency: formData.paymentFrequency,
      });

      setUser(updatedUser);
      toast({
        title: 'Sucesso',
        description: 'Perfil atualizado com sucesso',
      });
    } catch (error) {
      console.error('Error updating user:', error);
      toast({
        title: 'Erro',
        description: 'Não foi possível atualizar o perfil',
        variant: 'destructive',
      });
    } finally {
      setIsSaving(false);
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
      <main className="mx-auto max-w-2xl px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold">Perfil</h1>
          <p className="text-muted-foreground mt-1">Gerencie suas informações pessoais</p>
        </div>

        <Card>
          <CardHeader>
            <div className="flex items-center gap-4">
              <div className="h-16 w-16 rounded-full bg-primary/10 flex items-center justify-center">
                <UserIcon className="h-8 w-8 text-primary" />
              </div>
              <div>
                <CardTitle>Informações Pessoais</CardTitle>
                <CardDescription>Atualize seus dados cadastrais</CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="name">Nome Completo</Label>
                <Input
                  id="name"
                  name="name"
                  placeholder="Seu nome completo"
                  value={formData.name}
                  onChange={handleChange}
                  required
                  disabled={isSaving}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  name="email"
                  type="email"
                  placeholder="seu@email.com"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  disabled={isSaving}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="paymentFrequency">Frequência de Pagamento</Label>
                <Select
                  value={formData.paymentFrequency}
                  onValueChange={(value) =>
                    setFormData({ ...formData, paymentFrequency: value as PaymentFrequency })
                  }
                  disabled={isSaving}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Selecione a frequência" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={PaymentFrequency.MONTHLY}>Mensal</SelectItem>
                    <SelectItem value={PaymentFrequency.BIWEEKLY}>Quinzenal</SelectItem>
                    <SelectItem value={PaymentFrequency.WEEKLY}>Semanal</SelectItem>
                    <SelectItem value={PaymentFrequency.DAILY}>Diário</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="pt-4">
                <Button type="submit" className="w-full" disabled={isSaving}>
                  {isSaving ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Salvando...
                    </>
                  ) : (
                    'Salvar Alterações'
                  )}
                </Button>
              </div>
            </form>

            {user && (
              <div className="mt-6 pt-6 border-t">
                <h3 className="text-sm font-medium mb-2">Informações da Conta</h3>
                <div className="space-y-2 text-sm text-muted-foreground">
                  <p>
                    <span className="font-medium">Criado em:</span>{' '}
                    {user?.createdAt
                      ? new Date(user.createdAt).toLocaleDateString('pt-BR')
                      : 'N/A'}
                  </p>
                  <p>
                    <span className="font-medium">Última atualização:</span>{' '}
                    {user?.updatedAt
                      ? new Date(user.updatedAt).toLocaleDateString('pt-BR')
                      : 'N/A'}
                  </p>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </main>
    </div>
  );
}
