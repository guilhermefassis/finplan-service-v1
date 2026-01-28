'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { signOut, useSession } from 'next-auth/react';
import { 
  LayoutDashboard, 
  CreditCard, 
  Receipt, 
  FileText, 
  User, 
  LogOut,
  DollarSign
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

const navItems = [
  { href: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/cards', label: 'Cartões', icon: CreditCard },
  { href: '/transactions', label: 'Transações', icon: Receipt },
  { href: '/invoices', label: 'Faturas', icon: FileText },
  { href: '/profile', label: 'Perfil', icon: User },
];

export function Navbar() {
  const pathname = usePathname();
  const { data: session } = useSession() || {};

  const handleSignOut = async () => {
    await signOut({ redirect: true, callbackUrl: '/login' });
  };

  if (!session) {
    return null;
  }

  return (
    <nav className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          <div className="flex items-center gap-2">
            <DollarSign className="h-6 w-6 text-primary" />
            <span className="text-xl font-bold">FinPlan</span>
          </div>

          <div className="hidden md:flex items-center gap-1">
            {navItems?.map?.((item) => {
              const Icon = item?.icon;
              const isActive = pathname === item?.href;
              return (
                <Link key={item?.href ?? ''} href={item?.href ?? '#'}>
                  <Button
                    variant={isActive ? 'default' : 'ghost'}
                    className={cn(
                      'gap-2',
                      isActive && 'bg-primary text-primary-foreground'
                    )}
                  >
                    {Icon && <Icon className="h-4 w-4" />}
                    {item?.label ?? ''}
                  </Button>
                </Link>
              );
            }) ?? null}
          </div>

          <div className="flex items-center gap-2">
            <span className="hidden sm:inline text-sm text-muted-foreground">
              {session?.user?.name ?? session?.user?.email ?? 'Usuário'}
            </span>
            <Button variant="ghost" size="icon" onClick={handleSignOut}>
              <LogOut className="h-4 w-4" />
            </Button>
          </div>
        </div>

        <div className="md:hidden pb-3 flex gap-2 overflow-x-auto">
          {navItems?.map?.((item) => {
            const Icon = item?.icon;
            const isActive = pathname === item?.href;
            return (
              <Link key={item?.href ?? ''} href={item?.href ?? '#'}>
                <Button
                  variant={isActive ? 'default' : 'outline'}
                  size="sm"
                  className="gap-2"
                >
                  {Icon && <Icon className="h-4 w-4" />}
                  {item?.label ?? ''}
                </Button>
              </Link>
            );
          }) ?? null}
        </div>
      </div>
    </nav>
  );
}
